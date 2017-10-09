package graphCore;

import java.awt.Color;
import java.awt.Graphics2D;

import roadGraph.Vector2d;

public class Point {
	
	protected Coord pos;
	public static int radius = 2;
	public static Color color = Color.BLACK;
	
	public Point(double iX, double iY){
		//INIT
		
		this.pos = new Coord(iX, iY);
	}
	
	// Calculation methods
	public double disTo(Point p){
		return pos.disTo(p.pos());
	}
	
	// Return methods
	public Coord pos(){
		return this.pos;
	}
	
	public void drawPoint(Graphics2D g2d, Vector2d v, double displayZoom) {
		g2d.setColor(color);
		g2d.fillOval(v.INTX()-(int) Math.round(radius*Math.pow(displayZoom, -1)), v.INTY()-(int) Math.round(radius*Math.pow(displayZoom, -1)), (int) Math.round(radius*2*Math.pow(displayZoom, -1)), (int) Math.round(radius*2*Math.pow(displayZoom, -1)));
	}
	
	public void draw(Graphics2D g2d){ // Rendering Method
		this.drawPoint(g2d, new Vector2d(this.pos().X(), this.pos().Y()), 1.0f);
	}
}
