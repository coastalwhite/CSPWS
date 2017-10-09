package windowManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import roadGraph.Vector2d;

public class ModeButton {
	private int POS_X, POS_Y, WIDTH = 50, HEIGHT = 50;
	public int borderWidth = 1;
	private int clickTimer = 100;
	private boolean clicked = false; 
	private int MODE;
	
	private String imagePath;
	
	public ModeButton(int iX, int iY, int iM, String iPath) {
		this.POS_X = iX;
		this.POS_Y = iY;
		
		this.MODE = iM;
		this.imagePath = iPath;
	}
	
	public void attemptToClick(Vector2d mV) {
		if(mV.inRange(POS_X, POS_X+WIDTH, POS_Y, POS_Y+HEIGHT)) {
			if(MODE == -1) {
				CSControl.EDIT_MODE = CSControl.EDIT_MODE ? false : true;
				CSControl.refreshDisplay();
			} else {
				CSDisplay.MODE = MODE;
			}
			clicked = true;
			borderWidth = 6;
		}
	}
	
	public void tick() {
		if(clicked) {
			clickTimer--;
			if(clickTimer == 0) {
				borderWidth = 1;
				clicked = false;
				clickTimer = 100;
			}
		}
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(POS_X-borderWidth, POS_Y-borderWidth, WIDTH+borderWidth*2, HEIGHT+borderWidth*2, 5, 5);
        g2d.draw(roundedRectangle);
		
		Image img = null;
		try {
			img = ImageIO.read(new File("img\\buttons\\" + imagePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		g2d.drawImage(img,POS_X,POS_Y,POS_X+WIDTH,POS_Y+HEIGHT,0,0,256,256,null);
	}
}
