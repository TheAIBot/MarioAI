package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import ch.idsia.mario.engine.sprites.Enemy;

public abstract class EnemySimulator {
	protected float x;
	protected float y;
	protected float xa;
	protected float ya;
	protected final int width;
	protected final int height;
	protected final int kind;
	private final ArrayList<Point2D.Float> positionAtTime = new ArrayList<Point2D.Float>();
	protected int positionsIndexOffset = 0;

	public EnemySimulator(int kind, int width, int height) {
		this.kind = kind;
		this.width = width;
		this.height = height;
	}

	protected abstract void move();

	public abstract boolean collideCheck(float enemyX, float enemyY, float marioX, float marioY, float marioHeight);

	public abstract EnemySimulator copy();

	public int getKind() {
		return kind;
	}

	public void moveTimeForward() {
		positionsIndexOffset++;
	}

	public void moveTimeBackwards() {
		if (positionsIndexOffset == 0) {
			throw new Error("positionsIndexOffset can't be less than 0");
		}
		positionsIndexOffset--;
	}

	public void moveEnemy() {
		move();
		positionAtTime.add(new Point2D.Float(x, y));
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public Point2D.Float getCurrentPosition() {
		return getPositionAtTime(0);
	}

	public synchronized Point2D.Float getPositionAtTime(int time) {
		if (positionAtTime.size() - positionsIndexOffset <= time) {
			// synchronized (createPositionsLock) {
			while (positionAtTime.size() - positionsIndexOffset <= time) {
				moveEnemy();
			}
			// }
		}
		return positionAtTime.get(time + positionsIndexOffset);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void insertPosition(float x, float y) {
		positionAtTime.add(new Point2D.Float(x, y));
	}

	/**
	 * Based on the normal enemies code. Returns true if a stomp would
	 * happen, else false.
	 * 
	 * @param time
	 * @return
	 */
	public boolean stomp(float marioHeight, float marioX, float marioY, Point2D.Float enemyPositionInPixels, boolean movingDownwards, boolean isOrWasNotOnGround) {
		//System.out.println("Check correct values"); //Still must be done
		final float yMarioD = marioY - enemyPositionInPixels.y;
		if (this.kind != Enemy.ENEMY_SPIKY && this.kind != 12) {
			if (movingDownwards && yMarioD <= 0 && isOrWasNotOnGround) {
				System.out.println("It should stomp");
				System.out.println("Expected x,y = " + marioX + ", " + marioY);

				System.out.println(yMarioD);
				System.out.println("TODO update");
				return true;
			}
		}
		return false;
	}
}
