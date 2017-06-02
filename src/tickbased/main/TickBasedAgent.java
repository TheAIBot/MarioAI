package tickbased.main;

import java.util.Iterator;
import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tickbased.search.MarioAction;
import tickbased.search.TickProblem;

public class TickBasedAgent implements Agent {
	protected String name = "TickBasedAgent";
	
	AStarTickBased aStar;
	Problem problem;
	
	List<Action> plan;
	Iterator<Action> iter;
	
	public TickBasedAgent() {
		reset();
	}

	public boolean[] getAction(Environment observation) {
		boolean finishedNewRun = true;
		if (finishedNewRun) {
			iter = plan.iterator();
			finishedNewRun = false;
		}
		
		if (plan == null) {
			boolean[] action = new boolean[Environment.numberOfButtons];
	        action[Mario.KEY_RIGHT] = true;
	        action[Mario.KEY_SPEED] = true;
	        action[Mario.KEY_JUMP] = true;
	        return action;
		}
		
		return ((MarioAction) (iter.next())).action;
		
		//aStar.runAStar(problem);
	}
	
	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return "TickBasedAgent";
	}

	public void setName(String name) {
	}

	@Override
	public void reset() {
		aStar = new AStarTickBased();
		problem = new TickProblem();
		plan = null;
		iter = null;
	}
}
