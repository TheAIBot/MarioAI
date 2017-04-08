package MarioAI.enemy;

import java.awt.Point;


public class BulletBillSimulator extends EnemySimulator
{
    public int facing;

    public BulletBillSimulator(float x, float y, int dir)
    {
        this.x = x;
        this.y = y;

        facing = 0;
        ya = -5;
        this.facing = dir;
    }

    @Override
    public void move()
    {
        final float sideWaysSpeed = 4f;

        xa = facing * sideWaysSpeed;
        x += xa;
        
        positionAtTime.add(new Point((int)x, (int)y));
    }         
}