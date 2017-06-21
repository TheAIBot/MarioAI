package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;

import MarioAI.World;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.Shell;

/**
 * Code in this class is mostly from the games source code
 * 
 * @author Andreas Gramstrup
 *
 */
public class WalkingEnemySimulator extends EnemySimulator {
	public static final int ENEMY_RED_KOOPA = 0;
	public static final int ENEMY_GREEN_KOOPA = 1;
	public static final int ENEMY_GOOMBA = 2;
	public static final int ENEMY_SPIKY = 3;
	public static final int ENEMY_FLOWER = 4;

	private static float GROUND_INERTIA = 0.89f;

	private boolean onGround = false;

	protected final LevelScene world;
	private final boolean avoidCliffs;
	private final boolean winged;
	private final int type;

	private int facing;

	public WalkingEnemySimulator(LevelScene world, float x, float y, float xa, float ya, int type, int kind, boolean winged) {
		super(kind, 4, (type > 1) ? 12 : 24);

		this.world = world;
		this.x = x;
		this.y = y;
		this.xa = xa;
		this.ya = ya;
		this.winged = winged;
		this.type = type;

		avoidCliffs = (type == Enemy.ENEMY_RED_KOOPA);

		this.facing = (xa >= 0) ? 1 : -1;
	}

	@Override
	protected void move() {
		float sideWaysSpeed = 1.75f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		if (!move(xa, 0))
			facing = -facing;
		onGround = false;
		move(0, ya);

		ya *= winged ? 0.95f : 0.85f;
		xa *= GROUND_INERTIA;

		if (!onGround) {
			if (winged) {
				ya += 0.6f;
			} else {
				ya += 2;
			}
		} else if (winged) {
			ya = -10;
		}
	}

	private boolean move(float xa, float ya) {
		while (xa > 8) {
			if (!move(8, 0))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(-8, 0))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(0, 8))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(0, -8))
				return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0) {
			if (isBlocking(x + xa - width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(x + xa + width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya))
				collide = true;
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya))
				collide = true;
		}
		if (ya < 0) {
			if (isBlocking(x + xa, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya))
				collide = true;
		}
		if (xa > 0) {
			if (isBlocking(x + xa + width, y + ya - height, xa, ya))
				collide = true;
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya))
				collide = true;
			if (isBlocking(x + xa + width, y + ya, xa, ya))
				collide = true;

			if (avoidCliffs && onGround && !world.level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1))
				collide = true;
		}
		if (xa < 0) {
			if (isBlocking(x + xa - width, y + ya - height, xa, ya))
				collide = true;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya))
				collide = true;
			if (isBlocking(x + xa - width, y + ya, xa, ya))
				collide = true;

			if (avoidCliffs && onGround && !world.level.isBlocking((int) ((x + xa - width) / 16), (int) ((y) / 16 + 1), xa, 1))
				collide = true;
		}

		if (collide) {
			if (xa < 0) {
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0) {
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0) {
				y = (int) ((y - height) / 16) * 16 + height;
				this.ya = 0;
			}
			if (ya > 0) {
				y = (int) (y / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		} else {
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(float _x, float _y, float xa, float ya) {
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) {
			return false;
		}

		return world.level.isBlocking(x, y, xa, ya);
	}

	@Override
	public boolean collideCheck(float marioX, float marioY, float marioHeight, int time) {
		final Point2D.Float enemyPos = getPositionAtTime(time);
		final float enemyX = enemyPos.x;
		final float enemyY = enemyPos.y;

		final float xMarioD = marioX - enemyX;
		final float yMarioD = marioY - enemyY;

		return (xMarioD > -width * 2 - 4 && xMarioD < width * 2 + 4 && yMarioD > -height && yMarioD < marioHeight);
	}

	@Override
	public EnemySimulator copy() {
		WalkingEnemySimulator copy = new WalkingEnemySimulator(world, x, y, xa, ya, type, kind, winged);
		copy.x = x;
		copy.y = y;
		copy.xa = xa;
		copy.ya = ya;
		copy.onGround = onGround;
		// copy.positionsIndexOffset = positionsIndexOffset;
		Point2D.Float currentPosition = getCurrentPosition();
		copy.insertPosition(currentPosition.x, currentPosition.y);

		return copy;
	}
}