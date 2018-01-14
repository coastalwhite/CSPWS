package simulation;

import java.awt.Color;
import java.util.Random;

public class Car extends Vehicle {	
	public Car(double speed, double speedSD) {
		super(speed);
		color = Color.WHITE;
		
		WIDTH = 1.7;
		LENGTH = 3.83;
		Random r = new Random();
		this.prefSpeed = Math.round(r.nextGaussian()*speedSD+speed);
	}
}
