package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;

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