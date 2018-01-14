package roadGraph;

import java.awt.Color;

import windowManager.CSDisplay;

public class TrafficLight extends Bend {
	
	public byte mode = 0;
	public long TLTimingR = 5000, TLTimingG = 5000, TLTimingO = 5000, prevTime = 0, timePassed = 0;

	public TrafficLight(double iX, double iY) {
		super(iX, iY);
		
		this.color = Color.RED;
	}
	
	public void trafficUpdate() {
		long nanoTime = System.nanoTime();
		if(prevTime == 0) {
			prevTime = nanoTime; 
		}
		
		/*
		 * MODES
		 * 1 : RED
		 * 2 : GREEN
		 * 3 : ORANGE
		 */
		
		timePassed += (nanoTime - prevTime);
		double modeTime = 0;
		if(mode == 0) {
			modeTime = TLTimingR;
		} else if(mode == 1) {
			modeTime = TLTimingG;
		} else {
			modeTime = TLTimingO;
		}
		if (timePassed / Math.pow(10, 6) >= modeTime) {
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
			timePassed -= modeTime * Math.pow(10, 6);
		}
		
		prevTime = nanoTime;
	}

}