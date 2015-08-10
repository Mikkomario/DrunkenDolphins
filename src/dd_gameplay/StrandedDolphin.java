package dd_gameplay;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import dd_main.DDHandlerType;
import genesis_event.Actor;
import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseListener;
import genesis_event.MultiEventSelector;
import genesis_event.StrictEventSelector;
import genesis_event.MouseEvent.MouseButtonEventScale;
import genesis_event.MouseEvent.MouseButtonEventType;
import genesis_util.DepthConstants;
import genesis_util.SimpleHandled;
import genesis_util.Transformable;
import genesis_util.Transformation;
import genesis_util.Vector3D;

/**
 * Stranded dolphins are stuck outside glasses and must be saved by keeping the mouse over 
 * them
 * @author Mikko Hilpinen
 * @since 6.8.2015
 */
public class StrandedDolphin extends SimpleHandled implements Transformable,
		Drawable, MouseListener, Actor
{
	// ATTRIBUTES	--------------------
	
	private double saveLeft, anglePhase, originalAngle;
	private DolphinData data;
	private Transformation transformation;
	private EventSelector<MouseEvent> mouseSelector;
	private HandlerRelay handlers;
	
	
	// CONSTRUCTOR	--------------------
	
	/**
	 * Creates a new stranded dolphin to the specified location
	 * @param handlers The handlers that will handle the dolphin
	 * @param data The dolphin's data
	 * @param position The dolphin's position
	 * @param direction The dolphin's direction
	 */
	public StrandedDolphin(HandlerRelay handlers, DolphinData data, Vector3D position, 
			double direction)
	{
		super(handlers);
		
		this.saveLeft = 50;
		this.anglePhase = 0;
		this.data = data;
		this.originalAngle = direction;
		this.transformation = new Transformation(position, Vector3D.identityVector(), 
				Vector3D.zeroVector(), direction);
		this.handlers = handlers;
		
		MultiEventSelector<MouseEvent> downAndReleased = new MultiEventSelector<>();
		StrictEventSelector<MouseEvent, MouseEvent.Feature> localMouseDown = 
				MouseEvent.createButtonEventTypeSelector(MouseButtonEventType.DOWN);
		localMouseDown.addRequiredFeature(MouseButtonEventScale.LOCAL);
		downAndReleased.addOption(localMouseDown);
		downAndReleased.addOption(MouseEvent.createButtonEventTypeSelector(
				MouseButtonEventType.RELEASED));
		
		this.mouseSelector = downAndReleased;
	}
	
	
	// IMPLEMENTED METHODS	----------------

	@Override
	public void act(double duration)
	{
		getData().decreaseHealth(duration * 0.3);
		this.anglePhase += getData().getHealth() * 0.005 * duration;
		setTrasformation(getTransformation().withAngle(this.originalAngle + 
				Math.sin(this.anglePhase) * 70));
		
		if (getData().getHealth() < 0)
			getIsDeadStateOperator().setState(true);
	}

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.mouseSelector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		return getTransformation().inverseTransform(position).getLength() < 32;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		// Tries to save the dolphin on press down, fails if released
		if (event.getButtonEventType() == MouseButtonEventType.RELEASED)
			this.saveLeft = 50;
		else
		{
			this.saveLeft -= event.getDuration();
			this.anglePhase += event.getDuration() * 0.1;
			
			// If the dolphin is saved, it tries to jump to the nearest glass
			if (this.saveLeft < 0)
			{
				Glass nearest = ((GlassRelay) this.handlers.getHandler(
						DDHandlerType.GLASSRELAY)).getClosestGlass(
						getTransformation().getPosition(), false);
				if (nearest == null)
					this.saveLeft += 20;
				else
				{
					new JumpingDolphin(this.handlers, getData(), 
							getTransformation().getPosition(), 
							nearest.getRandomPositionInside());
					getIsDeadStateOperator().setState(true);
				}
			}
		}
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		AffineTransform lastTransform = getTransformation().transform(g2d);
		getData().draw(g2d);
		g2d.setTransform(lastTransform);
	}

	@Override
	public int getDepth()
	{
		return DepthConstants.NORMAL;
	}

	@Override
	public Transformation getTransformation()
	{
		return this.transformation;
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.transformation = t;
	}

	
	// ACCESSORS	--------------
	
	/**
	 * @return The dolphin's basic statistics
	 */
	public DolphinData getData()
	{
		return this.data;
	}
}
