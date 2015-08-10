package dd_gameplay;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * DolphinData contains all important information about a single dolphin
 * @author Mikko Hilpinen
 * @since 5.8.2015
 */
public class DolphinData
{
	// ATTRIBUTES	--------------------
	
	private Beverage drinkType;
	private int drunkness;
	private double health;
	
	
	// CONSTRUCTOR	-------------------
	
	/**
	 * Creates a new set of dolphin data
	 * @param drinkType The type of drink this dolphin drinks
	 */
	public DolphinData(Beverage drinkType)
	{
		this.drinkType = drinkType;
		this.drunkness = 0;
		this.health = 100;
	}
	
	
	// ACCESSORS	------------------
	
	/**
	 * Makes the dolphin more drunk
	 * @param amount How much the drunkness is increased
	 */
	public void increaseDrunkness(int amount)
	{
		this.drunkness += amount;
		this.health += amount;
		
		if (this.health > 100)
			this.health = 100;
	}
	
	/**
	 * @return How drunk the dolphin is
	 */
	public int getDrunkness()
	{
		return this.drunkness;
	}
	
	/**
	 * @return The type of drink this dolphin drinks
	 */
	public Beverage getDrinkType()
	{
		return this.drinkType;
	}
	
	/**
	 * Decreases the health of the dolphin by specified amount
	 * @param amount How much the dolphin's health is decreased
	 */
	public void decreaseHealth(double amount)
	{
		this.health -= amount;
	}
	
	/**
	 * @return How much health the dolphin has left [0, 100]
	 */
	public double getHealth()
	{
		return this.health;
	}

	
	// OTHER METHODS	--------------
	
	/**
	 * @return The color used for drawing the dolphin
	 */
	public Color getColor()
	{
		Color color = null;
		
		switch (this.drinkType)
		{
			case BEER: color = Color.ORANGE; break;
			case WHISKEY: color = Color.CYAN; break;
			case CHAMPANGE: color = Color.RED; break;
		}
		
		for (int i = 10; i < this.drunkness; i += 10)
		{
			color = color.darker();
		}
		
		return color;
	}
	
	/**
	 * Draws a box representing the dolphin
	 * @param g2d The graphics object that does the drawing
	 */
	public void draw(Graphics2D g2d)
	{
		g2d.setColor(getColor());
		g2d.fillRect(-20, -10, 40, 20);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(-20, -10, 40, 20);
	}
	
	/**
	 * Calculates the dolphin's initiative for the given drink
	 * @param drink The type of drink that should be drank
	 * @return How high initiative the dolphin has for drinking
	 */
	public int getInitiativeToDrink(Beverage drink)
	{
		int initiative = getDrinkType().getInitiative();
		if (drink == getDrinkType())
			initiative += 10000;
		initiative -= this.drunkness;
		
		return initiative;
	}
}
