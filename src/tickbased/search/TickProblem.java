package tickbased.search;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.mario.engine.sprites.Mario;
import tickbased.game.world.LevelScene;
import tickbased.main.Action;
import tickbased.main.Problem;
import tickbased.main.SearchNode;
import tickbased.main.State;

public class TickProblem extends Problem {

	private static final float MAX_RIGHT = 200;

	@Override
	public List<Action> actions(State state) {
		Node node = (Node) state;
		
		List<Action> actions = new ArrayList<Action>();

		// move right
		actions.add(new MarioAction(node, createAction(false, true, false, false, true)));
		actions.add(new MarioAction(node, createAction(false, true, false, true, true)));
		actions.add(new MarioAction(node, createAction(false, true, false, false, false)));
		actions.add(new MarioAction(node, createAction(false, true, false, true, false)));

		// move left
		actions.add(new MarioAction(node, createAction(true, false, false, false, false)));
		actions.add(new MarioAction(node, createAction(true, false, false, true, false)));
		actions.add(new MarioAction(node, createAction(true, false, false, false, true)));
		actions.add(new MarioAction(node, createAction(true, false, false, true, true)));

		// jump straight up
		actions.add(new MarioAction(node, createAction(false, false, false, true, true)));
		
		// stand still
		actions.add(new MarioAction(node, createAction(false, false, false, true, false)));
		
		return actions;
	}

	/**
	 * Auxiliary method
	 * @param left
	 * @param right
	 * @param down
	 * @param jump
	 * @param speed
	 * @return
	 */
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
	public SearchNode childNode(SearchNode searchNode, Action action) {
		Node node = (Node) searchNode.state;
		MarioAction marioAction = (MarioAction) action;
		
		// Clone the levelScene and update it by executing the given action
		LevelScene levelScene = new LevelScene(node.levelScene);
		levelScene.mario.setKeys(marioAction.action);
		levelScene.tick();
		
		State newNode = new Node(levelScene);
		states.add(newNode);
		
		SearchNode sn = new SearchNode(newNode, action);
		return sn;
	}

	@Override
	public double pathCost(SearchNode n1, SearchNode n2) {
		return 1;
	}

	@Override
	public double heuristicFunction(SearchNode searchNode, SearchNode goal) {
		// TODO note: goal should be an auxiliary node far to the right
		return Math.sqrt(Math.pow(((Node) goal.state).x, 2) + Math.pow(((Node) searchNode.state).x, 2));
	}

	/**
	 * Reached a location as far to the right as possible
	 * This will actually never occur, since there is not enough time nor information to complete this task
	 */
	@Override
	public boolean goalTest(State node) {
		return ((Node) node).x > MAX_RIGHT;
	}

}
