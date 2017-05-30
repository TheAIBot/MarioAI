package MarioAI.enemySimuation.simulators;


public class BulletBillSimulator extends EnemySimulator
{
    private int facing;

    public BulletBillSimulator(float x, float y, int dir, int kind)
    {
    	super(kind, 16, 14);
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
}