package roadGraph;

import java.awt.Color;

public class CrossoverRoad extends Road {

	public CrossoverRoad(Bend ip1, Bend ip2, double iWeight) {
		super(ip1, ip2, iWeight);

		this.color = Color.LIGHT_GRAY;
	}

}
