package windowManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JOptionPane;

import roadGraph.Bend;
import roadGraph.Vector2d;

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
	
	public void draw(Graphics2D g2d) {
		if(o instanceof Bend) {
			/*
			 * 1: carsPerSecond
			 * 2: bikesPerSecond
			 */
			
			Bend b = (Bend) o;
			
			g2d.setFont(new Font("sansserif", Font.BOLD, 16));
			
			drawStroke(g2d, "carsPerSecond", Double.toString(b.carsPerSecond), 0);
			drawStroke(g2d, "bikesPerSecond", Double.toString(b.bikesPerSecond), 1);
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
							CSDisplay.refreshDisplay();
						}
						break;
					case 1:
						textInput = JOptionPane.showInputDialog(this 
								 ,Double.toString(b.bikesPerSecond));
						
						if (textInput != "") {
							b.bikesPerSecond = Double.parseDouble(textInput);
							CSControl.refreshDisplay();
							CSDisplay.refreshDisplay();
						}
						break;
					}
				}
			}
		}
	}
}