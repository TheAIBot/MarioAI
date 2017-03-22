package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

public class DebugSquare extends DebugDrawing {
	private final Color color;
	private final ArrayList<Point> startPoints; 
	private final ArrayList<Point> sizes; 
	
	public DebugSquare(Color color, ArrayList<Point> startPoints, ArrayList<Point> sizes) {
		this.color = color;
		this.startPoints = startPoints;
		this.sizes = sizes;
	}
	
	@Override
	public void draw(Graphics g) {
		final Color defaultColor = g.getColor();
		g.setColor(color);

		drawSquares(g);
		
		// reset graphics values
		g.setColor(defaultColor);
	}
	
	private void drawSquares(Graphics g) {
		for (int i = 0; i < startPoints.size(); i++) {
			Point startPoint = startPoints.get(i);
			Point size = sizes.get(i);
			g.fillRect(startPoint.x, startPoint.y, size.x, size.y);
		}
	}
}
