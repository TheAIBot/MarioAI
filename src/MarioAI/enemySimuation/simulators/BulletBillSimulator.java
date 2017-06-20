package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;

import ch.idsia.mario.engine.sprites.Sprite;

/**
 * Code in this class is mostly from the games source code
 * @author Andreas Gramstrup
 *
 */
public class BulletBillSimulator extends EnemySimulator
{
    private int facing;

    public BulletBillSimulator(float x, float y, int dir)
    {
    	super(Sprite.KIND_BULLET_BILL, 4, 12);
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
    public boolean collideCheck(float marioX, float marioY, float marioHeight, int time)
    {    	
    	final Point2D.Float enemyPos = getPositionAtTime(time);
    	final float enemyX = enemyPos.x;
    	final float enemyY = enemyPos.y;
    	
        final float xMarioD = marioX - enemyX;
        final float yMarioD = marioY - enemyY;
        
        return (xMarioD > -16 && 
        		xMarioD < 16 && 
        		yMarioD > -height && 
        		yMarioD < marioHeight);
    }

	@Override
	public EnemySimulator copy() {
		EnemySimulator copy = new BulletBillSimulator(x, y, facing);
		copy.x = x;
		copy.y = y;
		copy.xa = xa;
		copy.ya = ya;
		//copy.positionsIndexOffset = positionsIndexOffset;
		Point2D.Float currentPosition = getCurrentPosition();
		copy.insertPosition(currentPosition.x, currentPosition.y);
		
		return copy;
	}

	@Override
	public int timeOffset() {
		return 1;
	}
}