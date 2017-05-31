package tickbased.search;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.mario.engine.sprites.Mario;
import tickbased.main.Action;
import tickbased.main.Problem;
import tickbased.main.SearchNode;
import tickbased.main.State;

public class TickProblem extends Problem {

	@Override
	public List<Action> actions(State state) {
		List<boolean[]> actions = new ArrayList<boolean[]>();

		// move right
		actions.add(createAction(false, true, false, false, true));
		actions.add(createAction(false, true, false, true, true));
		actions.add(createAction(false, true, false, false, false));
		actions.add(createAction(false, true, false, true, false));

		// move left
		actions.add(createAction(true, false, false, false, false));
		actions.add(createAction(true, false, false, true, false));
		actions.add(createAction(true, false, false, false, true));
		actions.add(createAction(true, false, false, true, true));

		// jump straight up
		actions.add(createAction(false, false, false, true, true));
		
		// stand still
		actions.add(createAction(false, false, false, true, false));
		
	}

	private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed) {
		boolean[] action = new boolean[5];
		action[Mario.KEY_DOWN] = down;
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_SPEED] = speed;
		return action;
	}

	@Override
	public SearchNode childNode(SearchNode node, Action action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double pathCost(SearchNode n1, SearchNode n2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double heuristicFunction(SearchNode node, SearchNode goal) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean goalTest(State goal) {
		// TODO Auto-generated method stub
		return false;
	}

}
