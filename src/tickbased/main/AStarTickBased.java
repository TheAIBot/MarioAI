package tickbased.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import tickbased.search.Node;

public class AStarTickBased {

	public boolean finishedNewRun = true;

	public List<Action> runAStar(Problem problem, long startTime) {
//		long startTime = System.currentTimeMillis();
		finishedNewRun = false;
		
		SearchNode start = new SearchNode(problem.initialState);
		SearchNode goal = new SearchNode(problem.goalState);
		SearchNode currentBest = start;
		
		// Set of nodes already explored
		Set<SearchNode> explored = new HashSet<SearchNode>();
		// Nodes yet to be explored
		PriorityQueue<SearchNode> frontier = new PriorityQueue<SearchNode>();
		Map<Integer, SearchNode> frontierMap = new HashMap<Integer, SearchNode>();

		// Initialization
		frontier.add(start);
		frontierMap.put(start.hashCode(), start);
		start.gScore = 0;
		start.fScore = problem.heuristicFunction(start, goal);
		
		// Continue exploring as long as there are states in the state space, which have not been visited, or until goal is reached
		
		while (!frontier.isEmpty() && (System.currentTimeMillis() - startTime < problem.MAX_ALLOWED_RUN_TIME)) {
			SearchNode current = frontier.remove();
			frontierMap.remove(current.hashCode());

			// If goal is reached return solution path
			if (problem.goalTest(current.state)) {
				finishedNewRun = true;
				return reconstructPath(current);
			}
			
			// If the searchNode has a higher x value than the previous ones then this will be the new currentBest
			if (((Node) current.state).x > ((Node) currentBest.state).x) currentBest = current;
			
			// Current node has been explored
			explored.add(current);
			
			// Explore each neighbor of current node
			for (Action action : problem.actions(current.state)) {
				SearchNode child = problem.childNode(current, action);
				
				// Cost of reaching child node
				double tentativeGScore = current.gScore + problem.pathCost(current, child);
				
				if (!explored.contains(child) && !frontierMap.containsKey(child.hashCode())) {
					insertChildNode(child, current, tentativeGScore, problem, goal, frontier, frontierMap);
				} else if (frontierMap.containsKey(child.hashCode()) && frontierMap.get(child.hashCode()).gScore > tentativeGScore) {
					// the path the child node gives rise to is better than the original node (and corresponding path) so add child node instead
					frontier.remove(child);
					frontierMap.remove(child.hashCode());
					insertChildNode(child, current, tentativeGScore, problem, goal, frontier, frontierMap);
				}
			}
		}
		
		// No solution exists or no solution was found in the given time.
		// Return the best route found so far.
		finishedNewRun = true;
		return reconstructPath(currentBest);
	}
	
	/**
	 * Inserts a given child node into the frontier along with setting its parent, gScore and fScore
	 * @param child
	 * @param node
	 * @param tentativeGScore
	 * @param problem
	 * @param goal
	 * @param frontier
	 * @param frontierMap
	 */
	private void insertChildNode(SearchNode child, SearchNode node, double tentativeGScore, Problem problem,
										SearchNode goal, PriorityQueue<SearchNode> frontier, Map<Integer, SearchNode> frontierMap) {
		child.parent = node;
		child.gScore = tentativeGScore;
		child.fScore = child.gScore + problem.heuristicFunction(child, goal);
		frontier.add(child);
		frontierMap.put(child.hashCode(), child);
	}
	
	/**
	 * Constructs the solution path by retracing steps using parent links
	 * @param current
	 * @return solution path
	 */
	private List<Action> reconstructPath(SearchNode current) {
		List<Action> path = new ArrayList<Action>();
		while (current.parent != null) {
			path.add(current.action);
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}

}