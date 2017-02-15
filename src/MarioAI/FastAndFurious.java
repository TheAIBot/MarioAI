package MarioAI;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class FastAndFurious implements Agent {

	private static final String name = "THE ULTIME AND SUPREME OVERLORD THAT BRINGS DEATH AND DESTRUCTION";

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		//observation.getMarioFloatPos()
		
		
		return new boolean[Environment.numberOfButtons];
	}

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
	}
}
