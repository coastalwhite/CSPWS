package graphics;

import javax.swing.*;

public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int FRAME_WIDTH  = 1200,
					  FRAME_HEIGHT = 900;
	
	public static MainWindow GUI;
	
	public static void main(String s[]) {
		GUI = new MainWindow();
		GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GUI.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		GUI.setTitle("CS PWS");
		GUI.setVisible(true);
		GUI.setResizable(false);
		
		ScreenGraphics sg = new ScreenGraphics(FRAME_WIDTH, FRAME_HEIGHT);
		GUI.add(sg);
		GUI.setLocationRelativeTo(null);
		
		GUI.pack();
	}
}
