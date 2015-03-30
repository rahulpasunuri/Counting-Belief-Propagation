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
	
	public Tree(FactorGraph g, int queryId)
	{
		//creates a tree using the query as root.
		this.queryId= queryId;
		this.fg = g;
		runBFS();
	}
	
	
	private void runBFS()
	{
		System.out.println("Creating a Tree for the Query node");
		List<Integer> visitedIds = new ArrayList<Integer>();
		root = new TreeNode(fg.getGraph().getClusteredPredicateVertexByID(queryId), null); //parent is null for root.
		visitedIds.add(queryId);
		
		Queue<TreeNode> q1= new LinkedList<TreeNode>();
		q1.add(root);
		while(!q1.isEmpty())
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
						TreeNode childTreeNode =new TreeNode(child, tn); 
						q2.add(childTreeNode);
						tn.AddChild(childTreeNode);
					}
				}
			}
			//swap q2 with q1
			q1=q2;
		}
		printTree();
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
				for(TreeNode ch : tn.getChildren())
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
