package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.text.DefaultEditorKit.CopyAction;

public abstract class EnemySimulator {
    protected float x;
    protected float y;
    protected float xa;
    protected float ya;
    protected final int width;
    protected final int height;
    protected final int kind;
    private final ArrayList<Point2D.Float> positionAtTime = new ArrayList<Point2D.Float>(); 
	
    public EnemySimulator(int kind, int width, int height) {
    	this.kind = kind;
    	this.width = width;
    	this.height = height;
    }
    
    protected abstract void move();
    
    public abstract boolean collideCheck(float enemyX, float enemyY, float marioX, float marioY, float marioHeight);
    
    public abstract EnemySimulator copy();
	
    public int getKind() {
    	return kind;
    }

    public void moveTimeForward() {
    	if (positionAtTime.size() > 0) {
			positionAtTime.remove(0);
		}
    	else {
    		move();
    	}
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
    
    public synchronized Point2D.Float getPositionAtTime(int time) {
    	if (positionAtTime.size() <= time) {
			//synchronized (createPositionsLock) {
		    	while (positionAtTime.size() <= time) {
		    		moveEnemy();
				}
			//}
		}
    	return positionAtTime.get(time);
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
}
