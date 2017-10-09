package windowManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import graphCore.Coord;
import graphCore.Point;
import graphics.MainWindow;
import graphics.ScreenGraphics;
import roadGraph.Bend;
import roadGraph.Road;
import roadGraph.Vector2d;

public class CSControl {
	public static int WIDTH = 200,
		         	  HEIGHT = 0,
		         	  POS_X = CSDisplay.WIDTH,
		        	  POS_Y = 0;
	
	public static boolean EDIT_MODE = false;

	public static Color borderColor = new Color(150,150,150),
					    bgColor     = new Color(255,255,255);
	
	public CSControl() {
		HEIGHT = ScreenGraphics.FRAME_HEIGHT;
	}
	
	public int findIndex(ArrayList<Coord> coords, Coord c) {
		int i = 0;
		for(Coord p : coords) {
			if(p.X() == c.X() && p.Y() == c.Y()) {
				return i;
			}
			i++;
		}
		
		return -1;
	}
	
	public void loadState(String stateName) throws IOException {
		String s = new String(Files.readAllBytes(Paths.get("states\\state1.txt")));
		String[] split = s.split(">");
		String[] prop;
		
		ArrayList<Bend> bends = new ArrayList<Bend>();
		ArrayList<Road> roads = new ArrayList<Road>();
		
		for(int i = 0; i < split.length; i++) {
			split[i] = split[i].replace('<', '0');
			prop = split[i].split(",");
			
			switch(prop[1]) {
			case "Point":
				bends.add(new Bend(Double.parseDouble(prop[3]), Double.parseDouble(prop[4])));
				break;
			case "Line":
				roads.add(new Road(bends.get(Integer.parseInt(prop[3])-roads.size()-1), bends.get(Integer.parseInt(prop[4])-roads.size()-1)));
				break;
			}
		}
		
		CSDisplay.lines = roads;
		CSDisplay.points = bends;
	
		CSDisplay.refreshDisplay();
	}
	
	public void saveState(String stateName) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("states\\" + stateName + ".txt", "UTF-8");
		int i = 0;
		
		ArrayList<Coord> coords = new ArrayList<Coord>();
		
		for(Bend b : CSDisplay.points) {
			i++;
			coords.add(b.pos());
			writer.println("<" + Integer.toString(i) + ",Point,Type1," + Double.toString(b.pos().X()) + "," + Double.toString(b.pos().Y()) + ">");
		}
		
		int b1, b2;
		for(Road r : CSDisplay.lines) {
			i++;
			
			b1 = this.findIndex(coords, r.p1().pos());
			b2 = this.findIndex(coords, r.p2().pos());
			
			writer.println("<" + Integer.toString(i) + ",Line,Type1," + Integer.toString(b1+1) + "," + Integer.toString(b2+1) + ">");
		}
		
		writer.close();
	}
	
	public void draw(Graphics2D g2d) {
		g2d.clearRect(POS_X, POS_Y, WIDTH, HEIGHT);
		
		g2d.setColor(borderColor);
		g2d.drawRect(POS_X, POS_Y, WIDTH, HEIGHT);
		
		g2d.setColor(bgColor);
		g2d.fillRect(POS_X+1, POS_Y+1, WIDTH-2, HEIGHT-2);
		
		g2d.setColor(Color.GREEN);
		g2d.fillRect(POS_X+1, 1, MainWindow.FRAME_WIDTH-702, 98);
		g2d.setColor(Color.BLACK);
		g2d.drawString("Edit", POS_X+10, 50);
		
		if(EDIT_MODE) {
			g2d.setColor(Color.GREEN);
			g2d.fillRect(POS_X+1, 120, MainWindow.FRAME_WIDTH-702, 98);
			g2d.setColor(Color.BLACK);
			g2d.drawString("Save", POS_X+10, 170);
			
			g2d.setColor(Color.GREEN);
			g2d.fillRect(POS_X+1, 300, MainWindow.FRAME_WIDTH-702, 30);
			g2d.setColor(Color.BLACK);
			g2d.drawString("Add Point", POS_X+10, 310);
			g2d.setColor(Color.GREEN);
			g2d.fillRect(POS_X+1, 340, MainWindow.FRAME_WIDTH-702, 30);
			g2d.setColor(Color.BLACK);
			g2d.drawString("Add Line", POS_X+10, 350);
		}
	}
	
	public void mouseClick(MouseEvent e) {
		
		Vector2d mouseV = new Vector2d(e.getX(),e.getY());
		
		if(mouseV.inRange(POS_X+1, POS_X+1+WIDTH, 1, 98)){ // Edit Button
			EDIT_MODE = EDIT_MODE ? false : true;
		}
		
		if(EDIT_MODE) {
			if(mouseV.inRange(POS_X+1, POS_X+1+WIDTH, 120, 218)) {
				try {
					saveState("state1");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			} else if(mouseV.inRange(POS_X+1, POS_X+1+WIDTH, 300, 330)) {
				CSDisplay.MODE = 1;
			} else if(mouseV.inRange(POS_X+1, POS_X+1+WIDTH, 340, 370)) {
				CSDisplay.MODE = 2;
			}
		}
		
	}
}
