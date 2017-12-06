package simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

import graphCore.Line;
import graphics.ScreenGraphics;
import roadGraph.CrossoverPoint;
import roadGraph.Road;
import roadGraph.TrafficLight;
import roadGraph.Vector2d;
import windowManager.CSDisplay;

public class Vehicle {
	public double prefSpeed = 0.0f;
	public double speed = 30.0f; // m / s
	protected double progress = 0.0f; // %
	
	protected int reactionTime = 700; // ms
	protected boolean speedUp = false;
	protected int timePassed = 0;
	
	public Road nextRoad = null;
	
	protected static double comfDec = 5.7;
	protected double safeDis = 5;
	
	public  boolean ufCarsChanged = false,
					ufTLChanged = false,
					ufCPChanged = false;
	
	protected double colAcc = 0.0;
	protected Vehicle colVeh = null;
	
	public double WIDTH = 1.5;
	public double LENGTH = 5;
	
	protected Color color = Color.GREEN;
	
	public Vehicle(double pSpeed) {
		Random r = new Random();
		this.prefSpeed = Math.round(r.nextGaussian()*2+30);
		this.speed = pSpeed;
	}
	
	public boolean isIn(Vector2d v, Road r) {
		Vector2d v1 = CSDisplay.tSR(r.b1().pos().v());
		Vector2d v2 = CSDisplay.tSR(r.b2().pos().v());
		
		Vector2d difVector = v2.difVector(v1);
		
		double c = difVector.Y() / difVector.X();
		Vector2d carYOffset = new Vector2d(
											( -1 * Math.pow(CSDisplay.displayZoom(), -1) * ((WIDTH * Math.pow(Line.convertFactor, -1)) / 2) * c / Math.sqrt(c*c + 1) ),
											Math.pow(CSDisplay.displayZoom(), -1) * ((WIDTH * Math.pow(Line.convertFactor, -1)) / 2) / Math.sqrt(c*c + 1)
										);
		Vector2d yOffset = new Vector2d(
				( -1 * Math.pow(CSDisplay.displayZoom(), -1) * (Road.lineWidth / 2) * c / Math.sqrt(c*c + 1) ),
				Math.pow(CSDisplay.displayZoom(), -1) * (Road.lineWidth / 2) / Math.sqrt(c*c + 1)
			);
		
		Vector2d progressVector = new Vector2d ( progress * difVector.X(), progress * difVector.Y() ).sumVector(v1);
		Vector2d carLength = new Vector2d (
											Math.pow(CSDisplay.displayZoom(), -1) * (LENGTH * Math.pow(Line.convertFactor, -1)) / Math.sqrt(c*c + 1),
											Math.pow(CSDisplay.displayZoom(), -1) * (LENGTH * Math.pow(Line.convertFactor, -1)) * c / Math.sqrt(c*c + 1) 
										   );
		
		Vector2d [] carVectors = {
					progressVector.difVector(carLength).difVector(carYOffset),
					progressVector.difVector(carLength).sumVector(carYOffset),
					progressVector.sumVector(carYOffset),
					progressVector.difVector(carYOffset)
		};
			
		int [] xPoints = new int[4];
		int [] yPoints = new int[4];
		
		int i = 0;
		for(Vector2d s : carVectors) {
			s = s.getTransformRS(CSDisplay.displayZoom()).sumVector(CSDisplay.displayPosition()); 
			xPoints[i] = s.INTX();
			yPoints[i] = s.INTY();
			i++;
		}
		
		int crossingNumber = 0;
		
		
		for(int j = 0; j <= 4; j++) {
			crossingNumber += Road.isCrossing(new Vector2d(xPoints[j%4], yPoints[j%4]), new Vector2d(xPoints[(j+1)%4], yPoints[(j+1)%4]), v, new Vector2d(Math.pow(2, 60), v.Y())) ? 1 : 0;
		}
		return crossingNumber % 2 == 1;
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
				if(v.speed <= this.speed && (i>=r.vehicles.size() || (i<r.vehicles.size() && !v.equals(this) && v.progress > this.progress))) {
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
	protected boolean ufTrafficLight(Road r) {
		boolean changed = false;
		if(r.b2() instanceof TrafficLight && !changed) {
			if(((TrafficLight) r.b2()).mode == 0) {
				double comfDis = Math.pow((this.speed), 2) / comfDec;
				
				if(comfDis + safeDis > (1 - this.progress) * r.weight()) {
					changed = true;
					if(speed-(comfDec/ScreenGraphics.ticksPerSecond) < 0) {
						speed = 0;
					} else {
						speed -= (comfDec/ScreenGraphics.ticksPerSecond);
					}
				}
			} else if (((TrafficLight) r.b2()).mode == 2) {
				double comfDis = Math.pow((this.speed), 2) / comfDec;
				if(((1 - this.progress) * r.weight()) > comfDis + 2 && ((1 - this.progress) * r.weight()) < comfDis + 4) {
					changed = true;
					if(speed-(comfDec/ScreenGraphics.ticksPerSecond) < 0) {
						speed = 0;
					} else {
						speed -= (comfDec/ScreenGraphics.ticksPerSecond);
					}
				}
			}
		}
		return changed;
	}
	protected boolean ufCrosspoint(Road r) {
		boolean changed = false;
		if(r.b2().priorityList.size() > 1 && !changed) {
			double s = (1 - this.progress) * r.weight();
			double t = s / this.prefSpeed;
			
			double vehicleDistance, vehicleTime;
			
			ArrayList<Road> roads = new ArrayList<Road>();
			roads.addAll(CSDisplay.lines);
			roads.remove(r);
			
			for(Road road : roads) {
				if(road.b2().equals(r.b2())) {
					for(Vehicle v : road.vehicles) {
						vehicleDistance = (1 - v.progress) * road.weight();
						vehicleTime = vehicleDistance / v.speed;
						
						double comfDis = Math.pow((this.speed), 2) / comfDec;
						
						if(vehicleTime < t+1 && vehicleTime > t-1 && nextRoad.b1().priorityList.indexOf(r) > nextRoad.b1().priorityList.indexOf(road)) {
							if(comfDis + safeDis > (1 - this.progress) * r.weight()) {
								changed = true;
								if(speed-(comfDec/ScreenGraphics.ticksPerSecond) <= 0) {
									speed = 0;
								} else {
									speed -= (comfDec/ScreenGraphics.ticksPerSecond);
								}
							}
						}
							
					}
				}
			}
		}
		return changed;
	}
	
	public boolean updateProgress(Road r) {
		/*
		 * P = Progress of the car on the road in percent
		 * V = Velocity of the car
		 * W = Weight / Length of the road
		 * T = Seconds passed
		 * 
		 * P += ( W / V ) / T
		 * 
		 */
		
		boolean changed = false;
		
		changed = changed ? true : (ufCarsChanged = ufCars(r));
		changed = changed ? true : (ufTLChanged = ufTrafficLight(r));
		changed = changed ? true : (ufCPChanged = ufCrosspoint(r));
		
		if(!changed) {
			speedUp = true;
		}
		
		if(speedUp) {
			if(timePassed > reactionTime) {
				if(speed+(comfDec/ScreenGraphics.ticksPerSecond) > prefSpeed) {
					speed = prefSpeed;
				} else {
					speed += (comfDec/ScreenGraphics.ticksPerSecond);
				}
			} else {
				timePassed += 10;
			}
			speedUp = false;
		} else {
			timePassed = 0;
		}
		
		this.progress += (speed/ScreenGraphics.ticksPerSecond) / r.weight();
		CSDisplay.refreshDisplay();
		if(progress >= 1.0) {
			progress = 0.0;
			return true;
		}
		
		return false;
	}
	
	public double disTo(Road r, Vehicle v) {
		if(!r.vehicles.contains(v)) {
			return r.weight() * (1 - this.progress) + this.nextRoad.weight() * v.progress;
		} else {
			return r.weight() * Math.abs(this.progress - v.progress);
		}
	}
	public final void draw (Graphics2D g2d, Road r, double displayZoom) {
		
		Vector2d v1 = CSDisplay.tSR(r.b1().pos().v());
		Vector2d v2 = CSDisplay.tSR(r.b2().pos().v());
		
		Vector2d difVector = v2.difVector(v1);
		
		double c = difVector.Y() / difVector.X();
		Vector2d carYOffset = new Vector2d(
											( -1 * Math.pow(displayZoom, -1) * ((WIDTH * Math.pow(Line.convertFactor, -1)) / 2) * c / Math.sqrt(c*c + 1) ),
											Math.pow(displayZoom, -1) * ((WIDTH * Math.pow(Line.convertFactor, -1)) / 2) / Math.sqrt(c*c + 1)
										);
		Vector2d yOffset = new Vector2d(
				( -1 * Math.pow(displayZoom, -1) * (Road.lineWidth / 2) * c / Math.sqrt(c*c + 1) ),
				Math.pow(displayZoom, -1) * (Road.lineWidth / 2) / Math.sqrt(c*c + 1)
			);
		
		Vector2d progressVector = new Vector2d ( progress * difVector.X(), progress * difVector.Y() ).sumVector(v1);
		Vector2d carLength = new Vector2d (
											Math.pow(displayZoom, -1) * (LENGTH * Math.pow(Line.convertFactor, -1)) / Math.sqrt(c*c + 1),
											Math.pow(displayZoom, -1) * (LENGTH * Math.pow(Line.convertFactor, -1)) * c / Math.sqrt(c*c + 1) 
										   );
		
		Vector2d [] carVectors = {
					progressVector.difVector(carLength).difVector(carYOffset),
					progressVector.difVector(carLength).sumVector(carYOffset),
					progressVector.sumVector(carYOffset),
					progressVector.difVector(carYOffset)
		};
			
		int [] xPoints = new int[4];
		int [] yPoints = new int[4];
		
		Vector2d [] lineVectors = {
				v1.difVector(yOffset),
				v1.sumVector(yOffset),
				v2.sumVector(yOffset),
				v2.difVector(yOffset)
			   };
		
		int i = 0;
		for(Vector2d v : lineVectors) {
			xPoints[i] = v.INTX() > CSDisplay.POS_X+CSDisplay.WIDTH ? CSDisplay.POS_X+CSDisplay.WIDTH : v.INTX();
			yPoints[i] = v.INTY();
			i++;
		}
		
		g2d.setClip(new Polygon(xPoints, yPoints, 4));
		
		xPoints = new int[4];
		yPoints = new int[4];
		
		i = 0;
		for(Vector2d v : carVectors) {
			xPoints[i] = v.INTX();
			yPoints[i] = v.INTY();
			i++;
		}
		
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
	}

	public void setSpeed(double d) {
		this.speed = d;
	}
	public void setProgress(double d) {
		this.progress = d;
	}
	public double progress() {
		return this.progress;
	}
}