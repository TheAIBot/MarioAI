package ch.idsia.ai.agents.ai;

import ch.idsia.mario.environments.Environment;

public class Grapher {
	
	Environment observation;
	private byte[][] currentObservation;
	
	public Grapher(Environment observation) {
		this.observation = observation;
		currentObservation = observation.getCompleteObservation();
	}
	
	public void updateLevelGraph() {
		currentObservation = this.observation.getCompleteObservation();
	}
	
}
