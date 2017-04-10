package MarioAI.debugGraphics;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import ch.idsia.mario.engine.Art;

public class DebugString extends DebugDrawing {
	private final String text;
	private final Point position;
	
	public DebugString(String text, Point position) {
		this.text = text;
		this.position = position;
	}

	@Override
	public void draw(Graphics g) {
		final Font font = g.getFont();
		g.setFont(new Font(font.getFontName(), Font.BOLD, 6 * Art.SIZE_MULTIPLIER));
		
		drawString(g);

		// reset graphics values
		g.setFont(font);
	}
	
	private void drawString(Graphics g) {
		g.drawString(text, position.x, position.y);
	}
}
