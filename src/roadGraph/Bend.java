package roadGraph;

import java.awt.Color;
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

	public boolean priorityEdit = false;
	public ArrayList<Road> priorityList;
	
	private ArrayList<Vector2d> textFields;

	public Bend(double iX, double iY) {
		super(iX, iY);
		
		priorityList = new ArrayList<Road>();
		textFields = new ArrayList<Vector2d>();
	}
	
	public void attemptToRender(Graphics2D g2d, double displayZoom) {
		Vector2d v1;
		
		this.doDisplay = false;
		
		v1 = CSDisplay.tSR(new Vector2d(pos.X(), pos.Y()));
		
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
			
			this.drawPoint(g2d, v1, displayZoom);
		}
		
	}
	
	public void drawPriorityEdit(Graphics2D g2d) {
		if(priorityEdit) {
			Vector2d posV1, posV2;
			
			int i = 0;
			for(Road r : this.priorityList) {
				if(r.doDisplay()) {
					posV1 = new Vector2d((r.b1.pos().X()+r.b2.pos().X())/2-40, (r.b1.pos().Y()+r.b2.pos().Y())/2-10);
					posV2 = new Vector2d((r.b1.pos().X()+r.b2.pos().X())/2+40, (r.b1.pos().Y()+r.b2.pos().Y())/2+10);
					
					textFields.add(posV1);
					textFields.add(posV2);
					
					posV1 = CSDisplay.tSR(posV1);
					posV2 = CSDisplay.tSR(posV2);
					
					g2d.setColor(Color.WHITE);
					g2d.fillRect(posV1.INTX(), posV1.INTY(), posV2.difVector(posV1).INTX(), posV2.difVector(posV1).INTY());
					g2d.setColor(Color.BLACK);
					g2d.drawRect(posV1.INTX(), posV1.INTY(), posV2.difVector(posV1).INTX(), posV2.difVector(posV1).INTY());

					posV1 = CSDisplay.tSR(new Vector2d((r.b1.pos().X()+r.b2.pos().X())/2-38, (r.b1.pos().Y()+r.b2.pos().Y())/2+4));

					g2d.drawString(Integer.toString(i+1), posV1.INTX(), posV1.INTY());
					
					i++;
				}
			}
		}
	}

	public boolean editPriority(Vector2d mouseV) {
		for(int i = 0; i < textFields.size(); i += 2) {
			if(mouseV.inIn(textFields.get(i), textFields.get(i+1))) {
				Road r = this.priorityList.get((int) Math.round(i/2));
				int currentPriority = 1+(int) Math.round(i/2);
				String textInput = JOptionPane.showInputDialog(this 
						 ,Integer.toString(currentPriority));
				
				int newPriority = Integer.parseInt(textInput)-1;
				
				if (textInput != "" && newPriority >= 1 && newPriority <= priorityList.size()) {
					priorityList.set((int) Math.round(i/2), priorityList.get(newPriority));
					priorityList.set(newPriority, r);
					
					CSDisplay.refreshDisplay();
				}
				
				return true;
			}
		}
		
		return false;
	}
}
