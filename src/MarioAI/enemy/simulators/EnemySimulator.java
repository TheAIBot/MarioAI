package MarioAI.enemy.simulators;

import java.awt.Point;
import java.util.ArrayList;

public abstract class EnemySimulator {
    protected float x;
    protected float y;
    protected float xa;
    protected float ya;
    protected final int type;
    protected final int kind;
    protected final ArrayList<Point> positionAtTime = new ArrayList<Point>(); 
	
    public EnemySimulator(int type, int kind) {
    	this.type = type;
    	this.kind = kind;
    }
    
	public abstract void move();
	
    public int getKind() {
    	return kind;
    }
    
    public int getX() {
    	return (int)x;
    }
    
    public int getY() {
    	return (int)y;
    }

    public void moveTime() {
    	positionAtTime.remove(0);
    }
    
    public Point getPositionAtTime(int time) {
    	while (positionAtTime.size() <= time) {
			move();
		}
    	return positionAtTime.get(time);
    }
}
