package tickbased.main;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TickBasedAgent implements Agent {

	public TickBasedAgent() {
		reset();
	}

	public boolean[] getAction(Environment observation) {
		boolean[] action = new boolean[Environment.numberOfButtons];
		action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = observation.mayMarioJump() || !observation.isMarioOnGround();
		return action;
	}
	
	public void reset() {
		boolean[] action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
	}

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return "TickBasedAgent";
	}

	public void setName(String name) {
	}
}
