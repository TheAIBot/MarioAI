package ch.idsia.scenarios;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import tickbased.main.TickBasedAgent;

public class Play {

	public static void main(String[] args) {
        //Agent controller = new HumanKeyboardAgent();
        Agent controller = new TickBasedAgent();
        //HumanKeyboardAgent controller = new HumanKeyboardAgent();
//        if (args.length > 0) {
//            controller = AgentsPool.load(args[0]);
//            AgentsPool.addAgent(controller);
//        }
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setAgent(controller);
        Task task = new ProgressTask(options);
        options.setMaxFPS(false);
        options.setVisualization(true);
        options.setNumberOfTrials(1);
        options.setMatlabFileName("");
        options.setLevelRandSeed(1);//(int) (Math.random () * Integer.MAX_VALUE));
        options.setLevelDifficulty(-1);
        task.setOptions(options);
        
        System.out.println ("Score: " + task.evaluate (controller)[0]);
	}
}
