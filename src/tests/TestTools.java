package tests;

import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.sprites.BulletBill;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.FlowerEnemy;
import ch.idsia.mario.engine.sprites.Shell;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

public class TestTools {

	public static final int LEVEL_INIT_TICKS = 30;

	public static int runOneTick(Environment observation) {
		return ((MarioComponent) observation).runOneTick();
	}
	
	public static void runWholeLevel(Environment observation) {
		((MarioComponent) observation).run1(0, 1);
	}
	
	public static byte[][] getLevelMap(Environment observation)
	{
		return ((MarioComponent) observation).getLevel().map;
	}
	
	public static Environment loadLevel(String filepath, Agent agent) {
		return loadLevel(filepath, agent, false);
	}
	
	public static Environment loadLevel(String filepath, Agent agent, boolean showGUI) {
		Level level = null;
		try {
			level = Level.load(new DataInputStream(new FileInputStream("src/tests/testLevels/" + filepath)));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			return null;
		}
		
		EvaluationOptions options = new CmdLineOptions(new String[0]);
		options.setAgent(agent);
		Task task = new ProgressTask(options);
		options.setMaxFPS(!showGUI);
		options.setVisualization(showGUI);
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
	
	public static Environment loadRandomLevel(Agent agent) {
		return loadRandomLevel(agent, false);
	}
	
	public static Environment loadRandomLevel(Agent agent, boolean showGUI) {		
		return loadLevelWithSeed(agent, (int) (Math.random () * Integer.MAX_VALUE), showGUI);
	}
	
	public static Environment loadLevelWithSeed(Agent agent, int seed) {
		return loadLevelWithSeed(agent, seed, false);
	}
	
	public static Environment loadLevelWithSeed(Agent agent, int seed, boolean showGUI) {
		EvaluationOptions options = new CmdLineOptions(new String[0]);
		options.setAgent(agent);
		Task task = new ProgressTask(options);
		options.setMaxFPS(!showGUI);
		options.setVisualization(showGUI);
		options.setNumberOfTrials(1);
		options.setMatlabFileName("");
		options.setLevelRandSeed(seed);
		options.setLevelDifficulty(-1);
		task.setOptions(options);

		Environment environment = (Environment) task.setRandomLevel(agent);
		waitForLevelInit(environment);
		return environment;
	}
	
	public static void unloadLevel(Environment observation) {
		JFrame window = (JFrame) SwingUtilities.windowForComponent((MarioComponent) observation);
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
	}

	private static void waitForLevelInit(Environment observation) {
		for (int i = 0; i < LEVEL_INIT_TICKS; i++) {
			runOneTick(observation);
		}
	}

	public static void SpawnEnemy(Environment observation, int mapX, int mapY, int direction, int type, boolean winged) {
		final LevelScene world = ((MarioComponent)observation).getLevelScene();
		final float x = mapX * 16;
		final float y = mapY * 16;
		
		Sprite enemy = null;
		switch (type) {
		case Sprite.KIND_BULLET_BILL:
			enemy = new BulletBill(world, x, y, direction);
			break;
		case Sprite.KIND_ENEMY_FLOWER:
			enemy = new FlowerEnemy(world, (int)x, type, mapX, mapY);
			break;
		case Sprite.KIND_SHELL:
			enemy = new Shell(world, x, y, type);
			break;
		default:
			enemy = new Enemy(world, (int)x, (int)y, direction, type, winged, mapX, mapY);
			break;
		}
		world.addSprite(enemy);
	}
}
