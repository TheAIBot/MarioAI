package MarioAI.enemy.simulators;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public abstract class EnemySimulator {
    protected float x;
    protected float y;
    protected float xa;
    protected float ya;
    protected final int kind;
    protected final ArrayList<Point2D.Float> positionAtTime = new ArrayList<Point2D.Float>(); 
	
    public EnemySimulator(int kind) {
    	this.kind = kind;
    }
    
    protected abstract void move();
	
    public int getKind() {
    	return kind;
    }
    
    public float getX() {
    	return x;
    }
    
    public float getY() {
    	return y;
    }

    public void moveTime() {
    	if (positionAtTime.size() > 0) {
    		positionAtTime.remove(0);
		}
    }
    
    public void moveEnemy() {
    	move();
    	positionAtTime.add(new Point2D.Float(x, y));
    }
    
    public Point2D.Float getPositionAtTime(int time) {
    	while (positionAtTime.size() <= time) {
    		moveEnemy();
		}
    	return positionAtTime.get(time);
    }
}
