package windowManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import roadGraph.*;

public class ModeButton {
	private int POS_X, POS_Y, WIDTH = 50, HEIGHT = 50;
	public int borderWidth = 1;
	private int clickTimer = 100;
	private boolean clicked = false; 
	private int MODE;
	
	private Color innerColor;
	
	public static Color selectedColor = new Color(100,255,100);
	public static Color idleColor = Color.WHITE;
	
	private static boolean buttonsDisabled = false;
	
	private String imagePath;
	
	public ModeButton(int iX, int iY, int iM, String iPath) {
		this.POS_X = iX;
		this.POS_Y = iY;
		
		this.MODE = iM;
		this.imagePath = iPath;
		
		this.innerColor = idleColor;
	}
	
	public void attemptToClick(Vector2d mV) {
		if(mV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
			boolean doClick = true;
			
			JFileChooser fileChooser;
			ArrayList<Bend> bends;
			ArrayList<Road> roads;
			ArrayList<Text> texts;
			switch (MODE) {
			case 8:
				CSDisplay.MODE = this.MODE;
				CSControl.MODE = this.MODE;
				CSControl.EDIT_MODE = false;
				break;
			case 7:
				String textInput = JOptionPane.showInputDialog(this 
						 ,"Enter text...");
				
				if (textInput != "" && textInput != "Enter text...") {
					CSDisplay.textInput = textInput;
					CSDisplay.MODE = MODE;
					CSControl.MODE = MODE;
				}
				break;
			case -1: // Edit Button
				if(!buttonsDisabled) {
					CSControl.EDIT_MODE = CSControl.EDIT_MODE ? false : true;
					CSControl.MODE = -1;
					this.innerColor = CSControl.EDIT_MODE ? selectedColor : idleColor;
				} else {
					doClick = false;
				}
				break;
			case -2: // Save Button
				fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("states\\"));
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
				  
					try {
						CSControl.saveState(file.getAbsolutePath());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				break;
			case -3: // Load Button
				bends = CSDisplay.points;
				roads = CSDisplay.lines;
				texts = CSDisplay.textObjects;
				
				fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("states\\"));
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					
					try {
						CSControl.loadState(file.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				CSControl.saveLastState(bends, roads, texts);
				break;
			case -4: // Add Background Image Button
				if(!CSDisplay.displayBackground) {
					this.innerColor = selectedColor;
					
					fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File("img\\background\\"));
					if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						
						CSDisplay.loadBackground(file.getAbsolutePath());
						CSDisplay.displayBackground = true;
					}
				} else {
					this.innerColor = idleColor;
					CSDisplay.displayBackground = false;
				}
				break;
			case -5: // New State
				bends = CSDisplay.points;
				roads = CSDisplay.lines;
				texts = CSDisplay.textObjects;
				
				CSControl.saveLastState(bends, roads, texts);
				
				CSDisplay.resetState();
				break;
			case -6: // Start / Stop playing simulation
				if(CSDisplay.PLAY_SIMULATION) {
					this.imagePath = "Start.png";
					CSDisplay.PLAY_SIMULATION = false;
					buttonsDisabled = false;
				} else {
					for(Road r : CSDisplay.lines) {
						r.getNextRoad();
					}
					buttonsDisabled = true;
					CSControl.EDIT_MODE = false;
					this.imagePath = "Stop.png";
					CSDisplay.PLAY_SIMULATION = true;
				}
			default:
				CSDisplay.MODE = this.MODE;
				CSControl.MODE = this.MODE;
				break;
			}
			if(doClick) {
				clicked = true;
				borderWidth = 6;
				
				CSDisplay.refreshDisplay();
			}
		}
	}
	
	public void tick() {
		if(CSControl.MODE == this.MODE && innerColor == idleColor && CSControl.MODE >= 0) {
			this.innerColor = selectedColor;
		} else if(CSControl.MODE != this.MODE && innerColor != idleColor && this.MODE != -1 && this.MODE != -4) {
			this.innerColor = idleColor;
		}
		
		if (MODE == -1) {
			if (buttonsDisabled && (innerColor == idleColor || innerColor == selectedColor)) {
				innerColor = Color.LIGHT_GRAY;
			} else if((!buttonsDisabled && innerColor == Color.LIGHT_GRAY) || (!CSControl.EDIT_MODE && innerColor == selectedColor)) {
				innerColor = Color.WHITE;
			}
		}
		
		if(clicked) {
			clickTimer--;
			if(clickTimer == 0) {
				borderWidth = 1;
			} else if(clickTimer < 0 && !CSControl.getDisplayChanged()) {
				clicked = false;
				clickTimer = 100;
			}
			CSControl.refreshDisplay();
		}
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.drawRoundRect(POS_X-borderWidth, POS_Y-borderWidth, WIDTH+borderWidth*2, HEIGHT+borderWidth*2, 5, 5);
		g2d.setColor(innerColor);
		g2d.fillRoundRect(POS_X-borderWidth+1, POS_Y-borderWidth+1, WIDTH+borderWidth*2-2, HEIGHT+borderWidth*2-2, 5, 5);
		
		Image img = null;
		try {
			img = ImageIO.read(new File("img\\buttons\\" + imagePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		g2d.drawImage(img,POS_X,POS_Y,POS_X+WIDTH,POS_Y+HEIGHT,0,0,256,256,null);
	}
}
