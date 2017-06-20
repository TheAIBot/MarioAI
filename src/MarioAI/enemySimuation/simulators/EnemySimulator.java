package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Super class that each enemy simulation extends
 * @author Andreas Gramstrup
 *
 */
public abstract class EnemySimulator {
    protected float x;
    protected float y;
    protected float xa;
    protected float ya;
    protected final int width;
    protected final int height;
    protected final int kind;
    private int positionTimeOffsetIndex = 0;
    //Contains the position of the enemy for all the requested times so they don't have
    //to be recalculated. index 0 is the enemy's current position
    private final ArrayList<Point2D.Float> positionAtTime = new ArrayList<Point2D.Float>(); 
	
    public EnemySimulator(int kind, int width, int height) {
    	this.kind = kind;
    	this.width = width;
    	this.height = height;
    }
    
    /**
     * Moves the enemy
     */
    protected abstract void move();
    
    /**
     * Collision check between the enemy and mario
     * @param enemyX
     * @param enemyY
     * @param marioX
     * @param marioY
     * @param marioHeight
     * @return
     */
    public abstract boolean collideCheck(float marioX, float marioY, float marioHeight, int time);
    
    /**
     * Returns a copy of this simulation
     * @return
     */
    public abstract EnemySimulator copy();
	
    public abstract int timeOffset();
    
    public int getKind() {
    	return kind;
    }

    /**
     * Moves the enemys position forward one tick
     */
    public void moveTimeForward() {
    	positionTimeOffsetIndex++;
    }
    
    public void moveEnemy() {
    	move();
    	positionAtTime.add(new Point2D.Float(x, y));
    }
        
    public void setX(float x) {
    	this.x = x;
    }
    
    public void setY(float y) {
    	this.y = y;
    }

    public Point2D.Float getCurrentPosition() {
    	return getPositionAtTime(0);
    }
    
    /**
     * Returns the enemys position at a given time
     * @param time
     * @return
     */
    public synchronized Point2D.Float getPositionAtTime(int time) {
    	while (positionAtTime.size() - positionTimeOffsetIndex <= time) {
    		moveEnemy();
		}
    	
    	return positionAtTime.get(time + positionTimeOffsetIndex);
    }
    
    public float getWidth() {
    	return width;
    }
    
    public float getHeight() {
    	return height;
    }
    
    public void insertPosition(float x, float y) {
    	positionAtTime.add(new Point2D.Float(x, y));
    }
    
    public void onlyForwardAccelerationByOne(float x, float y) {
    	move();
    	this.x = x;
    	this.y = y;
    }
}
