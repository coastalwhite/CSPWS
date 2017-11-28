package roadGraph;

import java.awt.Color;
import java.util.Random;

import simulation.Car;
import simulation.Vehicle;

public class CarRoad extends Road {

	private double carSpawnTime = 0;
	private int spawnTicksPerSec = 100;
	
	public CarRoad(Bend ip1, Bend ip2, double iWeight) {
		super(ip1, ip2, iWeight);
		
		color = Color.BLACK;
		
	}
	
	public int randomBinom(double prob) {
		if(prob <= 0 && prob >= 1) {
			return 0;
		}
		Random r = new Random();
		
		for(int i = 1; i <= 1000; i++) {
			if(r.nextInt(1000)<=prob*1000) {
				return i;
			}
		}
		return 1000;
	}

	public void spawnTick() {
		if(this.b1.carsPerSecond > 0) {
			long nanoTime = System.nanoTime();
			if(prevTime == 0) {
				carSpawnTime = randomBinom(b1.carsPerSecond/spawnTicksPerSec) * (Math.pow(10,  9) / spawnTicksPerSec);
				prevTime = nanoTime; 
			}
			carSpawnTime = carSpawnTime > 20 * (Math.pow(10,  9) / spawnTicksPerSec) ? carSpawnTime : 20 * (Math.pow(10,  9) / spawnTicksPerSec);
			
			timePassed += (nanoTime - prevTime);
			if (timePassed >= carSpawnTime) {
				timePassed = 0;
				if(vehicles.size() == 0) {
					this.addVehicle(new Car(30.0));
				} else {
					Vehicle v = vehicles.get(vehicles.size()-1);
					if(!(v.progress() * this.weight <= v.LENGTH * convertFactor && v.speed == 0.0)) {
						this.addVehicle(new Car(30.0));
					}
				}
				carSpawnTime = (Math.pow(10, 9) / spawnTicksPerSec) * randomBinom(b1.carsPerSecond/spawnTicksPerSec);
			}
			
			prevTime = nanoTime;
		}
	}
	
}
