package CBP.Infer.GraphStructure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import CBP.Infer.GraphStructure.FactorGraph;;


public class Tree {

	private FactorGraph fg;
	private int queryId;
	private TreeNode root;
	private List<TreeNode> leaves; //will hold all the leaf nodes of the tree,,
	
	public Tree(FactorGraph g, int queryId)
	{
		//creates a tree using the query as root.
		this.queryId= queryId;
		this.fg = g;
		this.leaves=new ArrayList<TreeNode>();
		runBFS();
	}
	
	
	private void runBFS()
	{
		System.out.println("\nCreating a Tree for the Query node");
		List<Integer> visitedIds = new ArrayList<Integer>();
		root = new TreeNode(fg.getGraph().getClusteredPredicateVertexByID(queryId), null); //parent is null for root.
		visitedIds.add(queryId);
		
		Queue<TreeNode> q1= new LinkedList<TreeNode>();
		q1.add(root);
		while(true)
		{			
			Queue<TreeNode> q2= new LinkedList<TreeNode>();
			for(TreeNode tn: q1)
			{
				Vertex currVertex = tn.getVertex();
				List<Edge> children = currVertex.getNeighbors();
				for(Edge e : children)
				{
					Vertex child = e.getNeighborVertex(currVertex);
					if(!visitedIds.contains(child.getNode().getID()))
					{
						//create a new Tree node as this is the first time it is being visited..
						visitedIds.add(child.getNode().getID()); //mark that the node is visited..
						TreeNode childTreeNode =new TreeNode(child, tn); 
						q2.add(childTreeNode);
						tn.AddChild(childTreeNode);
					}
				}
			}

			if(q2.isEmpty())
			{
				for(TreeNode tn : q1)
				{	
					leaves.add(tn);
				}
				break; //tree construction is complete..
			}	
			
			//swap q2 with q1
			q1=q2;
		}
		//printLeaves();
		//printTree();
	}

	public void runBoxPropagation()
	{
		List<TreeNode> liNodes = leaves;
		List<TreeNode> parents;
		while(true)
		{	
			parents= new ArrayList<TreeNode>();		
			//get the parents of the leaf nodes..
			for(TreeNode tn: liNodes)
			{			
				TreeNode p =tn.getParent();
				if(p!=null)
				{
					//check whether it is a new parent or not..
					if(parents.size()==0)
					{
						parents.add(p);					
					}
					else
					{
						int size = parents.size();
						if(parents.get(size-1)!=p)
						{
							//new parent
							parents.add(p);
						}
					}
				}			
			}
			
			if(parents.size()==0)
			{
				//compute probabilities here..TODO
				break; //box propagation complete..
				
			}
			
			for(TreeNode tn : parents)
			{
				if(tn.isClauseNode())
				{
					//the parent is clause node..
					//all its children will be predicate nodes..
					
				}
				else
				{
					//the parent is predicate node.
					//all children will be clause node..
					
				}
			}
			
			liNodes=parents; //go to the next level of message passing..				
		}
		 
	}
	
	private void printLeaves()
	{
		System.out.println("Printing leaf nodes for the tree");
		for(TreeNode tn : leaves)
		{
			tn.printNode();			
		}		
	}
	
	private void printTree()
	{
		//prints a level order view of tree
		//for debugging purposes.
		System.out.println("Printing Level Order View of Tree");
		Queue<TreeNode> q = new LinkedList<TreeNode>();
		q.add(root);
		while(!q.isEmpty())
		{
			Queue<TreeNode> q2=new LinkedList<TreeNode>();
			for(TreeNode tn : q)
			{
				tn.printNode();
				List<TreeNode> children =tn.getChildren(); 
				for(TreeNode ch : children)
				{
					q2.add(ch);
				}
			}
			//swap queues
			q=q2;
			System.out.println(); // a new line to separate different levels..
		}
		System.out.println("End Printing Level Order View of Tree");
	}

}
