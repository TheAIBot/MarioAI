package MarioAI.graph.nodes;

import java.awt.geom.Point2D;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyCollision;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

public class StateNode implements Comparable<StateNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public StateNode parent;
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
	private final boolean isStateNodeUseable;
	//Bit string, representing the enemies living. 
	//Must not be reused.
	public long livingEnemies; 
	
	private static final int MAX_TICKS_OF_INVINCIBILITY = 32; // source: Mario.java line 596
	public static int MAX_MARIO_LIFE = 3;
	public int ticksOfInvincibility = 0;
	public int lives = MAX_MARIO_LIFE;
	
	public StateNode(Node node, float vx, long hash, long livingEnemies) {
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
		this.isStateNodeUseable = true;
		this.hash = hash;
		this.livingEnemies = livingEnemies;
	}
	
	public StateNode(Node node, float marioX, float vx, long hash, long livingEnemies) {
		this(node, vx, hash, livingEnemies);
		this.currentXPos = marioX;
	}
	
	///Should only be used for testing purposes
	public StateNode(Node node, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, long livingEnemies, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.creationXPos = parentXPos + moveInfo.getXMovementDistance();
		this.currentXPos = this.creationXPos;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isStateNodeUseable = true;
		this.hash = hash;
		this.livingEnemies = livingEnemies;
	}
	
	public StateNode(Node node, StateNode parent, DirectedEdge ancestorEdge, long hash, long livingEnemies, World world) {
		this(node, parent, parent.creationXPos, parent.vx, ancestorEdge, hash, livingEnemies, world);
	}
	
	public StateNode(Node node, StateNode parent, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, long livingEnemies, World world) {
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
		this.isStateNodeUseable = determineIfThisNodeIsUseable(world);
		this.hash = hash;
		this.livingEnemies = livingEnemies;
	}
	
	private boolean determineIfThisNodeIsUseable(World world) {
		if (this.ancestorEdge instanceof FallEdge &&
			 !MarioControls.canMarioUseFallEdge(ancestorEdge, currentXPos)) {
			return false;
		}
		
		if (this.ancestorEdge instanceof JumpingEdge && 
			!MarioControls.canMarioUseJumpEdge(ancestorEdge, currentXPos)) {
			return false;
		}
			
		//Make sure the edge is possible to use
		//all Running edges are possible
		//not all jumps are possible
		if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped())) {
			return false;
		}
		
		if (getMoveInfo().hasCollisions(parent, world)) {
			return false;
		}
		
		return true;
	}
	
	public boolean isStateNodeUseable(World world) {
		final float diffX = Math.abs(creationXPos - currentXPos);
		if (diffX > MarioControls.ACCEPTED_DEVIATION) {
			//In a jump it's possible to jump too far
			//and there is nothing that mario can do about it
			//TODO this should maybe be removed in the future
			
			if (this.ancestorEdge instanceof JumpingEdge && 
				!MarioControls.canMarioUseJumpEdge(ancestorEdge, currentXPos)) {
				return false;
			}
			
			//Make sure the edge is possible to use
			//all Running edges are possible
			//not all jumps are possible
			if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped())) {
				return false;
			}
			
			if (getMoveInfo().hasCollisions(parent, world)) {
				return false;
			}	
			
			return true;
		}
		else {
			return isStateNodeUseable;
		}
	}
	
	public boolean doesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight,  EnemyCollision firstCollision) {
		int currentTick = startTime;
		this.ticksOfInvincibility = parent.ticksOfInvincibility;
		parent.ticksOfInvincibility = 0;				
		
		boolean hasEnemyCollision = false;
		for (int i = 0; i < moveInfo.getMoveTime(); i++) {
			final float currentXPos = moveInfo.getXPositions()[i];
			final float currentYPos = moveInfo.getYPositions()[i];
			final float x = parentXPos  + currentXPos;
			final float y = parent.yPos - currentYPos;
			
			//In the beginning of a movement, Mario will always be on the ground, thus not accelerating downwards.
			//Necessary to stomp the enemies.
			boolean movingDownwards = (i == 0)? false: (moveInfo.getYPositions()[i-1] >  currentYPos);
			//He must also land on the enemy, ie, not be on the ground, or not have been on the ground the tick before:
			boolean wasOnGround 		= (i == 0)? true:  (moveInfo.getYPositions()[i-1] != currentYPos);
			//He is on the ground, if and only if he is a running edge,
			//or he is at the last two positions on any other type of edge
			//( the last, curtesy of mario always landing before coming to the end of a block):
			boolean isOnGround	= (this.ancestorEdge instanceof RunningEdge || moveInfo.getMoveTime() - 2 <= i);
			boolean isOrWasNotOnGround = !wasOnGround || !isOnGround;
			
			//I will take the first actual collision, 
			//as though that is the one that determines the type of collision with enemies.
			if (enemyPredictor.hasEnemy(x, y - (1f / World.PIXELS_PER_BLOCK), marioHeight, currentTick, movingDownwards, isOrWasNotOnGround, firstCollision, this.livingEnemies)) {
				if(firstCollision.isStompType){ //Stomp type collision
					ticksOfInvincibility = 1; //Gets one tick of invincibility, in case of a stomp.
					//Notice that if has more ticks of invincibility than 1, this is overwritten.
					//This is how it is done in the game code.
					//TODO check correct.
					return true; //Stops the collision here, as the path taken must be changed.
				} else if(ticksOfInvincibility == 0){ //Normal collision, where Mario is damaged
					hasEnemyCollision = true;
					lives--;
					ticksOfInvincibility = MAX_TICKS_OF_INVINCIBILITY;
				} else {
					ticksOfInvincibility--;
				}
			} else {
				ticksOfInvincibility--;
			}
			
			currentTick++;
		}		
		return hasEnemyCollision;
	}
	
	/**
	 * Old collision method. Momentarily only for ease of reference.
	 * TODO remove this method
	 * @param startTime
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return
	 */
	public boolean tempDoesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight) {
		int currentTick = startTime;		
		for (int i = 0; i < moveInfo.getMoveTime(); i++) {
			final float x = parentXPos + moveInfo.getXPositions()[i];
			final float y = parent.yPos - moveInfo.getYPositions()[i];

			if (enemyPredictor.hasEnemy(x, y - (1f / World.PIXELS_PER_BLOCK), marioHeight, currentTick, false, false, null, -1)) {
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
		if (b instanceof StateNode) {
			StateNode bb = (StateNode) b;
			return bb.hash == hash;
		} else {
			return false;
		}
	}
	
	public int compareTo(StateNode o) {
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}
	
	@Override
	public String toString() {
		return node.toString() + (" gScore: " + gScore + ", fScore: " + fScore + "\n");
	}

	public StateNode getStompVersion(EnemyCollision firstCollision, Node targetNode, World world) {
		//Need to limit the "lenght" of the movement.
		moveInfo.setStopTime(firstCollision.tickForCollision);
		DirectedEdge stompAncestorEdge = ancestorEdge.getStompVersion(targetNode);	//TODO save it in a hash set, so it can be reused.
		//TODO currently has not hash, set to -1 instead.
		long stompHash = -1;
		
		StateNode stompVersion = new StateNode(targetNode, parent, stompAncestorEdge, parent.livingEnemies, stompHash, world);		
		return stompVersion;
	}
	
	
}
