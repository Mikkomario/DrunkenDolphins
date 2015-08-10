package dd_main;

import dd_gameplay.Glass;
import dd_gameplay.SwimmingDolphin;
import genesis_event.HandlerType;

/**
 * These are the handler types introduced in this project
 * @author Mikko Hilpinen
 * @since 6.8.2015
 */
public enum DDHandlerType implements HandlerType
{
	/**
	 * Glasses handle a bunch of swimming dolphins
	 */
	GLASS,
	/**
	 * The glassRelay holds all the glasses in the game
	 */
	GLASSRELAY;
	
	
	// METHODS	--------------------

	@Override
	public Class<?> getSupportedHandledClass()
	{
		switch (this)
		{
			case GLASS: return SwimmingDolphin.class;
			case GLASSRELAY: return Glass.class;
		}
		
		return null;
	}
}
