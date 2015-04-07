package CBP.Compression;

public class BoxMessage {
	private boolean isSimplex;
	private boolean isPredicateMessage;
	
	private double lBound;
	private double uBound;
	
	public BoxMessage(double lBound, double uBound,boolean isPredicateMessage, boolean isSimplex)
	{
		//init variables
		this.isSimplex = isSimplex;
		this.isPredicateMessage = isPredicateMessage; 
		this.uBound = uBound;
		this.lBound = lBound;		
	}
	
	public BoxMessage(boolean isPredicateMessage)
	{
		//default constructor for the simplex message..
		isSimplex=true;
		uBound=1;
		lBound=1;
		this.isPredicateMessage=isPredicateMessage;
	}
	
	
	public boolean getIsSimplex()
	{
		return isSimplex;		
	}

	public boolean getIsPredicateMessage()
	{
		return isPredicateMessage;
	}
	
	public double getUpperBound()
	{
		return uBound;		
	}
	
	public double getLowerBound()
	{
		return lBound;		
	}
}
