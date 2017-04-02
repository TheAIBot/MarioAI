package MarioAI.astar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Problem {
	
	public State initialState;
	public State goalState;
	private Map<Integer, State> statesMap = new HashMap<Integer, State>();

	public void defineProblem(List<State> states, State initialState) {
		for (State state : states) {
			statesMap.put(state.id, state);
		}
		this.initialState = initialState;
	}
	
	public State getStateById(int id) {
		return statesMap.get(id);
	}
	
	public abstract List<Action> actions(State state);

	public abstract SearchNode childNode(SearchNode node, Action action);
	
	
	public abstract double pathCost(SearchNode n1, SearchNode n2);

	/**
	 * @param start
	 * @param goal
	 * @return the estimated cost of the cheapest path from current node to goal node
	 */
	public abstract double heuristicFunction(final SearchNode node, final SearchNode goal);
	
	public abstract boolean goalTest(State goal);
	
}
