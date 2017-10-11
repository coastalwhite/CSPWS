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
		
		for(Text t : textObjects) {
			bv = new Vector2d(t.X()+t.defaultLength(), t.Y());
			
			if(bv.difVector(v).length() <= 10) {
				return t;
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
	
	private Bend getBendAtLocation(Vector2d mouseV) {
		Bend tb = null;
		Vector2d bv;
		for(Bend b : points) {
			bv = new Vector2d(b.pos().X(), b.pos().Y());
			if(bv.difVector(mouseV).length() <= Point.radius*Math.pow(displayZoom, -1)) {
				tb = b;
				continue;
			}
		}
		return tb;
	}
	private void removeObjects(Vector2d mouseV) {
		Object o = getObjectAtLocation(mouseV);
		
		if(o != null) {
			if(o instanceof Bend) {
				points.remove(points.indexOf((Bend) o));
				
				ArrayList<Integer> roads = getRoadsWithBend((Bend) o);
				int j = 0;
				for(int i : roads) {
					lines.remove(i-j++);
				}
			} else if(o instanceof Road) {
				lines.remove(lines.indexOf((Road) o));
			} else if(o instanceof Text) {
				textObjects.remove((Text) o); 
			}
			
			displayChanged = true;
		}
	}
	private void addRoad(int type, Vector2d mouseV) {
		Bend tb = getBendAtLocation(mouseV);
		
		if(SELECTED_POINT != null) {
			if(tb == null) {
				tb = new Bend(mouseV.X(), mouseV.Y());
				points.add(tb);
			}
			
			switch (type) {
			case 1:
				lines.add(new CarRoad(tb,SELECTED_POINT));
				break;
			case 2:
				lines.add(new BicycleRoad(tb,SELECTED_POINT));
				break;
			default:
				lines.add(new Road(tb,SELECTED_POINT));
				break;
			}
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			displayChanged = true;
		} else {
			SELECTED_POINT = tb;
		}
	}
	private void addCrossover(Vector2d mouseV) {
		Bend tb = getBendAtLocation(mouseV);
		Vector2d bv;
		
		if(SELECTED_POINT != null) {
			if(tb == null) {
				tb = new CrossoverPoint(mouseV.X(), mouseV.Y());
				points.add(tb);
			}
			
			ArrayList<Double> ts = new ArrayList<Double>();
			ArrayList<Road> roads = new ArrayList<Road>();
			
			CrossoverRoad cr = new CrossoverRoad(tb,SELECTED_POINT);
			
			for(Road r : lines) {
				bv = r.getCrosspoint(cr);
				
				if(bv != null) {
					ts.add(bv.X());
					roads.add(r);
				}
			}
			
			double smallestT, smallestTS;
			int startJ = 0, sTIndex, j = 0;
			
			Road smallestRoad;
			
			CrossoverPoint stBend;
			
			for(int i = 0; i < ts.size(); i++) {
				smallestT = 1.1;
				sTIndex = 0;
				for(j = startJ; j < ts.size(); j++) {
					if(ts.get(j)<smallestT) {
						sTIndex = j;
						smallestT = ts.get(j);
					}
				}
				smallestTS = ts.get(sTIndex);
				smallestRoad = roads.get(sTIndex);
				
				ts.set(sTIndex, ts.get(startJ));
				roads.set(sTIndex, roads.get(startJ));
				
				ts.set(startJ, smallestTS);
				roads.set(startJ, smallestRoad);
				
				stBend = new CrossoverPoint(
									cr.p1().pos().X() + smallestTS*
									(cr.p2().pos().X() - cr.p1().pos().X()),
								cr.p1().pos().Y()+smallestTS*
									(cr.p2().pos().Y() - cr.p1().pos().Y())
						);
				
				lines.remove(lines.indexOf(smallestRoad));
				
				if(CarRoad.class == smallestRoad.getClass()) {
					lines.add(new CarRoad(smallestRoad.b1(),stBend));
					lines.add(new CarRoad(smallestRoad.b2(),stBend));
				} else if(BicycleRoad.class == smallestRoad.getClass()) {
					lines.add(new BicycleRoad(smallestRoad.b1(),stBend));
					lines.add(new BicycleRoad(smallestRoad.b2(),stBend));
				} else if(CrossoverRoad.class == smallestRoad.getClass()) {
					lines.add(new CrossoverRoad(smallestRoad.b1(),stBend));
					lines.add(new CrossoverRoad(smallestRoad.b2(),stBend));
				} else {
					lines.add(new Road(smallestRoad.b1(),stBend));
					lines.add(new Road(smallestRoad.b2(),stBend));
				}
					
				
				points.add(stBend);
				lines.add(new CrossoverRoad(tb,stBend));
				tb = stBend;
				
				startJ++;
			}

			lines.add(new CrossoverRoad(tb,SELECTED_POINT));
			
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			
			displayChanged = true;
		} else {
			if(tb == null) {
				tb = new CrossoverPoint(mouseV.X(), mouseV.Y());
				points.add(tb);
			}
			
			SELECTED_POINT = tb;
		}
		
		displayChanged = true;
	}
	private void addText(Vector2d mouseV) {
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
	}
	private void addTrafficLight(Vector2d mouseV) {
		Bend tb = getBendAtLocation(mouseV);
		Vector2d bv;
		
		if(SELECTED_POINT != null) {
			if(tb == null) {
				tb = new CrossoverPoint(mouseV.X(), mouseV.Y());
			}
			
			ArrayList<Double> ts = new ArrayList<Double>();
			ArrayList<Road> roads = new ArrayList<Road>();
			
			Road tempR = new Road(tb,SELECTED_POINT);
			
			for(Road r : lines) {
				bv = r.getCrosspoint(tempR);
				
				if(bv != null) {
					ts.add(bv.X());
					roads.add(r);
				}
			}
			
			double smallestT, smallestTS;
			int startJ = 0, sTIndex, j = 0;
			
			Road smallestRoad;
			
			TrafficLight stBend;
			
			for(int i = 0; i < ts.size(); i++) {
				smallestT = 1.1;
				sTIndex = 0;
				for(j = startJ; j < ts.size(); j++) {
					if(ts.get(j)<smallestT) {
						sTIndex = j;
						smallestT = ts.get(j);
					}
				}
				smallestTS = ts.get(sTIndex);
				smallestRoad = roads.get(sTIndex);
				
				ts.set(sTIndex, ts.get(startJ));
				roads.set(sTIndex, roads.get(startJ));
				
				ts.set(startJ, smallestTS);
				roads.set(startJ, smallestRoad);
				
				stBend = new TrafficLight(
									tempR.p1().pos().X() + smallestTS*
									(tempR.p2().pos().X() - tempR.p1().pos().X()),
									tempR.p1().pos().Y()+smallestTS*
									(tempR.p2().pos().Y() - tempR.p1().pos().Y())
						);
				
				lines.remove(lines.indexOf(smallestRoad));
				
				if(CarRoad.class == smallestRoad.getClass()) {
					lines.add(new CarRoad(smallestRoad.b1(),stBend));
					lines.add(new CarRoad(smallestRoad.b2(),stBend));
				} else if(BicycleRoad.class == smallestRoad.getClass()) {
					lines.add(new BicycleRoad(smallestRoad.b1(),stBend));
					lines.add(new BicycleRoad(smallestRoad.b2(),stBend));
				} else if(CrossoverRoad.class == smallestRoad.getClass()) {
					lines.add(new CrossoverRoad(smallestRoad.b1(),stBend));
					lines.add(new CrossoverRoad(smallestRoad.b2(),stBend));
				} else {
					lines.add(new Road(smallestRoad.b1(),stBend));
					lines.add(new Road(smallestRoad.b2(),stBend));
				}
					
				
				points.add(stBend);
				tb = stBend;
				
				startJ++;
			}
			
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			
			displayChanged = true;
		} else {
			if(tb == null) {
				tb = new Bend(mouseV.X(), mouseV.Y());
			}
			
			SELECTED_POINT = tb;
		}
		
		displayChanged = true;
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
			switch (MODE) {
			case 0:
				break;
			case 1: // Remove objects
				this.removeObjects(mouseV);
				break;
			case 2: // Add Point
				points.add(new Bend(mouseV.X(), mouseV.Y()));
				
				displayChanged = true;
				break;
			case 3: // Add Car Road
				this.addRoad(1, mouseV);
				break;
			case 4: // Add Bicycle Road
				this.addRoad(2, mouseV);
				break;
			case 5: // Add Traffic Light
				this.addTrafficLight(mouseV);
				break;
			case 6: // Add Crossover
				this.addCrossover(mouseV);
				break;
			case 7:
				this.addText(mouseV);
				break;
			}
		}
	}
	public void mouseDrag(MouseEvent e) {
		if(CLICK_DOWN && !displayBackground) {
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
	public void mouseReleased(MouseEvent e) {
		CLICK_DOWN = false;
		if(displayBackground) {
			Vector2d dVector = new Vector2d(e.getX()-CLICK_X, e.getY()-CLICK_Y);
			displayPosition = displayPosition.difVector(dVector.getTransformRS(displayZoom));
		
			this.CLICK_X = e.getX();
			this.CLICK_Y = e.getY();
			
			displayChanged = true;
		}
	}
	public void zoom(MouseWheelEvent e){
		int wr = e.getWheelRotation();
		
		if(((WIDTH*displayZoom)+(zoomConstant.X()*wr))/WIDTH > 0.1 && ((WIDTH*displayZoom)+(zoomConstant.X()*wr))/WIDTH < 4.0) {
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
				t.attemptToRender(g2d, displayZoom);
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
					
					new Text(point_vectors[0].X(), point_vectors[0].Y(), angle, textInput).attemptToRender(g2d, displayZoom);
				} else {
					new Text(point_vectors[1].X(), point_vectors[1].Y(), 0, textInput).attemptToRender(g2d, displayZoom);
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
				} else if(MODE == 4) {
					g2d.setColor(Color.RED);
				} else if(MODE == 6) {
					g2d.setColor(Color.LIGHT_GRAY);
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
