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
	
	public boolean running = false;
	
	public static int FRAME_WIDTH, FRAME_HEIGHT, OFFSET_X, OFFSET_Y_TOP, OFFSET_Y_BOT;
	public long nanoTime = 0;
	public byte displayWarmUp = 8;
	
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
		while(running){
			repaint();
			
			if (System.nanoTime() - nanoTime >= 1000000) {
				nanoTime = System.nanoTime();
				tick();
			}
		}
	}
	
	public void start(){
		running = true;
		
		thread = new Thread(this, "Game Loop");
		thread.start();
	}
	
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		
		if(displayWarmUp != 0) { csdisplay.refreshDisplay(); cscontrol.refreshDisplay(); displayWarmUp--; }
		cscontrol.draw(g2d);
		csdisplay.draw(g2d);
	}
	
	public void tick(){
		csdisplay.tick();
		cscontrol.tick();
	}

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
		csdisplay.mouseReleased();
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
