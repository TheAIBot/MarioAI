package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import ch.idsia.mario.engine.Art;

/**
 * 
 * @author Andreas Gramstrup
 *
 */
class DebugPoints extends DebugDrawing {
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
			final Point start = points.get(i);
			final int correctedSize = size * Art.SIZE_MULTIPLIER;
			g.fillOval(start.x - (correctedSize / 2), start.y - (correctedSize / 2), correctedSize, correctedSize);
		}
	}
}
