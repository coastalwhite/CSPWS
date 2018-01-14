package windowManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import graphCore.Coord;
import roadGraph.*;
import simulation.Vehicle;

public class EditField {
	public static Road r = null;
	
	private static Object o = null;
	private int x = 1010,
				y = 90,
				WIDTH = 180,
				SPACE = 60;
	
	private int buttonCount = 0;
	private Emphasis emp;
	
	public EditField(Object iO) {
		EditField.o = iO;
		
		emp = new Emphasis (1.0f);
	}
	
	private void drawEditValue(Graphics2D g2d, String s, String v, String d, int i) {
		g2d.setColor(Color.WHITE);
		g2d.drawRect(x, y+5+i*SPACE, WIDTH/2-2, 30);
		g2d.drawRect(x+WIDTH/2+2, y+5+i*SPACE, WIDTH/2-2, 30);
		g2d.setColor(Color.BLACK);
		g2d.drawString(s, x+2, y+i*SPACE);
		g2d.drawString(v, x+5, y+28+i*SPACE);
		g2d.drawString(d, x+5+WIDTH/2, y+28+i*SPACE);
		
		buttonCount++;
	}
	private void drawStroke(Graphics2D g2d, String s, String v, int i) {
		g2d.setColor(Color.WHITE);
		g2d.drawRect(x, y+5+i*SPACE, WIDTH, 30);
		g2d.setColor(Color.BLACK);
		g2d.drawString(s, x+2, y+i*SPACE);
		g2d.drawString(v, x+5, y+28+i*SPACE);
		
		buttonCount++;
	}
	private void drawButton(Graphics2D g2d, String s, int i) {
		g2d.setColor(Color.WHITE);
		g2d.drawRect(x, y+5+i*SPACE, WIDTH, 30);
		g2d.setColor(Color.BLACK);
		g2d.drawString(s, x+5, y+28+i*SPACE);
		
		buttonCount++;
	}
	private void drawImgButton(Graphics2D g2d, String s, String imgPath, int i) {
		g2d.setColor(Color.WHITE);
		g2d.drawRect(x, y+5+i*SPACE, WIDTH, 30);
		g2d.setColor(Color.BLACK);
		g2d.drawString(s, x+5, y+28+i*SPACE);
		
		Image img = null;
		try {
			img = ImageIO.read(new File("img" + CSControl.slash + imgPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		g2d.drawImage(img,x+WIDTH-27,y+8+i*SPACE,x+WIDTH-2,y+33+i*SPACE,0,0,256,256,null);
		
		buttonCount++;
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setFont(new Font("sansserif", Font.BOLD, 13));
		int digits = 3;
		
		if(o instanceof Bend) {
			/*
			 * 0: carsPerSecond
			 * 1: bikesPerSecond
			 */
			
			if(o instanceof TrafficLight) {
				TrafficLight b = (TrafficLight) o;
				
				drawStroke(g2d,
						"TL Timing RED",
						Double.toString(Math.round(b.TLTimingR*Math.pow(10, digits))/Math.pow(10, digits*2)),
						0);
				drawStroke(g2d,
						"TL Timing GREEN",
						Double.toString(Math.round(b.TLTimingG*Math.pow(10, digits))/Math.pow(10, digits*2)),
						1);
				drawStroke(g2d,
						"TL Timing ORANGE",
						Double.toString(Math.round(b.TLTimingO*Math.pow(10, digits))/Math.pow(10, digits*2)),
						2);
			} else {
				Bend b = (Bend) o;
				
				drawStroke(g2d,
						"Cars per Second",
						Double.toString(Math.round(b.carsPerSecond*Math.pow(10, digits))/Math.pow(10, digits)),
						0);
				drawStroke(g2d,
						"Bikes per Second",
						Double.toString(Math.round(b.bikesPerSecond*Math.pow(10, digits))/Math.pow(10, digits)),
						1);
				

				drawImgButton(g2d, "Priority", "buttons" + CSControl.slash + "dice.png", 2);
			}
		} else if (o instanceof Road) {
			/*
			 * 0: switch Directions
			 * 1: Change Weight
			 */
			
			Road r = (Road) o;
			
			drawButton(g2d, "switchDirection", 0);
			drawEditValue(g2d,
					"Speed (Avg - SD)",
					Double.toString(Math.round(r.maxSpeed*Math.pow(10, digits))/Math.pow(10, digits)),
					Double.toString(Math.round(r.SDSpeed*Math.pow(10, digits))/Math.pow(10, digits)),
					1);
			drawStroke(g2d, "Real world length", Double.toString(r.weight()), 2);
			drawImgButton(g2d, "Progression", "buttons" + CSControl.slash + "dice.png", 3);
			
			drawStroke(g2d, "Amount of cars on road", Integer.toString(r.vehicles.size()), 5);
			drawStroke(g2d, "Vehicle Density", Double.toString(Math.round(r.vehicleDensity()*Math.pow(10, digits+2))/Math.pow(10, digits)) + "%", 6);
			
			drawStroke(g2d, "Save Density Data", (r.saveDensity) ? "On!" : "Off...", 7);
		} else if (o instanceof Vehicle) {
			Vehicle v = (Vehicle) o;
			
			drawStroke(g2d, "Speed", Double.toString(v.speed), 0);
			drawStroke(g2d, "prefSpeed", Double.toString(v.prefSpeed), 1);
			
			drawStroke(g2d, "Stopping for slower Vehicle", Boolean.toString(v.ufCarsChanged), 3);
			drawStroke(g2d, "Stopping for Traffic Light", Boolean.toString(v.ufTLChanged), 4);
			drawStroke(g2d, "Stopping for Crossing point", Boolean.toString(v.ufCPChanged), 5);
			
			drawStroke(g2d, "Debug Value", v.debugval, 7);
		}
	}
	
	public void drawEmphasis(Graphics2D g2d) {
		if(o instanceof Bend) {
			Bend b = (Bend) o;
			
			emp.EmpRadius = 10;
			emp.drawEmphasis(g2d, CSDisplay.tSR(b.pos().v()));
		} else if (o instanceof Road) {
			Road r = (Road) o;
			
			emp.EmpRadius = r.p1().disTo(r.p2())*0.5 + 10;
			Coord c = r.getPos();
			emp.drawEmphasis(g2d, new Vector2d(c.X(), c.Y()));
		} else if (o instanceof Vehicle) {
			Vehicle v = (Vehicle) o;
			
			emp.EmpRadius = 8;
			Coord c = v.getPos(r);
			emp.drawEmphasis(g2d, new Vector2d(c.X(), c.Y()));
		}
	}
	
	public void attemptToClick(Vector2d v) {
		String textInput;
		for (int i = 0; i < buttonCount; i++) {
			if(v.inRange(x, x+WIDTH, y+5+i*SPACE, y+5+i*SPACE+30)) {
				if(EditField.o instanceof Bend) {
					Bend b = (Bend) o;
					if(o instanceof TrafficLight) {
						TrafficLight tl = (TrafficLight) o;
						switch(i) {
						case 0:
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(tl.TLTimingR/1000));
							
							if(textInput != null && textInput != "") {
								tl.TLTimingR = (long) Math.round(Double.parseDouble(textInput)*1000);
								CSControl.refreshDisplay();
							}
							break;
						case 1:
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(tl.TLTimingG/1000));
							
							if(textInput != null && textInput != "") {
								tl.TLTimingG = (long) Math.round(Double.parseDouble(textInput)*1000);
								CSControl.refreshDisplay();
							}
							break;
						case 2:
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(tl.TLTimingO/1000));
							
							if(textInput != null && textInput != "") {
								tl.TLTimingO = (long) Math.round(Double.parseDouble(textInput)*1000);
								CSControl.refreshDisplay();
							}
							break;
						}
					} else {
						switch(i) {
						case 0:
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(b.carsPerSecond));
							
							if(textInput != null && textInput != "") {
								b.carsPerSecond = Double.parseDouble(textInput);
								CSControl.refreshDisplay();
							}
							break;
						case 1:
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(b.bikesPerSecond));
							
							if (textInput != null && textInput != "") {
								b.bikesPerSecond = Double.parseDouble(textInput);
								CSControl.refreshDisplay();
							}
							break;
						case 2:
							for(Road road : ((Bend) EditField.o).priorityList) {
								if(road.color != road.defaultColor) {
									road.color = road.defaultColor;
									((Bend) EditField.o).priorityEdit = false;
									CSDisplay.priorityEdit = null;
								} else {
									road.color = Color.GREEN;
									((Bend) EditField.o).priorityEdit = true;
									CSDisplay.priorityEdit = ((Bend) EditField.o);
								}
							}
							CSDisplay.refreshDisplay();
							break;
						}
					}
				} else if(EditField.o instanceof Road) {
					Road r = (Road) o;
					switch (i) {
					case 0:
						r.switchDirection();
						CSDisplay.refreshDisplay();
						break;
					case 1:
						if(v.inRange(x, x+WIDTH/2, y+5+i*SPACE, y+5+i*SPACE+30)){
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(r.maxSpeed));
							
							if(textInput != null && textInput != "") {
								r.maxSpeed = Double.parseDouble(textInput);
								CSControl.refreshDisplay();
							}
						} else {
							textInput = JOptionPane.showInputDialog(this 
									 ,Double.toString(r.SDSpeed));
							
							if (textInput != null && textInput != "") {
								r.SDSpeed = Double.parseDouble(textInput);
								CSControl.refreshDisplay();
							}
						}
						break;
					case 2:
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(r.weight()));
						
						if (textInput != null && textInput != "") {
							double f = Double.parseDouble(textInput) / r.getWeight();
							CSDisplay.factors.add(f);
							CSDisplay.calcFactor();
							CSControl.refreshDisplay();
						}
						CSDisplay.refreshDisplay();
						break;
					case 3:
						for(Road road : r.nextRoad) {
							if(road.color != road.defaultColor) {
								road.color = road.defaultColor;
								r.weightEdit = false;
								CSDisplay.weightEdit = null;
							} else {
								r.weightEdit = true;
								CSDisplay.weightEdit = r;
								road.color = Color.GREEN;
							}
						}
						CSDisplay.refreshDisplay();
						break;
					case 7:
						if(r.saveDensity) {
							r.saveDensity = false;
						} else {
							if(r.saveName == "") {
								textInput = JOptionPane.showInputDialog(this 
										 ,"Road X");
							} else {
								textInput = JOptionPane.showInputDialog(this 
										 ,r.saveName);
							}
							
							if(textInput != null && textInput != "") {
								r.saveName = textInput;
								r.saveDensity = true;
							}
						}
						CSControl.refreshDisplay();
					}
				} else if (o instanceof Vehicle) {
					Vehicle veh = (Vehicle) o;
					switch (i) {
					case 0:
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(veh.speed));
						
						if(textInput != null && textInput != "") {
							veh.speed = Double.parseDouble(textInput);
							CSControl.refreshDisplay();
						}
						break;
					}
				}
			}
		}
	}
	
	public static Object o() {
		return o;
	}
}