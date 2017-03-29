package ch.idsia.scenarios;

import MarioAI.FastAndFurious;
import ch.idsia.ai.agents.Agent;
<<<<<<< HEAD
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
=======
>>>>>>> refs/remotes/origin/GraphingOfJustice
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import tests.TestTools;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */
public class Play {

    public static void main(String[] args) {
        boolean loadLevel = true;
        if (loadLevel) {
            Agent controller = new FastAndFurious();
        	//Agent controller = new HumanKeyboardAgent();
<<<<<<< HEAD
            //Environment observation = TestTools.loadLevel("jumpLevels/4Width.lvl", controller, true);
            Environment observation = TestTools.loadLevel("flat.lvl", controller, true);
=======
            Environment observation = TestTools.loadLevel("TestAStarJump.lvl", controller, true);
            //Environment observation = TestTools.loadLevel("flatWithBump.lvl", controller, true);
>>>>>>> refs/remotes/origin/GraphingOfJustice
            TestTools.runWholeLevel(observation);
		} else {
	        Agent controller = new FastAndFurious();
	        
	        EvaluationOptions options = new CmdLineOptions(new String[0]);
	        options.setAgent(controller);
	        Task task = new ProgressTask(options);
	        options.setMaxFPS(false);
	        options.setVisualization(true);
	        options.setNumberOfTrials(1);
	        options.setMatlabFileName("");
<<<<<<< HEAD
	        options.setLevelRandSeed(2);
=======
	        options.setLevelRandSeed(3);
	        //options.setLevelRandSeed(2);
>>>>>>> refs/remotes/origin/GraphingOfJustice
	        //options.setLevelRandSeed(41);
	        //options.setLevelRandSeed(42);
	        //options.setLevelRandSeed(650);
	        //options.setLevelRandSeed(666);
	        //options.setLevelRandSeed(42243);
	        //options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
	        
	        //options.setLevelRandSeed(42243);(*) Includes a missing feature.
	        options.setLevelDifficulty(-1);
	        task.setOptions(options);

	        System.out.println ("Score: " + task.evaluate (controller)[0]);
			
		}
    }
}
