package MarioAI.astar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchNode implements Comparable<SearchNode> {
	public final float SCORE_MULTIPLIER = 128;
	public State state;
	public Action action; // action peformed to get to this node
	public SearchNode parent;
	
	//public HashMap<Integer, Node> neighborMap = new HashMap<Integer, Node>();
	public List<SearchNode> neighbors = new ArrayList<SearchNode>();
	public int hash;
	
	public double gScore; // g(n) for current node - cost of path from start node to this node
	public double fScore; // f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	
	public SearchNode(State state) {
		this.hash = state.id;
	}

	public List<SearchNode> getNeighbors() {
		return neighbors;
	}
	
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof SearchNode) {
			SearchNode bb = (SearchNode) b;
			return bb.hashCode() == hashCode();
		} else {
			return false;
		}
	}

	public int hashCode() {
		return hash;
	}
	
	public int compareTo(SearchNode o) {
		// TODO Auto-generated method stub
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}

}
