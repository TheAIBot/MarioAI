package MarioAI.enemy;

import ch.idsia.mario.engine.LevelScene;

public class FlowerEnemy extends WalkingEnemySimulator
{
    private int yStart;
    private int jumpTime = 0;
    
    public FlowerEnemy(LevelScene world, int x, int y, int mapX, int mapY)
    {
        //super(world, x, y, 1, ENEMY_SPIKY, false, mapX, mapY);
    	super(world, x, y, 0, 0, WalkingEnemySimulator.ENEMY_SPIKY, 7, false);
        
        yStart = y;
        ya = -8;
        
        this.y-=1;
        
        for (int i=0; i<4; i++)
        {
            move();
        }
    }

    @Override
    public void move()
    {
    	if (y>=yStart)
        {
            y = yStart;

            int xd = (int)(Math.abs(world.mario.x-x));
            jumpTime++;
            if (jumpTime>40 && xd>24)
            {
                ya = -8;
            }
            else
            {
                ya = 0;
            }
        }
        else
        {
            jumpTime = 0;
        }
        
        y+=ya;
        ya*=0.9;
        ya+=0.1f;
    }
}