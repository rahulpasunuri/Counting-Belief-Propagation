package CBP.Infer.GraphStructure;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	private Vertex current;
	private TreeNode parent;
	private List<TreeNode> children;
	
	public TreeNode(Vertex current, TreeNode parent)
	{
		this.current=current;
		this.parent=parent;
		this.children=new ArrayList<TreeNode>();
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

}
