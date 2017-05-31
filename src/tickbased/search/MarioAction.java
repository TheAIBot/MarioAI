package tickbased.search;

import tickbased.main.Action;
import tickbased.main.State;

public class MarioAction implements Action {
	public final State parent;
	public int hash;
	
	// action executed
	boolean[] action = new boolean[5];
	
	public MarioAction(Vertex parent, boolean[] action) {
		this.parent = parent;
		this.action = action;
	}
	
}
