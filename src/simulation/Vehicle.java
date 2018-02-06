package simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Random;

import graphCore.*;
import graphics.ScreenGraphics;
import roadGraph.*;
import windowManager.CSDisplay;

public class Vehicle {
	public float speedDif = 0.0f;
	public float speed = 13.88f; // m / s
	
	protected int reactionTime = 700; // ms
	protected boolean speedUp = false;
	protected int timePassed = 0;
	
	protected static float decRate = 2.75f;
	protected static float accRate = 2.75f;
	public static float safeDis = 10f;
	public static float crossingDis = 10f;
	
	public boolean removed = false;
	
	protected float progress = 0f;
	protected ArrayList<Road> roadsToTake = new ArrayList<Road>();
	
	private int stopTicks;
	
	public String debugval = "";
	
	public boolean ufCarsChanged = false,
					ufTLChanged = false,
					ufCPChanged = false;
	
	public double WIDTH = 1.5;
	public double LENGTH = 5;
	
	protected Color color = Color.GREEN;
	
	public Vehicle(Road r) {
		progress = 0.0f;
		
		createTrack(r);
	}
	
	private void createTrack(Road r) {
		Random random = new Random();
		
		int chanceTotal = 1000, randomNumber, topLevel;
		Road nRoad = r;
		
		int maxNextRoad = r.nextRoad.size();
		
		while (nRoad != null) {
			maxNextRoad = (nRoad.nextRoad.size() > maxNextRoad) ? nRoad.nextRoad.size() : maxNextRoad;
			roadsToTake.add(nRoad);
			
			if(nRoad.nextRoad.size() > 0) {
				randomNumber = random.nextInt(chanceTotal);
				
				topLevel = 0;
				
				for(int i = 0; i < nRoad.nextRoad.size(); i++) {
					topLevel += nRoad.nextRoadProbability.get(i)*1000;
					if(randomNumber <= topLevel) {
						topLevel = i;
						i = nRoad.nextRoad.size();
						nRoad = nRoad.nextRoad.get(topLevel);
					}
				}
				
				if(nRoad.equals(r)) {
					if(maxNextRoad == 1) {
						nRoad = null;
					} else {
						roadsToTake = new ArrayList<Road>();
						createTrack(r);
					}
					
				}
			} else {
				nRoad = null;
			}
		}
	}
	
	public boolean isIn(Vector2d v, Road r) {
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
	
	protected void brake() {
		float currentDec = convertWithTime(decRate);
		speed = (speed - currentDec < 0) ? 0 : speed - currentDec;
	}
	
	protected void stopTick(int i) {
		if(i > stopTicks) {
			stopTicks = i;
		}
	}
	
	protected boolean ufCars() {
		Vehicle v;
		float distance;
		int i =	r().vehicles.indexOf(this), toIndex = (roadsToTake.size() > 5) ? 5 : (roadsToTake.size()-1);
		
		if(i > 0) {
			v = r().vehicles.get(i-1);
			
			if(v.speed < speed ) {
				distance = (v.progress-progress) * r().length();
				
				if(distance < (3.5*speed+safeDis)) {
					brake();
					return true;
				}
			}
		}
		
		if(roadsToTake.size() == 1) {
			return false;
		}
		
		distance = (1-progress) * r().length();
		
		for(Road r : roadsToTake.subList(1, toIndex)) {
			if (r.vehicles.size() > 0) {
				v = r.vehicles.get(r.vehicles.size()-1);
				
				if(v.speed < speed) {
					if((distance + v.progress * r.length()) < (3.5*speed+safeDis)) {
						brake();
						return true;
					}
				}
			}
			distance += r.length();
		}
		
		return false;
	}
	protected boolean ufTrafficLight() {
		float stoppingDistance = (float) (Math.pow((this.speed), 2) / decRate + safeDis);
		for(Road r : roadsToTake) {
			if(disToBend(r.b2()) > stoppingDistance) {
				return false;
			}
			
			if(r.b2() instanceof TrafficLight) { // for every vehicle
				
				if(((TrafficLight) r.b2()).mode == 0) { // RED
					if(disToBend(r.b2()) <= stoppingDistance) {
						brake();
						return true;
					}
				} else if (((TrafficLight) r.b2()).mode == 2) { // ORANGE
					if(disToBend(r.b2()) <= stoppingDistance && disToBend(r.b2()) >= Math.pow((this.speed), 2) / decRate) {
						stopTick((int) (Math.ceil(speed/decRate)*ScreenGraphics.ticksPerSecond));
						return true;
					}
				}
				return false;
			}
		}
		
		return false;
	}
	protected boolean ufCrosspoint() {
		float stoppingDistance = (float) (Math.pow((this.speed), 2) / decRate + safeDis);
		
		ArrayList<Road> roads = new ArrayList<Road>();
		ArrayList<Road> tryRoads = new ArrayList<Road>();
		
		Road pRoad;
		
		for(Road r : roadsToTake) {
			if(disToBend(r.b2()) > stoppingDistance) {
				return false;
			}
			if(r.b2() instanceof CrossoverPoint) {
				if(r.b2().priorityList.indexOf(r) != 0) {
					double disToBend;
					float  brakeDis = (float) (Math.pow((this.speed), 2) / decRate + safeDis);
					
					double timeToBend = disToBend(r.b2()) / speed;
					
					tryRoads.addAll(r.b2().priorityList.subList(0, r.b2().priorityList.indexOf(r)));
					
					for(int i = 0; (i < tryRoads.size() && i < 4); i++) {
						pRoad = tryRoads.get(i);
						if(!pRoad.vehicles.isEmpty()) {
							roads.add(pRoad);
						}
						
						pRoad.debugVal = Double.toString(pRoad.disToBend(r.b2()));
						
						tryRoads.addAll(pRoad.prevRoad);
					}
					
					for(Road road : roads) {
						for(Vehicle v : road.vehicles) {
							disToBend = v.disToBend(r.b2());
							v.debugval = Double.toString(disToBend);
							if(disToBend > 0) {
								if(
									(disToBend-crossingDis) / v.speed < timeToBend &&
									(disToBend+crossingDis) / v.speed > timeToBend &&
									 disToBend(r.b2()) <= brakeDis
									) {
									stopTick((int) (Math.ceil(speed / decRate * ScreenGraphics.ticksPerSecond)));
									return true;
								}
							}
						}
					}
				}
				
				if(disToBend(r.b2()) > stoppingDistance) {
					return false;
				}
			}
		}
		
		return false;
	}
	
	protected Road r() {
		return roadsToTake.get(0);
	}
	protected Road r(int i) {
		return roadsToTake.get(i);
	}
	
	protected float convertWithTime(float i) {
		return (float) (i/ScreenGraphics.ticksPerSecond);
	}
	
	public void updateProgress() {
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
		
		changed = (ufCarsChanged = ufCars());
		changed = changed ? true : (ufTLChanged = ufTrafficLight());
		changed = changed ? true : (ufCPChanged = ufCrosspoint());
		
		if(stopTicks > 0) {
			changed = true;
			brake();
			stopTicks --;
		}
		
		if(!changed) {
			float currentAcc = (float) (convertWithTime(accRate));
			speed = (speed + currentAcc > r().maxSpeed + speedDif) ? (r().maxSpeed + speedDif) : (speed + currentAcc);
		}
		
		float deltaProgress = (float) (convertWithTime(speed) / r().length());
		
		if(progress + deltaProgress > 1) {
			if(roadsToTake.size() > 1) {
				progress = (convertWithTime(speed) - (1-progress)*r().length()) / r(1).length();
				toNextRoad();
			} else {
				removed = true;
				r().vehicles.remove(this);
				r().refresh = true;
			}
		} else {
			progress += deltaProgress;
		}
	}
	public float disToBend(Bend p) {
		float distance = -1 * progress * r().length();
		
		for(Road r : roadsToTake) {
			distance += r.length();
			if(r.b2().equals(p)) {
				return distance;
			}
		}
		
		return -1;
	}
	
	public void toNextRoad() {
		roadsToTake.get(1).vehicles.add(this);
		roadsToTake.get(0).vehicles.remove(this);
		roadsToTake.remove(0);
	}
	
	public final void draw (Graphics2D g2d, Road r, double displayZoom) {
		if(r.vehicles.contains(this)) {
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

	public void setSpeed(float d) {
		this.speed = d;
	}
	
	public Coord getPos(Road r) {
		if(!r.vehicles.contains(this)) {
			return new Coord(0,0);
		}
		
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
	public float p() {
		return progress;
	}
}
