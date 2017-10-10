package roadGraph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import windowManager.CSDisplay;

public class Text {
	private double POS_X, POS_Y;
	private double angle;
	
	public static Color color = Color.BLACK;
	public String text = "";
	
	public Text(double iX, double iY, double iAngle, String t) {
		this.POS_X = iX;
		this.POS_Y = iY;
		this.angle = iAngle;
		
		this.text = t;
	}
	
	public double X() { return POS_X; }
	public double Y() { return POS_Y; }
	public double Angle() { return angle; }
	
	public void attemptToRender(Graphics2D g2d, int textWidth) {
		Vector2d posV = CSDisplay.linTrans(new Vector2d(POS_X, POS_Y));
		
		// Rendering
		AffineTransform t = g2d.getTransform(); // Saving current rotation state
		g2d.rotate(
				   angle,
				   posV.X()+textWidth,
				   posV.Y()
				  ); // Rotating next render
		
		// Actual square rendering
		g2d.setColor(color);
		g2d.drawString(text, posV.INTX(), posV.INTY());
	
		g2d.setTransform(t); // Returning to old rotation state
		
	}
}
