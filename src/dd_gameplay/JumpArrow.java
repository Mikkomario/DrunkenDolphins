package dd_gameplay;

import java.awt.Color;
import java.awt.Graphics2D;

import dd_main.DDHandlerType;
import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.GenesisHandlerType;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseListener;
import genesis_event.MouseEvent.MouseButtonEventType;
import genesis_util.DepthConstants;
import genesis_util.SimpleHandled;
import genesis_util.StateOperator;
import genesis_util.Vector3D;

/**
 * JumpArrow creates a connection between two glasses, allowing the dolphins to jump from 
 * one to another
 * @author Mikko Hilpinen
 * @since 6.8.2015
 */
public class JumpArrow extends SimpleHandled implements Drawable, MouseListener
{
	// ATTRIBUTES	-----------------
	
	private Glass start, end;
	private GlassRelay glasses;
	private EventSelector<MouseEvent> mouseSelector;
	private Vector3D lastPressPosition;
	
	
	// CONSTRUCTOR	-----------------
	
	/**
	 * Creates a new jumpArrow
	 * @param handlers The handlers that will handle the arrow
	 */
	public JumpArrow(HandlerRelay handlers)
	{
		super(handlers);
		
		this.start = null;
		this.end = null;
		this.glasses = (GlassRelay) handlers.getHandler(DDHandlerType.GLASSRELAY);
		this.mouseSelector = MouseEvent.createButtonStateChangeSelector();
		this.lastPressPosition = Vector3D.zeroVector();
		
		getHandlingOperators().setShouldBeHandledOperator(GenesisHandlerType.DRAWABLEHANDLER, 
				new StateOperator(false, true));
	}
	
	
	// IMPLEMENTED METHODS	----------

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.mouseSelector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		return false;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		// On pressed, marks the spot down, on released, may rearrange or make a dolphin jump
		if (event.getButtonEventType() == MouseButtonEventType.PRESSED)
			this.lastPressPosition = event.getPosition();
		else
		{
			if (event.getPosition().minus(this.lastPressPosition).getLength() > 100)
			{
				this.start = this.glasses.getGlassAtPosition(this.lastPressPosition);
				if (this.start == null)
					this.end = null;
				else
				{
					this.end = this.glasses.getClosestGlass(event.getPosition(), true);
					if (this.end != null && this.end.equals(this.start))
						this.end = null;
				}
				
				updateVisibility();
			}
			else if (this.start != null && this.end != null)
			{
				// Only jumps if the end glass is clicked, otherwise cancels
				if (this.end.isInAreaOfInterest(event.getPosition()))
					this.start.jumpOneTo(this.end);
				else
				{
					this.start = null;
					this.end = null;
					updateVisibility();
				}
			}
		}
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (this.start != null && this.end != null)
		{
			g2d.setColor(Color.BLACK);
			Vector3D startPoint = this.start.getTransformation().getPosition();
			Vector3D endPoint = this.end.getTransformation().getPosition();
			
			g2d.drawLine(startPoint.getFirstInt(), startPoint.getSecondInt(), 
					endPoint.getFirstInt(), endPoint.getSecondInt());
		}
	}

	@Override
	public int getDepth()
	{
		return DepthConstants.NORMAL - 20;
	}
	
	
	// OTHER METHODS	---------------
	
	private void updateVisibility()
	{
		getHandlingOperators().getShouldBeHandledOperator(
				GenesisHandlerType.DRAWABLEHANDLER).setState(this.start != null && 
				this.end != null);
	}
}
