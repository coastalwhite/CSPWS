package roadGraph;

import java.awt.Color;

public class BicycleRoad extends Road {

	public BicycleRoad(Bend ip1, Bend ip2, double iWeight) {
		super(ip1, ip2, iWeight);
		
		color = Color.RED;
	}

}