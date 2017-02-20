package MarioAI;

public class SecondOrderPolynomial{
	private float a,b,c; //Parameters of the polynomial.
	private float topPunktX, topPunktY; //Cordinates of the toppunkt
	//TODO Maybe change ceil after discussions. Problem associated with making this into an integer
	//, as sometimes two possible jump position in a row will be registrated as possible to reach,
	//even though it is only one it should be possible to reach. Mario changing position in the air can compensate for this.
	private short ceiledTopPunktX,ceiledTopPunktY; //Ceildes coordinates of the toppunkt
	
	public SecondOrderPolynomial(Node startingPosition, float jumpRange, float jumpHeight) {
		setToJumpPolynomial(startingPosition, jumpRange, jumpHeight);
	}
	
	public SecondOrderPolynomial() {
		a = 0;
		b = 0;
		c = 0;
	}
	
	//
	public void setToJumpPolynomial(Node startingPosition, float jumpRange, float jumpHeight) {
		a = -4 * jumpHeight / (jumpRange * jumpRange);
		b = (8 * startingPosition.coloumn + 4 * jumpRange) * jumpHeight / (jumpRange * jumpRange);
		c = (-4 * startingPosition.coloumn  * (startingPosition.coloumn + jumpRange) * jumpHeight + startingPosition.row
				* jumpRange * jumpRange) / (jumpRange * jumpRange);
		setTopPunkt();
	}
	

	public boolean isPastTopPunkt(short startPosition, short currentPosition) {
		return (startPosition <= topPunktX && startPosition <= currentPosition ||
			    startPosition >= topPunktX && startPosition >= currentPosition);
	}	

	public float getTopPunktX() {
		return (topPunktX);
	}
	
	public float getTopPunktY() {
		return topPunktY;
	}

	public float getCeiledTopPunktX() {
		return (ceiledTopPunktX);
	}
	
	public float getCeiledTopPunktY() {
		return ceiledTopPunktY;
	}
	
	private void setTopPunkt() {
		topPunktX = ((-b/a)/2);
		topPunktY = f(topPunktX);
		ceiledTopPunktX = (short) Math.ceil(ceiledTopPunktX);
		ceiledTopPunktY = (short) Math.ceil(ceiledTopPunktY);
	}
		
	public float f(float x) {
		return a*x*x+b*x+c;
	}
	
}