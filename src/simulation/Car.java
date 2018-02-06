package simulation;

import java.awt.Color;
import java.util.Random;

import roadGraph.Road;

public class Car extends Vehicle {	
	public Car(Road road) {
		super(road);
		color = Color.WHITE;
		
		WIDTH = 1.7;
		LENGTH = 3.83;
		Random r = new Random();
		this.speedDif = Math.round(r.nextGaussian()*r().SDSpeed);
		this.speed = r().maxSpeed + speedDif;
		
		safeDis = 8;
	}
}
