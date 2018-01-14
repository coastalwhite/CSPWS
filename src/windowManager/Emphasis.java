package windowManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import roadGraph.Vector2d;

public class Emphasis {
	public double EmpRadius = 0;
	
	public static Color EmpColor = Color.GREEN;
	
	public Emphasis(double radius) {
		EmpRadius = radius;
	}
	
	public void drawEmphasis(Graphics2D g2d, Vector2d v) {
		double r = EmpRadius / CSDisplay.displayZoom();
		
		g2d.setColor(EmpColor);
		g2d.setStroke(new BasicStroke((float) (2/CSDisplay.displayZoom())));
		g2d.draw(new Ellipse2D.Double(v.X()-r, v.Y()-r, r*2, r*2));
	}
}
