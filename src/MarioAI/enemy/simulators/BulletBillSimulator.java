package MarioAI.enemy.simulators;


public class BulletBillSimulator extends EnemySimulator
{
    public int facing;

    public BulletBillSimulator(float x, float y, int dir, int kind)
    {
    	super(kind);
        this.x = x;
        this.y = y;

        facing = 0;
        ya = -5;
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