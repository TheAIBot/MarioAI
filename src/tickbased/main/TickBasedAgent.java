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
	TickProblem problem;
	public final static int MAX_ALLOWED_RUN_TIME = 42;
	
	List<Action> plan;
	Iterator<Action> iter;
	boolean finishedNewRun = true;
	
	public TickBasedAgent() {
		reset();
	}

	public boolean[] getAction(Environment observation) {
		long startTime = System.currentTimeMillis();
		updateLevel(observation);
		
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
		
		aStar.runAStar(problem);
		
		//problem.timeLeft = MAX_ALLOWED_RUN_TIME - (int)(System.currentTimeMillis() - startTime);
		
		
		return ((MarioAction) (iter.next())).action;
	}
	
	private void updateLevel(Environment observation) {
		byte[][] worldScene = observation.getLevelSceneObservationZ(0);
    	float[] enemyPositions = observation.getEnemiesFloatPos();
		float[] marioPos = observation.getMarioFloatPos();
		
		
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
		aStar = new AStarTickBased();
		problem = new TickProblem();
		plan = null;
		iter = null;
	}
}
