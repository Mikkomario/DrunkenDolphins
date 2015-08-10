package dd_gameplay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import dd_main.DDHandlerType;
import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.Handler;
import genesis_event.HandlerRelay;
import genesis_event.HandlerType;
import genesis_event.MouseEvent;
import genesis_event.MouseEvent.MouseButtonEventScale;
import genesis_event.MouseEvent.MouseButtonEventType;
import genesis_event.MouseListener;
import genesis_event.StrictEventSelector;
import genesis_util.DepthConstants;
import genesis_util.HelpMath;
import genesis_util.SimpleHandled;
import genesis_util.Transformable;
import genesis_util.Transformation;
import genesis_util.Vector3D;

/**
 * Glasses sit on a table and contain certain type of beverage
 * @author Mikko Hilpinen
 * @since 5.8.2015
 */
public class Glass extends SimpleHandled implements Drawable, Transformable, MouseListener
{
	// ATTRIBUTES	-------------------
	
	private int radius;
	private double fillAmount;
	private Beverage beverage;
	private Transformation transformation;
	private EventSelector<MouseEvent> mouseSelector;
	private GlassDolphinHandler handler;
	private HandlerRelay handlers;
	
	private static Random random = new Random();
	
	
	// CONSTRUCTOR	-------------------
	
	/**
	 * Creates a new glass of beverage
	 * @param handlers The handlers that will handle the object
	 * @param position The glasses position
	 * @param beverage The The beverage inside the glass
	 * @param radius The radius of the glass (in pixels)
	 * @param fill How full the glass is [0, 1]
	 */
	public Glass(HandlerRelay handlers, Vector3D position, Beverage beverage, int radius, 
			double fill)
	{
		super(handlers);
		
		this.transformation = new Transformation(position);
		this.beverage = beverage;
		this.radius = radius;
		this.fillAmount = fill;
		this.handlers = handlers;
		
		StrictEventSelector<MouseEvent, MouseEvent.Feature> localPressSelector = 
				MouseEvent.createButtonEventTypeSelector(MouseButtonEventType.PRESSED);
		localPressSelector.addRequiredFeature(MouseButtonEventScale.LOCAL);
		this.mouseSelector = localPressSelector;
		this.handler = new GlassDolphinHandler();
	}
	
	
	// IMPLEMENTED METHODS	----------------

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		AffineTransform lastTransform = getTransformation().transform(g2d);
		
		g2d.setColor(Color.CYAN);
		g2d.drawOval(-this.radius, -this.radius, this.radius * 2, this.radius * 2);
		
		if (!isEmpty())
		{
			int contentRadius = getContentRadius();
			g2d.setColor(getBeverage().getColor());
			g2d.fillOval(-contentRadius, -contentRadius, contentRadius * 2, contentRadius * 2);
		}
		
		g2d.setTransform(lastTransform);
	}

	@Override
	public int getDepth()
	{
		return DepthConstants.BACK;
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
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.mouseSelector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		if (getTransformation() != null)
			return getTransformation().inverseTransform(position).getLength() < getRadius();
		else
			return false;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		// Makes the dolphins drink
		if (!isEmpty())
			getHandler().drink(this, this.handlers);
	}

	
	// GETTERS & SETTERS	-----------------
	
	/**
	 * @return The type of beverage held within this glass
	 */
	public Beverage getBeverage()
	{
		return this.beverage;
	}
	
	/**
	 * @return The radius of the glass
	 */
	public int getRadius()
	{
		return this.radius;
	}
	
	/**
	 * @return The handler used for handling the dolphins inside the glass
	 */
	public GlassDolphinHandler getHandler()
	{
		return this.handler;
	}
	
	
	// OTHER METHODS	---------------------
	
	/**
	 * @return The radius of the beverage in the glass
	 */
	public int getContentRadius()
	{
		return (int) (this.radius * this.fillAmount);
	}
	
	/**
	 * @return Is the glass empty
	 */
	public boolean isEmpty()
	{
		return this.fillAmount <= 0;
	}
	
	/**
	 * Decreases the amount of beverage in the glass
	 * @param amount How many pixels (radius) are removed from the glass
	 */
	public void drink(int amount)
	{
		this.fillAmount -= amount / (double) getRadius();
	}
	
	/**
	 * Makes a dolphin from this glass jump to another glass
	 * @param other The glass the dolphin will jump to
	 */
	public void jumpOneTo(Glass other)
	{
		// Finds the dolphin to jump
		SwimmingDolphin dolphin = getHandler().findNextJumper(other);
		if (dolphin != null)
			dolphin.jumpTo(other);
	}
	
	/**
	 * @return A semi-randomly chosen point inside the glass
	 */
	public Vector3D getRandomPositionInside()
	{
		return getTransformation().getPosition().plus(HelpMath.lenDir(0.2 + 
				random.nextDouble() * getContentRadius() * 0.4, 
				random.nextDouble() * 360));
	}
	
	
	// SUBCLASSES	-----------------------
	
	/**
	 * This handler handles all the dolphins inside the glass
	 * @author Mikko Hilpinen
	 * @since 6.8.2015
	 */
	public static class GlassDolphinHandler extends Handler<SwimmingDolphin>
	{
		// CONSTRUCTOR	----------------
		
		/**
		 * Creates a new handler
		 */
		public GlassDolphinHandler()
		{
			super(false);
		}
		
		
		// IMPLEMENTED METHODS	--------

		@Override
		public HandlerType getHandlerType()
		{
			return DDHandlerType.GLASS;
		}

		@Override
		protected boolean handleObject(SwimmingDolphin h)
		{
			h.drink();
			return true;
		}
		
		
		// OTHER METHODS	----------------
		
		private void drink(Glass glass, HandlerRelay handlers)
		{
			// Makes all the dolphins drink from the glass
			double lastFill = glass.fillAmount;
			handleObjects(true);
			
			// Moves the dolphins according to the radius change
			double fillScale = glass.fillAmount / lastFill;
			handleObjects(new DolphinMovingOperator(fillScale), false);
			
			// If the glass became empty, throws the dolphins to somewhere else
			if (glass.isEmpty())
				handleObjects(new DolphinRelocationOperator((GlassRelay) handlers.getHandler(
						DDHandlerType.GLASSRELAY), glass), true);
		}
		
		private SwimmingDolphin findNextJumper(Glass target)
		{
			BestDolphinSearchOperator operator = new BestDolphinSearchOperator(
					target.getBeverage());
			handleObjects(operator, true);
			return operator.getBestDolphin();
		}
		
		
		// SUBCLASSES	--------------------
		
		private class DolphinMovingOperator extends HandlingOperator
		{
			// ATTRIBUTES	----------------
			
			private double scale;
			
			
			// CONSTRUCTOR	----------------
			
			public DolphinMovingOperator(double scale)
			{
				this.scale = scale;
			}
			
			
			// IMPLEMENTED METHODS	--------
			
			@Override
			protected boolean handleObject(SwimmingDolphin h)
			{
				// Moves the dolphin
				h.setTrasformation(h.getOwnTransformation().withPosition(
						h.getOwnTransformation().getPosition().times(this.scale)));
				return true;
			}
		}
		
		private class BestDolphinSearchOperator extends HandlingOperator
		{
			// ATTRIBUTES	----------------
			
			private SwimmingDolphin best;
			private Beverage target;
			private int bestInitiative = -1000;
			
			
			// CONSTRUCTOR	----------------
			
			public BestDolphinSearchOperator(Beverage target)
			{
				this.target = target;
				this.best = null;
			}
			
			
			// IMPLEMENTED METHODS	--------
			
			@Override
			protected boolean handleObject(SwimmingDolphin h)
			{
				int initiative = h.getData().getInitiativeToDrink(this.target);
				
				if (this.best == null || initiative > this.bestInitiative)
				{
					this.best = h;
					this.bestInitiative = initiative;
				}
				
				return true;
			}
			
			
			// ACCESSORS	----------------
			
			public SwimmingDolphin getBestDolphin()
			{
				return this.best;
			}
		}
		
		private class DolphinRelocationOperator extends HandlingOperator
		{
			// ATTRIBUTES	-----------------
			
			private List<Glass> glasses;
			
			
			// CONSTRUCTOR	-----------------
			
			public DolphinRelocationOperator(GlassRelay relay, Glass start)
			{
				this.glasses = relay.getAllGlasses();
				this.glasses.remove(start);
				
				Collection<Glass> emptyGlasses = new ArrayList<>();
				for (Glass glass : this.glasses)
				{
					if (glass.isEmpty())
						emptyGlasses.add(glass);
				}
				
				this.glasses.removeAll(emptyGlasses);
			}
			
			
			// IMPLEMENTED METHODS	--------
			
			@Override
			protected boolean handleObject(SwimmingDolphin h)
			{
				if (this.glasses.isEmpty())
					return false;
				
				h.jumpTo(this.glasses.get(random.nextInt(this.glasses.size())));
				return true;
			}
		}
	}
}
