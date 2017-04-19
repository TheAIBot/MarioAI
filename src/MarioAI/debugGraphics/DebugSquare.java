package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import ch.idsia.mario.engine.Art;

public class DebugSquare extends DebugDrawing {
	private final Color color;
	private final Point startPoint; 
	private final Point size; 
	
	public DebugSquare(Color color, Point startPoint, Point size) {
		this.color = color;
		this.startPoint = startPoint;
		this.size = size;
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
		g.fillRect(startPoint.x, startPoint.y, size.x, size.y);
	}
}
