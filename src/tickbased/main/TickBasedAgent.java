package tickbased.main;

import java.util.Iterator;
import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tickbased.game.world.Level;
import tickbased.game.world.LevelScene;
import tickbased.search.MarioAction;
import tickbased.search.Node;
import tickbased.search.TickProblem;

public class TickBasedAgent implements Agent {
	protected String name = "TickBasedAgent";
	boolean[] action;
	
	AStarTickBased aStar;
	TickProblem problem;
	Iterator<Action> iter;
	
	List<Action> plan;
	
	public TickBasedAgent() {
		reset();
	}

	public boolean[] getAction(Environment observation) {
		long startTime = System.currentTimeMillis();
		problem.updateLevel(observation);
		
		if (aStar.finishedNewRun) {
			plan = aStar.runAStar(problem, startTime);
			iter = plan.iterator();
		}
		
		if (plan == null || plan.size() == 0) {
//	        action[Mario.KEY_RIGHT] = true;
//	        action[Mario.KEY_SPEED] = true;
//	        action[Mario.KEY_JUMP] = true;
	        return action;
		}
		
		return ((MarioAction) (iter.next())).action;
	}
	
	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return "TickBasedAgent";
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
		
		aStar = new AStarTickBased();
		problem = new TickProblem();
		
		LevelScene levelScene = new LevelScene();
		problem.levelScene = levelScene;
		
		problem.initialState = new Node(levelScene);
		problem.goalState = new Node(levelScene);
		((Node) problem.goalState).x = 100;
		
		plan = null;
		iter = null;
	}
}
