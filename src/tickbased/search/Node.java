package tickbased.search;

import tickbased.game.world.LevelScene;
import tickbased.main.State;

public class Node implements State {
	
	public LevelScene levelScene;

	public float x, y; // coordinates for the position
//	public List<Action> marioActions = new ArrayList<Action>();
	int ticksInFuture;

	
	// TODO add ticks in future to constructor
//	public Node(LevelScene levelScene) {
//		this.levelScene = levelScene;
//		this.x = levelScene.mario.x;
//		this.y = levelScene.mario.y;
//	}
	
	public Node(LevelScene levelScene, int ticksInFuture) {
		this.levelScene = levelScene;
		this.x = levelScene.mario.x;
		this.y = levelScene.mario.y;
		this.ticksInFuture = ticksInFuture;
	}

//	public void addAction(MarioAction action) {
//		marioActions.add(action);
//	}
	
	public String toString() {
		return "(" + x + "," + y + "," + ticksInFuture + ")";
	}
	
	// TODO compare x,y and ticks in future
	public boolean equals(Object o) {
		float delta = 2f; // for now
		float timeDelta = 2f; // for now
		if (o == null) return false;
		if (o instanceof Node) {
			Node oo = (Node) o; 
			if (Math.abs(oo.x - x) < delta
				&& Math.abs(oo.y - y) < delta
				&& Math.abs(oo.ticksInFuture - ticksInFuture) < timeDelta) {
			}
		}
		return false;
	}
	
//	public int hashCode() {
//		return hash;
//	}
}
