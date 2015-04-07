package CBP.Compression;

public class BoxMessage {
	
	private double lBound;
	private double uBound;
	
	public BoxMessage(double lBound, double uBound)
	{
		//init variables 
		this.uBound = uBound;
		this.lBound = lBound;		
	}
	
	public BoxMessage()
	{
		//default constructor for the simplex message..
		uBound=1;
		lBound=0;
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
