package graphCore;

import java.awt.Color;
import java.awt.Graphics2D;

import roadGraph.Vector2d;
import windowManager.CSDisplay;

public class Line {
	protected Point p1, p2;
	protected double weight;
	public static int lineWidth = 3;
	public Color color = Color.GRAY;
	public Color defaultColor;
	public Color arrowColor = Color.RED;
	public boolean drawArrow = true;
	
	public static double convertFactor = 1.0;
	
	public Line(Point ip1, Point ip2){
		// INIT
		
		this.p1 = ip1;
		this.p2 = ip2;
		
		defaultColor = Color.GRAY;
	}
	
	public void drawLine(Graphics2D g2d, Vector2d v1, Vector2d v2) {
		/*
		 * Formula examined in the Chapter "Normal Vector" of Results
		 * 
		 * v(vect) = [  x   y  ]
		 * c = y / x
		 * n(vect) = [ -y   x  ]
		 * length(n) = sqrt ( (-y)^2 + x^2 ) = sqrt ( (x*c)^2 + x^2 ) = x * sqrt ( c^2 + 1 )
		 * x = length(n) / sqrt ( c^2 + 1 )
		 * 
		 * n(vect) = [ ( -1 * length(n) * c / sqrt ( c^2 + 1 ) )     ( length(n) / sqrt ( c^2 + 1 ) ) ]
		 */
		
		
		double displayZoom = CSDisplay.displayZoom();
		
		// Get Line Vector and Gradient
		Vector2d difVector = v2.difVector(v1);
		double c = difVector.Y() / difVector.X();
		
		// LINE
		// 		Draw side offset
		Vector2d yOffset = new Vector2d( ( -1 * Math.pow(displayZoom, -1) * ((lineWidth * Math.pow(convertFactor, -1)) / 2) * c / Math.sqrt(c*c + 1) ), Math.pow(displayZoom, -1) * ((lineWidth * Math.pow(convertFactor, -1)) / 2) / Math.sqrt(c*c + 1));
		
		// 		Draw Corners of line
		Vector2d [] lineVectors = {
									v1.difVector(yOffset),
									v1.sumVector(yOffset),
									v2.sumVector(yOffset),
									v2.difVector(yOffset)
								   };
			
		int [] xPoints = new int[4];
		int [] yPoints = new int[4];
		
		// 		Create Coordinates
		int i = 0;
		for(Vector2d v : lineVectors) {
			xPoints[i] = v.INTX();
			yPoints[i] = v.INTY();
			i++;
		}
		
		// 		Draw Rectangle (Polygon)
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
		
		// ARROW
		if(drawArrow) { // Check if needed to draw
			//	 	Draw side offset
			yOffset = new Vector2d( ( -1 * Math.pow(displayZoom, -1) * (lineWidth * Math.pow(convertFactor, -1)) * c / Math.sqrt(c*c + 1) ), Math.pow(displayZoom, -1) * (lineWidth * Math.pow(convertFactor, -1)) / Math.sqrt(c*c + 1));
			Vector2d arrowLength = new Vector2d (
													Math.pow(displayZoom, -1) * 2 / Math.sqrt(c*c + 1),
													Math.pow(displayZoom, -1) * 2 * c / Math.sqrt(c*c + 1) 
												);
			
			// 		Turn arrow the arrow if needed
			if(difVector.X() < -1) {
				arrowLength = arrowLength.product(-1);
			}
			
			// 		Create corners
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
			
			//		Create Coordinates
			i = 0;
			for(Vector2d v : arrowVectors) {
				xAPoints[i] = v.INTX();
				yAPoints[i] = v.INTY();
				i++;
			}
			
			// 		Draw Arrow(Polygon)
			g2d.setColor(arrowColor);
			g2d.fillPolygon(xAPoints, yAPoints, 6);
		}
	}
	
	// Getter
	public Point p1(){
		return this.p1;
	}
	public Point p2(){
		return this.p2;
	}
	public double weight(){
		return this.getWeight() * convertFactor;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
