package roadGraph;

import java.awt.Color;

import simulation.Car;
import simulation.Vehicle;

public class CarRoad extends Road {

	private int i = 0;
	
	public CarRoad(Bend ip1, Bend ip2, double iWeight) {
		super(ip1, ip2, iWeight);
		
		color = Color.BLACK;
	}

	public void spawnTick() {
		if(this.b1.carsPerSecond > 0) {
			long nanoTime = System.nanoTime();
			if(prevTime == 0) {
				prevTime = nanoTime; 
			}
			
			timePassed += (nanoTime - prevTime);
			if (timePassed / Math.pow(10, 9) >= (1/b1.carsPerSecond)) {
				timePassed -= (1/b1.carsPerSecond) * Math.pow(10, 9);
				if(vehicles.size() == 0) {
					this.addVehicle(new Car(30.0));
				} else {
					Vehicle v = vehicles.get(vehicles.size()-1);
					if(!(v.progress() * this.weight <= v.LENGTH * convertFactor && v.speed == 0.0)) {
						this.addVehicle(new Car(30.0));
					}
				}
				i++;
			}
			
			prevTime = nanoTime;
		}
	}
	
}
