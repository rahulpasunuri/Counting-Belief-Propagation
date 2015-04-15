package CBP.Infer.GraphStructure;

import java.util.ArrayList;
import java.util.List;
import CBP.Compression.BoxMessage;
import CBP.Compression.Clause;
import java.util.HashMap;

public class TreeNode {
	private Vertex current;
	private TreeNode parent;
	private List<TreeNode> children;
	public boolean isLeaf;
	
	//below two attributes are only meaningful for clause tree nodes.
	private List<Boolean> isNegated;
	public boolean isParentNegated; 
	
	
	//map from node id to corresponding message
	public void AddChildNegationProperty(boolean sign)
	{
		isNegated.add(sign);
	}
	/*
	private HashMap<Integer, BoxMessage> messages;
	
	
	public BoxMessage getMessage(int id)
	{		
		return messages.get(id);
	}
	
	public void putMessage(int id, BoxMessage msg)
	{
		messages.put(id, msg);		
	}
	*/
	
	public BoxMessage getParentMessage()
	{
		if(this.isLeaf)
		{
			return new BoxMessage();
		}
		
		//get the messages from all its children
		List<BoxMessage> liMsg = new ArrayList<BoxMessage>();
		
		
		boolean sendSimplex=false;
		
		for(TreeNode tn : this.children)
		{
			BoxMessage bm = tn.getParentMessage();
			
			if(tn.isLeaf)
			{
				sendSimplex=true;
			}
			liMsg.add(bm);
		}
		double lower=0, upper=0;
		if(this.isClauseNode())
		{
			//this is a clause node..			
			//have to account for the "compressed" aspect TODO
			
			List<String> tfCombinations=createTFCombinations(this.children.size());
			
			Clause c = this.current.getClause();
			
			//value of the potential if the clause is true..
			double potentialTrue = Math.exp(c.getweight());
			
			//value of the potential if the clause is false..
			double potentialFalse = 1;					

			for(String s : tfCombinations)
			{
				double prod=1;				
				boolean isTrue=false;
				int strIndex=0;
				for(BoxMessage bm : liMsg)
				{
					if(!isTrue)
					{
						if(s.charAt(strIndex) == 'T' && !isNegated.get(strIndex)) 
						{
							isTrue=true;									
						}
						else if(s.charAt(strIndex) == 'F' && isNegated.get(strIndex))
						{
							isTrue=true;				
						}
						
						if(s.charAt(strIndex)=='T')
						{
							prod*=bm.getUpperBound();
						}
						else
						{
							prod*=bm.getLowerBound();
						}
						
					}
					strIndex++;
				
					if(isTrue)
					{
						//the clause is true independent of the parent's value..
						lower+=potentialTrue*prod;
						upper+=potentialTrue*prod;
					}
					else
					{
						if(isParentNegated)
						{
							lower+=potentialTrue*prod;
							upper+=potentialFalse*prod;	
						}
						else
						{
							//this is true because the parent is false, and it is negated in the clause.
							lower+=potentialTrue*prod;
							upper+=potentialFalse*prod;
						}
					}
				}
			}
			
			//normalize the message..
			double sum=lower+upper;			
			lower /=sum;
			upper /=sum;
			
			if(lower>upper)
			{
				double temp=lower;
				lower=upper;
				upper=temp;
			}
		}
		else
		{		
			//this is a predicate node..
			if(sendSimplex)
			{
				return new BoxMessage();	//send a simplex message..			
			}
			else
			{
				//have to account for the "compressed" aspect TODO
				for(BoxMessage bm: liMsg)
				{
					lower*=bm.getLowerBound();
					upper*=bm.getUpperBound();
				}
			}
		}
		return new BoxMessage(lower, upper);
	}
	
	public TreeNode(Vertex current, TreeNode parent)
	{
		this.current=current;
		this.parent=parent;
		this.children=new ArrayList<TreeNode>();
		//messages = new HashMap<Integer, BoxMessage>();
		isLeaf=false;
		
		if(current.getNode().isClause)
		{
			this.isNegated=new ArrayList<Boolean>();			
		}
	}
	
    private ArrayList<String> createTFCombinations(int length)
    {
    	ArrayList<String> res = new ArrayList<String>();
    	if(length==1)
    	{
    		res.add("T");
    		res.add("F");
    		return res;
    	}
    	ArrayList<String> t = createTFCombinations(length-1);
    	for(String s : t)
    	{
    		res.add("T"+s);
    		res.add("F"+s);
    	}
    	return res;
    }
	
	
	public boolean isClauseNode()
	{
		return current.getNode().isClause;		
	}
	
	public Vertex getVertex()
	{
		return current;
	}
	
	public void AddChild(TreeNode v)
	{
		this.children.add(v);		
	}
	
	public boolean isRoot()
	{
		if(parent==null)
		{
			return true;
		}
		return false;
	}
	
	public List<TreeNode> getChildren()
	{
		return children;
	}
	
	public TreeNode getParent()
	{
		return parent;		
	}
	
	public void printNode()
	{
		String s="Predicate";
		if(this.isClauseNode())
		{
			s="Clause";
		}
		s+=" id:";
		s+=Integer.toString(this.current.getNode().getID());
		s+="\t";
		System.out.print(s);
	}

}
