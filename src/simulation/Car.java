package simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import roadGraph.Road;
import roadGraph.Vector2d;
import windowManager.CSDisplay;

public class Car {
	private double speed = 30.0f; // m / s
	private double progress = 0.0f; // %
	
	private static double WIDTH = 1.5, LENGTH = 5;
	
	private static Color color = Color.GREEN;
	
	public Car() {
		
	}
	
	public boolean updateProgress(Road r) {
		/*
		 * P = Progress of the car on the road in percent
		 * V = Velocity of the car
		 * W = Weight / Length of the road
		 * T = Seconds passed
		 * 
		 * P += ( W / V ) / T
		 * 
		 */
		
		this.progress += speed / r.weight();
		
		if(progress >= 1.0) {
			progress = 0.0;
			return true;
		}
		
		return false;
	}
	
	public final void draw (Graphics2D g2d, Road r, double displayZoom) {
		Vector2d v1 = CSDisplay.linTrans(r.b1().pos().v());
		Vector2d v2 = CSDisplay.linTrans(r.b2().pos().v());
		
		Vector2d difVector = v2.difVector(v1);
		
		double c = difVector.Y() / difVector.X();
		Vector2d carYOffset = new Vector2d(
											( -1 * Math.pow(displayZoom, -1) * (WIDTH / 2) * c / Math.sqrt(c*c + 1) ),
											Math.pow(displayZoom, -1) * (WIDTH / 2) / Math.sqrt(c*c + 1)
										);
		Vector2d yOffset = new Vector2d(
				( -1 * Math.pow(displayZoom, -1) * (Road.lineWidth / 2) * c / Math.sqrt(c*c + 1) ),
				Math.pow(displayZoom, -1) * (Road.lineWidth / 2) / Math.sqrt(c*c + 1)
			);
		
		Vector2d progressVector = new Vector2d ( progress * difVector.X(), progress * difVector.Y() ).sumVector(v1);
		Vector2d carLength = new Vector2d (
											Math.pow(displayZoom, -1) * LENGTH / Math.sqrt(c*c + 1),
											Math.pow(displayZoom, -1) * LENGTH * c / Math.sqrt(c*c + 1) 
										   );
		
		Vector2d [] carVectors = {
					progressVector.difVector(carLength).difVector(carYOffset),
					progressVector.difVector(carLength).sumVector(carYOffset),
					progressVector.sumVector(carYOffset),
					progressVector.difVector(carYOffset)
		};
			
		int [] xPoints = new int[4];
		int [] yPoints = new int[4];
		
		Vector2d [] lineVectors = {
				v1.difVector(yOffset),
				v1.sumVector(yOffset),
				v2.sumVector(yOffset),
				v2.difVector(yOffset)
			   };
		
		int i = 0;
		for(Vector2d v : lineVectors) {
			xPoints[i] = v.INTX();
			yPoints[i] = v.INTY();
			i++;
		}
		
		g2d.setClip(new Polygon(xPoints, yPoints, 4));
		
		xPoints = new int[4];
		yPoints = new int[4];
		
		i = 0;
		for(Vector2d v : carVectors) {
			xPoints[i] = v.INTX();
			yPoints[i] = v.INTY();
			i++;
		}
		
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
		
		/*// Calculations
		int yOffset = (int) Math.round(Math.ceil(WIDTH / 2) * Math.pow(displayZoom, -1));
		Vector2d difVector = v2.difVector(v1);
		
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
				   v1.X()+progress*dx,
				   v1.Y()+progress*dy
				  ); // Rotating next render
		
		// Actual square rendering
		g2d.setColor(color);
		g2d.fillRect(
						(int) Math.round(v1.X()+progress*dx),
						(int) Math.round(v1.Y()+progress*dy) - yOffset,
						(int) Math.round(LENGTH*Math.pow(displayZoom, -1)),
						(int) Math.round(WIDTH*Math.pow(displayZoom, -1))
				    );
	
		g2d.setTransform(t); // Returning to old rotation state*/
	}
}
