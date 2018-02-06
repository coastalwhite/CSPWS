package graphics;

import windowManager.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.*;

public class ScreenGraphics extends JPanel implements Runnable, MouseListener, MouseWheelListener, MouseMotionListener {

	/**
	 * CS PWS
	 * Pim Mattijssen & Gijs Burghoorn
	 */
	private static final long serialVersionUID = 1L;
	private static Thread thread;
	
	public static float speedFactor = 100.0f;
	
	public boolean running = false;
	
	public static int FRAME_WIDTH, FRAME_HEIGHT, OFFSET_X, OFFSET_Y_TOP, OFFSET_Y_BOT;
	public long nanoTime = 0;
	public byte displayWarmUp = 3;
	
	public static double ticksPerSecond = 100;
	
	public CSDisplay csdisplay;
	public CSControl cscontrol;
	
	public ScreenGraphics(int fW, int fH){
		FRAME_WIDTH = fW;
		FRAME_HEIGHT = fH;
		
		this.csdisplay = new CSDisplay();
		this.cscontrol = new CSControl();
		
		setFocusable(true);
		setPreferredSize(new Dimension(FRAME_WIDTH+OFFSET_X,FRAME_HEIGHT));
		start();
		
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	public void run() {
		// Create loop
		while(running){
			repaint();
			tick();
		}
	}
	
	public void start(){
		running = true;
		
		thread = new Thread(this, "Game Loop");
		thread.start();
	}
	
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		
		// Warm up to create instant view
		if(displayWarmUp != 0) { CSDisplay.refreshDisplay(); CSControl.refreshDisplay(); displayWarmUp--; }
		
		// Draw two sides
		cscontrol.draw(g2d);
		csdisplay.draw(g2d);
	}
	
	public void tick(){
		// Timed updates
		if (System.nanoTime() - nanoTime >= Math.pow(10, 9) / (ticksPerSecond*speedFactor) ) {
			nanoTime = System.nanoTime();
			csdisplay.tick();
			cscontrol.tick();
			CSDisplay.collectData();
		}
		
	}

	// Events
	@Override
	public void mouseClicked(MouseEvent e) {
		cscontrol.mouseClick(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		csdisplay.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		csdisplay.mouseReleased(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		csdisplay.zoom(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		csdisplay.mouseDrag(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		csdisplay.mouseMove(e);
	}
}
