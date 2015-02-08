/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Infer.GraphStructure;

/**
 *
 * @author shrutika
 */
public class Edge
{	//v1 is a clause..
	//v2 is a predicate..
	
    private Vertex v1; //this is the clause
    private Vertex v2;
    private boolean isEdgeVisited=false;
    
    
    //whats the exact distinction between the below three??
    private double variableToClause[]={1.0,1.0};
    private Message clauseMsg;
    private Message predMsg;
    
    
    private boolean sign=true;
    public Edge(Vertex a, Vertex b)
    {
        v1=a;
        v2=b;
        clauseMsg = new Message();
        predMsg= new Message();
    }
    
    public void setEdgeVisited()
    {
        isEdgeVisited=true;
    }
    
    public boolean isEdgeVisited()
    {
        return isEdgeVisited;
    }

    public Vertex getNeighborVertex(Vertex v)
    {
        if(v1.equals(v))
            return v2;
        else
            return v1;
    }

    Vertex getClause()
    {
        return v1;
    }

    Vertex getPredicate()
    {
        return v2;
    }
    public void setEvidenceMessage(int evidence)
    {
        variableToClause[(evidence+1)%2] =0.0;        
    }

    public void setClauseMsg(Message msg)
    {
        clauseMsg=msg;
    }
    public Message getClausemsg()
    {
        return clauseMsg;
    }

    public Message getPredMsg()
    {
        return predMsg;
    }

    public void setPredmsg(Message msg)
    {
        predMsg=msg;
    }

    public void setSign(boolean t)
    {
        sign=t;
    }
    
    public boolean getSign()
    {
        return sign;
    }
}
