package MarioAI;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.sun.istack.internal.FinalArrayList;
import com.sun.org.apache.bcel.internal.generic.RETURN;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent.
 */
public class FastAndFurious implements Agent {

	private static final String name = "THE ULTIME AND SUPREME OVERLORD THAT BRINGS DEATH AND DESTRUCTION";
	private final Graph graph = new Graph();
	private boolean[] action = new boolean[Environment.numberOfButtons]; 
	private int tickCount = 0;
	private List<Node> newestPath = null;
	private boolean isStuck = false; 
	private int ticksSinceLastUpdate = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		if (tickCount == 30) {
			graph.createStartGraph(observation);
			Grapher.graph(graph.getLevelMatrix(), graph.getMarioNode(observation));
			List<Node> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
			if (path != null) {
				newestPath = path;
			}
			
		} else if (tickCount > 30) {
			if (graph.updateMatrix(observation) || isStuck) {
				if (isStuck) graph.createStartGraph(observation);
				Grapher.graph(graph.getLevelMatrix(), graph.getMarioNode(observation));
				List<Node> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
				if (path != null) {
					newestPath = path;
				}
				isStuck = false;
				ticksSinceLastUpdate = 0;
			}
		}
		if (newestPath != null &&
			newestPath.size() >= 1 &&
			GraphMath.distanceBetween(graph.getMarioNode(observation), newestPath.get(0)) <= 0.1) {
			newestPath.remove(0);
		}
		if (newestPath != null && newestPath.size() > 0) {
			action = MarioControls.getNextAction(observation, newestPath);
			drawPath(observation, newestPath);
		}
		tickCount++;
		
		ticksSinceLastUpdate++;
		System.out.println("TICKS SINCE LAST UPDATE " + ticksSinceLastUpdate);
		if (ticksSinceLastUpdate > 100) {
			System.out.println("STUCK");
			isStuck = true;
		}
		
		graph.printMatrix();
		return action;
	}
	
	private void drawPath(final Environment observation, final List<Node> path)
	{
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<Point> guiPath = new ArrayList<Point>(); 
		Point marioPoint = new Point(marioXPos, marioYPos);
		
		guiPath.add(convertPointToOnScreenPoint(observation, marioPoint));
		
		for (int i = 0; i < path.size(); i++) {
			final Node node = path.get(i);
			final Point point = new Point(node.x, node.y);
    		
    		guiPath.add(convertPointToOnScreenPoint(observation, point));
		}
		
		((MarioComponent)observation).setPath(guiPath);
	}
	
	private Point convertPointToOnScreenPoint(final Environment observation, final Point point) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		point.x = (int)((point.x - Math.max(marioXPos - 11, 0)) * 16) - 8;
		point.y = (int)((marioYPos * 16) + ((point.y - marioYPos) * 16)) - 8;
		
		return point;
	}

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
	}
}
