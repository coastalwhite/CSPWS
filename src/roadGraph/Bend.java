package roadGraph;

import java.awt.Graphics2D;

import graphCore.Point;
import windowManager.CSDisplay;

public class Bend extends Point {
	private boolean doDisplay = false;

	public Bend(double iX, double iY) {
		super(iX, iY);
	}
	
	public void attemptToRender(Graphics2D g2d, double displayZoom) {
		Vector2d v1;
		
		this.doDisplay = false;
		
		v1 = CSDisplay.linTrans(new Vector2d(pos.X(), pos.Y()));
		
		if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, -1 * radius, CSDisplay.HEIGHT+radius); }
		
		if(!doDisplay) {
			// TOP
			if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, -1 * radius, 0); }
		}
		
		if(!doDisplay) {
			// BOTTOM
			if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, CSDisplay.HEIGHT, CSDisplay.HEIGHT+radius); }
		}
		
		if(!doDisplay) {
			// LEFT
			if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * radius, 0, -1 * radius, CSDisplay.HEIGHT+radius); }
		}
		
		if(!doDisplay) {
			// RIGHT
			if(!this.doDisplay) { this.doDisplay = v1.inRange(CSDisplay.WIDTH, CSDisplay.WIDTH+radius, -1 * radius, CSDisplay.HEIGHT+radius); }
		}
		
		if(doDisplay) {
			v1 = CSDisplay.linTrans(new Vector2d(pos.X(), pos.Y()));
			
			this.drawPoint(g2d, v1, displayZoom);
		}
		
	}
}
