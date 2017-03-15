package MarioAI.debugGraphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;

public class DebugPoints extends DebugDrawing {
	private final Color color;
	private final ArrayList<Point> points; 
	private final int size;
	
	public DebugPoints(Color color, ArrayList<Point> points) {
		this(color, points, 10);
	}
	
	public DebugPoints(Color color, ArrayList<Point> points, int size) {
		this.color = color;
		this.points = points;
		this.size = size;
	}
	
	@Override
	public void draw(Graphics g) {
		final Color defaultColor = g.getColor();
		g.setColor(color);

		drawPoints(g);
		
		// reset graphics values
		g.setColor(defaultColor);
	}
	
	private void drawPoints(Graphics g) {
		for (int i = 0; i < points.size(); i++) {
			Point start = points.get(i);
			g.fillOval(start.x - (size / 2), start.y - (size / 2), size, size);
		}
	}
}
