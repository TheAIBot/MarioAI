package tickbased.main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Problem {
	
	public Set<State> states = new HashSet<State>();
	public State initialState;
	public State goalState;
	public int timeUsed = 0;
	public final int MAX_ALLOWED_RUN_TIME = 42;
	
	public Problem() {
	}
	
	public Problem(Set<State> states) {
		this.states = states;
	}
	
	public void addState(State state) {
		states.add(state);
	}
	
	public abstract List<Action> actions(State state);
	
	public abstract SearchNode childNode(SearchNode node, Action action);
	
	public abstract double pathCost(SearchNode n1, SearchNode n2);
	
	public abstract double heuristicFunction(SearchNode node, SearchNode goal);
	
	public abstract boolean goalTest(State goal);
	
	public void setInitialState(State state) {
		this.initialState = state;
	}
	
	public void setGoalState(State state) {
		this.goalState = state;
	}
	
}
