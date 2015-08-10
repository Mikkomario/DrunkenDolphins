package dd_gameplay;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

import dd_main.DDHandlerType;
import dd_main.Main;
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
 * Jumping dolphins try to jump from a glass to another
 * @author Huoltokäyttis
 *
 */
public class JumpingDolphin extends SimpleHandled implements Drawable,
		Transformable, Actor
{
	// ATTRIBUTES	---------------------
	
	private Transformation transformation;
	private DolphinData data;
	private Vector3D velocity;
	private HandlerRelay handlers;
	
	private static int flyTime = 100;
	private static Random random = new Random();
	
	
	// CONSTRUCTOR	---------------------
	
	/**
	 * Creates a new dolphin that jumps from the starting point to the end point
	 * @param handlers The handlers that will handle the dolphin
	 * @param data The dolphin's data
	 * @param start The start point of the jump
	 * @param end The end point of the jump
	 */
	public JumpingDolphin(HandlerRelay handlers, DolphinData data, Vector3D start, 
			Vector3D end)
	{
		super(handlers);
		
		this.transformation = new Transformation(start);
		this.data = data;
		this.handlers = handlers;
		
		// Vxy = d / t
		// Vz = Gt/2
		Vector3D horizontalVelocity = end.minus(start).dividedBy(flyTime);
		// Drunkness affects horizontal velocity
		double missAngle = -getData().getDrunkness() * 0.25 + 
				random.nextDouble() * getData().getDrunkness() * 0.5;
		double missSpeed = random.nextDouble() * getData().getDrunkness() * 0.01;
		if (random.nextDouble() < 0.5)
			missSpeed *= -1;
		horizontalVelocity = HelpMath.lenDir(horizontalVelocity.getLength() * (1 + missSpeed), 
				horizontalVelocity.getZDirection() + missAngle);
		
		Vector3D verticalVelocity = new Vector3D(0, 0, Main.gravity * flyTime / 2);
		this.velocity = horizontalVelocity.plus(verticalVelocity);
		
		Transformable.transform(this, Transformation.rotationTransformation(
				horizontalVelocity.getZDirection()));
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public void act(double duration)
	{
		// Applies gravity and velocity
		Transformable.transform(this, Transformation.transitionTransformation(
				this.velocity.times(duration)));
		this.velocity = this.velocity.plus(new Vector3D(0, 0, -Main.gravity * duration));
		
		// Scales the fish according to height
		double scale = 1 + getTransformation().getPosition().getThird() * 0.003;
		setTrasformation(getTransformation().withScaling(new Vector3D(scale, scale)));
		
		// The fish stops once it hits the ground
		if (getTransformation().getPosition().getThird() < 0)
			land();
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
		return DepthConstants.FOREGROUND;
	}

	
	// ACCESSORS	-------------------
	
	/**
	 * @return The dolphin's data
	 */
	public DolphinData getData()
	{
		return this.data;
	}
	
	
	// OTHER METHODS	---------------
	
	private void land()
	{
		// If there is a glass, starts swimming
		//System.out.println(this.handlers.containsHandlerOfType(DDHandlerType.GLASSRELAY));
		GlassRelay glassRelay = 
				(GlassRelay) this.handlers.getHandler(DDHandlerType.GLASSRELAY);
		Glass target = glassRelay.getGlassAtPosition(getTransformation().getPosition());
		
		if (target != null && !target.isEmpty())
		{
			Vector3D position = getTransformation().getPosition();
			Vector3D relativePos = target.getTransformation().inverseTransform(position);
			Vector3D directionalVelocity = this.velocity.vectorProjection(Vector3D.unitVector(
					HelpMath.pointDirection(target.getTransformation().getPosition(), position
					) + 90));
			double speed = directionalVelocity.getLength();
			if (HelpMath.getAngleDifference180(this.velocity.getZDirection(), 
					directionalVelocity.getZDirection()) > 90)
				speed *= -1;
			
			new SwimmingDolphin(this.handlers, this.data, relativePos, speed, target);
		}
		// Otherwise gets stranded
		else
			new StrandedDolphin(this.handlers, getData(), 
					getTransformation().getPosition().in2D(), getTransformation().getAngle());
		
		getIsDeadStateOperator().setState(true);
	}
}
