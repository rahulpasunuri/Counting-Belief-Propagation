/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Infer.GraphStructure;

import CBP.Compression.Clause;
import CBP.Compression.Predicate;
import java.util.ArrayList;

/**
 *
 * @author shrutika
 */
public class Vertex
{
    
    private int nodeID=0;
    private final Node node;
    private final ArrayList<Edge> neighbors;
    private boolean visited = false;
    private boolean isCopyNode=false;
    public boolean forDFS=false;
    private int copyID;
    
    public Vertex(Node n)
    {
        node = n;
        neighbors = new ArrayList<Edge>();
    }
    
    public Node getNode()
    {
        return node;
    }
    public Predicate getPredicate()
    {
        return node.getPredicate();
    }
    
    public ArrayList<Edge> getNeighbors()
    {
        return neighbors;
    }
    
    public boolean addNeighbor(Edge e)
    {
        if(e==null)
        {
            throw new NullPointerException();
        }
        else
        {
            neighbors.add(e);
            return true;
        }
        
    }
    public void setNodeID(int x)
    {
        nodeID=x;
    }
    public void markVisited()
    {
        visited = true;
        
    }
    public boolean isVisited()
    {
        return visited;
    }
    
    
    
    public boolean isNeighbor(Edge v)
    {
        return neighbors.contains(v);
    }
    
    public boolean isCopyNode()
    {
        return isCopyNode;
    }
    public void setCopyNode()
    {
        isCopyNode=true;
    }
    public void setCopyID(int x)
    {
        copyID=x;
    }
    public int getCopyID()
    {
        return copyID;
    }

    public Clause getClause()
    {
        return node.getClause();
           
    }

    
}
