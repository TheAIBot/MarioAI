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
import ch.idsia.mario.simulation.BasicSimulator;
import ch.idsia.mario.simulation.Simulation;
import ch.idsia.mario.simulation.SimulationOptions;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.LOGGER;

public class TestTools {

	public static Level loadLevel(String filepath)
	{
		try {
			return Level.load(new DataInputStream(new FileInputStream(filepath)));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			return null;
		}
	}
	
	public static void runLevelOneTick(Level level)
	{
		Agent agent = new FastAndFurious();
		
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setAgent(agent);
        options.setMaxFPS(false);
        options.setVisualization(true);
        options.setNumberOfTrials(1);
        options.setMatlabFileName("");
        options.setLevelRandSeed(421);
        //options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
        options.setLevelDifficulty(-1);
        
        SimulationOptions simulationOptions = options.getSimulationOptionsCopy();
        
        GlobalOptions.VisualizationOn = simulationOptions.isVisualization();
        MarioComponent marioComponent = GlobalOptions.getMarioComponent();
        
        Mario.resetStatic(simulationOptions.getMarioMode());        
        agent.reset();
        marioComponent.setAgent(agent);
        marioComponent.setZLevelScene(simulationOptions.getZLevelMap());
        marioComponent.setZLevelEnemies(simulationOptions.getZLevelEnemies());
        marioComponent.startLevel(simulationOptions.getLevelRandSeed(), simulationOptions.getLevelDifficulty()
                                 , simulationOptions.getLevelType(), simulationOptions.getLevelLength(),
                                  simulationOptions.getTimeLimit());
        marioComponent.setPaused(simulationOptions.isPauseWorld());
        marioComponent.setZLevelEnemies(simulationOptions.getZLevelEnemies());
        marioComponent.setZLevelScene(simulationOptions.getZLevelMap());
        marioComponent.setMarioInvulnerable(simulationOptions.isMarioInvulnerable());
        
        marioComponent.runOneTick();
	}
	
}
