package tickbased.search;

import java.util.ArrayList;
import java.util.List;

import tickbased.game.LevelScene;
import tickbased.main.Action;
import tickbased.main.State;

public class Node implements State {
	
	public LevelScene world;

	public final float x, y; // coordinates for the current position in the graph
	public List<Action> marioActions = new ArrayList<Action>(); //An arrayList (i.e. a dynamic array/table) is used rather than a linked list)
	public int hash;

	public Node(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void addEdge(MarioAction action) {
		marioActions.add(action);
	}

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
	
	public int hashCode() {
		return hash;
	}
}
