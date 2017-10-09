package roadGraph;

import java.awt.Graphics2D;

import graphCore.*;
import windowManager.CSDisplay;

public class Road extends Line {
	private boolean doDisplay = false;
	public static int zoomRange = 5;
	
	private Bend b1, b2;

	public Road(Bend ip1, Bend ip2) {
		super((Point) ip1, (Point) ip2);
		
		this.b1 = ip1;
		this.b2 = ip2;
	}
	
	public Bend b1() { return this.b1; }
	public Bend b2() { return this.b2; }
	
	private final static boolean inRange(double i, double c1, double c2) {
		double min = c1,
			max = c2;
		if (c1 > c2) {
			min = c2;
			max = c1;
		}
		return i >= min && i <= max;
	}
	
	private final static boolean isCrossing(Vector2d v1, Vector2d v2, Vector2d w1, Vector2d w2) {
		/* https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
		 * https://www.youtube.com/playlist?list=PLZHQObOWTQDPD3MizzM2xVFitgF8hE_ab
		 * p = v1
		 * q = w1
		 * r = v1 - v2
		 * s = w1 - w2
		 * t = (q - p) x s / (r x s)
		 * u = (p - q) x r / (s x r)
		 */
		
		// Check if v1->v2 crosses w1->w2
		Vector2d p, q, r, s;
		double t, u;
		
		p = v1;
		q = w1;
		r = v1.difVector(v2);
		s = w1.difVector(w2);
		
		if(r.det(s) == 0) {
			return (q.difVector(p).det(r) == 0);
		}
		
		t = q.difVector(p).det(s) / r.det(s);
		u = p.difVector(q).det(r) / s.det(r);
		
		return (inRange(t,-1.0f,0.0f) && inRange(u,-1.0f,0.0f));
	}

	public void attemptToRender(Graphics2D g2d, double displayZoom, boolean displayChanged) {
		Vector2d v1, v2;
		
		if(displayChanged) {
			this.doDisplay = false;
			
			v1 = CSDisplay.linTrans(new Vector2d(b1.pos().X(), b1.pos().Y()));
			v2 = CSDisplay.linTrans(new Vector2d(b2.pos().X(), b2.pos().Y()));
			
			//System.out.println("X: " + v1.X());
			
			if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * lineWidth, CSDisplay.WIDTH+lineWidth, -1 * lineWidth, CSDisplay.HEIGHT+lineWidth); }
			if(!this.doDisplay) { this.doDisplay = v2.inRange(-1 * lineWidth, CSDisplay.WIDTH+lineWidth, -1 * lineWidth, CSDisplay.HEIGHT+lineWidth); }
			
			Vector2d w1, w2;
			
			if(!doDisplay) {
				// TOP
				w1 = new Vector2d(0, 0);
				w2 = new Vector2d(700, 0);
				
				if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
			}
			
			if(!doDisplay) {
				// BOTTOM
				w1 = new Vector2d(0, CSDisplay.HEIGHT);
				w2 = new Vector2d(CSDisplay.WIDTH, CSDisplay.HEIGHT);
	
				if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
			}
			
			if(!doDisplay) {
				// LEFT
				w1 = new Vector2d(0, 0);
				w2 = new Vector2d(0, CSDisplay.HEIGHT);
	
				if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
			}
			
			if(!doDisplay) {
				// RIGHT
				w1 = new Vector2d(CSDisplay.WIDTH, 0);
				w2 = new Vector2d(CSDisplay.WIDTH, CSDisplay.HEIGHT);
	
				if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
			}
		}
		
		if(doDisplay) {
			v1 = CSDisplay.linTrans(new Vector2d(b1.pos().X(), b1.pos().Y()));
			v2 = CSDisplay.linTrans(new Vector2d(b2.pos().X(), b2.pos().Y()));
			
			this.drawLine(g2d, v1, v2, displayZoom);
		}
		
	}	
}
