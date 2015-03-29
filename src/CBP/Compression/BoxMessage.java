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
	
	public double getLowerrBound()
	{
		return lBound;		
	}
}
