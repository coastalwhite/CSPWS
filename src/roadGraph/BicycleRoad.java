package roadGraph;

import java.awt.Color;

import simulation.*;

public class BicycleRoad extends Road {

	public BicycleRoad(Bend ip1, Bend ip2, double iWeight) {
		super(ip1, ip2, iWeight);
		
		color = Color.RED;
		arrowColor = Color.BLACK;
	}

	
	public void spawnTick() {
		if(this.b1.bikesPerSecond > 0) {
			long nanoTime = System.nanoTime();
			if(prevTime == 0) {
				prevTime = nanoTime; 
			}
			System.out.print("hi!");
			
			timePassed += (nanoTime - prevTime);
			if (timePassed / Math.pow(10, 9) >= (1/b1.bikesPerSecond)) {
				timePassed -= Math.pow(10, 9);
				this.addVehicle(new Bicycle(10.0));
			}
			
			prevTime = nanoTime;
		}
	}
}