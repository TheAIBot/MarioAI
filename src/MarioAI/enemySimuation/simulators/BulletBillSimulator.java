package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;

public class BulletBillSimulator extends EnemySimulator
{
    private int facing;

    public BulletBillSimulator(float x, float y, int dir, int kind)
    {
    	super(kind, 4, 12);
        this.x = x;
        this.y = y;
        
        this.facing = dir;
    }

    @Override
    protected void move()
    {
        final float sideWaysSpeed = 4f;

        xa = facing * sideWaysSpeed;
        x += xa;
    }
    
    @Override
    public boolean collideCheck(float enemyX, float enemyY, float marioX, float marioY, float marioHeight)
    {    	
        final float xMarioD = marioX - enemyX;
        final float yMarioD = marioY - enemyY;
        
        return (xMarioD > -16 && 
        		xMarioD < 16 && 
        		yMarioD > -height && 
        		yMarioD < marioHeight);
    }

	@Override
	public EnemySimulator copy() {
		EnemySimulator copy = new BulletBillSimulator(x, y, facing, kind);
		copy.x = x;
		copy.y = y;
		copy.xa = xa;
		copy.ya = ya;
		//copy.positionsIndexOffset = positionsIndexOffset;
		Point2D.Float currentPosition = getCurrentPosition();
		copy.insertPosition(currentPosition.x, currentPosition.y);
		
		return copy;
	}
}