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
	
	public boolean isIn(Vector2d v) {
		Vector2d difVector = p2.pos().v().difVector(p1.pos().v());
		
		double c = difVector.Y() / difVector.X();
		Vector2d yOffset = new Vector2d( ( -1 * Math.pow(CSDisplay.displayZoom(), -1) * (lineWidth / 2) * c / Math.sqrt(c*c + 1) ), Math.pow(CSDisplay.displayZoom(), -1) * (lineWidth / 2) / Math.sqrt(c*c + 1));
		
		
		System.out.println(p1.pos().X());
		
		Vector2d [] vect = {
									p1.pos().v().difVector(yOffset),
									p1.pos().v().sumVector(yOffset),
									p2.pos().v().sumVector(yOffset),
									p2.pos().v().difVector(yOffset)
								   };
		
		double A = 1/2 * (-1 * vect[1].Y() * vect[2].X() + vect[0].Y() * (-1 * vect[1].X() + vect[2].X()) + vect[0].X() * (vect[1].Y() - vect[2].Y()) + vect[1].X() * vect[2].Y());
	    double sign = A < 0 ? -1 : 1;
	    double s = (vect[0].Y() * vect[2].X() - vect[0].X() * vect[2].Y() + (vect[2].Y() - vect[0].Y()) * v.X() + (vect[0].X() - vect[2].X()) * v.Y()) * sign;
	    double t = (vect[0].Y() * vect[1].X() - vect[0].X() * vect[1].Y() + (vect[0].Y() - vect[1].Y()) * v.X() + (vect[1].X() - vect[0].X()) * v.Y()) * sign;
	    
	    boolean c1 = s > 0 && t > 0 && (s + t) < 2 * A * sign;
	    
	    A = 1/2 * (-1 * vect[1].Y() * vect[2].X() + vect[3].Y() * (-1 * vect[1].X() + vect[2].X()) + vect[3].X() * (vect[1].Y() - vect[2].Y()) + vect[1].X() * vect[2].Y());
	    sign = A < 0 ? -1 : 1;
	    s = (vect[3].Y() * vect[2].X() - vect[3].X() * vect[2].Y() + (vect[2].Y() - vect[3].Y()) * v.X() + (vect[3].X() - vect[2].X()) * v.Y()) * sign;
	    t = (vect[3].Y() * vect[1].X() - vect[3].X() * vect[1].Y() + (vect[3].Y() - vect[1].Y()) * v.X() + (vect[1].X() - vect[3].X()) * v.Y()) * sign;
	    
	    boolean c2 = s > 0 && t > 0 && (s + t) < 2 * A * sign;
	    
	    return c1 || c2;
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
		
		
		Vector2d difVector = v2.difVector(v1);
		
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
		
		yOffset = new Vector2d( ( -1 * Math.pow(displayZoom, -1) * lineWidth * c / Math.sqrt(c*c + 1) ), Math.pow(displayZoom, -1) * lineWidth / Math.sqrt(c*c + 1));
		Vector2d arrowLength = new Vector2d (
				Math.pow(displayZoom, -1) * 2 / Math.sqrt(c*c + 1),
				Math.pow(displayZoom, -1) * 2 * c / Math.sqrt(c*c + 1) 
			   );
		
		/*if(difVector.Y() < -1) {
			arrowLength = arrowLength.product(-1);
		}*/
		if(difVector.X() < -1) {
			arrowLength = arrowLength.product(-1);
		}
		
		Vector2d [] arrowVectors = {
									v1.sumVector(difVector.quotient(2)),
									v1.sumVector(difVector.quotient(2)).difVector(arrowLength).difVector(yOffset),
									v1.sumVector(difVector.quotient(2)).difVector(yOffset),
									v1.sumVector(difVector.quotient(2)).sumVector(arrowLength),
									v1.sumVector(difVector.quotient(2)).sumVector(yOffset),
									v1.sumVector(difVector.quotient(2)).difVector(arrowLength).sumVector(yOffset)
								   };
			
		int [] xAPoints = new int[6];
		int [] yAPoints = new int[6];
		
		i = 0;
		for(Vector2d v : arrowVectors) {
			xAPoints[i] = v.INTX();
			yAPoints[i] = v.INTY();
			i++;
		}
		g2d.setColor(Color.RED);
		g2d.fillPolygon(xAPoints, yAPoints, 6);
	}
		
	public void draw(Graphics2D g2d){ // Rendering method
		this.drawLine(g2d, new Vector2d(p1.pos().X(),p1.pos().Y()), new Vector2d(p2.pos().X(),p2.pos().Y()),1);
	}
}
