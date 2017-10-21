package roadGraph;

import java.awt.Color;

public class CarRoad extends Road {

	
	
	public CarRoad(Bend ip1, Bend ip2, double iWeight) {
		super(ip1, ip2, iWeight);
		
		color = Color.BLUE;
	}

}
