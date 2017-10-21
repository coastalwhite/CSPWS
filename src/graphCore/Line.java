package graphCore;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import roadGraph.Vector2d;
import windowManager.CSDisplay;

public class Line {
	protected Point p1, p2;
	protected double weight;
	public static int lineWidth = 3;
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
		/*
		 * v(vect) = [  x   y  ]
		 * c = y / x
		 * n(vect) = [ -y   x  ]
		 * length(n) = sqrt ( (-y)^2 + x^2 ) = sqrt ( (x*c)^2 + x^2 ) = x * sqrt ( c^2 + 1 )
		 * x = length(n) / sqrt ( c^2 + 1 )
		 * 
		 * n(vect) = [ ( -1 * length(n) * c / sqrt ( c^2 + 1 ) )     ( length(n) / sqrt ( c^2 + 1 ) ) ]
		 */
		
		
		Vector2d difVector = v1.difVector(v2);
		
		double c = difVector.Y() / difVector.X();
		Vector2d yOffset = new Vector2d( ( -1 * Math.pow(displayZoom, -1) * (lineWidth / 2) * c / Math.sqrt(c*c + 1) ), Math.pow(displayZoom, -1) * (lineWidth / 2) / Math.sqrt(c*c + 1));
		
		Vector2d [] lineVectors = {
									v1.difVector(yOffset),
									v1.sumVector(yOffset),
									v2.sumVector(yOffset),
									v2.difVector(yOffset)
								   };
			
		int [] xPoints = new int[4];
		int [] yPoints = new int[4];
		
		int i = 0;
		for(Vector2d v : lineVectors) {
			xPoints[i] = v.INTX();
			yPoints[i] = v.INTY();
			i++;
		}
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
	}
	
	
	public void draw(Graphics2D g2d){ // Rendering method
		this.drawLine(g2d, new Vector2d(p1.pos().X(),p1.pos().Y()), new Vector2d(p2.pos().X(),p2.pos().Y()),1);
	}
}
