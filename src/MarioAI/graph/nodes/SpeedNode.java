package MarioAI.graph.nodes;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

/**
 * @author Emil
 * Speed nodes are used as search nodes internally in the A* search algorithm as well as
 * containing various kinds of additional usfull information. Corresponds to states in the world (state space)
 */
public class SpeedNode implements Comparable<SpeedNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public SpeedNode parent;
	public float parentXPos;
	public final float parentVx;
	public final long hash;
	public final DirectedEdge ancestorEdge; // The edge (action) used to get to this speed node
	public final float creationXPos;
	public float currentXPos;
	public final int yPos;
	public int gScore = 0;
	public float fScore = 0;
	private final MovementInformation moveInfo; // movement information about the movement to get to this node
	private final boolean isSpeedNodeUseable; // field for quickly checking if this speed node is possible to get to
											  // with the ancestor edge and no collisions occured.
	
//	private static final int MAX_TICKS_OF_INVINCIBILITY = 32; // source: Mario.java line 596
//	public static int MAX_MARIO_LIFE = 3;
//	public int ticksOfInvincibility = 0;
//	public int lives = MAX_MARIO_LIFE;
	
	public SpeedNode(Node node, float vx, long hash) {
		this.node = node;
		this.moveInfo = null;
		this.vx = vx;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.creationXPos = node.x;
		this.currentXPos = this.creationXPos;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	public SpeedNode(Node node, float marioX, float vx, long hash) {
		this.node = node;
		this.moveInfo = null;
		this.vx = vx;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.creationXPos = marioX;
		this.currentXPos = this.creationXPos;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
		
	}
	
	// Constructor only used in unit tests
	public SpeedNode(Node node, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.creationXPos = parentXPos + moveInfo.getXMovementDistance();
		this.currentXPos = this.creationXPos;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	public SpeedNode(Node node, SpeedNode parent, DirectedEdge ancestorEdge, long hash, World world) {
		this(node, parent, parent.creationXPos, parent.vx, ancestorEdge, hash, world);
	}
	
	public SpeedNode(Node node, SpeedNode parent, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.creationXPos = parentXPos + moveInfo.getXMovementDistance();
		this.currentXPos = this.creationXPos;
		this.parent = parent;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = determineIfThisNodeIsUseable(world);
		this.hash = hash;
	}
	
	/**
	 * Used for setting the field isThisSpeedNodeUsuable to quickly return an answer to this check when possible.
	 * @param world
	 * @return true if the edge to this speed node is possible to use and there are no collisions with the world
	 * during the movement of the said edge.
	 */
	private boolean determineIfThisNodeIsUseable(World world) {
		
		if (moveInfo.getXPositions().length == 0) {
			return false;
		}
		//Make sure the edge is possible to use
		//all Running edges are possible
		//not all jumps are possible
		if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped(), moveInfo.getXMovementDistance(), moveInfo.getXPositions())) {
			return false;
		}
		
		if (getMoveInfo().hasCollisions(parent, world)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Determines if this node is usable based on if there are collisions with the world moving to this node
	 * using the edge leading to this node as well as checking if the edge can be used.
	 * If there is a minimal difference in x position between the speed node's current x position (the latter can
	 * be changed in the fetching of speed nodes in A* to allowed reuse when applicable) then the check
	 * for the edge being usable and no collisions with the world can be done quickly by just returning
	 * the precomputed isSpeedNodeUsable field.
	 * @param world
	 * @return true if the edge to this speed node is possible to use and there are no collisions with the world
	 * during the movement of the said edge.
	 */
	public boolean isSpeedNodeUseable(World world) {
		final float diffX = Math.abs(creationXPos - currentXPos);
		if (diffX <= MarioControls.ACCEPTED_DEVIATION) {
			return isSpeedNodeUseable;
		}
			
		if (moveInfo.getXPositions().length == 0) {
			return false;
		}
		//Make sure the edge is possible to use
		//all Running edges are possible
		//not all jumps are possible
		if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped(), moveInfo.getXMovementDistance(), moveInfo.getXPositions())) {
			return false;
		}
		
		if (getMoveInfo().hasCollisions(parent, world)) {
			return false;
		}	
		
		return true;
	}
	
	/**
	 * Original collision method. Checks if there is a collision with an enemy during the movement
	 * using the edge to get to the speed node.
	 * @param startTime
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return true if movement collides with an enemey, false otherwise.
	 */
	public boolean originalDoesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight) {
		int currentTick = startTime;
		
        for (int i = 0; i < moveInfo.getMoveTime(); i++) {
            final float x = parentXPos  + moveInfo.getXPositions()[i];
            final float y = parent.yPos - moveInfo.getYPositions()[i];

            if (enemyPredictor.hasEnemy(x, y - (1f / World.PIXELS_PER_BLOCK), marioHeight, currentTick)) {
                return true;
            }
	
            currentTick++;
        }
		return false;
	}
	
	/**
	 * Method for functionality not implemented as of this moment.
	 * Check if Mario collides with an enemey and set ticks of invincibility, lives and penalty accordingly.
	 * @param startTime
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return true if Mario collides with an enemy during the movement leading to this speed node, false otherwise
	 */
	/*
	public boolean doesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight) {
		if (ticksOfInvincibility < 0) {
			System.out.println("Error");
		}
		
		// If Mario is invincible longer than the time taken to get to traverse edge it does not matter
		// if an enemy is hit underway or not, so just deduct the ticks it takes from the ticks left of invincibility 
		if (ticksOfInvincibility >= moveInfo.getMoveTime()) {
			ticksOfInvincibility -= moveInfo.getMoveTime();
			return false;
		}
		
		int currentTick = ticksOfInvincibility + startTime;
		boolean hasEnemyCollision = false;
		for (int i = ticksOfInvincibility; i < moveInfo.getMoveTime(); i++) {
			final float x = parentXPos  + moveInfo.getXPositions()[i];
			final float y = parent.yPos - moveInfo.getYPositions()[i];
			
			if (enemyPredictor.hasEnemy(x, y - (1f / World.PIXELS_PER_BLOCK), marioHeight, currentTick)) {
				if (ticksOfInvincibility == 0) {
					hasEnemyCollision = true;
					lives--;
					ticksOfInvincibility = MAX_TICKS_OF_INVINCIBILITY;
					penalty += PENALTY_SCORE;
				} else if (ticksOfInvincibility > 0){
					ticksOfInvincibility--;
				} else {
					throw new Error("Negative invincibility error. It is: " + ticksOfInvincibility);
				}
			} else if(ticksOfInvincibility > 0) {
				ticksOfInvincibility--;
			}
			
			currentTick++;
		}
		if (ticksOfInvincibility < 0) {
			System.out.println("Error");
		}
		
		return hasEnemyCollision;
	}*/
	
	public MovementInformation getMoveInfo() {
		return moveInfo;
	}
	
	public void use() {
		ancestorEdge.setMoveInfo(moveInfo);
	}
	
	public int getMoveTime() {
		return moveInfo.getMoveTime();
	}
	
	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof SpeedNode) {
			SpeedNode bb = (SpeedNode) b;
			return bb.hash == hash;
		} else {
			return false;
		}
	}
	
	/**
	 * Used in the priority queue in A* when coparing the fitness (f-score) of this speed node with another
	 */
	public int compareTo(SpeedNode o) {
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}
	
	@Override
	public String toString() {
		return node.toString() + (" gScore: " + gScore + ", fScore: " + fScore + "\n");
	}
}
