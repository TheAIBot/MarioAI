package MarioAI.graph.nodes;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

/**
 * A speed node corresponds to a search node in the A* search algorithm, but includes more information and functionallity.
 */
public class SpeedNode implements Comparable<SpeedNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public SpeedNode parent;
	public float parentXPos;
	public final float parentVx;
	public final long hash;
	public final DirectedEdge ancestorEdge;
	public final float creationXPos;
	public float currentXPos;
	public final int yPos;
	public int gScore = 0;
	public float fScore = 0;
	private final MovementInformation moveInfo;
	private final boolean isSpeedNodeUseable;
	
	public static final int MAX_TICKS_OF_INVINCIBILITY = 32; // source: Mario.java line 596

	public static final int PENALTY_SCORE = 70005; // semi arbitrary high value
	public int ticksOfInvincibility = 0;
	public int lives;
	public int penalty; // the penalty this node will get for hitting an enemy. To be used for influencing the choices made in A*
	
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
		this.penalty = 0; // parent is null
	}
	
	public SpeedNode(Node node, float marioX, float vx, long hash, int lives) {
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
		this.penalty = 0; // parent is null
		this.lives = lives;
	}
	
	///Should only be used for testing purposes
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
		this.penalty = 0; // parent is null
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
		this.ticksOfInvincibility = parent.ticksOfInvincibility;
		this.lives = parent.lives;
		this.penalty = parent.penalty;
	}
	
	/**
	 * @param world
	 * @return true if the speednode does not have collisions with blocks in the world and the movement leading to this speed node
	 * is actually possible to carry out.
	 */
	private boolean determineIfThisNodeIsUseable(World world) {
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
	 * @param world
	 * @return true if the speednode does not have collisions with blocks in the world and the movement leading to this speed node
	 * is actually possible to carry out.
	 */
	public boolean isSpeedNodeUseable(World world) {
		final float diffX = Math.abs(creationXPos - currentXPos);
		if (diffX > MarioControls.ACCEPTED_DEVIATION) {
			//Make sure the edge is possible to use
			//all Running edges are possible
			//not all jumps are possible
			if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped(), moveInfo.getXMovementDistance(), moveInfo.getXPositions())) {
				return false;
			}
			
			// Are there any collisions with blocks in the world (this is not an enemy collision check) 
			if (getMoveInfo().hasCollisions(parent, world)) {
				return false;
			}	
			
			return true;
		}
		else {
			return isSpeedNodeUseable;
		}
	}
	
	/**
	 * Check if Mario collides with an enemey and set ticks of invincibility, lives and penalty accordingly.
	 * 
	 * @param startTime
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return true if Mario collides with an enemy during the movement leading to this speed node, false otherwise
	 */
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
	}
	
	/**
	 * Deprecated collision method. Still present for ease of reference.
	 * TODO remove this method
	 * @param startTime
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return
	 */
	public boolean tempDoesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight) {
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
	
	public int compareTo(SpeedNode o) {
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}
	
	@Override
	public String toString() {
		return node.toString() + (" gScore: " + gScore + ", fScore: " + fScore + "\n");
	}
}
