package simulation;

import java.awt.Color;

public class Bicycle extends Vehicle {
	public Bicycle(double i) {
		super(i);
		color = Color.BLUE;
		
		WIDTH = 1.0;
		LENGTH = 2.0;
	}
}
