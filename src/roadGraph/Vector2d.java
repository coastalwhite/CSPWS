package roadGraph;

public class Vector2d {
	private double x, y;
	
	public Vector2d(double dx, double dy) {
		this.x = dx;
		this.y = dy;
	}
	
	public Vector2d getTransformSR(double displayZoom) {
		double  f = displayZoom == 0 ? 0 : Math.pow(displayZoom, (-1));
				
				return new Vector2d(f*this.x, f*this.y);
	}
	public Vector2d getTransformRS(double displayZoom) {
		double  f = displayZoom;
		
		return new Vector2d(f*this.x, f*this.y);
	}
	
	public double length() {
		return Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2));
	}
	
	public double X() {
		return x;
	}
	public double Y() {
		return y;
	}
	public int INTX() {
		return (int) Math.round(x);
	}
	public int INTY() {
		return (int) Math.round(y);
	}
	
	public Vector2d sumVector(Vector2d v) {
		return new Vector2d(this.x + v.X(), this.y + v.Y());
	}
	public Vector2d difVector(Vector2d v) {
		return new Vector2d(this.x - v.X(), this.y - v.Y());
	}
	public double det(Vector2d w) {
		return (this.x * w.Y() - this.y * w.X());
	}
	public double dotProduct(Vector2d v) {
		return Math.acos((v.X()*this.x+v.Y()*this.y)/(this.length()*v.length()));
	}
	public boolean inRange(int minX, int maxX, int minY, int maxY) {
		return (this.x >= minX && this.x <= maxX && y >= minY && y <= maxY);
	}
	
	public void pr(String n) { // DEBUG FUNCTION
		System.out.println(n + ": (X: " + this.x + " , Y: " + this.y + ")");
	}
}
