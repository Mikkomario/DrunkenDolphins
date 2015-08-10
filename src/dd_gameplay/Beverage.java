package dd_gameplay;

import java.awt.Color;
import java.util.Random;

/**
 * Beverages represent different types of alcohol drinks that appear in the game
 * @author Mikko Hilpinen
 * @since 5.8.2015
 */
public enum Beverage
{
	/**
	 * The cheap old beer, probably karhu
	 */
	BEER,
	/**
	 * Good old whiskey
	 */
	WHISKEY,
	/**
	 * Exquisite
	 */
	CHAMPANGE;
	
	
	// METHODS	-----------------
	
	/**
	 * @return The drink's basic color
	 */
	public Color getColor()
	{
		switch(this)
		{
			case BEER: return Color.YELLOW;
			case WHISKEY: return Color.BLUE;
			case CHAMPANGE: return Color.PINK;
		}
		
		return null;
	}
	
	/**
	 * @return How much initiative creatures drinkin this type of beverage should have
	 */
	public int getInitiative()
	{
		switch (this)
		{
			case BEER: return 0;
			case WHISKEY: return 200;
			case CHAMPANGE: return 400;
		}
		
		return -1;
	}
	
	/**
	 * @return A type of beverage chosen randomly
	 */
	public static Beverage getRandomBeverage()
	{
		Random random = new Random();
		return values()[random.nextInt(values().length)];
	}
}
