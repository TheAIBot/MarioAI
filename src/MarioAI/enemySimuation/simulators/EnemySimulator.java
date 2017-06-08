package MarioAI.enemySimuation.simulators;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.text.DefaultEditorKit.CopyAction;

public abstract class EnemySimulator {
    protected float x;
    protected float y;
    protected float xa;
    protected float ya;
    protected final int widthInPixels;
    protected final int heightInPixels;
    protected final int kind;
    private final ArrayList<Point2D.Float> positionAtTime = new ArrayList<Point2D.Float>(); 
    protected int positionsIndexOffset = 0;
    private final Object createPositionsLock = new Object();
	
    public EnemySimulator(int kind, int widthInPixels, int heightInPixels) {
    	this.kind = kind;
    	this.widthInPixels = widthInPixels;
    	this.heightInPixels = heightInPixels;
    }
    
    protected abstract void move();
    
    public abstract EnemySimulator copy();
	
    public int getKind() {
    	return kind;
    }

    public void moveTimeForward() {
    	positionsIndexOffset++;
    }
    
    public void moveTimeBackwards() {
    	if (positionsIndexOffset == 0) {
			throw new Error("positionsIndexOffset can't be less than 0");
		}
    	positionsIndexOffset--;
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
    	if (positionAtTime.size() - positionsIndexOffset <= time) {
			//synchronized (createPositionsLock) {
		    	while (positionAtTime.size() - positionsIndexOffset <= time) {
		    		moveEnemy();
				}
			//}
		}
    	return positionAtTime.get(time + positionsIndexOffset);
    }
    
    public int getWidthInPixels() {
    	return widthInPixels;
    }
    
    public int getHeightInPixels() {
    	return heightInPixels;
    }
}
