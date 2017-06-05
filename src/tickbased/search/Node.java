package tickbased.search;

import tickbased.game.world.LevelScene;
import tickbased.main.State;

public class Node implements State {
	
	public LevelScene levelScene;

	public float x, y; // coordinates for the position
//	public List<Action> marioActions = new ArrayList<Action>();
	public int hash;

	public Node(LevelScene levelScene) {
		this.levelScene = levelScene;
		this.x = levelScene.mario.x;
		this.y = levelScene.mario.y;
	}

//	public void addAction(MarioAction action) {
//		marioActions.add(action);
//	}
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Node) {
			return this.hashCode() == o.hashCode();
		}
		return false;
	}
	
//	public int hashCode() {
//		return hash;
//	}
}
