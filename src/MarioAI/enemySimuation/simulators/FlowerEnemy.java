package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import MarioAI.World;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Sprite;

/**
 * Code in this class is mostly from the games source code
 * @author Andreas Gramstrup
 *
 */
public class FlowerEnemy extends EnemySimulator
{
	private static final ArrayList<FlowerState> flowerStates = new ArrayList<FlowerState>();
	
	private final LevelScene world;
    private final int yStart;
    private int jumpTime = 0;
    
    public FlowerEnemy(LevelScene world, float x, float y, float ya) {
    	this(world, x, y, ya, true);
    }
    
    private FlowerEnemy(LevelScene world, float x, float y, float ya, boolean fromFlowerState)
    {
    	super(Sprite.KIND_ENEMY_FLOWER, 4, 12);
        
    	this.world = world;
    	this.x = x;
    	this.y = y;
    	this.ya = ya;
    	if (fromFlowerState) {
    		final FlowerState state = getFlowerState(ya);
        	this.yStart = (int) Math.round(y - state.y);
		}
    	else {
    		this.yStart = (int) y;
    	}
    }
    
    private FlowerState getFlowerState(float ya) {
    	FlowerState bestMatch = flowerStates.get(0);
    	float yaDiff = Math.abs(ya - bestMatch.ya);
    	float bestDiff = yaDiff;
    	
    	for (int i = 1; i < flowerStates.size(); i++) {
    		FlowerState state = flowerStates.get(i);
    		yaDiff = Math.abs(ya - state.ya);
    		if (yaDiff < bestDiff) {
				bestMatch = state;
				bestDiff = yaDiff;
			}
		}
    	
    	return bestMatch;
    }
    
    public static void createStateTable(LevelScene levelScene) {
    	final FlowerEnemy enemy = new FlowerEnemy(levelScene, 0, 0, -8, false);
        
        enemy.setY(-1);
        
        do {
        	enemy.move();
        	flowerStates.add(new FlowerState(enemy.jumpTime, enemy.y, enemy.ya));
		} while (enemy.y != 0);
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
    
    @Override
    public boolean collideCheck(float marioX, float marioY, float marioHeight, int time)
    {
    	final Point2D.Float enemyPos = getPositionAtTime(time);
    	final float enemyX = enemyPos.x;
    	final float enemyY = enemyPos.y;
    	
        final float xMarioD = marioX - enemyX;
        final float yMarioD = marioY - enemyY;
        
        return (xMarioD > -width*2-4 && 
        		xMarioD < width*2+4 &&
        		yMarioD > -height && 
        		yMarioD < marioHeight);
    }

	@Override
	public EnemySimulator copy() {
		FlowerEnemy copy = new FlowerEnemy(world, x, y, ya, true);
		copy.x = x;
		copy.y = y;
		copy.xa = xa;
		copy.ya = ya;
		//copy.positionsIndexOffset = positionsIndexOffset;
		Point2D.Float currentPosition = getCurrentPosition();
		copy.insertPosition(currentPosition.x, currentPosition.y);
		
		copy.jumpTime = jumpTime;
		
		return copy;
	}
}