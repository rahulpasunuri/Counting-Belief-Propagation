package CBP.Infer.GraphStructure;

import java.util.ArrayList;
import java.util.List;
import CBP.Compression.BoxMessage;
import java.util.HashMap;

public class TreeNode {
	private Vertex current;
	private TreeNode parent;
	private List<TreeNode> children;
	public boolean isLeaf;
	//map from node id to corresponding message
	private HashMap<Integer, BoxMessage> messages;
	
	
	public BoxMessage getMessage(int id)
	{		
		return messages.get(id);
	}
	
	public void putMessage(int id, BoxMessage msg)
	{
		messages.put(id, msg);		
	}
	
	public BoxMessage getParentMessage()
	{
		//TODO
		return new BoxMessage(false);
	}
	
	public TreeNode(Vertex current, TreeNode parent)
	{
		this.current=current;
		this.parent=parent;
		this.children=new ArrayList<TreeNode>();
		messages = new HashMap<Integer, BoxMessage>();
		isLeaf=false;
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
