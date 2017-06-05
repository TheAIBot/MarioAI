package tickbased.main;

public class SearchNode implements Comparable<SearchNode> {
	public State state;
	public Action action; // action peformed to get to this node
	public SearchNode parent;
	
	public int hash;
	
	public double gScore; // path cost g(n) for current node - cost of path from start node to this node
	public double fScore; // fitness f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	
	public SearchNode(State state) {
		this.state = state;
//		this.hash = state.hashCode();
	}
	
	public SearchNode(State state, Action action) {
		this.state = state;
		this.action = action;
		this.hash = state.hashCode();
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof SearchNode) {
			SearchNode oo = (SearchNode) o;
			return oo.hashCode() == hashCode();
		}
		return false;
	}

//	public int hashCode() {
//		return hash;
//	}
	
	public String toString() {
		return state.toString();
	}
	
	@Override
	public int compareTo(SearchNode o) {
		return (int) (this.fScore - o.fScore); // Note: Changed from orignal order
	}

}
