package windowManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import graphCore.Coord;
import graphics.ScreenGraphics;
import roadGraph.*;

public class CSControl {
	public static int WIDTH = 200,
		         	  HEIGHT = 0,
		         	  POS_X = CSDisplay.WIDTH,
		        	  POS_Y = 0;
	
	public static boolean EDIT_MODE = false;
	public static int MODE = 0;

	public static Color borderColor = new Color(150,150,150),
					    bgColor     = new Color(205,205,205);
	
	private static boolean displayChanged = true;
	
	private static EditField editField;
	
	private static ArrayList<ModeButton> modebuttons = new ArrayList<ModeButton>();
	
	public CSControl() {
		HEIGHT = ScreenGraphics.FRAME_HEIGHT;
		
		try {
			CSControl.loadState("states\\carTest.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int rowDim = 60;
		
		/*
		 * -5 = Add New State
		 * -4 = Image Import & Use
		 * -3 = State Import
		 * -2 = Save State
		 * -1 = Activate Edit Mode
		 * 0 = Default View Mode
		 * 1 = Remove Objects Mode
		 * 2 = Add Points Mode
		 * 3 = Add Car Road Mode
		 * 4 = Add Bicycle Path Mode
		 * 5 = Add Traffic Light Mode
		 * 6 = Add Crossover Mode
		 * 7 = Add Text Mode
		 */
		
		//modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+0*rowDim, 0, ".png"));
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+0*rowDim, -1, "Potlood.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+0*rowDim, 8, "Info.png"));
		modebuttons.add(new ModeButton(POS_X+10+2*rowDim, POS_Y+10+0*rowDim, -6, "Start.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+2*rowDim, 2, "PuntAdd.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+3*rowDim, 3, "AutoWegAdd.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+3*rowDim, 4, "FietspadAdd.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+4*rowDim, 5, "StoplichtAdd.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+4*rowDim, 6, "ZebrapadAdd.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+5*rowDim, 7, "TekstAdd.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+5*rowDim, -4, "ImageImport.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+7*rowDim, 1, "MuisPrullenbak.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+9*rowDim, -2, "SaveButton.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+9*rowDim, -3, "StateImport.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+11*rowDim, -5, "NewState.png"));
		
		editField = new EditField(null);
	}
	
	public static boolean getDisplayChanged() {
		return displayChanged;
	}
	public static void refreshDisplay() {
		displayChanged = true;
	}
	
	private static int findIndex(ArrayList<Coord> coords, Coord c) {
		int i = 0;
		for(Coord p : coords) {
			if(p.X() == c.X() && p.Y() == c.Y()) {
				return i;
			}
			i++;
		}
		
		return -1;
	}
	
	public static void loadState(String path) throws IOException {
		String s = new String(Files.readAllBytes(Paths.get(path)));
		String[] split = s.split(">");
		String[] prop;
		
		ArrayList<Bend> bends = new ArrayList<Bend>();
		ArrayList<Road> roads = new ArrayList<Road>();
		ArrayList<Text> texts = new ArrayList<Text>();
		
		split = Arrays.copyOf(split, split.length-1);
		
		Road r;
		Bend b;
		
		for(int i = 0; i < split.length; i++) {
			split[i] = split[i].replace('<', '0');
			prop = split[i].split(",");
			
			switch(prop[1]) {
			case "Point": // <#ID,Point,Type,#X,#Y,CarsPerSecond,BikesPerSecond>
				switch (prop[2]) {
				case "Type1":
					b = new Bend(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					if(prop.length > 5) {
						b.carsPerSecond = Double.parseDouble(prop[5]);
						b.bikesPerSecond = Double.parseDouble(prop[6]);
					}
					bends.add(b);
					break;
				case "Type2":
					b = new TrafficLight(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					bends.add(b);
					break;
				case "Type3":
					b = new CrossoverPoint(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					bends.add(b);
					break;
				default:
					b = new Bend(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					if(prop.length > 5) {
						b.carsPerSecond = Double.parseDouble(prop[5]);
						b.bikesPerSecond = Double.parseDouble(prop[6]);
					}
					bends.add(b);
					break;
				}
				break;
			case "Line": // <#ID,Line,Type,#PointID1,#PointID2>
				switch (prop[2]) {
				case "Type1":
					r = new CarRoad(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), Double.parseDouble(prop[5]));
					roads.add(r);
					break;
				case "Type2":
					r = new BicycleRoad(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), Double.parseDouble(prop[5]));
					roads.add(r);
					break;
				case "Type3":
					r = new CrossoverRoad(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), Double.parseDouble(prop[5]));
					roads.add(r);
					break;
				default:
					r = new Road(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), Double.parseDouble(prop[5]));
					roads.add(r);
					break;
				}	
				break;
			case "Text": // <#ID,Text,Type,#X,#Y,#Angle,TextInput>
				texts.add(new Text(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]), Double.parseDouble(prop[5]), prop[6]));
				break;
			}
		}
		
		CSDisplay.lines = roads;
		CSDisplay.points = bends;
		CSDisplay.textObjects = texts;
		
		CSDisplay.refreshDisplay();
	}
	public static void saveState(String path) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		int i = 0;
		
		ArrayList<Coord> coords = new ArrayList<Coord>();
		
		for(Bend b : CSDisplay.points) {
			i++;
			coords.add(b.pos());
			
			String type = "Type0";
			
			if(b.getClass() == Bend.class) {
				type = "Type1";
			} else if(b.getClass() == TrafficLight.class) {
				type = "Type2";
			} else if(b.getClass() == CrossoverPoint.class) {
				type = "Type3";
			}
			
			writer.println("<" + Integer.toString(i) + ",Point," + type + "," + Double.toString(b.pos().X()) + "," + Double.toString(b.pos().Y()) + "," + Double.toString(b.carsPerSecond) + "," + Double.toString(b.bikesPerSecond) + ">");
		}
		
		int b1, b2;
		for(Road r : CSDisplay.lines) {
			i++;
			
			b1 = findIndex(coords, r.p1().pos());
			b2 = findIndex(coords, r.p2().pos());
			
			String type = "Type0";
			
			if(r.getClass() == CarRoad.class) {
				type = "Type1";
			} else if(r.getClass() == BicycleRoad.class) {
				type = "Type2";
			} else if(r.getClass() == CrossoverRoad.class) {
				type = "Type3";
			}
			
			writer.println("<" + Integer.toString(i) + ",Line," + type + "," + Integer.toString(b1+1) + "," + Integer.toString(b2+1) + "," + Double.toString(r.weight()) + ">");
		}
		for(Text t : CSDisplay.textObjects) {
			i++;
			
			writer.println("<" + Integer.toString(i) + ",Text,Type1," + Double.toString(t.X()) + "," + Double.toString(t.Y()) + "," + Double.toString(t.Angle()) + "," + t.text + ">");
		}
		
		writer.close();
	}
	public static void saveLastState(ArrayList<Bend> bends, ArrayList<Road> roads, ArrayList<Text> texts) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("states\\lastState.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		int i = 0;
		
		ArrayList<Coord> coords = new ArrayList<Coord>();
		
		for(Bend b : bends) {
			i++;
			coords.add(b.pos());
			
			String type = "Type0";
			
			if(b.getClass() == Bend.class) {
				type = "Type1";
			} else if(b.getClass() == TrafficLight.class) {
				type = "Type2";
			} else if(b.getClass() == CrossoverPoint.class) {
				type = "Type3";
			}
			
			writer.println("<" + Integer.toString(i) + ",Point," + type + "," + Double.toString(b.pos().X()) + "," + Double.toString(b.pos().Y()) + "," + Double.toString(b.carsPerSecond) + "," + Double.toString(b.bikesPerSecond) + ">");
		}
		
		int b1, b2;
		for(Road r : roads) {
			i++;
			
			b1 = findIndex(coords, r.p1().pos());
			b2 = findIndex(coords, r.p2().pos());
			
			String type = "Type0";
			
			if(r.getClass() == CarRoad.class) {
				type = "Type1";
			} else if(r.getClass() == BicycleRoad.class) {
				type = "Type2";
			} else if(r.getClass() == CrossoverRoad.class) {
				type = "Type3";
			}
			
			writer.println("<" + Integer.toString(i) + ",Line," + type + "," + Integer.toString(b1+1) + "," + Integer.toString(b2+1) + "," + Double.toString(r.weight()) + ">");
		}
		for(Text t : texts) {
			i++;
			
			writer.println("<" + Integer.toString(i) + ",Text,Type1," + Double.toString(t.X()) + "," + Double.toString(t.Y()) + "," + Double.toString(t.Angle()) + "," + t.text + ">");
		}
		
		writer.close();
	}
	
	public static void showObjectInfo(Object o) {
		EDIT_MODE = false;
		editField = new EditField(o);
		
		refreshDisplay();
	}
	
	// Event Handlers
	public void mouseClick(MouseEvent e) {
		Vector2d mouseV = new Vector2d(e.getX(),e.getY());
		
		if(EDIT_MODE) {
			for(ModeButton mb : modebuttons) {
				mb.attemptToClick(mouseV);
			}
		} else {
			modebuttons.get(0).attemptToClick(mouseV);
			modebuttons.get(1).attemptToClick(mouseV);
			modebuttons.get(2).attemptToClick(mouseV);
		}
		
		if(MODE == 8) {
			editField.attemptToClick(mouseV);
		}
	}
	
	public void tick() {
		for(ModeButton mb : modebuttons) {
			mb.tick();
		}
	}
	public void draw(Graphics2D g2d) {
		g2d.setClip(new Rectangle(POS_X, POS_Y, WIDTH, HEIGHT));
		if (displayChanged) {
			g2d.clearRect(POS_X, POS_Y, WIDTH, HEIGHT);
			
			g2d.setColor(borderColor);
			g2d.drawRect(POS_X, POS_Y, WIDTH, HEIGHT);
			
			g2d.setColor(bgColor);
			g2d.fillRect(POS_X+1, POS_Y+1, WIDTH-2, HEIGHT-2);
			
			if(EDIT_MODE) {
				for(ModeButton mb : modebuttons) {
					mb.draw(g2d);
				}
			} else {
				modebuttons.get(0).draw(g2d);
				modebuttons.get(1).draw(g2d);
				modebuttons.get(2).draw(g2d);
			}
			
			if(MODE == 8) {
				editField.draw(g2d);
			}
			
			displayChanged = false;
		}
	}

}
