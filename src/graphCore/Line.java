package graphCore;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import roadGraph.Vector2d;

public class Line {
	protected Point p1, p2;
	protected double weight;
	public static int lineWidth = 2;
	public Color color = Color.GRAY;
	
	public Line(Point ip1, Point ip2){
		// INIT
		
		this.p1 = ip1;
		this.p2 = ip2;
	}
	
	// Return Methods
	public Point p1(){
		return this.p1;
	}
	public Point p2(){
		return this.p2;
	}
	public double weight(){
		return this.weight;
	}
	
	public void drawLine(Graphics2D g2d, Vector2d v1, Vector2d v2, double displayZoom) {
		// Calculations
		int yOffset = (int) Math.round(Math.ceil(lineWidth / 2) * Math.pow(displayZoom, -1));
		Vector2d difVector = v1.difVector(v2); 
		
		double distance = difVector.length();
		
		double dx = difVector.X();
		double dy = difVector.Y();
		
		double angle = Math.atan(dy/dx);
		
		if(dx>=0){ // Check for 180 degree turns
			angle += Math.PI;
		}
		
		// Rendering
		AffineTransform t = g2d.getTransform(); // Saving current rotation state
		g2d.rotate(
				   angle,
				   v1.X(),
				   v1.Y()
				  ); // Rotating next render
		
		// Actual square rendering
		g2d.setColor(color);
		g2d.fillRect(
						(int) Math.round(v1.X()),
						(int) Math.round(v1.Y()) - yOffset,
						(int) Math.round(distance),
						(int) Math.round(lineWidth*Math.pow(displayZoom, -1))
				    );
	
		g2d.setTransform(t); // Returning to old rotation state
	}
	
	
	public void draw(Graphics2D g2d){ // Rendering method
		this.drawLine(g2d, new Vector2d(p1.pos().X(),p1.pos().Y()), new Vector2d(p2.pos().X(),p2.pos().Y()),1);
	}
}
