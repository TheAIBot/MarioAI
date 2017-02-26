package MarioAI.debugGraphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;

public class debugLines {
	private final Color color;
	private final ArrayList<Point> lines; 
	
	public debugLines(Color color, ArrayList<Point> lines) {
		this.color = color;
		this.lines = lines;
	}
	
	public void draw(Graphics g) {
		// need atleast two points to draw a line
		if (lines.size() > 1) {
			final Color defaultColor = g.getColor();
			g.setColor(color);

			final Stroke stroke = ((Graphics2D) g).getStroke();
			((Graphics2D) g).setStroke(new BasicStroke(2));
			
			drawLines(g);

			// reset graphics values
			((Graphics2D) g).setStroke(stroke);
			g.setColor(defaultColor);
		}
	}
	
	private void drawLines(Graphics g) {
		for (int i = 0; i < lines.size() - 1; i++) {
			final Point start = lines.get(i);
			final Point end = lines.get(i + 1);

			g.drawLine(start.x, start.y, end.x, end.y);
		}
	}

}
