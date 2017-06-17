package tickbased.main;

public class SearchNode implements Comparable<SearchNode> {
	public State state;
	public Action action; // action peformed to get to this node
	public SearchNode parent;
	
	public double gScore; // path cost g(n) for current node - cost of path from start node to this node
	public double fScore; // fitness f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	
	public SearchNode(State state) {
		this.state = state;
	}
	
	public SearchNode(State state, Action action) {
		this.state = state;
		this.action = action;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof SearchNode) {
			SearchNode oo = (SearchNode) o;
			return (oo.state.equals(this.state) && oo.action.equals(this.action) && oo.parent.equals(this.parent));
		}
		return false;
	}

	public String toString() {
		return state.toString();
	}
	
	@Override
	public int compareTo(SearchNode o) {
		return (int) (this.fScore - o.fScore); // Note: Changed from orignal order
	}

}
