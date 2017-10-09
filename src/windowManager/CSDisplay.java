package windowManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import graphCore.Line;
import graphCore.Point;
import roadGraph.*;

public class CSDisplay {
	public static int WIDTH,
					  HEIGHT,
					  POS_X,
					  POS_Y;
	
	public static Vector2d zoomConstant;
	
	private static Vector2d displayPosition;
	private static double displayZoom;
	private static boolean displayChanged;
	
	private static Bend SELECTED_POINT = null;
	private static Vector2d[] point_vectors = new Vector2d [2];
	public static int MODE = 0;
	
	private int CLICK_X, CLICK_Y;
	
	private boolean CLICK_DOWN;
	
	public static Color borderColor, bgColor;
	
	public static ArrayList<Bend> points = new ArrayList<Bend>();
	public static ArrayList<Road> lines = new ArrayList<Road>();
	
	public CSDisplay() {
		WIDTH = 1000;
		HEIGHT = 900;
		POS_X = 0;
		POS_Y = 0;

		zoomConstant = new Vector2d (100,90);

		displayPosition = new Vector2d(0.0f, 0.0f);
		displayZoom = 1.0f;
		displayChanged = false;

		CLICK_DOWN = false;
		
		CLICK_X = 0;
		CLICK_Y = 0;

		borderColor = new Color(150,150,150);
		bgColor     = new Color(255,255,255);
	}
	
	public static void refreshDisplay() {
		displayChanged = true;
	}
	
	public static Vector2d linTrans(Vector2d v) {
		return v.difVector(displayPosition).getTransformSR(displayZoom);
	}
	
	public void moveScreen(int dx, int dy){
		displayPosition = displayPosition.sumVector(new Vector2d(dx,dy).getTransformRS(displayZoom));
		displayChanged = true;
	}
	
	public Vector2d getBendAtLocation(Vector2d v) {
		Vector2d bv;
		
		for(Bend b : points) {
			bv = new Vector2d(b.pos().X(), b.pos().Y());
			if(bv.difVector(v).length() <= Point.radius*Math.pow(displayZoom, -1)) {
				return bv;
			}
		}
		
		Vector2d rv, p1v, p2v;
		
		for(Road r : lines) {
			rv = new Vector2d((r.p1().pos().X()+r.p2().pos().X())/2, (r.p1().pos().Y()+r.p2().pos().Y())/2);
			p1v = new Vector2d(r.p1().pos().X(), r.p1().pos().Y());
			p2v = new Vector2d(r.p2().pos().X(), r.p2().pos().Y());
			if(p1v.difVector(v).length()+p2v.difVector(v).length() <= (p1v.difVector(p2v).length()+Road.zoomRange*Math.pow(displayZoom, -1))) {
				return rv;
			}
		}
		
		return null;
	}
	
	// Event Handlers
	public void mousePressed(MouseEvent e) {
		this.CLICK_X = e.getX();
		this.CLICK_Y = e.getY();
		
		if(e.getButton() == 1) {
			CLICK_DOWN = true;
		}
		
		if(MODE == 1 && e.getButton() == 3) { // ADD POINT
			Vector2d mouseV = new Vector2d(CLICK_X,CLICK_Y).getTransformRS(displayZoom).sumVector(displayPosition);
			points.add(new Bend(mouseV.X(), mouseV.Y()));
			
			displayChanged = true;
		} else if (MODE == 2 && e.getButton() == 3) { // ADD LINE
			Vector2d bv;
			Bend tb = null;
			for(Bend b : points) {
				bv = new Vector2d(b.pos().X(), b.pos().Y());
				if(bv.difVector(new Vector2d(CLICK_X, CLICK_Y).getTransformRS(displayZoom).sumVector(displayPosition)).length() <= Point.radius*Math.pow(displayZoom, -1)) {
					tb = b;
					continue;
				}
			}
			
			if(SELECTED_POINT != null) {
				if(tb == null) {
					Vector2d mouseV = new Vector2d(CLICK_X,CLICK_Y).getTransformRS(displayZoom).sumVector(displayPosition);
					tb = new Bend(mouseV.X(), mouseV.Y());
					points.add(tb);
				}
				
				lines.add(new Road(tb,SELECTED_POINT));
				SELECTED_POINT = null;
				displayChanged = true;
			} else if(SELECTED_POINT == null && tb != null) {
				SELECTED_POINT = tb;
			}
		}
	}
	public void mouseDrag(MouseEvent e) {
		if(CLICK_DOWN) {
			Vector2d dVector = new Vector2d(e.getX()-CLICK_X, e.getY()-CLICK_Y);
			displayPosition = displayPosition.difVector(dVector.getTransformRS(displayZoom));
		
			this.CLICK_X = e.getX();
			this.CLICK_Y = e.getY();
		}
		
		displayChanged = true;
	}
	public void mouseMove(MouseEvent e) {
		if(SELECTED_POINT != null) {
			Vector2d v1 = new Vector2d(SELECTED_POINT.pos().X(), SELECTED_POINT.pos().Y());
			Vector2d v2 = new Vector2d(e.getX(), e.getY()).getTransformRS(displayZoom).sumVector(displayPosition);
			
			point_vectors[0] = v1;
			point_vectors[1] = v2;
			
			displayChanged = true;
		}
	}
	public void mouseReleased() {
		CLICK_DOWN = false;
	}
	public void zoom(MouseWheelEvent e){
		int wr = e.getWheelRotation();
		
		if(((WIDTH*displayZoom)+(zoomConstant.X()*wr))/WIDTH > 0.1 && ((WIDTH*displayZoom)+(zoomConstant.X()*wr))/WIDTH < 2.0) {
			int mouseX = e.getX();
			int mouseY = e.getY();
			
			Vector2d mPos = new Vector2d(mouseX, mouseY).getTransformRS(displayZoom).sumVector(displayPosition);
			
			Vector2d bv = getBendAtLocation(mPos);
			if(bv != null) {
				mPos = bv;
			}
			
			Vector2d relPos = new Vector2d((double) (mouseX)/WIDTH, (double) (mouseY)/HEIGHT);
			Vector2d newMPOS = new Vector2d(relPos.X()*((WIDTH*displayZoom)+(zoomConstant.X()*wr)), relPos.Y()*((HEIGHT*displayZoom)+(zoomConstant.Y()*wr)));
			Vector2d nPos = mPos.difVector(newMPOS);
		
			displayZoom = ((WIDTH*displayZoom)+(zoomConstant.X()*wr))/WIDTH;
			displayPosition = nPos;
		
			displayChanged = true;
		}
	}
	
	public void tick() { // LOOP FUNCTION
		
	}
	
	public void draw(Graphics2D g2d) { // RENDER FUNCTION
		if (this.displayChanged) {
			g2d.clearRect(POS_X, POS_Y, WIDTH, HEIGHT);
			
			g2d.setColor(borderColor);
			g2d.drawRect(POS_X, POS_Y, WIDTH, HEIGHT);
			
			g2d.setColor(bgColor);
			g2d.fillRect(POS_X+1, POS_Y+1, WIDTH-2, HEIGHT-2);
			
			/*Image img = null;
			try {
				img = ImageIO.read(new File("img\\background\\BACKGROUND.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			Vector2d imgDim = new Vector2d(WIDTH,HEIGHT).getTransformRS(displayZoom);
			g2d.drawImage(img,0,0,WIDTH,HEIGHT,displayPosition.INTX(),displayPosition.INTY(),displayPosition.INTX()+imgDim.INTX(),displayPosition.INTY()+imgDim.INTY(),null);*/
			
			for(Road l : lines){
				l.attemptToRender(g2d, displayZoom, displayChanged);
			}
			
			for(Bend p : points){
				p.attemptToRender(g2d, displayZoom, displayChanged);
			}
			
			if(SELECTED_POINT != null) {
				Vector2d v1 = linTrans(point_vectors[0]);
				Vector2d v2 = linTrans(point_vectors[1]);
				
				// Calculations
				int yOffset = (int) Math.round(Math.ceil(Line.lineWidth / 2) * Math.pow(displayZoom, -1));
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
				g2d.setColor(Line.color);
				g2d.fillRect(
								(int) Math.round(v1.X()),
								(int) Math.round(v1.Y()) - yOffset,
								(int) Math.round(distance),
								(int) Math.round(Line.lineWidth*Math.pow(displayZoom, -1))
						    );
			
				g2d.setTransform(t); // Returning to old rotation state
			}
			
			displayChanged = false;
		}
	}
}
