package roadGraph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import graphCore.*;
import simulation.*;
import windowManager.CSControl;
import windowManager.CSDisplay;

public class Road extends Line {
	private boolean doDisplay = false;
	public static int zoomRange = 5;
	
	protected Bend b1, b2;
	
	public ArrayList<Road> prevRoad;
	
	public String debugVal = "";
	
	public ArrayList<Road> nextRoad;
	public ArrayList<Double> nextRoadProbability;
	
	public ArrayList<Vehicle> vehicles;
	
	public boolean saveDensity = false;
	public String saveName = "Unknown";
	public int saveNumber = 0;
	
	public float maxSpeed;
	public float SDSpeed;
	
	public boolean refresh = true;
	
	protected int ticksToSpawn = -1;
	
	public boolean weightEdit = false;
	private float vehicleDensity;
	private float vehicleAvgSpeed;
	
	public Road(Bend ip1, Bend ip2, float iWeight) {
		super((Point) ip1, (Point) ip2);
		
		this.setWeight(iWeight);
		
		vehicles = new ArrayList<Vehicle>();
		
		prevRoad = new ArrayList<Road>();
		
		nextRoad = new ArrayList<Road>();
		nextRoadProbability = new ArrayList<Double>();
		
		this.b1 = ip1;
		this.b2 = ip2;
	}
	
	public Bend b1() { return this.b1; }
	public Bend b2() { return this.b2; }
	
	public void switchDirection() {
		Bend b = this.b1;
		this.b1 = this.b2;
		this.b2 = b;
	}
	
	public void resetTime() {
		ticksToSpawn = -1;
	}
	private void calculateData() {
		double l = 0;
		float speedTotal = 0.0f;
		int speedCount = 0;
		for(Vehicle v : vehicles) {
			if(v.p() > 0) {
				l += v.LENGTH;
				speedTotal += v.speed;
				speedCount++;
			}
		}
		
		this.vehicleAvgSpeed = speedTotal/speedCount;
		this.vehicleDensity = (float) (l / this.length());
	}
	
	public boolean isIn(Vector2d v) {
		Vector2d difVector = p1.pos().v().difVector(p2.pos().v());
		
		double c = difVector.Y() / difVector.X();
		Vector2d yOffset = new Vector2d( (lineWidth / 2) * c / Math.sqrt(c*c + 1) ,(lineWidth / 2) / Math.sqrt(c*c + 1));
		
		
		Vector2d [] lineVectors = {
									p1.pos().v().difVector(yOffset),
									p1.pos().v().sumVector(yOffset),
									p2.pos().v().sumVector(yOffset),
									p2.pos().v().difVector(yOffset)
								   };
			
		int [] xPoints = new int[4];
		int [] yPoints = new int[4];
		
		int i = 0;
		for(Vector2d p : lineVectors) {
			xPoints[i] = p.INTX();
			yPoints[i] = p.INTY();
			i++;
		}
		
		int crossingNumber = 0;
		
		for(int j = 1; j <= 4; j++) {
			crossingNumber += isCrossing(new Vector2d(xPoints[j%4], yPoints[j%4]), new Vector2d(xPoints[(j+1)%4], yPoints[(j+1)%4]), v, new Vector2d(Math.pow(2, 60), v.Y())) ? 1 : 0;
		}
		return crossingNumber % 2 == 1;
	}
	
	private final static boolean inRange(double i, double c1, double c2) {
		double min = c1,
			max = c2;
		if (c1 > c2) {
			min = c2;
			max = c1;
		}
		return i >= min && i <= max;
	}
	
	public final static boolean isCrossing(Vector2d v1, Vector2d v2, Vector2d w1, Vector2d w2) {
		/* https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
		 * https://www.youtube.com/playlist?list=PLZHQObOWTQDPD3MizzM2xVFitgF8hE_ab
		 * p = v1
		 * q = w1
		 * r = v1 - v2
		 * s = w1 - w2
		 * t = (q - p) x s / (r x s)
		 * u = (p - q) x r / (s x r)
		 */
		
		// Check if v1->v2 crosses w1->w2
		Vector2d p, q, r, s;
		double t, u;
		
		p = v1;
		q = w1;
		r = v1.difVector(v2);
		s = w1.difVector(w2);
		
		if(r.det(s) == 0) {
			return (q.difVector(p).det(r) == 0);
		}
		
		t = q.difVector(p).det(s) / r.det(s);
		u = p.difVector(q).det(r) / s.det(r);
		
		return (inRange(t,-1.0f,0.0f) && inRange(u,-1.0f,0.0f));
	}
	public Vector2d getCrosspoint(Road road) {
		// Check if v1->v2 crosses w1->w2
		Vector2d p, q, r, s;
		double t, u;
		
		p = new Vector2d(road.b1().pos().X(), road.b1.pos().Y());
		q = new Vector2d(b1.pos().X(), b1.pos().Y());
		r = new Vector2d(road.b2().pos().X(), road.b2.pos().Y()).difVector(p);
		s = new Vector2d(b2.pos().X(), b2.pos().Y()).difVector(q);
		
		if(r.det(s) == 0) {
			return null;
		}
		
		t = q.difVector(p).det(s) / r.det(s);
		u = p.difVector(q).det(r) / s.det(r);
		
		if(inRange(t,0.0f,1.0f) && inRange(u,0.0f,1.0f)) {
			return new Vector2d(t, u);
		}
		
		return null;
	}
	
	public Vector2d toIntersect(Vector2d v) {
		Vector2d v1 = new Vector2d(b1.pos().X(), b1.pos().Y());
		Vector2d v2 = new Vector2d(b2.pos().X(), b2.pos().Y());
		
		return v1.sumVector(v2.difVector(v1).product(v.Y()));
	}
	
	public void tick () {
		for(int i = 0; i < vehicles.size(); i++) {
			vehicles.get(i).updateProgress();
		}
		calculateData();
		for(Road r : nextRoad) {
			if(!r.prevRoad.contains(this)) {
				r.prevRoad.add(this);
			}
		}
	}
	
	public void renderVehicles(Graphics2D g2d, double displayZoom) {
		if(vehicles.size() > 0) {
			for (int i = 0; i < vehicles.size(); i++) {
				vehicles.get(i).draw(g2d, this, displayZoom);
			}
		}
	}
	
	public void attemptToRender(Graphics2D g2d) {
		Vector2d v1, v2;
		
		this.doDisplay = false;
		
		v1 = CSDisplay.tSR(new Vector2d(b1.pos().X(), b1.pos().Y()));
		v2 = CSDisplay.tSR(new Vector2d(b2.pos().X(), b2.pos().Y()));
		
		if(!this.doDisplay) { this.doDisplay = v1.inRange(-1 * lineWidth, CSDisplay.WIDTH+lineWidth, -1 * lineWidth, CSDisplay.HEIGHT+lineWidth); }
		if(!this.doDisplay) { this.doDisplay = v2.inRange(-1 * lineWidth, CSDisplay.WIDTH+lineWidth, -1 * lineWidth, CSDisplay.HEIGHT+lineWidth); }
		
		Vector2d w1, w2;
		
		if(!doDisplay) {
			// TOP
			w1 = new Vector2d(0, 0);
			w2 = new Vector2d(700, 0);
			
			if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
		}
		
		if(!doDisplay) {
			// BOTTOM
			w1 = new Vector2d(0, CSDisplay.HEIGHT);
			w2 = new Vector2d(CSDisplay.WIDTH, CSDisplay.HEIGHT);

			if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
		}
		
		if(!doDisplay) {
			// LEFT
			w1 = new Vector2d(0, 0);
			w2 = new Vector2d(0, CSDisplay.HEIGHT);

			if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; }
		}
		
		if(!doDisplay) {
			// RIGHT
			w1 = new Vector2d(CSDisplay.WIDTH, 0);
			w2 = new Vector2d(CSDisplay.WIDTH, CSDisplay.HEIGHT);

			if(isCrossing(v1, v2, w1, w2)) { doDisplay = true; CSControl.refreshDisplay(); }
		}
		
		if(doDisplay) {
			v1 = CSDisplay.tSR(new Vector2d(b1.pos().X(), b1.pos().Y()));
			v2 = CSDisplay.tSR(new Vector2d(b2.pos().X(), b2.pos().Y()));
			
			this.drawLine(g2d, v1, v2);
		}
		
	}
	
	public void organizeNextRoad(double max) {
		double total = 0;
		for(double d : nextRoadProbability) {
			total += d;
		}
		
		int digits = 3;
		
		for(int i = 0; i < nextRoadProbability.size(); i++) {
			nextRoadProbability.set(i, (double) (Math.round((nextRoadProbability.get(i) / (total/max))*Math.pow(10, digits))/Math.pow(10, digits)));
		}
	}
	
	public void drawTextField(Graphics2D g2d, String i) {
		Vector2d posV1 = new Vector2d((b1.pos().X()+b2.pos().X())/2-40, (b1.pos().Y()+b2.pos().Y())/2-10),
				 posV2 = new Vector2d((b1.pos().X()+b2.pos().X())/2+40, (b1.pos().Y()+b2.pos().Y())/2+10);
		
		posV1 = CSDisplay.tSR(posV1);
		posV2 = CSDisplay.tSR(posV2);
		
		g2d.setColor(new Color(1f,1f,1f,0.5f));
		g2d.fillRect(posV1.INTX(), posV1.INTY(), posV2.difVector(posV1).INTX(), posV2.difVector(posV1).INTY());
		g2d.setColor(Color.BLACK);
		g2d.drawRect(posV1.INTX(), posV1.INTY(), posV2.difVector(posV1).INTX(), posV2.difVector(posV1).INTY());

		posV1 = CSDisplay.tSR(new Vector2d((b1.pos().X()+b2.pos().X())/2-38, (b1.pos().Y()+b2.pos().Y())/2+4));

		g2d.drawString(i, posV1.INTX(), posV1.INTY());
	}	
	public void drawEditWeight(Graphics2D g2d) {
		if(weightEdit) {
			int i = 0;
			for(Road r : nextRoad) {
				r.drawTextField(g2d, Double.toString(nextRoadProbability.get(i++)));
			}
		}
	}
	public boolean clickedTextField(Vector2d mouseV) {
		return mouseV.inIn(
				new Vector2d(
						(b1.pos().X()+b2.pos().X())/2-40,
						(b1.pos().Y()+b2.pos().Y())/2-10
					),
				new Vector2d(
						(b1.pos().X()+b2.pos().X())/2+40,
						(b1.pos().Y()+b2.pos().Y())/2+10
					)
			   );
	}
	
	@SuppressWarnings("unused")
	public float disToBend(Bend b) {
		ArrayList<Road> roads = new ArrayList<Road>();
		ArrayList<Float> distance = new ArrayList<Float>();
		roads.addAll(nextRoad);
		for(Road x : nextRoad) {
			distance.add(0.0f);
		}
		Road road;
		
		for(int i = 0; i < roads.size(); i++) {
			road = roads.get(i);
			if(!road.b2().equals(b)) {
				if(!road.nextRoad.isEmpty()) {
					roads.addAll(road.nextRoad);
					for(Road x : road.nextRoad) {
						distance.add(distance.get(i)+road.length());
					}
				}
			} else {
				return distance.get(i)+road.length();
			}
		}
		return -1;
	}
	
	public boolean editWeight(Vector2d mouseV) {
		int i = 0;
		for(Road r : nextRoad) {
			if(r.clickedTextField(mouseV)) {
				Double currentProbability = this.nextRoadProbability.get(i);
				String textInput = JOptionPane.showInputDialog(this 
						 ,Double.toString(currentProbability));
				
				if(textInput == null) {
					return false;
				}
				Double newProbability = Double.parseDouble(textInput);
				
				if (textInput != "" && newProbability >= 0.0 && newProbability <= 1.0) {
					if(newProbability == 0) {
						nextRoad.get(i).color = nextRoad.get(i).defaultColor;
						
						nextRoadProbability.remove(i);
						nextRoad.remove(i);
						
						organizeNextRoad(1.0);
					} else {
						this.nextRoadProbability.set(i, 0.0);
						organizeNextRoad(1-newProbability);
						this.nextRoadProbability.set(i, newProbability);
					}
					CSDisplay.refreshDisplay();
				}
				
				return true;
			}
			i++;
		}
		
		return false;
	}


	public void addNextRoad(Road r) {
		if(nextRoad.size() == 0) {
			nextRoad.add(r);
			nextRoadProbability.add(1.0);
		} else {
			for(int i = 0; i < nextRoad.size(); i++) {
				nextRoadProbability.set(i, nextRoadProbability.get(i)/2);
			}
			nextRoad.add(r);
			nextRoadProbability.add(0.5);
		}
		
		if(this.b2.priorityList.indexOf(this) < 0) {
			this.b2.priorityList.add(this);
		}
	}
	public void setB1(Bend b) {
		b1 = b;
	}
	public void setB2(Bend b) {
		b2 = b;
	}
	
	public boolean doDisplay() {
		return doDisplay;
	}
	
	public Coord getPos() {
		Vector2d v = CSDisplay.tSR(b1.pos().v().sumVector(b2.pos().v()).quotient(2));
		return new Coord(v.X(), v.Y());
	}
	
	public void spawnTick() {
		// Init
	}
	
	public float vehicleDensity() {
		return vehicleDensity;
	}
	
	public float vehicleAvgSpeed() {
		return vehicleAvgSpeed;
	}
	
	protected int randomBinom(double prob) {
		if(prob <= 0 && prob >= 1) {
			return -1;
		}
		Random r = new Random();
		
		for(int i = 1; i <= 100000; i++) {
			if(r.nextInt(100000)<=prob*100000) {
				return i;
			}
		}
		return 100000;
	}
}
