package MarioAI.enemy.simulators;

import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Sprite;

public class FlowerEnemy extends EnemySimulator
{
	private LevelScene world;
    private int yStart;
    private int jumpTime = 0;
    
    public FlowerEnemy(LevelScene world, float x, float y, float ya)
    {
    	super(Sprite.KIND_ENEMY_FLOWER);
        
    	this.world = world;
    	//the magic number is how much
    	//the flower has moved up since it was spawned
    	//and added as a simulator
        yStart = (int)Math.round(y + 52.1752f);
        this.ya = ya;
    }

    @Override
    protected void move()
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