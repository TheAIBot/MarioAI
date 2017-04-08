package MarioAI.enemy;

import java.awt.Point;
import java.util.ArrayList;

public abstract class EnemySimulator {
    protected float x;
    protected float y;
    protected float xa;
    protected float ya;
    protected int type;
    protected int kind;
    protected ArrayList<Point> positionAtTime = new ArrayList<Point>(); 
	
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
