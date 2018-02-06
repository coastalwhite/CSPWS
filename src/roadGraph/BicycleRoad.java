package roadGraph;

import java.awt.Color;

import graphics.ScreenGraphics;
import simulation.*;

public class BicycleRoad extends Road {

	public BicycleRoad(Bend ip1, Bend ip2, float iWeight) {
		super(ip1, ip2, iWeight);
		
		ip1.bikeRoadCon = true;
		
		color = Color.RED;
		arrowColor = Color.BLACK;
		
		maxSpeed = 5.556f;
		SDSpeed = 0.7638f;
		
		this.defaultColor = Color.RED;
	}

	public void spawnTick() {
		if(this.b1.bikesPerSecond > 0) {
			if(ticksToSpawn == -1) {
				ticksToSpawn = randomBinom(b1.bikesPerSecond/ScreenGraphics.ticksPerSecond);
			} else {
				if (ticksToSpawn == 0) {
					vehicles.add(new Bicycle(this));
					ticksToSpawn = randomBinom(b1.bikesPerSecond/ScreenGraphics.ticksPerSecond);
					
					if(ticksToSpawn / ScreenGraphics.ticksPerSecond < (Vehicle.safeDis/maxSpeed)) {
						ticksToSpawn = (int) (Math.ceil((Vehicle.safeDis/maxSpeed) * ScreenGraphics.ticksPerSecond));
					}
				} else {
					ticksToSpawn--;
				}
			}
		}
	}
}