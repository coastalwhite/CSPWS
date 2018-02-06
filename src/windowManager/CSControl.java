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
import graphics.MainWindow;
import roadGraph.*;

public class CSControl {
	public static int  
				      WIDTH   =                     200,
		         	  HEIGHT  = MainWindow.FRAME_HEIGHT,
		         	  POS_X   =         CSDisplay.WIDTH,
		        	  POS_Y   =                       0;
	
	public static boolean EDIT_MODE = false;
	public static int MODE = 0;

	public static Color borderColor = new Color(150,150,150),
					    bgColor     = new Color(205,205,205);
	
	private static boolean displayChanged = true;
	
	public static float spawnMultiplier = 1.0f;
	
	public static int rowDim = 55;

	public static String slash = "\\";
	public static EditField editField;
	public static ArrayList<ModeButton> modebuttons = new ArrayList<ModeButton>();
	
	public CSControl() {
		String osName = System.getProperty("os.name").toLowerCase();
		boolean isMacOs = osName.startsWith("mac os x");
		if (isMacOs) 
		{
			slash = "/";
		} else {
			slash = "\\";
		}
		
		try {
			CSControl.loadState("states" + CSControl.slash + "RW_26.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * -6 = Start Simulation
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
		 * 8 = Info Mode
		 */
		
		//modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+0*rowDim, 0, ".png"));
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+0*rowDim, -1, "Potlood.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+0*rowDim, 8, "Info.png"));
		modebuttons.add(new ModeButton(POS_X+10+2*rowDim, POS_Y+10+0*rowDim, -6, "Start.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+2*rowDim, 2, "PuntAdd.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+3*rowDim, 3, "AutoWegAdd.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+3*rowDim, 4, "FietspadAdd.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+4*rowDim, 5, "StoplichtAdd.png"));
		//modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+4*rowDim, 6, "ZebrapadAdd.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+5*rowDim, 7, "TekstAdd.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+5*rowDim, -4, "ImageImport.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+7*rowDim, 1, "MuisPrullenbak.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+9*rowDim, -2, "SaveButton.png"));
		modebuttons.add(new ModeButton(POS_X+10+1*rowDim, POS_Y+10+9*rowDim, -3, "StateImport.png"));
		
		modebuttons.add(new ModeButton(POS_X+10+0*rowDim, POS_Y+10+11*rowDim, -5, "NewState.png"));
		
		editField = new EditField(new CSDisplay());
	}
	
	public static boolean getDisplayChanged() {
		return displayChanged;
	}
	public static void refreshDisplay() {
		displayChanged = true;
	}
	
	public static void loadState(String path) throws IOException {
		String s = new String(Files.readAllBytes(Paths.get(path)));
		String[] split = s.split(">");
		String[] prop;
		
		ArrayList<Bend> bends = new ArrayList<Bend>();
		ArrayList<Road> roads = new ArrayList<Road>();
		ArrayList<Text> texts = new ArrayList<Text>();
		
		ArrayList<Double> factors = new ArrayList<Double>();
		
		split = Arrays.copyOf(split, split.length-1);
		
		String saveName;
		
		Road r;
		Bend b;
		
		ArrayList<String> nextRoadStrings = new ArrayList<String>();
		ArrayList<String> priorityListStrings = new ArrayList<String>();
		String[] trafficTimings;
		
		int bendAmount = 0;
		
		String a;
		
		for(int i = 0; i < split.length; i++) {
			split[i] = split[i].replace('<', '0');
			prop = split[i].split(",");
			
			switch(prop[1]) {
			case "Point": // <#ID,Point,Type,#X,#Y,{Priority},CarsPerSecond,BikesPerSecond>
				priorityListStrings.add(prop[5]);
				
				switch (prop[2]) {
				case "Type2":
					b = new TrafficLight(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					
					prop[6] = prop[6].replace('{', '0').replace("}", "");
					trafficTimings = prop[6].split(";");
					
					((TrafficLight) b).TLTimingR = Long.parseLong(trafficTimings[0]);
					((TrafficLight) b).TLTimingG = Long.parseLong(trafficTimings[1]);
					((TrafficLight) b).TLTimingO = Long.parseLong(trafficTimings[2]);
					bends.add(b);
					break;
				case "Type3":
					b = new CrossoverPoint(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					bends.add(b);
					break;
				default:
					b = new Bend(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]));
					if(prop.length > 5) {
						b.carsPerSecond = spawnMultiplier*Double.parseDouble(prop[6]);
						b.bikesPerSecond = spawnMultiplier*Double.parseDouble(prop[7]);
					}
					bends.add(b);
					break;
				}
				
				bendAmount++;
				break;
			case "Line": // <#ID,Line,Type,#PointID1,#PointID2, Attributes, NextRoads>
				switch (prop[2]) {
				case "Type1":
					r = new CarRoad(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), bends.get(Integer.parseInt(prop[3])-1).disTo(bends.get(Integer.parseInt(prop[4])-1)));
					a = prop[5].replace('{', '0').replace('}', '0');
					r.maxSpeed = Float.parseFloat(a.split(";")[0]);
					r.SDSpeed = Float.parseFloat(a.split(";")[1]);
					roads.add(r);
					
					nextRoadStrings.add(prop[6]);
					saveName = prop[7].replace("{", "").replace("}","").replace("Unknown","");
					if(!saveName.isEmpty()) {
						r.saveDensity = true;
						r.saveName = saveName;
					}
					
					break;
				case "Type2":
					r = new BicycleRoad(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), bends.get(Integer.parseInt(prop[3])-1).disTo(bends.get(Integer.parseInt(prop[4])-1)));
					a = prop[5].replace('{', '0').replace('}', '0');
					r.maxSpeed = Float.parseFloat(a.split(";")[0]);
					r.SDSpeed = Float.parseFloat(a.split(";")[1]);
					roads.add(r);
					
					nextRoadStrings.add(prop[6]);
					saveName = prop[7].replace("{", "").replace("}","").replace("Unknown","");
					if(!saveName.isEmpty()) {
						r.saveDensity = true;
						r.saveName = saveName;
					}
					
					break;
				/*case "Type3":
					r = new CrossoverRoad(bends.get(Integer.parseInt(prop[3])-1), bends.get(Integer.parseInt(prop[4])-1), bends.get(Integer.parseInt(prop[3])-1).disTo(bends.get(Integer.parseInt(prop[4])-1)));
					a = prop[5].replace('{', '0').replace('}', '0');
					r.maxSpeed = Double.parseDouble(a.split(";")[0]);
					r.SDSpeed = Double.parseDouble(a.split(";")[1]);
					roads.add(r);
					
					nextRoadStrings.add(prop[6]);
					
					break;*/
				}	
				break;
			case "Text": // <#ID,Text,Type,#X,#Y,#Angle,TextInput>
				texts.add(new Text(Double.parseDouble(prop[3]), Double.parseDouble(prop[4]), Double.parseDouble(prop[5]), prop[6]));
				break;
			case "Factors":
				for(int x = 2; x < prop.length; x++) {
					factors.add(Double.parseDouble(prop[x]));
				}
				break;
			}
			
		}
		
		int j = 0;
		String[] list;
		for(String str : nextRoadStrings) {
			str = str.replace('{', '0').replace('}', '0');
			if(str.length() != 2) {
				list = str.split(";");
				for(String rstr : list) {
					prop = rstr.split(":");
					
					if((Integer.parseInt(prop[0])-bends.size()-1) != -1){
					
						roads.get(j).nextRoad.add(roads.get((Integer.parseInt(prop[0])-bends.size()-1)));
						roads.get(j).nextRoadProbability.add(Double.parseDouble(prop[1]));
						
					}
				}
				
				roads.get(j).organizeNextRoad(1);
			}
			j++;
		}
		
		j = 0;
		for(String str : priorityListStrings) {
			str = str.replace('{', ' ').replace('}', ' ');
			if(str.length() != 2) {
				list = str.split(";");
				for(String rstr : list) {
					if((Integer.parseInt(rstr.trim())-bendAmount-1) != -1){
						bends.get(j).priorityList.add(roads.get(Integer.parseInt(rstr.trim())-bendAmount-1));
					}
				}
			}
			j++;
		}
		
		CSDisplay.lines = roads;
		CSDisplay.points = bends;
		CSDisplay.textObjects = texts;
		
		CSDisplay.factors = factors;
		CSDisplay.calcFactor();
		
		CSDisplay.refreshDisplay();
	}
	public static void saveState(String path) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		int i = 0;
		
		ArrayList<Coord> coords = new ArrayList<Coord>();
		ArrayList<String> strings = new ArrayList<String>();
		String type, str;
		
		for(Bend b : CSDisplay.points) {
			i++;
			coords.add(b.pos());
			
			type = "Type0";
			str = "-";
			
			if(b instanceof CrossoverPoint) {
				type = "Type3";
			} else if(b instanceof TrafficLight) {
				type = "Type2";
				str = "{" + Long.toString(((TrafficLight) b).TLTimingR) + ";" + Long.toString(((TrafficLight) b).TLTimingG) + ";" + Long.toString(((TrafficLight) b).TLTimingO) + "}";
			} else {
				type = "Type1";
				str = Double.toString(b.carsPerSecond) + "," + Double.toString(b.bikesPerSecond);
			}
			
			str = "<" + Integer.toString(i) + ",Point," + type + "," + Double.toString(b.pos().X()) + "," + Double.toString(b.pos().Y()) + ",#," + str + ">";
		
			strings.add(str);
		}
		
		int bendAmount = i;
		int b1, b2;
		
		int j = 0;
		int k;
		for(String s : strings) {
			str = "{";
			k = 0;
			for(Road r : CSDisplay.points.get(j).priorityList) {
				str += Integer.toString(CSDisplay.lines.indexOf(r) + bendAmount + 1);
				str += (++k == CSDisplay.points.get(j).priorityList.size()) ? "" : ";";
			}
			str += "}";
			
			s = s.replace("#", str);
			writer.println(s);
			j++;
		}
		
		for(Road r : CSDisplay.lines) {
			i++;
			
			b1 = coords.indexOf(r.p1().pos());
			b2 = coords.indexOf(r.p2().pos());
			
			type = "Type0";
			
			if(r.getClass() == CarRoad.class) {
				type = "Type1";
			} else if(r.getClass() == BicycleRoad.class) {
				type = "Type2";
			}/* else if(r.getClass() == CrossoverRoad.class) {
				type = "Type3";
			}*/
			
			str = "<" + Integer.toString(i) + ",Line," + type + "," + Integer.toString(b1+1) + "," + Integer.toString(b2+1) + ",{" + Double.toString(r.maxSpeed) + ";" + Double.toString(r.SDSpeed) + "},{";
			j = 0;
			for(Road nr : r.nextRoad) {
				str += Integer.toString(CSDisplay.lines.indexOf(nr)+bendAmount+1) + ":" + Double.toString(r.nextRoadProbability.get(j));
				
				j++;
				if(j != r.nextRoad.size()) {
					str += ";";
				}
			}
			str += "}" + ",{" + r.saveName + "}>";
			
			writer.println(str);
		}
		
		for(Text t : CSDisplay.textObjects) {
			i++;
			
			str = "<" + Integer.toString(i) + ",Text,Type1," + Double.toString(t.X()) + "," + Double.toString(t.Y()) + "," + Double.toString(t.Angle()) + "," + t.text + ">";
			
			writer.println(str);
		}
		
		str = "<" + Integer.toString(i) + ",Factors";
		for(double f : CSDisplay.factors) {
			str += "," + Double.toString(f);
		}
		str += ">";
		writer.println(str);
		
		writer.close();
	}
	
	
	public static void resetWeightEdit() {
		if(CSDisplay.weightEdit != null) {
			CSDisplay.weightEdit.weightEdit = false;
			
			for(Road road : CSDisplay.weightEdit.nextRoad) {
				road.color = CSDisplay.weightEdit.color;
			}
			CSDisplay.weightEdit = null;
		}
	}
	
	public static void showObjectInfo(Object o) {
		EDIT_MODE = false;
		
		if(o instanceof Road) {
			resetWeightEdit();
		}
		
		editField = new EditField(o);
		
		refreshDisplay();
		CSDisplay.refreshDisplay();
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
		
		if(EditField.o != null) {
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
			
			if(EditField.o != null) {
				editField.draw(g2d);
			}
			
			displayChanged = false;
		}
	}
}
