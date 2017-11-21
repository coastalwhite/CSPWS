package windowManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import roadGraph.*;

public class EditField {
	private Object o = null;
	private int x = 1010,
				y = 90,
				WIDTH = 180,
				SPACE = 60;
	
	private int buttonCount = 0;
	
	public EditField(Object iO) {
		this.o = iO;
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
			img = ImageIO.read(new File("img/" + imgPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		g2d.drawImage(img,x+WIDTH-27,y+8+i*SPACE,x+WIDTH-2,y+33+i*SPACE,0,0,256,256,null);
		
		buttonCount++;
	}
	
	public void draw(Graphics2D g2d) {
		if(o instanceof Bend) {
			/*
			 * 0: carsPerSecond
			 * 1: bikesPerSecond
			 */
			
			if(o instanceof TrafficLight) {
				TrafficLight b = (TrafficLight) o;
				
				g2d.setFont(new Font("sansserif", Font.BOLD, 16));
				
				drawStroke(g2d, "Cars per Second", Double.toString(b.carsPerSecond), 0);
				drawStroke(g2d, "Bikes per Second", Double.toString(b.bikesPerSecond), 1);
				drawStroke(g2d, "Change Mode Timing", Double.toString(b.modeTime/1000) + " Sec", 2);
			} else {
				Bend b = (Bend) o;
				
				g2d.setFont(new Font("sansserif", Font.BOLD, 16));
				
				drawStroke(g2d, "Cars per Second", Double.toString(b.carsPerSecond), 0);
				drawStroke(g2d, "Bikes per Second", Double.toString(b.bikesPerSecond), 1);
			}
		} else if (o instanceof Road) {
			/*
			 * 0: switch Directions
			 * 1: Change Weight
			 */
			
			Road r = (Road) o;
			
			g2d.setFont(new Font("sansserif", Font.BOLD, 16));
			
			drawButton(g2d, "switchDirection", 0);
			drawStroke(g2d, "Real world length", Double.toString(r.convertFactor * r.length()), 1);
			drawImgButton(g2d, "Progression", "buttons" + CSControl.slash + "dice.png", 2);
		}
	}
	
	public void attemptToClick(Vector2d v) {
		String textInput;
		for (int i = 0; i < buttonCount; i++) {
			if(v.inRange(x, x+WIDTH, y+5+i*SPACE, y+5+i*SPACE+30)) {
				if(this.o instanceof Bend) {
					Bend b = (Bend) o;
					switch(i) {
					case 0:
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(b.carsPerSecond));
						
						if (textInput != "") {
							b.carsPerSecond = Double.parseDouble(textInput);
							CSControl.refreshDisplay();
						}
						break;
					case 1:
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(b.bikesPerSecond));
						
						if (textInput != "") {
							b.bikesPerSecond = Double.parseDouble(textInput);
							CSControl.refreshDisplay();
						}
						break;
					case 2:
						
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(((TrafficLight) o).modeTime/1000));
						
						if (textInput != "") {
							((TrafficLight) o).modeTime = (long) (Math.round(Double.parseDouble(textInput)*1000));
							CSControl.refreshDisplay();
						}
						break;
					}
				} else if(this.o instanceof Road) {
					Road r = (Road) o;
					switch (i) {
					case 0:
						r.switchDirection();
						CSDisplay.refreshDisplay();
						break;
					case 1:
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(r.convertFactor * r.length()));
						
						if (textInput != "") {
							double f = Double.parseDouble(textInput) / r.length();
							CSDisplay.factors.add(f);
							CSDisplay.calcFactor();
							CSControl.refreshDisplay();
						}
						CSDisplay.refreshDisplay();
						break;
					case 2:
						for(Road road : r.nextRoad) {
							if(road.color == Color.GREEN) {
								road.color = r.color;
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
					}
				}
			}
		}
	}
}