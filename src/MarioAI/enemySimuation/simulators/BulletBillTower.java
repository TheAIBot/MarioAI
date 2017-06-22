package MarioAI.enemySimuation.simulators;

import java.awt.Point;

import MarioAI.World;

public class BulletBillTower {
	private static final int TICKS_PER_SPAWN = 100;
	private static final float BULLET_SPEED = 4;
	private static final int HEIGHT = 12;
	private final int yPos;
	private int ticksUntilFirstSpawn;
	public final Point towerPos;
	
	public BulletBillTower(Point towerPos, int tick) {
		this.towerPos = towerPos;
		this.ticksUntilFirstSpawn = -(TICKS_PER_SPAWN - ((tick - towerPos.x * 2) % TICKS_PER_SPAWN));
		
		//from game source code
		this.yPos = towerPos.y * 16 + 15;
	}

	public void update() {
		ticksUntilFirstSpawn++;
		System.out.println(" Tick until spawn = "+ ticksUntilFirstSpawn);
	}

	public boolean collideCheck(float marioX, float marioY, float marioHeight, int time) {
		if (ticksUntilFirstSpawn < 0 && time < Math.abs(ticksUntilFirstSpawn)) {
			return false;
		}

		int dir = 0;
		if (towerPos.x * World.PIXELS_PER_BLOCK + (World.PIXELS_PER_BLOCK / 2) > marioX + World.PIXELS_PER_BLOCK) {
			dir = -1;
		}
		if (towerPos.x * World.PIXELS_PER_BLOCK + (World.PIXELS_PER_BLOCK / 2) < marioX - World.PIXELS_PER_BLOCK) {
			dir = 1;
		}
    	if (dir != 0) {
    		final float directionOffset = towerPos.x * World.PIXELS_PER_BLOCK + (World.PIXELS_PER_BLOCK / 2) + dir * (World.PIXELS_PER_BLOCK / 2);
    		final int correctTime = time + ticksUntilFirstSpawn;
    		int timeOffset = 0;
    		
    		do {
            	final float enemyX = dir * BULLET_SPEED * (correctTime - timeOffset) + directionOffset;
            	final float enemyY = yPos;
            	
                final float xMarioD = marioX - enemyX;
                final float yMarioD = marioY - enemyY;
                
                if (xMarioD > -World.PIXELS_PER_BLOCK && 
            		xMarioD < World.PIXELS_PER_BLOCK && 
            		yMarioD > -HEIGHT && 
            		yMarioD < marioHeight) {
					return true;
				}

				timeOffset += TICKS_PER_SPAWN;
			} while (correctTime - timeOffset >= 0);
		}

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof BulletBillTower) {
			final BulletBillTower otherTower = (BulletBillTower) obj;
			return otherTower.towerPos.x == towerPos.x && otherTower.towerPos.y == towerPos.y;
		}

		return false;

	}
}
