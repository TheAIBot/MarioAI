package MarioAI.enemySimuation.simulators;

import java.awt.Point;
import java.util.ArrayList;

import MarioAI.World;

public class BulletBillTower {
	private static final int TICKS_PER_SPAWN = 100;
	private static final int height = 12;
	public final Point towerPos;
	private final int yPos;
	private int ticksUntilFirstSpawn;
	private static final float[] bulletPositions = new float[TICKS_PER_SPAWN];
	private static boolean createdPositions = false;
	
	
	public BulletBillTower(Point towerPos, int tick) {
		this.towerPos = towerPos;
		this.ticksUntilFirstSpawn = (tick - towerPos.x * 2) % TICKS_PER_SPAWN;
		
		//from game source code
		this.yPos = towerPos.y * 16 + 15;
		
		if (!createdPositions) {
			createPositions();
			createdPositions = true;
		}
	}
	
	public void createPositions() {
        final float sideWaysSpeed = 4f;
		for (int i = 0; i < TICKS_PER_SPAWN; i++) {
			bulletPositions[0] = i * sideWaysSpeed;
		}
	}
	
	public void update() {
		ticksUntilFirstSpawn++;
	}
	
    public boolean collideCheck(float marioX, float marioY, float marioHeight, int time)
    {
    	int dir = 0;
		if (towerPos.x * World.PIXELS_PER_BLOCK + (World.PIXELS_PER_BLOCK / 2) > marioX + World.PIXELS_PER_BLOCK) {
			dir = -1;
		}
		if (towerPos.x * World.PIXELS_PER_BLOCK + (World.PIXELS_PER_BLOCK / 2) < marioX - World.PIXELS_PER_BLOCK) {
			dir = 1;
		}
    	if (dir != 0) {
    		final int correctTime = time - ticksUntilFirstSpawn;
    		final float directionOffset = towerPos.x * World.PIXELS_PER_BLOCK + (World.PIXELS_PER_BLOCK / 2) + dir * (World.PIXELS_PER_BLOCK / 2);
        	final float enemyX = bulletPositions[correctTime] + directionOffset;
        	final float enemyY = yPos;
        	
            final float xMarioD = marioX - enemyX;
            final float yMarioD = marioY - enemyY;
            
            return (xMarioD > -16 && 
            		xMarioD < 16 && 
            		yMarioD > -height && 
            		yMarioD < marioHeight);	
		}
    	
    	return false;
    }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof BulletBillTower) {
			final BulletBillTower otherTower = (BulletBillTower)obj;
			return otherTower.towerPos.x == towerPos.x &&
				   otherTower.towerPos.y == towerPos.y;
		}
		
		return false;
		
	}
}
