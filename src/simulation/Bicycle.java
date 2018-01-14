package simulation;

import java.awt.Color;
import java.util.Random;

public class Bicycle extends Vehicle {
	public Bicycle(double speed, double speedSD) {
		super(speed);
		color = Color.BLUE;
		
		WIDTH = 1.0;
		LENGTH = 2.0;
		
		safeDis = 10;
		Random r = new Random();
		this.prefSpeed = Math.round(r.nextGaussian()*speedSD+speed);
	}
}
