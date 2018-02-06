package roadGraph;

import java.awt.Color;
import graphics.ScreenGraphics;
import simulation.Car;
import simulation.Vehicle;

public class CarRoad extends Road {
	
	public CarRoad(Bend ip1, Bend ip2, float iWeight) {
		super(ip1, ip2, iWeight);
		
		ip1.carRoadCon = true;
		
		color = Color.BLACK;
		
		maxSpeed = 14.44f;
		SDSpeed = 1.1f;
		
		defaultColor = Color.BLACK;
	}

	public void spawnTick() {
		if(this.b1.carsPerSecond > 0) {
			if(ticksToSpawn == -1) {
				ticksToSpawn = randomBinom(b1.carsPerSecond/ScreenGraphics.ticksPerSecond);
				System.out.println(ticksToSpawn);
			} else {
				if (ticksToSpawn == 0) {
					vehicles.add(new Car(this));
					ticksToSpawn = randomBinom(b1.carsPerSecond/ScreenGraphics.ticksPerSecond);
					
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
