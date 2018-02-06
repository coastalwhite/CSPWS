package roadGraph;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.ScreenGraphics;
import windowManager.CSDisplay;

public class TrafficLight extends Bend {
	
	public byte mode = 0;
	public long TLTimingR = 5000, TLTimingG = 5000, TLTimingO = 5000, prevTime = 0, timePassed = 0;
	
	protected int ticksDoneSince;

	public TrafficLight(double iX, double iY) {
		super(iX, iY);
		
		this.color = Color.RED;
	}
	
	public void drawPoint(Graphics2D g2d, Vector2d v) {
		double displayZoom = CSDisplay.displayZoom();
		
		double r = radius*0.5;
		
		// Draw Point(Oval)
		g2d.setColor(Color.BLACK);
		g2d.fillOval(
				v.INTX()-(int) Math.round(r*Math.pow(displayZoom, -1)+(1*Math.pow(displayZoom, -1))),
				v.INTY()-(int) Math.round(r*Math.pow(displayZoom, -1)+(1*Math.pow(displayZoom, -1))),
				(int) Math.round(r*2*Math.pow(displayZoom, -1)+(2*Math.pow(displayZoom, -1))),
				(int) Math.round(r*2*Math.pow(displayZoom, -1)+(2*Math.pow(displayZoom, -1)))
			);
		g2d.setColor(color);
		g2d.fillOval(
						v.INTX()-(int) Math.round(r*Math.pow(displayZoom, -1)),
						v.INTY()-(int) Math.round(r*Math.pow(displayZoom, -1)),
						(int) Math.round(r*2*Math.pow(displayZoom, -1)),
						(int) Math.round(r*2*Math.pow(displayZoom, -1))
					);
	}
	
	public void trafficUpdate() {
		
		/*
		 * MODES
		 * 1 : RED
		 * 2 : GREEN
		 * 3 : ORANGE
		 */
		
		double modeTime = 0;
		if(mode == 0) {
			modeTime = TLTimingR;
		} else if(mode == 1) {
			modeTime = TLTimingG;
		} else {
			modeTime = TLTimingO;
		}
		if (ticksDoneSince / ScreenGraphics.ticksPerSecond * 1000 >= modeTime) {
			mode = (byte) (((mode+1) > 2) ? 0 : (mode+1));
			switch(mode) {
			case 0:
				this.color = Color.RED;
				break;
			case 1:
				this.color = Color.GREEN;
				break;
			case 2:
				this.color = Color.ORANGE;
				break;
			}
			
			CSDisplay.refreshDisplay();
			ticksDoneSince = 0;
		} else {
			ticksDoneSince++;
		}
	}

}