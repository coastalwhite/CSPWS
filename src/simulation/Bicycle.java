package simulation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import graphics.ScreenGraphics;
import roadGraph.Road;
import windowManager.CSDisplay;

public class Bicycle extends Vehicle {
	public Bicycle(double i) {
		super(i);
		color = Color.BLUE;
		
		WIDTH = 1.0;
		LENGTH = 2.0;
		
		safeDis = 1;
		/*Random r = new Random();
		this.prefSpeed = Math.round(r.nextGaussian()*1+5.83);*/
	}
	
	protected boolean ufCars(Road r) {
		ArrayList<Vehicle> veh = new ArrayList<Vehicle>();
		veh.addAll(r.vehicles);
		if(this.nextRoad != null) {
			veh.addAll(this.nextRoad.vehicles);
		}
		
		int i = 0;
		boolean changed = false;
		if(veh.size() > 1 ) {
			for(Vehicle v : veh) {
				if((v instanceof Bicycle && (v.ufTLChanged || v.ufCPChanged)) && v.speed <= this.speed && (i>=r.vehicles.size() || (i<r.vehicles.size() && !v.equals(this) && v.progress > this.progress))) {
					double comfDis = Math.pow((this.speed-v.speed), 2) / comfDec;
					
					if(comfDis + 7 > this.disTo(r, v)) {
						changed = true;
						colVeh = v;
						colAcc = v.speed;
						if(speed-(comfDec/ScreenGraphics.ticksPerSecond) < v.speed) {
							speed = v.speed;
						} else {
							speed -= (comfDec/ScreenGraphics.ticksPerSecond);
						}
					}
				}
				i++;
			}
			
			if(!changed && colVeh != null) {
				if (colVeh.speed > colAcc) {
					colVeh = null;
					speedUp = true;
				} else {
					changed = true;
				}
			}
		}
		return changed;
	}
}
