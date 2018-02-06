package roadGraph;

import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import graphCore.Point;
import windowManager.CSControl;
import windowManager.CSDisplay;

public class Bend extends Point {
	private boolean doDisplay = false;
	
	public double carsPerSecond = 0.0;
	public double bikesPerSecond = 0.0;
	
	public boolean carRoadCon = false;
	public boolean bikeRoadCon = false;

	public boolean priorityEdit = false;
	public ArrayList<Road> priorityList;

	public Bend(double iX, double iY) {
		super(iX, iY);
		
		for(Road r : CSDisplay.lines) {
			r.b1().equals(this);
			if(r instanceof CarRoad) {
				this.carRoadCon = true;
			} else if(r instanceof BicycleRoad) {
				this.bikeRoadCon = true;
			}
		}
		
		priorityList = new ArrayList<Road>();
	}
	
	public void attemptToRender(Graphics2D g2d) {
		Vector2d v1;
		this.doDisplay = false;
		
		v1 = CSDisplay.tSR(new Vector2d(pos.X(), pos.Y()));
		
		// If in frame
		if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, -1 * radius, CSDisplay.HEIGHT+radius); }
		
		if(!doDisplay) {
			// TOP
			this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, -1 * radius, 0);
		}
		
		if(!doDisplay) {
			// BOTTOM
			this.doDisplay = v1.inRange(-1 * radius, CSDisplay.WIDTH+radius, CSDisplay.HEIGHT, CSDisplay.HEIGHT+radius);
		}
		
		if(!doDisplay) {
			// LEFT
			this.doDisplay = v1.inRange(-1 * radius, 0, -1 * radius, CSDisplay.HEIGHT+radius);
		}
		
		if(!doDisplay) {
			// RIGHT
			this.doDisplay = v1.inRange(CSDisplay.WIDTH, CSDisplay.WIDTH+radius, -1 * radius, CSDisplay.HEIGHT+radius);
			CSControl.refreshDisplay();
		}
		
		if(doDisplay) {
			v1 = CSDisplay.tSR(new Vector2d(pos.X(), pos.Y()));
			
			this.drawPoint(g2d, v1);
		}
		
	}
	
	public void drawPriorityEdit(Graphics2D g2d) {
		if(priorityEdit) {
			int i = 0;
			for(Road r : priorityList) {
				r.drawTextField(g2d, Integer.toString(i+1));
				i++;
			}
		}
	}
	public boolean editPriority(Vector2d mouseV) {
		int i = 0;
		for(Road r : priorityList) {
			if(r.clickedTextField(mouseV)) {
				int currentPriority = i+1;
				String textInput = JOptionPane.showInputDialog(this 
						 ,Integer.toString(currentPriority));
				
				if(textInput == null) {
					return false;
				}
				int newPriority = Integer.parseInt(textInput)-1;
				
				if (textInput != "" && newPriority >= -1 && newPriority <= priorityList.size()) {
					if(newPriority < 0) {
						priorityList.get(i).color = priorityList.get(i/2).defaultColor;
						priorityList.get(i).weightEdit = false;
						priorityList.remove(i);
					} else {
						if(newPriority >= priorityList.size()) {
							newPriority = priorityList.size()-1;
						}
						priorityList.set(i, priorityList.get(newPriority));
						priorityList.set(newPriority, r);
					}
					CSDisplay.refreshDisplay();
				}
				return true;
			}

			i++;
		}
		
		return false;
	}
}
