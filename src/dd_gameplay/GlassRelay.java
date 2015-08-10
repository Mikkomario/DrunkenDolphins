package dd_gameplay;

import java.util.ArrayList;
import java.util.List;

import dd_main.DDHandlerType;
import genesis_event.Handler;
import genesis_event.HandlerType;
import genesis_util.HelpMath;
import genesis_util.Vector3D;

/**
 * The glass relay keeps track of all the glasses in the game
 * @author Mikko Hilpinen
 * @since 6.8.2015
 */
public class GlassRelay extends Handler<Glass>
{
	// ATTRIBUTES	-------------------
	
	private Vector3D checkPosition;
	private Glass found;
	
	
	// CONSTRUCTOR	-------------------
	
	/**
	 * Creates a new relay
	 */
	public GlassRelay()
	{
		super(false);
	}
	
	
	// IMPLEMENTED METHODS	-----------

	@Override
	public HandlerType getHandlerType()
	{
		return DDHandlerType.GLASSRELAY;
	}

	@Override
	protected boolean handleObject(Glass h)
	{
		if (this.checkPosition.minus(h.getTransformation().getPosition()).getLength() < 
				h.getRadius())
		{
			this.found = h;
			return false;
		}
		return true;
	}
	
	
	// OTHER METHODS	--------------
	
	/**
	 * Finds a glass at the given position, if there is one
	 * @param position The position glasses are searched from
	 * @return A glass at the given position
	 */
	public Glass getGlassAtPosition(Vector3D position)
	{
		this.checkPosition = position;
		this.found = null;
		handleObjects(true);
		Glass found = this.found;
		
		this.found = null;
		return found;
	}
	
	/**
	 * Finds the glass closest to the given position
	 * @param position The position that is searched
	 * @param includeEmpty Should the empty glasses be included in the search
	 * @return The glass closest to that position
	 */
	public Glass getClosestGlass(Vector3D position, boolean includeEmpty)
	{
		FindNearestGlassOperator operator = new FindNearestGlassOperator(position, includeEmpty);
		handleObjects(operator, true);
		return operator.getClosestGlass();
	}
	
	/**
	 * @return All glasses currently stored in this relay
	 */
	public List<Glass> getAllGlasses()
	{
		ListGlassesOperator operator = new ListGlassesOperator();
		handleObjects(operator, true);
		return operator.getGlasses();
	}
	
	
	// SUBCLASSES	------------------
	
	private class FindNearestGlassOperator extends HandlingOperator
	{
		// ATTRIBUTES	--------------
		
		private Vector3D searchPoint;
		private Glass best;
		private double shortestDistance;
		private boolean acceptEmpty;
		
		
		// CONSTRUCTOR	--------------
		
		public FindNearestGlassOperator(Vector3D searchPoint, boolean acceptEmpty)
		{
			this.searchPoint = searchPoint;
			this.best = null;
			this.shortestDistance = 0;
			this.acceptEmpty = acceptEmpty;
		}
		
		
		// IMPLEMENTED METHODS	-------
		
		@Override
		protected boolean handleObject(Glass h)
		{
			double distance = HelpMath.pointDistance2D(this.searchPoint, 
					h.getTransformation().getPosition());
			
			if ((this.acceptEmpty || !h.isEmpty()) && (this.best == null || 
					distance < this.shortestDistance))
			{
				this.best = h;
				this.shortestDistance = distance;
			}
			
			return true;
		}
		
		
		// ACCESSORS	---------------
		
		public Glass getClosestGlass()
		{
			return this.best;
		}
	}
	
	private class ListGlassesOperator extends HandlingOperator
	{
		// ATTRIBUTES	--------------
		
		private List<Glass> glasses;
		
		
		// CONSTRUCTOR	--------------
		
		public ListGlassesOperator()
		{
			this.glasses = new ArrayList<>();
		}
		
		
		// IMPLEMENTED METHODS	-------
		
		@Override
		protected boolean handleObject(Glass h)
		{
			this.glasses.add(h);
			return true;
		}
		
		
		// ACCESSORS	---------------
		
		public List<Glass> getGlasses()
		{
			return this.glasses;
		}
	}
}
