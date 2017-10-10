package windowManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

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
	
	public static boolean displayBackground;
	private static String backgroundPath = "";
	
	public static boolean enterText = false;
	private String textInput;
	private int textInputWidth;
	private boolean enteredText = false;
	
	private static Bend SELECTED_POINT = null;
	private static Vector2d[] point_vectors = new Vector2d [2];
	public static int MODE = 0;
	
	private int CLICK_X, CLICK_Y;
	
	private boolean CLICK_DOWN;
	
	public static Color borderColor, bgColor;
	
	public static ArrayList<Bend> points;
	public static ArrayList<Road> lines;
	public static ArrayList<Text> textObjects;
	
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
		
		resetState();
	}
	
	public static void refreshDisplay() {
		displayChanged = true;
	}
	public static void loadBackground(String path) {
		backgroundPath = path;
	}
	public static void resetState() {
		points = new ArrayList<Bend>();
		lines = new ArrayList<Road>();
		textObjects = new ArrayList<Text>();
	}
	
	public static Vector2d linTrans(Vector2d v) {
		return v.difVector(displayPosition).getTransformSR(displayZoom);
	}
	
	public void moveScreen(int dx, int dy){
		displayPosition = displayPosition.sumVector(new Vector2d(dx,dy).getTransformRS(displayZoom));
		displayChanged = true;
	}
	
	public Object getObjectAtLocation(Vector2d v) {
		Vector2d bv;
		
		for(Bend b : points) {
			bv = new Vector2d(b.pos().X(), b.pos().Y());
			if(bv.difVector(v).length() <= Point.radius*Math.pow(displayZoom, -1)) {
				return b;
			}
		}
		
		Vector2d p1v, p2v;
		
		for(Road r : lines) {
			p1v = new Vector2d(r.p1().pos().X(), r.p1().pos().Y());
			p2v = new Vector2d(r.p2().pos().X(), r.p2().pos().Y());
			if(p1v.difVector(v).length()+p2v.difVector(v).length() <= (p1v.difVector(p2v).length()+Road.zoomRange*Math.pow(displayZoom, -1))) {
				return r;
			}
		}
		
		return null;
	}
	public Vector2d getVectorAtLocation(Vector2d v) {
		Object o = getObjectAtLocation(v);
		
		if (o == null) {
			return v;
		}
		
		if(o.getClass() == Road.class) {
			Road r = (Road) o;
			return new Vector2d((r.p1().pos().X()+r.p2().pos().X())/2, (r.p1().pos().Y()+r.p2().pos().Y())/2);
		} else if(o.getClass() == Bend.class) {
			Bend b = (Bend) o;
			return new Vector2d(b.pos().X(), b.pos().Y());
		}
		
		return v;
	}
	public ArrayList<Integer> getRoadsWithBend(Bend b) {
		ArrayList<Integer> roadList = new ArrayList<Integer>();
		
		int i = 0;
		for(Road r : lines) {
			if(r.b1().equals(b) || r.b2().equals(b)) {
				roadList.add(i);
			}
			i++;
		}
		
		return roadList;
	}
	
	// Event Handlers
	public void mousePressed(MouseEvent e) {
		this.CLICK_X = e.getX();
		this.CLICK_Y = e.getY();
		
		Vector2d mouseV = new Vector2d(CLICK_X,CLICK_Y).getTransformRS(displayZoom).sumVector(displayPosition);
		if(e.getButton() == 1 && mouseV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
			CLICK_DOWN = true;
		}
		
		if(e.getButton() == 3) {
			Vector2d bv;
			Bend tb;
			
			switch (MODE) {
			case 0:
				break;
			case 1: // Remove objects
				Object o = getObjectAtLocation(mouseV);
				if (o == null) {
					break;
				}
				
				if(o.getClass() == Bend.class) {
					points.remove(points.indexOf((Bend) o));
					
					ArrayList<Integer> roads = getRoadsWithBend((Bend) o);
					int j = 0;
					for(int i : roads) {
						lines.remove(i-j++);
					}
					
					displayChanged = true;
				} else if(o.getClass() == Road.class) {
					lines.remove(lines.indexOf((Road) o));
					
					displayChanged = true;
				}
				break;
			case 2: // Add Point
				points.add(new Bend(mouseV.X(), mouseV.Y()));
				
				displayChanged = true;
				break;
			case 3: // Add Car Road
				tb = null;
				for(Bend b : points) {
					bv = new Vector2d(b.pos().X(), b.pos().Y());
					if(bv.difVector(mouseV).length() <= Point.radius*Math.pow(displayZoom, -1)) {
						tb = b;
						continue;
					}
				}
				
				if(SELECTED_POINT != null) {
					if(tb == null) {
						tb = new Bend(mouseV.X(), mouseV.Y());
						points.add(tb);
					}
					
					lines.add(new CarRoad(tb,SELECTED_POINT));
					SELECTED_POINT = null;
					point_vectors[0] = null;
					point_vectors[1] = null;
					displayChanged = true;
				} else if(SELECTED_POINT == null && tb != null) {
					SELECTED_POINT = tb;
				}
				break;
			case 4: // Add Bicycle Road
				tb = null;
				for(Bend b : points) {
					bv = new Vector2d(b.pos().X(), b.pos().Y());
					if(bv.difVector(mouseV).length() <= Point.radius*Math.pow(displayZoom, -1)) {
						tb = b;
						continue;
					}
				}
				
				if(SELECTED_POINT != null) {
					if(tb == null) {
						tb = new Bend(mouseV.X(), mouseV.Y());
						points.add(tb);
					}
					
					lines.add(new BicycleRoad(tb,SELECTED_POINT));
					SELECTED_POINT = null;
					point_vectors[0] = null;
					point_vectors[1] = null;
					displayChanged = true;
				} else if(SELECTED_POINT == null && tb != null) {
					SELECTED_POINT = tb;
				}
				break;
				
			case 7:
				if(enteredText) {
					Vector2d middleVector = point_vectors[0].sumVector(new Vector2d(textInputWidth, 0).getTransformRS(displayZoom));
					
					Vector2d v1 = middleVector.difVector(point_vectors[0]);
					Vector2d v2 = middleVector.difVector(mouseV);
					
					double angle = v1.dotProduct(v2);
					
					angle *= -1;
					if(v1.Y()-v2.Y() <= 0) {
						angle = Math.PI*2 - angle;
					}
					
					textObjects.add(new Text(point_vectors[0].X(), point_vectors[0].Y(), angle, textInput));
					
					CSDisplay.refreshDisplay();
					CSDisplay.MODE = 0;
					CSControl.MODE = 0;
					
					textInput = null;
				} else {
					if (mouseV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
						point_vectors[0] = mouseV;
						point_vectors[1] = null;
						
						CSControl.refreshDisplay();
						
						enteredText = true;
					}
				}
				break;
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
		if(textInput != null) {
			Vector2d mouseV = new Vector2d(e.getX(), e.getY()).getTransformRS(displayZoom).sumVector(displayPosition);
			
			point_vectors[1] = mouseV;
			if (mouseV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
				displayChanged = true;
			} else {
				CSControl.refreshDisplay();
			}
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
			
			mPos = getVectorAtLocation(mPos);
			
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
		if (enterText) {
			enterText = false;
			textInput = JOptionPane.showInputDialog(this 
					 ,"Enter text:");
			
			if (textInput == "") {
				MODE = 0;
				this.textInput = null;
			} else {
				this.textInputWidth = g2d.getFontMetrics().stringWidth(textInput);
			}
		}
		if (displayChanged) {
			g2d.clearRect(POS_X, POS_Y, WIDTH, HEIGHT);
			
			g2d.setColor(borderColor);
			g2d.drawRect(POS_X, POS_Y, WIDTH, HEIGHT);
			
			g2d.setColor(bgColor);
			g2d.fillRect(POS_X+1, POS_Y+1, WIDTH-2, HEIGHT-2);
			
			Font currentFont = g2d.getFont();
			Font newFont = currentFont.deriveFont((float) (currentFont.getSize() * Math.pow(displayZoom,-1)));
			g2d.setFont(newFont);
			
			if (displayBackground) {
				Image img = null;
				try {
					img = ImageIO.read(new File(backgroundPath));
				} catch (IOException e) {
					e.printStackTrace();
				}
				Vector2d imgDim = new Vector2d(WIDTH,HEIGHT).getTransformRS(displayZoom);
				g2d.drawImage(img,0,0,WIDTH,HEIGHT,displayPosition.INTX(),displayPosition.INTY(),displayPosition.INTX()+imgDim.INTX(),displayPosition.INTY()+imgDim.INTY(),null);
			}
				
			for(Road l : lines){
				l.attemptToRender(g2d, displayZoom);
			}
			
			for(Bend p : points){
				p.attemptToRender(g2d, displayZoom);
			}
			for(Text t : textObjects) {
				t.attemptToRender(g2d, g2d.getFontMetrics().stringWidth(t.text));
			}
			
			if(textInput != null && point_vectors[1] != null) {
				if (enteredText) {
					textInputWidth = g2d.getFontMetrics().stringWidth(textInput);
					
					Vector2d middleVector = point_vectors[0].sumVector(new Vector2d(textInputWidth, 0).getTransformRS(displayZoom));
					
					Vector2d v1 = middleVector.difVector(point_vectors[0]);
					Vector2d v2 = middleVector.difVector(point_vectors[1]);
					
					double angle = v1.dotProduct(v2);
					
					angle *= -1;
					if(v1.Y()-v2.Y() <= 0) {
						angle = Math.PI*2 - angle;
					}
					
					new Text(point_vectors[0].X(), point_vectors[0].Y(), angle, textInput).attemptToRender(g2d, textInputWidth);
				} else {
					new Text(point_vectors[1].X(), point_vectors[1].Y(), 0, textInput).attemptToRender(g2d, g2d.getFontMetrics().stringWidth(textInput));
				}
			}
			if(SELECTED_POINT != null && point_vectors[0] != null) {
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
				if(MODE == 3) {
					g2d.setColor(Color.BLUE);
				} else {
					g2d.setColor(Color.RED);
				}
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
