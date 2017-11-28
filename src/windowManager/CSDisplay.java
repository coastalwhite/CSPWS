package windowManager;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import graphCore.Line;
import graphCore.Point;
import graphics.MainWindow;
import roadGraph.*;
import simulation.Car;
import simulation.Vehicle;

public class CSDisplay {
	
	// Window Settings
	public static int WIDTH                     =                         1000,
					  HEIGHT                    =      					  700,
					  POS_X                     =                            0,
					  POS_Y                     =                            0;
	
	public static Color borderColor             =       new Color(150,150,150),
						bgColor                 =       new Color(255,255,255);
	
	
	// View Settings
	private static Vector2d displayPosition     =    new Vector2d (0.0f, 0.0f);
	private static double displayZoom           =                         1.0f;
	private static Vector2d zoomConstant        = new Vector2d (100.0f, 70.0f);
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
	
	public static ArrayList<Double> factors     =      new ArrayList<Double>();
	
	// Simulation Settings
	public static boolean PLAY_SIMULATION       =                        false;
	public static boolean PAUSE                 =                        false;
	public static Road weightEdit               =                         null;
	public static Bend priorityEdit             =                         null;
	
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
	public static Vector2d displayPosition() {
		return displayPosition;
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
	
	public static Vector2d tSR(Vector2d v) {
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
		
		boolean x = true;
		for(Road r : lines) {
			if(r.isIn(v)){
				for(Vehicle c : r.vehicles) {
					System.out.println("hi!");
					if(c.isIn(v,r)) {
						x = false;
						return c;
					}
				}
				if (x) { return r; } 
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
	public static ArrayList<Integer> getRoadsWithBend(Bend b) {
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
	
	private void addRoad(Vector2d mouseV, boolean t) {
		Bend b = getBendAtLocation(mouseV);
		
		if(SELECTED_POINT == null) {
			// Create Clip on
			if(b != null) {
				SELECTED_POINT = b;
			}
		} else {
			// Add new point if none selected
			if(b == null) {
				b = new Bend(mouseV.X(), mouseV.Y());
				points.add(b);
			}
			
			Road r = new Road(SELECTED_POINT, b, 1);
			
			Bend bend = r.b1();
			CrossoverPoint cp = null;
			Road r1, r2, road, newRoad, s;
			Vector2d intersection = null;
			int size = lines.size(), size2 = lines.size(), index = 0;
			
			for(int i = 0; i < lines.size(); i++) {
				road = lines.get(i);
				
				// Add Intersections
				if(i < size) {
					// Check if intersecting
					intersection = r.getCrosspoint(road);
					if(intersection != null) {
						intersection = r.toIntersect(intersection);
						// Check if not same point used
						if(
								intersection.difVector(r.b1().pos().v()).length() > Point.radius &&
								intersection.difVector(r.b2().pos().v()).length() > Point.radius
						   ) {
							cp = new CrossoverPoint(intersection.X(), intersection.Y());
							
							// Split intersection roads in two
							r1 = road instanceof CarRoad ? new CarRoad(road.b1(),cp,cp.disTo(road.b1())) : new BicycleRoad(road.b1(),cp,cp.disTo(road.b1()));
							r2 = road instanceof CarRoad ? new CarRoad(cp,road.b2(),cp.disTo(road.b2())) : new BicycleRoad(cp,road.b2(),cp.disTo(road.b2()));
							
							r1.addNextRoad(r2);
							
							for(Road cRoad : lines) {
								if(cRoad.nextRoad.indexOf(road) >= 0) {
									cRoad.nextRoad.set(cRoad.nextRoad.indexOf(road), r1);
								}
							}
							
							r2.nextRoad.addAll(road.nextRoad);
							r2.nextRoadProbability.addAll(road.nextRoadProbability);
							
							lines.remove(road);
							lines.add(r2);
							lines.add(r1);
							
							points.add(cp);
							
							i--;
							size--;
							size2++;
						}
					}
				} else { // Add new road
					// Determine closest Road
					if(r.b2().disTo(road.b1()) < r.b2().disTo(bend)) {
						bend = road.b1();
						index = i;
					}
					
					// Add Road
					if (i == (size2 - 2)) {
						newRoad = t ? new CarRoad(bend, r.b2(), bend.disTo(r.b2())) : new BicycleRoad(bend, r.b2(), bend.disTo(r.b2()));
						r = new Road(r.b1(), bend, 1);
						
						if (i > size) {
							newRoad.addNextRoad(lines.get(lines.size()-1));
						}
						
						lines.add(newRoad);
						
						// Add Final Road
						if((size+(size2-(size+2))/2+2) == size2) {
							newRoad = t ? new CarRoad(r.b1(), bend, bend.disTo(r.b1())) : new BicycleRoad(r.b1(), bend, bend.disTo(r.b1()));
							newRoad.addNextRoad(lines.get(lines.size()-1));
							
							for(Road cRoad : lines) {
								if(cRoad.b2().equals(r.b1())) {
									cRoad.addNextRoad(newRoad);
								}
							}
							
							lines.add(newRoad);
							
							i = lines.size();
						} else {
							s = lines.get(size);
							lines.set(size,lines.get(index));
							lines.set(index,s);
							
							i = size;
							size += 2;
							bend = new CrossoverPoint(r.b1().pos().X(), r.b1().pos().Y());
						}
					}
					i++;
				}

			}
			
			// Add roads if there are no intersections
			if(size == lines.size()) {
				newRoad = t ? new CarRoad(r.b1(), r.b2(), r.b2().disTo(r.b1())) : new BicycleRoad(r.b1(), r.b2(), r.b2().disTo(r.b1()));
				
				for(Road cRoad : lines) {
					if(cRoad.b2().equals(r.b1())) {
						cRoad.addNextRoad(newRoad);
					}
				}
				
				for(Road cRoad : lines) {
					if(cRoad.b1().equals(b)) {
						newRoad.addNextRoad(cRoad);
					}
				}
				
				lines.add(newRoad);
			} else {
				for(Road cRoad : lines) {
					if(cRoad.b1().equals(b)) {
						lines.get(size2).addNextRoad(cRoad);
					}
				}
			}
			
			// Reset to default in mode
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			
			refreshDisplay();
		}
	}
	
	private void addCrossover(Vector2d mouseV) {
		if(SELECTED_POINT == null) {
			// Create Clip on
			SELECTED_POINT = new CrossoverPoint(mouseV.X(), mouseV.Y());
			points.add(SELECTED_POINT);
		} else {
			// Add new point if none selected
			Bend b = new CrossoverPoint(mouseV.X(), mouseV.Y());
			
			Road r = new Road(SELECTED_POINT, b, 1);
			
			Bend bend = r.b1();
			CrossoverPoint cp = null;
			Road r1, r2, road, newRoad, s;
			Vector2d intersection = null;
			int size = lines.size(), size2 = lines.size(), index = 0;
			
			for(int i = 0; i < lines.size(); i++) {
				road = lines.get(i);
				
				// Add Intersections
				if(i < size) {
					// Check if intersecting
					intersection = r.getCrosspoint(road);
					if(intersection != null) {
						intersection = r.toIntersect(intersection);
						// Check if not same point used
						if(
								intersection.difVector(r.b1().pos().v()).length() > Point.radius &&
								intersection.difVector(r.b2().pos().v()).length() > Point.radius
						   ) {
							cp = new CrossoverPoint(intersection.X(), intersection.Y());
							
							// Split intersection roads in two
							r1 = road instanceof CarRoad ? new CarRoad(road.b1(),cp,1) : new BicycleRoad(road.b1(),cp,1);
							r2 = road instanceof CarRoad ? new CarRoad(cp,road.b2(),1) : new BicycleRoad(cp,road.b2(),1);
							
							r1.addNextRoad(r2);
							
							r2.nextRoad = road.nextRoad;
							r2.nextRoadProbability = road.nextRoadProbability;
							
							lines.remove(road);
							lines.add(r2);
							lines.add(r1);
							
							//points.add(cp);
							
							i--;
							size--;
							size2++;
						}
					}
				} else { // Add new road
					// Determine closest Road
					if(r.b2().disTo(road.b1()) < r.b2().disTo(bend)) {
						bend = road.b1();
						index = i;
					}
					
					// Add Road
					if (i == (size2 - 2)) {
						newRoad = new CrossoverRoad(r.b1(), r.b2(), r.b2().disTo(r.b1()));
						r = new Road(r.b1(), bend, 1);
						
						lines.add(newRoad);
						
						// Add Final Road
						if((size+(size2-(size+2))/2+2) == size2) {
							newRoad = new CrossoverRoad(r.b1(), r.b2(), r.b2().disTo(r.b1()));
							lines.add(newRoad);
							
							i = lines.size();
						} else {
							s = lines.get(size);
							lines.set(size,lines.get(index));
							lines.set(index,s);
							
							i = size;
							size += 2;
							bend = new CrossoverPoint(r.b1().pos().X(), r.b1().pos().Y());
						}
					}
					i++;
				}
			}
			
			// Add roads if there are no intersections
			if(size == lines.size()) {
				newRoad = new CrossoverRoad(r.b1(), r.b2(), r.b2().disTo(r.b1()));
				lines.add(newRoad);
			}
			
			points.add(b);
			points.add(SELECTED_POINT);
			
			// Reset to default in mode
			SELECTED_POINT = null;
			refreshDisplay();
		}
	}
	private void addText(Vector2d mouseV) {
		if(clickedText) {
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
		Bend b = getBendAtLocation(mouseV);
		
		if(SELECTED_POINT == null) {
			// Create Clip on
			SELECTED_POINT = new Bend(mouseV.X(), mouseV.Y());
		} else {
			// Add new point if none selected
			if(b == null) {
				b = new Bend(mouseV.X(), mouseV.Y());
			}
			
			Road r = new Road(SELECTED_POINT, b, 1);
			TrafficLight cp = null;
			Road r1, r2, road;
			Vector2d intersection = null;
			int size = lines.size();
			
			for(int i = 0; i < lines.size(); i++) {
				road = lines.get(i);
				
				// Add Intersections
				if(i < size) {
					// Check if intersecting
					intersection = r.getCrosspoint(road);
					if(intersection != null) {
						intersection = r.toIntersect(intersection);
						// Check if not same point used
						if(
								intersection.difVector(r.b1().pos().v()).length() > Point.radius &&
								intersection.difVector(r.b2().pos().v()).length() > Point.radius
						   ) {
							cp = new TrafficLight(intersection.X(), intersection.Y());
							
							// Split intersection roads in two
							r1 = road instanceof CarRoad ? new CarRoad(road.b1(),cp,cp.disTo(road.b1())) : new BicycleRoad(road.b1(),cp,cp.disTo(road.b1()));
							r2 = road instanceof CarRoad ? new CarRoad(cp,road.b2(),cp.disTo(road.b2())) : new BicycleRoad(cp,road.b2(),cp.disTo(road.b2()));
							
							r1.addNextRoad(r2);
							
							for(Road cRoad : lines) {
								if(cRoad.nextRoad.indexOf(road) >= 0) {
									cRoad.nextRoad.set(cRoad.nextRoad.indexOf(road), r1);
								}
							}
							
							r2.nextRoad.addAll(road.nextRoad);
							r2.nextRoadProbability.addAll(road.nextRoadProbability);
							
							lines.remove(road);
							lines.add(r2);
							lines.add(r1);
							
							points.add(cp);
							
							i--;
							size--;
						}
					}
				}

			}
			
			// Reset to default in mode
			SELECTED_POINT = null;
			point_vectors[0] = null;
			point_vectors[1] = null;
			
			refreshDisplay();
		}
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
			if(weightEdit != null) {
				if(!weightEdit.editWeight(mouseV)) {
					CLICK_DOWN = true;
				}
			} else if(priorityEdit != null) {
				if(!priorityEdit.editPriority(mouseV)) {
					CLICK_DOWN = true;
				}
			} else {
				CLICK_DOWN = true;
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
				this.addRoad(mouseV, true);
				break;
			case 4: // Add Bicycle Road
				this.addRoad(mouseV, false);
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
	public void zoom(MouseWheelEvent e) {
		if(new Vector2d(e.getX(), e.getY()).inRange(POS_X, POS_X+WIDTH, POS_Y, POS_X+HEIGHT)) {
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
	}
	
	public void tick() { // LOOP FUNCTION
		if (PLAY_SIMULATION && !PAUSE) {
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
			if(CSControl.EDIT_MODE) {
				for(Bend p : points){
					p.attemptToRender(g2d, displayZoom);
				}
			}
			for(Text t : textObjects) {
				t.attemptToRender(g2d, displayZoom);
			}
			for(Road l : lines){
				l.drawEditWeight(g2d);
			}
			for(Bend p : points){
				p.drawPriorityEdit(g2d);
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
				if(r.vehicles.size() != 0) {
					r.attemptToRender(g2d, displayZoom);
					
					//r.b1().attemptToRender(g2d, displayZoom);
					//r.b2().attemptToRender(g2d, displayZoom);
					
					r.renderVehicles(g2d, displayZoom);
					
					g2d.setClip(new Rectangle(POS_X, POS_Y, WIDTH, HEIGHT));
				}
			}
		}
	}

	public void spawnTick() {
		if (PLAY_SIMULATION) {
			for(Road r : lines) {
				if(r instanceof CarRoad) {
					CarRoad cr = (CarRoad) r;
					cr.spawnTick();
				} else if(r instanceof BicycleRoad) {
					BicycleRoad cr = (BicycleRoad) r;
					cr.spawnTick();
				}
			}
			for(Bend b : points) {
				if(b instanceof TrafficLight) {
					((TrafficLight) b).trafficUpdate();
				}
			}
		}
	}

	public static void calcFactor() {
		double sum = 0;
		for(double f : factors) {
			sum += f;
		}
		
		Line.convertFactor = sum / factors.size();
	}
}
