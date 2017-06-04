package tickbased.search;

import tickbased.main.Action;
import tickbased.main.State;

public class MarioAction implements Action {
	public final State parent;
	public int hash;
	
	public boolean[] action = new boolean[5]; // action executed
	
	public MarioAction(Node parent, boolean[] action) {
		this.parent = parent;
		this.action = action;
	}
	
}
