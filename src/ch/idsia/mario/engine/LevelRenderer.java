package ch.idsia.mario.engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import MarioAI.debugGraphics.DebugDrawing;
import ch.idsia.mario.engine.level.*;
import jdk.internal.dynalink.linker.LinkerServices;

public class LevelRenderer {
	private int xCam;
	private int yCam;
	private Image image;
	private Graphics2D g;
	private static final Color transparent = new Color(0, 0, 0, 0);
	private Level level;

	private Random random = new Random();
	public boolean renderBehaviors = false;

	int width;
	int height;

	public LevelRenderer(Level level, GraphicsConfiguration graphicsConfiguration, int width, int height) {
		this.width = width;
		this.height = height;

		this.level = level;
		image = graphicsConfiguration.createCompatibleImage(width * Art.SIZE_MULTIPLIER, height * Art.SIZE_MULTIPLIER, Transparency.BITMASK);
		g = (Graphics2D) image.getGraphics();
		g.setComposite(AlphaComposite.Src);

		updateArea(0, 0, width, height);
	}

	public void setCam(int xCam, int yCam) {
		int xCamD = this.xCam - xCam;
		int yCamD = this.yCam - yCam;
		this.xCam = xCam;
		this.yCam = yCam;

		g.setComposite(AlphaComposite.Src);
		g.copyArea(0, 0, width * Art.SIZE_MULTIPLIER, height * Art.SIZE_MULTIPLIER, xCamD * Art.SIZE_MULTIPLIER, yCamD * Art.SIZE_MULTIPLIER);

		if (xCamD < 0) {
			if (xCamD < -width)
				xCamD = -width;
			updateArea(width + xCamD, 0, -xCamD, height);
		} else if (xCamD > 0) {
			if (xCamD > width)
				xCamD = width;
			updateArea(0, 0, xCamD, height);
		}

		if (yCamD < 0) {
			if (yCamD < -width)
				yCamD = -width;
			updateArea(0, height + yCamD, width, -yCamD);
		} else if (yCamD > 0) {
			if (yCamD > width)
				yCamD = width;
			updateArea(0, 0, width, yCamD);
		}
	}

	private void updateArea(int x0, int y0, int w, int h) {
		g.setBackground(transparent);
		g.clearRect(x0 * Art.SIZE_MULTIPLIER, y0 * Art.SIZE_MULTIPLIER, w * Art.SIZE_MULTIPLIER, h * Art.SIZE_MULTIPLIER);
		int xTileStart = (x0 + xCam) / 16;
		int yTileStart = (y0 + yCam) / 16;
		int xTileEnd = (x0 + xCam + w) / 16;
		int yTileEnd = (y0 + yCam + h) / 16;
		for (int x = xTileStart; x <= xTileEnd; x++) {
			for (int y = yTileStart; y <= yTileEnd; y++) {
				int b = level.getBlock(x, y) & 0xff;
				if (((Level.TILE_BEHAVIORS[b]) & Level.BIT_ANIMATED) == 0) {
					g.drawImage(Art.level[b % 16][b / 16], ((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, null);
				}
			}
		}
	}

	public void render(Graphics g, int tick, float alpha) {
		g.drawImage(image, 0, 0, null);

		for (int x = xCam / 16; x <= (xCam + width) / 16; x++)
			for (int y = yCam / 16; y <= (yCam + height) / 16; y++) {
				byte b = level.getBlock(x, y);

				if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0) {
					int animTime = (tick / 3) % 4;

					if ((b % 16) / 4 == 0 && b / 16 == 1) {
						animTime = (tick / 2 + (x + y) / 8) % 20;
						if (animTime > 3)
							animTime = 0;
					}
					if ((b % 16) / 4 == 3 && b / 16 == 0) {
						animTime = 2;
					}
					int yo = 0;
					if (x >= 0 && y >= 0 && x < level.width && y < level.height)
						yo = level.data[x][y];
					if (yo > 0)
						yo = (int) (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8);
					g.drawImage(Art.level[(b % 16) / 4 * 4 + animTime][b / 16], ((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam - yo) * Art.SIZE_MULTIPLIER, null);
				}
				/*
				 * else if (b == Level.TILE_BONUS) { int animTime = (tick / 3) %
				 * 4; int yo = 0; if (x >= 0 && y >= 0 && x < level.width && y <
				 * level.height) yo = level.data[x][y]; if (yo > 0) yo = (int)
				 * (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8);
				 * g.drawImage(Art.mapSprites[(4 + animTime)][0], (x << 4) -
				 * xCam, (y << 4) - yCam - yo, null); }
				 */

				if (renderBehaviors) {
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_UPPER) > 0) {
						g.setColor(Color.RED);
						g.fillRect(((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, 16 * Art.SIZE_MULTIPLIER, 2 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_ALL) > 0) {
						g.setColor(Color.RED);
						g.fillRect(((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, 16 * Art.SIZE_MULTIPLIER, 2 * Art.SIZE_MULTIPLIER);
						g.fillRect(((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam + 14) * Art.SIZE_MULTIPLIER, 16 * Art.SIZE_MULTIPLIER, 2 * Art.SIZE_MULTIPLIER);
						g.fillRect(((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, 2 * Art.SIZE_MULTIPLIER, 16 * Art.SIZE_MULTIPLIER);
						g.fillRect(((x << 4) - xCam + 14) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, 2 * Art.SIZE_MULTIPLIER, 16 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_LOWER) > 0) {
						g.setColor(Color.RED);
						g.fillRect(((x << 4) - xCam) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam + 14) * Art.SIZE_MULTIPLIER, 16 * Art.SIZE_MULTIPLIER, 2 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_SPECIAL) > 0) {
						g.setColor(Color.PINK);
						g.fillRect(((x << 4) - xCam + 2 + 4) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam + 2 + 4) * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BUMPABLE) > 0) {
						g.setColor(Color.BLUE);
						g.fillRect(((x << 4) - xCam + 2) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam + 2) * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BREAKABLE) > 0) {
						g.setColor(Color.GREEN);
						g.fillRect(((x << 4) - xCam + 2 + 4) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam + 2) * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_PICKUPABLE) > 0) {
						g.setColor(Color.YELLOW);
						g.fillRect(((x << 4) - xCam + 2) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam + 2 + 4) * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER, 4 * Art.SIZE_MULTIPLIER);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0) {
					}
				}

			}
	}

	public void renderDebugDrawings(Graphics g, ArrayList<DebugDrawing> drawings) {
		for (int i = 0; i < drawings.size(); i++) {
			drawings.get(i).draw(g);
		}
	}

	public void repaint(int x, int y, int w, int h) {
		updateArea(x * 16 - xCam, y * 16 - yCam, w * 16, h * 16);
	}

	public void setLevel(Level level) {
		this.level = level;
		updateArea(0, 0, width, height);
	}

	public void renderExit0(Graphics g, int tick, float alpha, boolean bar) {
		for (int y = level.yExit - 8; y < level.yExit; y++) {
			g.drawImage(Art.level[12][y == level.yExit - 8 ? 4 : 5], ((level.xExit << 4) - xCam - 16) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, null);
		}
		int yh = level.yExit * 16 - (int) ((Math.sin((tick + alpha) / 20) * 0.5 + 0.5) * 7 * 16) - 8;
		if (bar) {
			g.drawImage(Art.level[12][3], ((level.xExit << 4) - xCam - 16) * Art.SIZE_MULTIPLIER, (yh - yCam) * Art.SIZE_MULTIPLIER, null);
			g.drawImage(Art.level[13][3], ((level.xExit << 4) - xCam) * Art.SIZE_MULTIPLIER, (yh - yCam) * Art.SIZE_MULTIPLIER, null);
		}
	}

	public void renderExit1(Graphics g, int tick, float alpha) {
		for (int y = level.yExit - 8; y < level.yExit; y++) {
			g.drawImage(Art.level[13][y == level.yExit - 8 ? 4 : 5], ((level.xExit << 4) - xCam + 16) * Art.SIZE_MULTIPLIER, ((y << 4) - yCam) * Art.SIZE_MULTIPLIER, null);
		}
	}
}