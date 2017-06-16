package MarioAI.graph.edges.edgeCreation;

/** Enum for direction of Jumps, like right and left, with their up and down variants.
 * @author jesper.
 *
 */
public enum JumpDirection { 
	STRAIGHT_UPWARDS{
		@Override
		public int getHorizontalDirectionAsInt() {
			return 0;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return -1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return STRAIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return null; //There is no opposite.
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return STRAIGHT_DOWNWARDS;
		}

		@Override
		public boolean isUpwardsType() {
			return true;
		}

		@Override
		public boolean isLeftType() {
			return false;
		}
		
	},
	STRAIGHT_DOWNWARDS{
		@Override
		public int getHorizontalDirectionAsInt() {
			return 0;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return 1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return STRAIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return null; //There is no opposite.
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return STRAIGHT_DOWNWARDS;
		}

		@Override
		public boolean isUpwardsType() {
			return false;
		}

		@Override
		public boolean isLeftType() {
			return false;
		}
		
	},
	LEFT_UPWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return -1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return -1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return RIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return RIGHT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return LEFT_DOWNWARDS;
		}

		@Override
		public boolean isUpwardsType() {
			return true;
		}

		@Override
		public boolean isLeftType() {
			return true;
		}
	},
	LEFT_DOWNWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return -1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return 1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return RIGHT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return RIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return LEFT_UPWARDS;
		}

		@Override
		public boolean isUpwardsType() {
			return false;
		}

		@Override
		public boolean isLeftType() {
			return true;
		}
	},
	RIGHT_UPWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return 1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return -1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return LEFT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return LEFT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return RIGHT_DOWNWARDS;
		}

		@Override
		public boolean isUpwardsType() {
			return true;
		}

		@Override
		public boolean isLeftType() {
			return false;
		}
	},
	RIGHT_DOWNWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return 1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return 1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return LEFT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return LEFT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return RIGHT_UPWARDS;
		}

		@Override
		public boolean isUpwardsType() {
			return false;
		}

		@Override
		public boolean isLeftType() {
			return false;
		}
	};
	
	/** Returns if the direction is upwards.
	 * @return
	 */
	public abstract boolean isUpwardsType();
	/** Returns if the direction is of the left type.
	 * @return
	 */
	public abstract boolean isLeftType();
	
	/** Returns an integer representing the horizontal part of the direction.
	 * @return Returns 1 if the direction is upwards, -1 if it is downwards.
	 */
	public abstract int getHorizontalDirectionAsInt();
	
	/** Returns an integer representing the vertical part of the direction.
	 * @return Returns 1 if the direction is rightwards, -1 if it is leftwards.
	 */
	public abstract int getVerticalDirectionAsInt();
	
	/** Returns the complete opposite direction of the current direction. 
	 * This inverts both the horizontal and vertical movement.
	 * @return The direction with its horizontal and vertical component inverted.
	 */
	public abstract JumpDirection getOppositeDirection();
	
	/** Returns the direction with its horizontal component inverted.
	 * If it is going rightwards, it is now going leftwards, and the other way around.
	 * @return The direction with its horizontal component inverted.
	 */
	public abstract JumpDirection getOppositeHorizontalDirection();
	
	/** Returns the direction with its vertical component inverted.
	 * If it is going upwards, it is now going downwards, and the other way around.
	 * @return The direction with its vertical component inverted.
	 */
	public abstract JumpDirection getOppositeVerticalDirection();
}
