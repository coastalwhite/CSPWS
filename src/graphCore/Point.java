package graphCore;

import java.awt.Color;
import java.awt.Graphics2D;

import roadGraph.Vector2d;
import windowManager.CSDisplay;

public class Point {
	
	protected Coord pos;
	public static int radius = 5;
	public Color color = Color.BLACK;
	
	public Point(double iX, double iY){
		//INIT
		
		this.pos = new Coord(iX, iY);
	}
	
	// Calculation methods
	public float disTo(Point p){
		return pos.disTo(p.pos());
	}
	
	// Getter
	public Coord pos(){
		return this.pos;
	}
	
	public void drawPoint(Graphics2D g2d, Vector2d v) {
		double displayZoom = CSDisplay.displayZoom();
		
		double r = radius;
		
		// Draw Point(Oval)
		g2d.setColor(color);
		g2d.fillOval(
						v.INTX()-(int) Math.round(r*Math.pow(displayZoom, -1)),
						v.INTY()-(int) Math.round(r*Math.pow(displayZoom, -1)),
						(int) Math.round(r*2*Math.pow(displayZoom, -1)),
						(int) Math.round(r*2*Math.pow(displayZoom, -1))
					);
	}
}
