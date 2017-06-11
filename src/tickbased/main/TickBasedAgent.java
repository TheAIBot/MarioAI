package tickbased.main;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;
import tickbased.game.world.LevelScene;
import tickbased.search.MarioAction;
import tickbased.search.Node;
import tickbased.search.TickProblem;

public class TickBasedAgent implements Agent {
	protected String name = "TickBasedAgent";
	boolean[] action;
	
	AStarTickBased aStar;
	TickProblem problem;
	
	List<Action> plan = new ArrayList<Action>();
	
	
	public TickBasedAgent() {
		reset();
	}

	public boolean[] getAction(Environment observation) {
		long startTime = System.currentTimeMillis();
		problem.updateLevel(observation);
		
		if (plan.size() == 0) {
			problem.states.clear();
			plan = aStar.runAStar(problem, startTime);
		}
		
		if (plan == null || plan.size() == 0) {
	        return action; // empty action
		}
		
		return ((MarioAction) plan.remove(0)).action;
		
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
		problem.worldScene = problem.tentativeScene = levelScene;
		
		problem.initialState = new Node(levelScene, 0);
		problem.goalState = new Node(levelScene, 0);
		((Node) problem.goalState).x = ((Node) problem.initialState).x + TickProblem.SCREEN_WIDTH / 2 * 16;
		((Node) problem.goalState).y = 0; // top of screen
	}
}
