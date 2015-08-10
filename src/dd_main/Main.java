package dd_main;

import java.awt.Color;

import dd_gameplay.Beverage;
import dd_gameplay.Glass;
import dd_gameplay.GlassRelay;
import dd_gameplay.JumpArrow;
import dd_gameplay.SwimmingDolphin;
import genesis_event.ActorHandler;
import genesis_event.DrawableHandler;
import genesis_event.HandlerRelay;
import genesis_event.MouseListenerHandler;
import genesis_test.TextPerformanceMonitor;
import genesis_util.Vector3D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This class holds the main method for the project
 * @author Mikko Hilpinen
 * @since 5.8.2015
 */
public class Main
{
	// ATTRIBUTES	--------------------
	
	private static Vector3D resolution = new Vector3D(1980, 1020);
	/**
	 * The gravity constant used in the game
	 */
	public static double gravity = 0.9;
	
	
	// CONSTRUCTOR	--------------------
	
	private Main()
	{
		// The interface is static
	}

	
	// MAIN METHOD	--------------------
	
	/**
	 * Starts the game
	 * @param args Not used at this time
	 */
	public static void main(String[] args)
	{
		// Creates the window
		GameWindow window = new GameWindow(resolution, "Drunken Dolphins", true, 120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		panel.setBackground(Color.WHITE);
		
		// Sets up the handlers
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new DrawableHandler(false, panel.getDrawer()));
		handlers.addHandler(new ActorHandler(false, window.getStepHandler()));
		handlers.addHandler(new MouseListenerHandler(false, window.getHandlerRelay()));
		
		handlers.addHandler(new GlassRelay());
		
		// Creates the (test) objects
		new JumpArrow(handlers);
		
		Glass beerGlass = new Glass(handlers, new Vector3D(500, 600), Beverage.BEER, 150, 0.7);
		new Glass(handlers, new Vector3D(1200, 500), Beverage.WHISKEY, 130, 0.8);
		new Glass(handlers, new Vector3D(800, 300), 
				Beverage.CHAMPANGE, 100, 0.6);
		
		for (int i = 0; i < 20; i++)
		{
			SwimmingDolphin.createDolphin(handlers, beerGlass);
		}
		
		new TextPerformanceMonitor(1000, window.getStepHandler());
		//new StepHandler.PerformanceAccelerator(1000, window.getStepHandler());
	}
}
