package windowManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import roadGraph.Vector2d;

public class ModeButton {
	private int POS_X, POS_Y, WIDTH = 50, HEIGHT = 50;
	public int borderWidth = 1;
	private int clickTimer = 100;
	private boolean clicked = false; 
	private int MODE;
	
	private Color innerColor;
	
	public static Color selectedColor = new Color(100,255,100);
	public static Color idleColor = Color.WHITE;
	
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
			JFileChooser fileChooser;
			switch (MODE) {
			case -1:
				CSControl.EDIT_MODE = CSControl.EDIT_MODE ? false : true;
				this.innerColor = CSControl.EDIT_MODE ? selectedColor : idleColor;
				break;
			case -2:
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
			case -3:
				fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("states\\"));
				CSDisplay.refreshDisplay();
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					
					try {
						CSControl.loadState(file.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				CSDisplay.MODE = MODE;
				CSControl.MODE = this.MODE;
				break;
			}
			clicked = true;
			borderWidth = 6;
			
			CSDisplay.refreshDisplay();
		}
	}
	
	public void tick() {
		if(CSControl.MODE == this.MODE && innerColor == idleColor) {
			this.innerColor = selectedColor;
		} else if(CSControl.MODE != this.MODE && innerColor != idleColor && this.MODE != -1) {
			this.innerColor = idleColor;
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
