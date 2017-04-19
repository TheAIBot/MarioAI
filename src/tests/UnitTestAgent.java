package tests;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class UnitTestAgent implements Agent
{
    public boolean action[] = new boolean[Environment.numberOfButtons];

    public void reset() {
    }

    public boolean[] getAction(Environment observation) {
        return action;
    }

    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {
    	return "";    
    }

    public void setName(String Name) {
    }
}