package tests;

import java.io.DataInputStream;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import MarioAI.FastAndFurious;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

public class TestTools {

	public static final int LEVEL_INIT_TICKS = 30;

	public static void runOneTick(Environment observation) {
		((MarioComponent) observation).runOneTick();
	}

	public static void runWholeLevel(Environment observation) {
		//while (((MarioComponent) observation).runOneTick() == Mario.STATUS_RUNNING) { }
		((MarioComponent) observation).run1(0, 1);
	}
	
	public static byte[][] getLevelMap(Environment observation)
	{
		return ((MarioComponent) observation).getLevel().map;
	}

	public static Environment loadLevel(String filepath, Agent agent) {
		Level level = null;
		try {
			level = Level.load(new DataInputStream(new FileInputStream(filepath)));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			return null;
		}
		
		EvaluationOptions options = new CmdLineOptions(new String[0]);
		options.setAgent(agent);
		Task task = new ProgressTask(options);
		options.setMaxFPS(false);
		options.setVisualization(true);
		options.setNumberOfTrials(1);
		options.setMatlabFileName("");
		options.setLevelRandSeed(422);
		// options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
		options.setLevelDifficulty(-1);
		task.setOptions(options);

		Environment environment = (Environment) task.loadLevel(level, agent);
		waitForLevelInit(environment);
		return environment;
	}

	private static void waitForLevelInit(Environment observation) {
		for (int i = 0; i < LEVEL_INIT_TICKS; i++) {
			runOneTick(observation);
		}
	}
}
