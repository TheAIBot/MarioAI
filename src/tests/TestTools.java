package tests;

import java.io.DataInputStream;
import java.io.FileInputStream;

import org.junit.Assert;

import MarioAI.FastAndFurious;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.mario.simulation.BasicSimulator;
import ch.idsia.mario.simulation.Simulation;
import ch.idsia.mario.simulation.SimulationOptions;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.tools.LOGGER;
import ch.idsia.tools.ToolsConfigurator;

public class TestTools {

	public static void runOneTick(Environment observation)
	{
		((MarioComponent)observation).runOneTick();
	}
	
	public static void runWholeLevel(Environment observation)
	{
		while(((MarioComponent)observation).runOneTick() == Mario.STATUS_RUNNING);
	}
	
	public static Environment loadLevel(String filepath)
	{
		Level level = null;
		try {
			level = Level.load(new DataInputStream(new FileInputStream(filepath)));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			return null;
		}
		
		FastAndFurious agent = new FastAndFurious();
		
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setAgent(agent);
        options.setMaxFPS(false);
        options.setVisualization(false);
        options.setNumberOfTrials(1);
        options.setMatlabFileName("");
        options.setLevelRandSeed(421);
        options.setLevelDifficulty(-1);
        
        Evaluator evaluator = new Evaluator(options);
        evaluator.init(options);
        
        SimulationOptions simulationOptions = options.getSimulationOptionsCopy();
        
        ToolsConfigurator.CreateMarioComponentFrame();
        
        GlobalOptions.VisualizationOn = simulationOptions.isVisualization();
        
        MarioComponent marioComponent = new MarioComponent(320, 240);
        marioComponent.initNoGraphics();
        
        Mario.resetStatic(simulationOptions.getMarioMode());        
        agent.reset();
        marioComponent.setAgent(agent);
        marioComponent.setZLevelScene(simulationOptions.getZLevelMap());
        marioComponent.setZLevelEnemies(simulationOptions.getZLevelEnemies());
        marioComponent.startLevel(level);
        /*marioComponent.startLevel(simulationOptions.getLevelRandSeed(), 
        		simulationOptions.getLevelDifficulty(), 
        		simulationOptions.getLevelType(), 
        		simulationOptions.getLevelLength(),
        		simulationOptions.getTimeLimit());
        		*/
        marioComponent.setPaused(simulationOptions.isPauseWorld());
        marioComponent.setZLevelEnemies(simulationOptions.getZLevelEnemies());
        marioComponent.setZLevelScene(simulationOptions.getZLevelMap());
        marioComponent.setMarioInvulnerable(simulationOptions.isMarioInvulnerable());
        
        return (Environment)marioComponent;
	}
	
}
