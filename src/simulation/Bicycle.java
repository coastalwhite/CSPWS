package simulation;

import java.awt.Color;
import java.util.Random;

import roadGraph.Road;

public class Bicycle extends Vehicle {
	public Bicycle(Road road) {
		super(road);
		color = Color.BLUE;
		
		WIDTH = 1.0;
		LENGTH = 2.0;
		
		safeDis = 1;
		Random r = new Random();
		this.speedDif = Math.round(r.nextGaussian()*r().SDSpeed);
		this.speed = r().maxSpeed + speedDif;
	}
	
	protected boolean ufCars() {
		float distance = -1 * progress * r().length(),
				currentDec = convertWithTime(decRate);
		
		float stoppingDistance = (float) (Math.pow((this.speed), 2) / decRate + safeDis);
		
		for (Road r : roadsToTake) {
			for(Vehicle v : r.vehicles) {
				if(v.ufCPChanged || v.ufTLChanged) {
					if(v.speed < speed && !(r.equals(r()) && v.progress < progress)) {
						double brakeDis = Math.pow((this.speed-v.speed), 2) / decRate + safeDis;
						
						if(distance + (v.progress * r.length()) <= brakeDis) {
							speed = (speed - currentDec < 0) ? 0 : speed - currentDec;
							return true;
						}
					}
				}
			}
			
			distance += r.length();
			if(distance > stoppingDistance) {
				return false;
			}
		}
		
		return false;
	}
}
