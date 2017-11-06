package windowManager;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import graphCore.Line;
import graphCore.Point;
import graphics.MainWindow;
import graphics.ScreenGraphics;
import roadGraph.*;
import simulation.Car;

public class CSDisplay {
	
	// Window Settings
	public static int WIDTH                     =                         1000,
					  HEIGHT                    =                          900,
					  POS_X                     =                            0,
					  POS_Y                     =                            0;
	
	public static Color borderColor             =       new Color(150,150,150),
						bgColor                 =       new Color(255,255,255);
	
	
	// View Settings
	private static Vector2d displayPosition     =    new Vector2d (0.0f, 0.0f);
	private static double displayZoom           =                         1.0f;
	private static Vector2d zoomConstant        = new Vector2d (100.0f, 90.0f);
	private static boolean displayChanged       =                        false;
	
	// Editor Settings
	public static int MODE                      =                            0;
	
	// 		Image Loading
	public static boolean displayBackground     =                        false;
	private static String backgroundPath        =                           "";
	
	//		Text Loading
	public static String textInput				=						  null;
	
	//		Line adding
	private static Bend SELECTED_POINT          =                         null;
	private static Vector2d[] point_vectors     =             new Vector2d [2];
	
	// Content Vars
	public static ArrayList<Bend> points        =        new ArrayList<Bend>();
	public static ArrayList<Road> lines         =        new ArrayList<Road>();
	public static ArrayList<Text> textObjects   =        new ArrayList<Text>();
	
	// Simulation Settings
	public static boolean PLAY_SIMULATION       =                        false;
	public static Road weightEdit               =                         null;
	
	// Non Static
	private boolean clickedText = false;
	private int textInputWidth;
	
	// 		Mouse Dragging
	private boolean CLICK_DOWN;
	private int CLICK_X,
				CLICK_Y;
	
	public CSDisplay() {
		this.CLICK_DOWN = false;
		
		this.CLICK_X = 0;
		this.CLICK_Y = 0;
		
		this.clickedText = false;
		this.textInputWidth = 0;
	}
	
	public static double displayZoom() {
		return displayZoom;
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
		
		refreshDisplay();
	}
	
	public static Vector2d linTrans(Vector2d v) {
		return v.difVector(displayPosition).getTransformSR(displayZoom);
	}
	
	private Bend getBendAtLocation(Vector2d mouseV) {
		Bend tb = null;
		for(Bend b : points) {
			if(new Vector2d(b.pos().X(), b.pos().Y()).difVector(mouseV).length() <= Point.radius) {
				tb = b;
				continue;
			}
		}
		return tb;
	}

	public Object getObjectAtLocation(Vector2d v) {
		Vector2d bv;
		
		Bend b = getBendAtLocation(v);
		
		if (b != null) { return b; }
		
		/*
		 * http://www.geeksforgeeks.org/how-to-check-if-a-given-point-lies-inside-a-polygon/
		 */
		for(Road r : lines) {
			if(r.isIn(v)){
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

	
	// Click Actions
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
			
			refreshDisplay();
		}
	}
	private void addRoad(int type, Vector2d mouseV) {
		Bend tb = getBendAtLocation(mouseV);
		
		if(SELECTED_POINT != null) {
			boolean newPointCreated = false;
			if(tb == null) {
				tb = new Bend(mouseV.X(), mouseV.Y());
				points.add(tb);
				newPointCreated = true;
			}
			
			Road r;
			
			switch (type) {
			case 1:
				r = new CarRoad(SELECTED_POINT,tb,100000);
				break;
			case 2:
				r = new BicycleRoad(SELECTED_POINT,tb,100000);
				break;
			default:
				r = new Road(SELECTED_POINT,tb,100000);
				break;
			}
			
			if(!newPointCreated) {
				ArrayList<Integer> roadInts = getRoadsWithBend(tb);
				if(roadInts.size() != 0) {
					for(Integer rIndex : roadInts) {
						if(lines.get(rIndex).b1().equals(tb)) {
							r.nextRoad.add(lines.get(rIndex));
							r.nextRoadProbability.add(1.0);
						}
					}
					
					for(int i = 0; i < r.nextRoad.size(); i++) {
						r.nextRoadProbability.set(i, (1.0/r.nextRoad.size()));
					}
				}
				
			}
			
			if (!tb.equals(SELECTED_POINT)) {
				
				for(Integer rIndex : getRoadsWithBend(SELECTED_POINT)) {
					if(lines.get(rIndex).b2().equals(SELECTED_POINT)) {
						if(lines.get(rIndex).nextRoad.size() == 0) {
							lines.get(rIndex).nextRoad.add(r);
							lines.get(rIndex).nextRoadProbability.add(1.0);
						} else {
							for(int i = 0; i < lines.get(rIndex).nextRoad.size(); i++) {
								lines.get(rIndex).nextRoadProbability.set(i, lines.get(rIndex).nextRoadProbability.get(i)/2);
							}
							lines.get(rIndex).nextRoad.add(r);
							lines.get(rIndex).nextRoadProbability.add(0.5);
						}
					}
				}
				
				lines.add(r);
				
			}
			
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			refreshDisplay();
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
			
			CrossoverRoad cr = new CrossoverRoad(tb,SELECTED_POINT,1);
			
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
					lines.add(new CarRoad(smallestRoad.b1(),stBend,1));
					lines.add(new CarRoad(stBend,smallestRoad.b2(),1));
				} else if(BicycleRoad.class == smallestRoad.getClass()) {
					lines.add(new BicycleRoad(smallestRoad.b1(),stBend,1));
					lines.add(new BicycleRoad(stBend,smallestRoad.b2(),1));
				} else if(CrossoverRoad.class == smallestRoad.getClass()) {
					lines.add(new CrossoverRoad(smallestRoad.b1(),stBend,1));
					lines.add(new CrossoverRoad(stBend,smallestRoad.b2(),1));
				} else {
					lines.add(new Road(smallestRoad.b1(),stBend,1));
					lines.add(new Road(stBend,smallestRoad.b2(),1));
				}
					
				
				points.add(stBend);
				lines.add(new CrossoverRoad(tb,stBend,1));
				tb = stBend;
				
				startJ++;
			}

			lines.add(new CrossoverRoad(tb,SELECTED_POINT,1));
			
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			
			refreshDisplay();
		} else {
			if(tb == null) {
				tb = new CrossoverPoint(mouseV.X(), mouseV.Y());
				points.add(tb);
			}
			
			SELECTED_POINT = tb;
		}
		
		refreshDisplay();
	}
	private void addText(Vector2d mouseV) {
		if(clickedText) {
			System.out.println("hi!");
			Vector2d middleVector = point_vectors[0].sumVector(new Vector2d(textInputWidth, 0).getTransformRS(displayZoom));
			
			Vector2d v1 = new Vector2d(textInputWidth, 0).getTransformRS(displayZoom);
			Vector2d v2 = middleVector.difVector(mouseV.getTransformRS(displayZoom).sumVector(displayPosition));
			
			double angle = v1.dotProduct(v2);
			
			angle *= -1;
			if(v1.Y()-v2.Y() <= 0) {
				angle = Math.PI*2 - angle;
			}
			
			textObjects.add(new Text(point_vectors[0].X(), point_vectors[0].Y(), angle, textInput));
			
			clickedText = false;
			
			refreshDisplay();
			CSDisplay.MODE = 0;
			CSControl.MODE = 0;
			
			textInput = null;
		} else {
			System.out.println("hi!1");
			if (mouseV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
				point_vectors[0] = mouseV.getTransformRS(displayZoom).sumVector(displayPosition);
				point_vectors[1] = null;
				
				clickedText = true;
				
				CSDisplay.refreshDisplay();
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
			
			Road tempR = new Road(tb,SELECTED_POINT,1);
			
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
					lines.add(new CarRoad(smallestRoad.b1(),stBend,1));
					lines.add(new CarRoad(smallestRoad.b2(),stBend,1));
				} else if(BicycleRoad.class == smallestRoad.getClass()) {
					lines.add(new BicycleRoad(smallestRoad.b1(),stBend,1));
					lines.add(new BicycleRoad(smallestRoad.b2(),stBend,1));
				} else if(CrossoverRoad.class == smallestRoad.getClass()) {
					lines.add(new CrossoverRoad(smallestRoad.b1(),stBend,1));
					lines.add(new CrossoverRoad(smallestRoad.b2(),stBend,1));
				} else {
					lines.add(new Road(smallestRoad.b1(),stBend,1));
					lines.add(new Road(smallestRoad.b2(),stBend,1));
				}
					
				
				points.add(stBend);
				tb = stBend;
				
				startJ++;
			}
			
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			
			refreshDisplay();
		} else {
			if(tb == null) {
				tb = new Bend(mouseV.X(), mouseV.Y());
			}
			
			SELECTED_POINT = tb;
		}
		
		refreshDisplay();
	}
	private void showObjectInfo(Vector2d mouseV) {
		Object o = getObjectAtLocation(mouseV);
		if(o != null) {
			CSControl.showObjectInfo(o);
		}
	}
	
	// Event Handlers
	public void mousePressed(MouseEvent e) {
		this.CLICK_X = e.getX();
		this.CLICK_Y = e.getY();
		
		Vector2d mouseV = new Vector2d(CLICK_X,CLICK_Y).getTransformRS(displayZoom).sumVector(displayPosition);
		if(e.getButton() == 1 && new Vector2d(CLICK_X, CLICK_Y).inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
			if(weightEdit == null) {
				CLICK_DOWN = true;
			} else {
				if(!weightEdit.editWeight(mouseV)) {
					CLICK_DOWN = true;
				}
			}
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
				
				refreshDisplay();
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
			case 7: // Add Text
				this.addText(new Vector2d(CLICK_X, CLICK_Y));
				break;
			case 8: // Show Object Info
				this.showObjectInfo(mouseV);
				break;
			}
		}
	}
	public void mouseDrag(MouseEvent e) {
		// General mouse around
		if(CLICK_DOWN && !displayBackground) {
			MainWindow.GUI.setCursor(Cursor.HAND_CURSOR);
			displayPosition = displayPosition.difVector(new Vector2d(e.getX()-CLICK_X, e.getY()-CLICK_Y).getTransformRS(displayZoom));
		
			this.CLICK_X = e.getX();
			this.CLICK_Y = e.getY();
		}
		
		refreshDisplay();
	}
	public void mouseMove(MouseEvent e) {
		// Line preview
		if(SELECTED_POINT != null) {
			Vector2d v2 = new Vector2d(e.getX(), e.getY()).getTransformRS(displayZoom).sumVector(displayPosition);
			
			point_vectors[1] = v2;
			
			refreshDisplay();
		}
		
		// Text Rotation
		if(textInput != null) {
			Vector2d mouseV = new Vector2d(e.getX(), e.getY());
			
			if (mouseV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
				point_vectors[clickedText ? 1 : 0] = mouseV.getTransformRS(displayZoom).sumVector(displayPosition);
				refreshDisplay();
			}
		}
	}
	public void mouseReleased(MouseEvent e) {
		CLICK_DOWN = false;
		
		// Display Optimize for Image Loading
		if(displayBackground) {
			Vector2d dVector = new Vector2d(e.getX()-CLICK_X, e.getY()-CLICK_Y);
			displayPosition = displayPosition.difVector(dVector.getTransformRS(displayZoom));
		
			this.CLICK_X = e.getX();
			this.CLICK_Y = e.getY();
			
			refreshDisplay();
		}
	}
	public void zoom(MouseWheelEvent e){
		int c = e.getWheelRotation();
		double newZoom = displayZoom + zoomConstant.X() * c / WIDTH;
		
		if(newZoom > 0.1 && newZoom < 4.0) {
			/*
			 * Mx = Mouse X in Pixels
			 * My = Mouse Y in Pixels
			 * WIDTH = Display Width in Pixels
			 * HEIGHT = Display Height in Pixels
			 * Px = X position of the display
			 * Py = Y position of the display
			 * Z = Factor of zoom in display
			 * WZ = Amount of X zoom in Pixels
			 * HZ = Amount of Y zoom in Pixels
			 * C = -1 for zoom out / 1 for zoom in
			 * 
			 * Mouse Position (MP) : Vector ( Mx * Z + Px , My * Z + Py )
			 * Relative Position (R) : Vector ( Mx / WIDTH , My / HEIGHT )
			 * New Mouse Position (NM) : Vector ( Rx * ( ( WIDTH * Z ) + ( WZ * C ) ) , Ry * ( ( HEIGHT * Z ) + ( HZ * C ) ) )
			 * New display position : MP - NM = P - ( ( M * F * C ) / D )
			 */
			
			displayZoom = newZoom;
			displayPosition = displayPosition.difVector(new Vector2d((e.getX() * zoomConstant.X() * c) / WIDTH, (e.getY() * zoomConstant.Y() * c) / HEIGHT));
		
			refreshDisplay();
		}
	}
	
	public void tick() { // LOOP FUNCTION
		if (PLAY_SIMULATION) {
			for(Road r : lines) {
				r.tick();
			}
		}
		if (!CLICK_DOWN) { MainWindow.GUI.setCursor(Cursor.getDefaultCursor()); }
	}
	
	public void draw(Graphics2D g2d) { // RENDER FUNCTION
		g2d.setClip(new Rectangle(POS_X, POS_Y, WIDTH, HEIGHT));
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
				g2d.drawImage(img,0,0,WIDTH,HEIGHT,displayPosition.INTX(),displayPosition.INTY(),displayPosition.INTX()+new Vector2d(WIDTH,HEIGHT).getTransformRS(displayZoom).INTX(),displayPosition.INTY()+new Vector2d(WIDTH,HEIGHT).getTransformRS(displayZoom).INTY(),null);
			}
				
			for(Road l : lines){
				l.attemptToRender(g2d, displayZoom);
			}
			
			if(SELECTED_POINT != null && point_vectors[1] != null) {
				if(MODE == 3) {
					new CarRoad(SELECTED_POINT, new Bend(point_vectors[1].X(), point_vectors[1].Y()), 1).attemptToRender(g2d, displayZoom);
				} else if(MODE == 4) {
					new BicycleRoad(SELECTED_POINT, new Bend(point_vectors[1].X(), point_vectors[1].Y()), 1).attemptToRender(g2d, displayZoom);
				} else if(MODE == 6) {
					new CrossoverRoad(SELECTED_POINT, new Bend(point_vectors[1].X(), point_vectors[1].Y()), 1).attemptToRender(g2d, displayZoom);
				}
			}
			
			for(Bend p : points){
				p.attemptToRender(g2d, displayZoom);
			}
			for(Text t : textObjects) {
				t.attemptToRender(g2d, displayZoom);
			}
			for(Road l : lines){
				l.drawEditWeight(g2d);
			}
			
			if(textInput != null) {
				if(point_vectors[1] != null) {
					textInputWidth = g2d.getFontMetrics().stringWidth(textInput);
					
					Vector2d middleVector = point_vectors[0].sumVector(new Vector2d(textInputWidth, 0).getTransformRS(displayZoom));
					
					Vector2d v1 = new Vector2d(textInputWidth, 0).getTransformRS(displayZoom);
					Vector2d v2 = middleVector.difVector(point_vectors[1]);
					
					double angle = v1.dotProduct(v2);
					
					angle *= -1;
					if(v1.Y()-v2.Y() <= 0) {
						angle = Math.PI*2 - angle;
					}
					
					new Text(point_vectors[0].X(), point_vectors[0].Y(), angle, textInput).attemptToRender(g2d, displayZoom);
				} else if(point_vectors[0] != null) {
					new Text(point_vectors[0].X(), point_vectors[0].Y(), 0, textInput).attemptToRender(g2d, displayZoom);
				}
			}
			
			displayChanged = false;
		}
		if(PLAY_SIMULATION) {
			for (Road r : lines) {
				if(r.cars.size() != 0) {
					r.attemptToRender(g2d, displayZoom);
					
					r.b1().attemptToRender(g2d, displayZoom);
					r.b2().attemptToRender(g2d, displayZoom);
					
					r.renderCars(g2d, displayZoom);
					
					g2d.setClip(new Rectangle(POS_X, POS_Y, WIDTH, HEIGHT));
				}
			}
		}
	}
}
