package graphCore;

import roadGraph.Vector2d;

public class Coord {
	private double X, Y;
	
	public Coord(double iX, double iY){
		// INIT
		
		this.X = iX;
		this.Y = iY;
	}
	
	// Calculation methods
	public double disTo(Coord coord){ // Returns distance between two coordinates / Pythagoras
		double distance;
		
		distance = Math.sqrt(
						Math.pow(this.X-coord.X(), 2)
						+
						Math.pow(this.Y-coord.Y(), 2)
				   );
		
		return distance;
	}
	
	// Returning methods
	public double X(){
		return this.X;
	}
	public double Y(){
		return this.Y;
	}
	public int INTX(){
		return (int) Math.round(this.X);
	}
	public int INTY(){
		return (int) Math.round(this.Y);
	}
	public Vector2d v() {
		return new Vector2d(this.X, this.Y);
	}
}
