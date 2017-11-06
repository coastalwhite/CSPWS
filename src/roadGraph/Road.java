package roadGraph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import graphCore.*;
import simulation.Car;
import windowManager.CSControl;
import windowManager.CSDisplay;

public class Road extends Line {
	private boolean doDisplay = false;
	public static int zoomRange = 5;
	
	private Bend b1, b2;
	
	public ArrayList<Road> nextRoad;
	public ArrayList<Double> nextRoadProbability;
	public ArrayList<Car> cars;
	public boolean hadCar = false;
	
	private long timePassed = 0;
	private long prevTime = 0;
	
	public boolean weightEdit = false;
	private ArrayList<Vector2d> textFields;

	public Road(Bend ip1, Bend ip2, double iWeight) {
		super((Point) ip1, (Point) ip2);
		
		this.weight = iWeight;
		
		cars = new ArrayList<Car>();
		nextRoad = new ArrayList<Road>();
		nextRoadProbability = new ArrayList<Double>();
		textFields = new ArrayList<Vector2d>();
		
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
		
		
		for(int j = 0; j <= 4; j++) {
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
	
	private final Road getNextRoad() {
		Random random = new Random();
		int randomNumber = random.nextInt(1000);
		
		int topLevel = 0;
		for(int i = 0; i <= nextRoad.size(); i++) {
			topLevel += nextRoadProbability.get(i)*1000;
			if(randomNumber <= topLevel) {
				return nextRoad.get(i);
			}
		}
		
		return null;
	}	
	
	private final static boolean isCrossing(Vector2d v1, Vector2d v2, Vector2d w1, Vector2d w2) {
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
	
	public void addCar(Car c) {
		cars.add(c);
	}
	public void tick () {
		for(int i = 0; i < cars.size(); i++) {
			if(cars.get(i).updateProgress(this)) {
				if (nextRoad.size() > 0) {
					this.hadCar = true;
					this.getNextRoad().addCar(cars.get(i));
				}
				cars.remove(i);
			}
		}
		if(this.b1.carsPerSecond > 0) {//if (this instanceof CarRoad && this.b1.carsPerSecond > 0) {
			long nanoTime = System.nanoTime();
			if(prevTime == 0) {
				prevTime = nanoTime; 
			}
			
			timePassed += (nanoTime - prevTime);
			if (timePassed / Math.pow(10, 9) >= (1/b1.carsPerSecond)) {
				timePassed -= Math.pow(10, 9);
				this.addCar(new Car());
			}
			
			prevTime = nanoTime;
		}
	}
	
	public void renderCars(Graphics2D g2d, double displayZoom) {
		for (int i = 0; i < cars.size(); i++) {
			cars.get(i).draw(g2d, this, displayZoom);
		}
	}
	
	public void attemptToRender(Graphics2D g2d, double displayZoom) {
		Vector2d v1, v2;
		
		this.doDisplay = false;
		
		v1 = CSDisplay.linTrans(new Vector2d(b1.pos().X(), b1.pos().Y()));
		v2 = CSDisplay.linTrans(new Vector2d(b2.pos().X(), b2.pos().Y()));
		
		//System.out.println("X: " + v1.X());
		
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
			v1 = CSDisplay.linTrans(new Vector2d(b1.pos().X(), b1.pos().Y()));
			v2 = CSDisplay.linTrans(new Vector2d(b2.pos().X(), b2.pos().Y()));
			
			this.drawLine(g2d, v1, v2, displayZoom);
		}
		
	}
	
	public void drawEditWeight(Graphics2D g2d) {
		if(weightEdit) {
			Vector2d posV1, posV2;
			
			int i = 0;
			for(Road r : this.nextRoad) {
				if(r.doDisplay) {
					posV1 = new Vector2d((r.b1.pos().X()+r.b2.pos().X())/2-40, (r.b1.pos().Y()+r.b2.pos().Y())/2-10);
					posV2 = new Vector2d((r.b1.pos().X()+r.b2.pos().X())/2+40, (r.b1.pos().Y()+r.b2.pos().Y())/2+10);
					
					textFields.add(posV1);
					textFields.add(posV2);
					
					posV1 = CSDisplay.linTrans(posV1);
					posV2 = CSDisplay.linTrans(posV2);
					
					g2d.setColor(Color.WHITE);
					g2d.fillRect(posV1.INTX(), posV1.INTY(), posV2.difVector(posV1).INTX(), posV2.difVector(posV1).INTY());
					g2d.setColor(Color.BLACK);
					g2d.drawRect(posV1.INTX(), posV1.INTY(), posV2.difVector(posV1).INTX(), posV2.difVector(posV1).INTY());
					
					posV1 = CSDisplay.linTrans(new Vector2d((r.b1.pos().X()+r.b2.pos().X())/2-38, (r.b1.pos().Y()+r.b2.pos().Y())/2+4));
					g2d.drawString(Double.toString(this.nextRoadProbability.get(i)), posV1.INTX(), posV1.INTY());
					
					i++;
				}
			}
		}
	}
	
	public void setWeight(double iWeight) {
		this.weight = iWeight;
	}

	public boolean editWeight(Vector2d mouseV) {
		for(int i = 0; i < textFields.size(); i += 2) {
			if(mouseV.inIn(textFields.get(i), textFields.get(i+1))) {
				Road r = this.nextRoad.get((int) Math.round(i/2));
				Double currentProbability = this.nextRoadProbability.get((int) Math.round(i/2));
				String textInput = JOptionPane.showInputDialog(this 
						 ,Double.toString(currentProbability));
				
				Double newProbability = Double.parseDouble(textInput);
				
				if (textInput != "" && newProbability >= 0.0 && newProbability <= 1.0) {
					this.nextRoadProbability.set((int) Math.round(i/2), newProbability);
					CSDisplay.refreshDisplay();
				}
				
				return true;
			}
		}
		
		return false;
	}	
}
