package tickbased.search;

import tickbased.main.Action;
import tickbased.main.State;

public class DirectedEdge implements Action {
	public final String label;
	public final State source, target;
	public int hash;
	
	public DirectedEdge(String label, Vertex source, Vertex target) {
		this.label = label;
		this.source = source;
		this.target = target;
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof DirectedEdge) {
			return this.hashCode() == o.hashCode();
		}
		return false;
	}
	
	public String toString() {
		return source.toString() + " " + label + " " + target.toString();
	}

	public int hashCode() {
		return hash;
	}

}
