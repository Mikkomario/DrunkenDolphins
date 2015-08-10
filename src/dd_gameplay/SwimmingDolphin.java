package dd_gameplay;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

import genesis_event.Actor;
import genesis_event.Drawable;
import genesis_event.HandlerRelay;
import genesis_util.DepthConstants;
import genesis_util.HelpMath;
import genesis_util.SimpleHandled;
import genesis_util.Transformable;
import genesis_util.Transformation;
import genesis_util.Vector3D;

/**
 * This dolphin swims in a glass
 * @author Mikko Hilpinen
 * @since 5.8.2015
 */
public class SwimmingDolphin extends SimpleHandled implements Drawable,
		Transformable, Actor
{
	// ATTRIBUTES	--------------------
	
	private DolphinData data;
	private Transformation ownTransformation;
	private double speed;
	private Glass glass;
	private HandlerRelay handlers;
	
	private static Random random = new Random();
	
	
	// CONSTRUCTOR	--------------------
	
	/**
	 * Creates a new dolphin that swimms in a glass
	 * @param handlers The handlers that will handle the dolphin
	 * @param data The dolphin's data
	 * @param relativePosition The position of the dolphin in relation to the glass
	 * @param speed The speed with which the dolphin circles the glass (in pixels)
	 * @param glass The glass in which the dolphin swims
	 */
	public SwimmingDolphin(HandlerRelay handlers, DolphinData data, Vector3D relativePosition, 
			double speed, Glass glass)
	{
		super(handlers);
		
		this.data = data;
		this.ownTransformation = new Transformation(relativePosition);
		this.glass = glass;
		this.handlers = handlers;
		
		// Calculates the dolphin's travelling speed in degrees
		double r = relativePosition.getLength();
		this.speed = speed * 360 / ( 2 * Math.PI * r);
		
		// Sets the rotation right as well
		double direction = relativePosition.getZDirection() + 90;
		if (speed < 0)
			direction -= 180;
		
		this.ownTransformation = this.ownTransformation.withAngle(direction);
		
		// Adds the dolphin to the handler
		glass.getHandler().add(this);
	}
	
	
	// IMPLEMENTED METHODS	-------------------

	@Override
	public Transformation getTransformation()
	{
		return this.glass.getTransformation().transform(this.ownTransformation);
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.ownTransformation = t;
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
	public void act(double duration)
	{
		setTrasformation(this.ownTransformation.rotatedAroundAbsolutePoint(
				this.speed * duration, Vector3D.zeroVector()));
		
		if (this.speed > 8)
			this.speed -= 0.3 * duration;
		else if (this.speed < 2)
			this.speed += 0.3 * duration;
	}
	
	
	// ACCESSORS	--------------------
	
	/**
	 * @return The dolphin's data
	 */
	public DolphinData getData()
	{
		return this.data;
	}
	
	/**
	 * @return The dolphin's individual transformation
	 */
	public Transformation getOwnTransformation()
	{
		return this.ownTransformation;
	}
	
	
	// OTHER METHODS	----------------
	
	/**
	 * The dolphin tries to drink from the glass
	 */
	public void drink()
	{
		// Dolphins only drink beverage they like
		if (this.glass.getBeverage().equals(getData().getDrinkType()) && !this.glass.isEmpty())
		{
			this.glass.drink(1);
			getData().increaseDrunkness(1);
		}
	}
	
	/**
	 * Makes the dolphin leave the glass and jump to another position (this swimmingDolphin is 
	 * killed in the process)
	 * @param position The position the dolphin jumps to
	 */
	public void jumpTo(Vector3D position)
	{
		// Transforms into a jumping dolphin
		new JumpingDolphin(this.handlers, getData(), getTransformation().getPosition(), 
				position);
		getIsDeadStateOperator().setState(true);
	}
	
	/**
	 * The dolphin tries to jump to another glass
	 * @param glass The glass the dolphin jumps to
	 */
	public void jumpTo(Glass glass)
	{
		jumpTo(glass.getRandomPositionInside());
	}
	
	/**
	 * This method creates a random dolphin inside the given glass
	 * @param handlers The handlers that will handle the dolphin
	 * @param glass The glass the dolphin will be put into
	 * @return The dolphin that was created
	 */
	public static SwimmingDolphin createDolphin(HandlerRelay handlers, Glass glass)
	{
		DolphinData data = new DolphinData(Beverage.getRandomBeverage());
		Vector3D relativePos = HelpMath.lenDir(10 + random.nextInt(glass.getContentRadius() - 
				10), random.nextInt(360));
		
		return new SwimmingDolphin(handlers, data, relativePos, 2 + random.nextDouble() * 4, 
				glass);
	}
}
