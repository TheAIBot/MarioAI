package tests;

import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemySimuation.EnemyType;
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
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;

public class TestTools {

	public static final int LEVEL_INIT_TICKS = 30;

	public static int runOneTick(Environment observation) {
		return ((MarioComponent) observation).runOneTick();
	}
	
	public static void runWholeLevel(Environment observation) {
		((MarioComponent) observation).run1(0, 1);
	}
	
	public static int runWholeLevelWillWin(Environment observation) {
		EvaluationInfo ei = ((MarioComponent) observation).run1(0, 1);
		return ei.marioStatus;
	}
	
	public static byte[][] getLevelMap(Environment observation)
	{
		return ((MarioComponent) observation).getLevel().map;
	}
	
	public static Environment loadLevel(String filepath, Agent agent) {
		return loadLevel(filepath, agent, false);
	}
	
	public static Environment loadLevel(String filepath, Agent agent, boolean showGUI) {
		return loadLevel(filepath, agent, showGUI, !showGUI);
	}
	
	public static Environment loadLevel(String filepath, Agent agent, boolean showGUI, boolean maxFps) {
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
		options.setMaxFPS(maxFps);
		options.setVisualization(showGUI);
		options.setNumberOfTrials(1);
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
	
	public static Sprite spawnEnemy(Environment observation, int mapX, int mapY, int direction, EnemyType enemyType) {
		final LevelScene world = ((MarioComponent)observation).getLevelScene();
		final float x = mapX * 16;
		final float y = mapY * 16 - 1;
		
		Sprite enemy = null;
		
		switch (enemyType) {
		case RED_KOOPA:
		case RED_KOOPA_WINGED:
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
		case GOOMBA:
		case GOOMBA_WINGED:
		case SPIKY:
		case SPIKY_WINGED:
			enemy = new Enemy(world, (int)x, (int)y, direction, enemyType.getType(), enemyType.hasWings(), mapX, mapY);
			break;
		case FLOWER:
			enemy = new FlowerEnemy(world, (int)x, (int)y, mapX, mapY);
			break;
		case GREEN_SHELL:
		case RED_SHELL:
			Shell shell = new Shell(world, x, y, enemyType.getType());
			shell.facing = direction;
			enemy = shell;
			break;
		case BULLET_BILL:
			enemy = new BulletBill(world, x, y, direction);
			break;
		}
		
		world.addSprite(enemy);
		
		return enemy;
	}
	
	public static void removeEnemy(Environment observation, Sprite enemy) {
		final LevelScene world = ((MarioComponent)observation).getLevelScene();
		world.removeSprite(enemy);
	}
	
	public static void setMarioInvulnerability(Environment observation, boolean value) {
		((MarioComponent)observation).setMarioInvulnerable(value);
	}
	
	public static void renderLevel(Environment observation) {
		((MarioComponent)observation).render();
		DebugDraw.resetGraphics(observation);
	}
	
	public static void setMarioPosition(Environment observation, int x, int y) {
		((MarioComponent)observation).setMarioPosition(x, y);
	}
	
	public static void setMarioXPosition(Environment observation, int x) {
		((MarioComponent)observation).setMarioXPosition(x);
	}
	
	public static void setMarioPixelPosition(Environment observation, int x, int y) {
		((MarioComponent)observation).setMarioPixelPosition(x, y);
	}
	
	public static void setMarioXPixelPosition(Environment observation, int x) {
		((MarioComponent)observation).setMarioXPixelPosition(x);
	}
	
	public static void resetMarioSpeed(Environment observation) {
		((MarioComponent)observation).resetMarioSpeed();
	}
}
