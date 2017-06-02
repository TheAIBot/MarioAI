package MarioAI.graph.nodes;

import java.awt.geom.Point2D;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.Function;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

public class SpeedNode implements Comparable<SpeedNode>, Function {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public SpeedNode parent;
	public final float parentXPos;
	public final float parentVx;
	public final long hash;
	public final DirectedEdge ancestorEdge;
	public float xPos;
	public final int yPos;
	public int gScore = 0;
	public float fScore = 0;
	private final MovementInformation moveInfo;
	private final boolean isSpeedNodeUseable;
	
	public SpeedNode(Node node, float vx, long hash) {
		this.node = node;
		this.moveInfo = null;
		this.vx = vx;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.xPos = node.x;
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
		this.xPos = marioX;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	///Should only be used for testing purposes
	public SpeedNode(Node node, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.xPos = parentXPos + moveInfo.getXMovementDistance();
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	public SpeedNode(Node node, SpeedNode parent, DirectedEdge ancestorEdge, long hash, World world) {
		this(node, parent, parent.xPos, parent.vx, ancestorEdge, hash, world);
	}
	
	public SpeedNode(Node node, SpeedNode parent, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.xPos = parentXPos + moveInfo.getXMovementDistance();
		this.parent = parent;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = determineIfThisNodeIsUseable(world);
		this.hash = hash;
	}
	
	private boolean determineIfThisNodeIsUseable(World world) {
		

		//There are a lot of possible problems for a fall edge.
		//TODO move below, when implemented
		if (this.ancestorEdge instanceof FallEdge &&
			 !MarioControls.canMarioUseFallEdge(ancestorEdge, xPos)) {
			return false;
		}
		
		//Make sure the edge is possible to use
		//all Running edges are possible
		//not all jumps are possible
		if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped())) {
			return false;
		}
		
		//In a jump it's possible to jump too far
		//and there is nothing that mario can do about it
		//TODO this should maybe be removed in the future
		if (this.ancestorEdge instanceof JumpingEdge && 
			!MarioControls.canMarioUseJumpEdge(ancestorEdge, xPos)) {
			return false;
		}
		
		//TODO remove this when changed moveinformation is implemented
		if (getMoveInfo().hasCollisions(parent, world)) {
			return false;
		}
		
		return true;
	}
	
	public boolean doesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, int marioHeight) {
		int currentTick = startTime;
		
		for (Point2D.Float position : moveInfo.getPositions()) {
			final float x = parentXPos  + position.x;
			final float y = parent.yPos - position.y;
			
			if (enemyPredictor.hasEnemy(x, y, 1, marioHeight, currentTick)) {
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
	
	public boolean isSpeedNodeUseable() {
		return isSpeedNodeUseable;
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

	public float f(float x) {
		return moveInfo.f(x);
	}
	
	private boolean collissionDetector(){
		boolean hasCollided = false;
		return hasCollided;
	}
}
