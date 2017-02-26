package MarioAI.debugGraphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;

public class debugPoints {
	private final Color color;
	private final ArrayList<Point> points; 
	
	public debugPoints(Color color, ArrayList<Point> points) {
		this.color = color;
		this.points = points;
	}
	
	public void draw(Graphics g) {
		final Color defaultColor = g.getColor();
		g.setColor(color);

		final Stroke stroke = ((Graphics2D) g).getStroke();
		((Graphics2D) g).setStroke(new BasicStroke(2));

		drawPoints(g);
		
		// reset graphics values
		((Graphics2D) g).setStroke(stroke);
		g.setColor(defaultColor);
	}
	
	private void drawPoints(Graphics g) {
		for (int i = 0; i < points.size(); i++) {
			Point start = points.get(i);
			g.fillOval(start.x - 5, start.y - 5, 10, 10);
		}
	}
}
