package roadGraph;

import java.awt.Graphics2D;

import graphCore.Point;
import windowManager.CSControl;
import windowManager.CSDisplay;

public class Bend extends Point {
	private boolean doDisplay = false;
	
	public double carsPerSecond = 0.0;
	public double bikesPerSecond = 0.0;

	public Bend(double iX, double iY) {
		super(iX, iY);
	}
	
	public void attemptToRender(Graphics2D g2d, double displayZoom) {
		Vector2d v1;
		
		this.doDisplay = false;
		
		v1 = CSDisplay.tSR(new Vector2d(pos.X(), pos.Y()));
		
		if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, -1 * radius, CSDisplay.HEIGHT+radius); }
		
		if(!doDisplay) {
			// TOP
			this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, -1 * radius, 0);
		}
		
		if(!doDisplay) {
			// BOTTOM
			this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, CSDisplay.HEIGHT, CSDisplay.HEIGHT+radius);
		}
		
		if(!doDisplay) {
			// LEFT
			this.doDisplay = v1.inRange(-1 * radius, 0, -1 * radius, CSDisplay.HEIGHT+radius);
		}
		
		if(!doDisplay) {
			// RIGHT
			this.doDisplay = v1.inRange(CSDisplay.WIDTH, CSDisplay.WIDTH+radius, -1 * radius, CSDisplay.HEIGHT+radius);
			CSControl.refreshDisplay();
		}
		
		if(doDisplay) {
			v1 = CSDisplay.tSR(new Vector2d(pos.X(), pos.Y()));
			
			this.drawPoint(g2d, v1, displayZoom);
		}
		
	}
}
