package simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

import graphCore.*;
import graphics.ScreenGraphics;
import roadGraph.*;
import windowManager.CSDisplay;

public class Vehicle {
	public double prefSpeed = 0.0f;
	public double speed = 13.88f; // m / s
	
	protected int reactionTime = 700; // ms
	protected boolean speedUp = false;
	protected int timePassed = 0;
	
	protected static double decRate = 2.75;
	protected static double accRate = 2.75;
	public static double safeDis = 8;
	public static double crossingDis = 20;
	
	public double dp = 0.0;
	
	public String debugval = "";
	
	private boolean speedChanged = false;
	
	public  boolean ufCarsChanged = false,
					ufTLChanged = false,
					ufCPChanged = false;
	
	public double WIDTH = 1.5;
	public double LENGTH = 5;
	
	protected Color color = Color.GREEN;
	
	public Vehicle(double pSpeed) {
		Random r = new Random();
		this.prefSpeed = Math.round(r.nextGaussian()*1.1+14.44);
		this.speed = pSpeed;
	}
	
	public boolean isIn(Vector2d v, Road r) {
		double progress = r.vehicleProgress.get(r.vehicles.indexOf(this));
		
		Vector2d v1 = r.b1().pos().v();
		Vector2d v2 = r.b2().pos().v();
		
		Vector2d difVector = v2.difVector(v1);
		
		double c = difVector.Y() / difVector.X();
		Vector2d carYOffset = new Vector2d(
											( -1 * Math.pow(CSDisplay.displayZoom(), -1) * ((WIDTH * Math.pow(Line.convertFactor, -1)) / 2) * c / Math.sqrt(c*c + 1) ),
											Math.pow(CSDisplay.displayZoom(), -1) * ((WIDTH * Math.pow(Line.convertFactor, -1)) / 2) / Math.sqrt(c*c + 1)
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
			xPoints[i] = s.INTX();
			yPoints[i] = s.INTY();
			i++;
		}
		
		int crossingNumber = 0;
		
		for(int j = 1; j <= 4; j++) {
			crossingNumber += Road.isCrossing(new Vector2d(xPoints[j%4], yPoints[j%4]), new Vector2d(xPoints[(j+1)%4], yPoints[(j+1)%4]), v, new Vector2d(Math.pow(2, 60), v.Y())) ? 1 : 0;
		}
		return crossingNumber % 2 == 1;
	}
	
	protected boolean ufCars(Road r) {
		if(!r.vehicles.contains(this)) { // Check for wrong argument
			System.out.println("System Error(2.1)");
			return false;
		}
		
		double progress = r.vehicleProgress.get(r.vehicles.indexOf(this)); // get progress
		
		int i = 0;
		for(Vehicle v : r.vehicles) { // for every vehicle
			if(progress < r.vehicleProgress.get(i) && v.speed < this.speed) { // If car is in front of this car
				double brakeDis = Math.pow((this.speed-v.speed), 2) / decRate + safeDis,
					   currentDec = (decRate/ScreenGraphics.ticksPerSecond);
				if(disTo(r, v) <= brakeDis && disTo(r,v) > 0) {
					speed = (speed - currentDec < 0) ? 0 : speed - currentDec;
					return true;
				}
			}
			
			i++;
		}
		
		return false;
	}
	protected boolean ufTrafficLight(Road r) {
		if(!r.vehicles.contains(this)) { // Check for wrong argument
			System.out.println("System Error(2.2)");
			return false;
		}
		
		if(r.b2() instanceof TrafficLight) { // for every vehicle
			double brakeDis = Math.pow((this.speed), 2) / decRate + safeDis,
				   currentDec = (decRate/ScreenGraphics.ticksPerSecond);
			
			if(((TrafficLight) r.b2()).mode == 0) { // RED
				if(disToBend(r, r.b2()) <= brakeDis) {
					speed = (speed - currentDec < 0) ? 0 : speed - currentDec;
					return true;
				}
			} else if (((TrafficLight) r.b2()).mode == 2) { // ORANGE
				if(disToBend(r, r.b2()) <= brakeDis && disToBend(r, r.b2()) >= Math.pow((this.speed), 2) / decRate) {
					speed = (speed - currentDec < 0) ? 0 : speed - currentDec;
					return true;
				}
			}
		}
		
		return false;
	}
	protected boolean ufCrosspoint(Road r) {
		if(!r.vehicles.contains(this)) { // Check for wrong argument
			System.out.println("System Error(2.3)");
			return false;
		}
		
		if(r.b2() instanceof CrossoverPoint) {
			if(r.b2().priorityList.indexOf(r) != 0) {
				double disToBend;
				double currentDec = (decRate/ScreenGraphics.ticksPerSecond);
				
				double timeToBend = disToBend(r,r.b2()) / speed;
				
				double brakeDis = Math.pow((this.speed), 2) / decRate + safeDis;
				
				for(Road x : r.b2().priorityList.subList(0, r.b2().priorityList.indexOf(r))) {
					for(Vehicle v : x.vehicles) {
						disToBend = v.disToBend(x,r.b2());
						
						if(
							(disToBend-crossingDis) / v.speed < timeToBend &&
							(disToBend+crossingDis) / v.speed > timeToBend &&
							 disToBend(r, r.b2()) <= brakeDis
							) {
							speed = (speed - currentDec < 0) ? 0 : speed - currentDec;
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	
	/*
	protected boolean ufCars(Road r) {
		double progress = r.vehicleProgress.get(r.vehicles.indexOf(this));
		
		int i = 0;
		boolean changed = false;
		if(r.vehicles.size() > 1 ) {
			for(Vehicle v : r.vehicles) {
				if(v.speed <= this.speed && (i>=r.vehicles.size() || (i<r.vehicles.size() && !v.equals(this) && r.vehicleProgress.get(r.vehicles.indexOf(v)) > progress))) {
					double comfDis = Math.pow((this.speed-v.speed), 2) / comfDec;
					
					if(comfDis + 7 > this.disTo(r, v) && this.disTo(r, v) > 0) {
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
		if(r.b2() instanceof TrafficLight) {
			if(((TrafficLight) r.b2()).mode == 0) {
				double comfDis = Math.pow((this.speed), 2) / comfDec;
				
				if(comfDis + safeDis > disToBend(r, r.b2())) {
					if(speed-(comfDec/ScreenGraphics.ticksPerSecond) < 0) {
						speed = 0;
					} else {
						speed -= (comfDec/ScreenGraphics.ticksPerSecond);
					}
					return true;
				}
			} else if(((TrafficLight) r.b2()).mode == 2) {
				double comfDis = Math.pow((this.speed), 2) / comfDec;
				
				if(comfDis + safeDis > disToBend(r, r.b2())) {
					if(speed-(comfDec/ScreenGraphics.ticksPerSecond) < 0) {
						speed = 0;
					} else {
						speed -= (comfDec/ScreenGraphics.ticksPerSecond);
					}
					return true;
				}
			}
		}
		return false;
	}
	protected boolean ufCrosspoint(Road r) {
		if(r.b2().priorityList.size() > 1 && r.b2().priorityList.contains(r)) {
			double s = disToBend(r, r.b2());
			double t = s / this.prefSpeed;
			
			double vehicleDistance, vehicleTime;
			
			int priorityLoc = r.b2().priorityList.indexOf(r);
			
			if(priorityLoc == 0) {
				return false;
			}
			
			for(Road road : r.b2().priorityList.subList(0, priorityLoc)) {
				for(Vehicle v : road.vehicles) {
					vehicleDistance = v.disToBend(road, r.b2());
					vehicleTime = vehicleDistance / v.speed;
					
					double comfDis = Math.pow((this.speed), 2) / comfDec;
					
					if(vehicleTime < t+1 && vehicleTime > t-1) {
						if(comfDis + safeDis > disToBend(r, r.b2())) {
							if(speed-(comfDec/ScreenGraphics.ticksPerSecond) <= 0) {
								speed = 0;
							} else {
								speed -= (comfDec/ScreenGraphics.ticksPerSecond);
							}
							return true;
						}
					}
						
				}
			}
		}
		return false;
	}*/
	
	public double updateProgress(Road r) {
		/*
		 * P = Progress of the car on the road in percent
		 * V = Velocity of the car
		 * W = Weight / Length of the road
		 * T = Seconds passed
		 * 
		 * P += ( W / V ) / T
		 * 
		 */
		
		double progress = r.vehicleProgress.get(r.vehicles.indexOf(this));
		
		if(progress >= 0) {
			if(speedChanged) {
				speedChanged = false;
			} else {
				double currentAcc = (accRate/ScreenGraphics.ticksPerSecond);
				speed = (speed + currentAcc > prefSpeed) ? prefSpeed : speed + currentAcc;
			}
		}
		
		if(!speedChanged) {
			boolean changed = false;
			
			changed = changed ? true : (ufCarsChanged = ufCars(r));
			changed = changed ? true : (ufTLChanged = ufTrafficLight(r));
			changed = changed ? true : (ufCPChanged = ufCrosspoint(r));
		
			speedChanged = changed;
		}
		
		if(progress >= 0 && progress < 1) {
			return (speed/ScreenGraphics.ticksPerSecond) / r.weight();
		} else {
			return -1;
		}
	}
	
	public double disTo(Road r, Vehicle v) {
		double distance = 0;
		Road nRoad = r;
		
		if(r.vehicles.contains(this)) {
			distance -= r.vehicleProgress.get(r.vehicles.indexOf(this)) * r.weight();
		} else {
			System.out.println("System Error(1.1)");
			return 0;
		}
		
		while(!(nRoad.vehicleProgress.get(nRoad.vehicles.indexOf(v)) > 0)) {
			distance += nRoad.weight();
			
			if(nRoad.nextRoad.isEmpty()) {
				return Math.pow(2, 60);
			}
			
			int i = 0;
			for(Road x : nRoad.nextRoad) {
				if(x.vehicles.contains(this)) {
					continue;
				}
				i++;
			}
			
			if(i == nRoad.nextRoad.size()) {
				System.out.println("System Error(1.2)");
				return -1;
			}
			nRoad = nRoad.nextRoad.get(i);
		}
		
		distance += nRoad.vehicleProgress.get(nRoad.vehicles.indexOf(v)) * nRoad.weight();
		
		return distance;
	}
	public double disToBend(Road r, Bend p) {
		double distance = 0;
		Road nRoad = r;
		
		if(r.vehicles.contains(this)) {
			distance += (1 - r.vehicleProgress.get(r.vehicles.indexOf(this))) * r.weight();
		} else {
			System.out.println("System Error(1.1)");
			return 0;
		}
		
		while(!nRoad.b2().equals(p)) {
			if(nRoad.nextRoad.isEmpty()) {
				return Math.pow(2, 60);
			}
			
			int i = 0;
			for(Road x : nRoad.nextRoad) {
				if(x.vehicles.contains(this)) {
					continue;
				}
				i++;
			}
			
			if(i == nRoad.nextRoad.size()) {
				System.out.println("System Error(1.2)");
				return -1;
			}
			nRoad = nRoad.nextRoad.get(i);
			
			distance += nRoad.weight();
		}
		
		return distance;
	}
	
	public final void draw (Graphics2D g2d, Road r, double displayZoom) {
		if(r.vehicles.contains(this)) {
			double progress = r.vehicleProgress.get(r.vehicles.indexOf(this));
			
			if(progress >= 0 && progress <= 1) {
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
		}
	}

	public void setSpeed(double d) {
		this.speed = d;
	}
	
	public Coord getPos(Road r) {
		if(!r.vehicles.contains(this)) {
			return new Coord(0,0);
		}
		
		double progress = r.vehicleProgress.get(r.vehicles.indexOf(this));
		
		Vector2d v1 = CSDisplay.tSR(r.b1().pos().v());
		Vector2d v2 = CSDisplay.tSR(r.b2().pos().v());
		
		Vector2d difVector = v2.difVector(v1);
		double c = difVector.Y() / difVector.X();
		
		double displayZoom = CSDisplay.displayZoom();
		
		Vector2d carLength = new Vector2d (
				Math.pow(displayZoom, -1) * (LENGTH * Math.pow(Line.convertFactor, -1)) / Math.sqrt(c*c + 1),
				Math.pow(displayZoom, -1) * (LENGTH * Math.pow(Line.convertFactor, -1)) * c / Math.sqrt(c*c + 1) 
			   );
		
		Vector2d progressVector = new Vector2d ( progress * difVector.X(), progress * difVector.Y() ).sumVector(v1);
		
		Vector2d v = progressVector.difVector(carLength.quotient(2));
		
		return new Coord(v.X(), v.Y());
	}
}
