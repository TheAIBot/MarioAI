package ch.idsia.scenarios;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import tickbased.main.TickBasedAgent;

public class Play {

	public static void main(String[] args) {
        Agent controller = new TickBasedAgent();
        //HumanKeyboardAgent controller = new HumanKeyboardAgent();
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setAgent(controller);
        Task task = new ProgressTask(options);
        options.setMaxFPS(false);
        options.setVisualization(true);
        options.setNumberOfTrials(1);
        options.setMatlabFileName("");
        int seed = (int) (Math.random() * Integer.MAX_VALUE);
        options.setLevelRandSeed(seed);
        System.out.println("Seed = " + seed);
        options.setLevelDifficulty(0);
	}
}
