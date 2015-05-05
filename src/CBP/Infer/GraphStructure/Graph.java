/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Infer.GraphStructure;

import CBP.Compression.Predicate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author shrutika
 */
public class Graph
{

    private final ArrayList<Vertex> vertices;
    private final ArrayList<Edge> edges;
    private ArrayList<Vertex> predVertices = new ArrayList<Vertex>();
    private HashMap<Integer, Vertex>  predVertexMap = new HashMap<Integer, Vertex>();
    
    private HashMap<Integer, Integer>  predClusterMap = new HashMap<Integer, Integer>();
    
    private int count=1;
    public Graph()
    {
        vertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
    }

    public ArrayList<Vertex> getVertices()
    {
        return vertices;
    }
        
        
    public boolean addVertex(Vertex v)
    {
        if (v == null)
        {
            throw new NullPointerException();
        } 
        else
        {
            v.setNodeID(count);
            count++;
            vertices.add(v);
            return true;
        }
    }
	
    
    public boolean addPredVertices(ArrayList<Node> nodes)
    {
        if (nodes == null)
        {
            throw new NullPointerException();
        } 
        else
        {
            for (Node n : nodes)
            {
                Vertex v = new Vertex(n);
                addVertex(v);
                predVertices.add(v);
                int predId = v.getNode().getID();
                predVertexMap.put(predId,v);
                
                for(int id : v.getPredicate().getClusters())
                {
                	predClusterMap.put( id, predId);                	
                }
            }

            return true;
        }
    }

    public boolean addEdge(Vertex v1, Vertex v2, boolean t)
    {
        if (v1.equals(v2))
        {
            System.out.println("Tried adding edges between identical vertices");
            return false;
        } 
        else
        {
            Edge temp = new Edge(v1, v2);
            temp.setSign(t);
            edges.add(temp);
            v1.addNeighbor(temp);
            v2.addNeighbor(temp);
            return true;
        }
    }

    public Vertex getPredVertexByID(int id)
    {
    	/*
        for (Vertex v : predVertices)
        {
            Node temp = v.getNode();
            if (temp.getID() == id)
            {                
                return v;
            }
        }
        */
        return predVertexMap.get(id);
        //return null;
    }

    public void printVertices()
    {
        for (Vertex v : vertices)
        {
            
            if (v.getNode().isClause)
            {
                System.out.print(" Clause\t" + v.getNode().getID());
            } 
            else
            {
                System.out.print(" Predicate\t" + v.getNode().getID());
            }
            ArrayList<Edge> neighbors = v.getNeighbors();
            for (Edge n : neighbors)
            {
                
                if (n.getNeighborVertex(v).getNode().isClause)
                {
                    System.out.print("\n\tClause\t" + n.getNeighborVertex(v).getNode().getID());
                } else
                {
                    System.out.print("\n\tPredicate\t" + n.getNeighborVertex(v).getNode().getID());
                }
            }
            System.out.println();
        }

        System.out.println("\n" + vertices.size() + "\n\n\n");
    }

   

    void addEdge(Edge e)
    {
        edges.add(e);
    }

    public Edge getEdge(Vertex v, Vertex v1)
    {               
            ArrayList<Edge> n = v.getNeighbors();
            for(Edge e : n)
            {
                if(v1.equals(e.getNeighborVertex(v)))
                    return e;
            }
        
        return null;
    }

    
    public Vertex getClusteredPredicateVertexByID(int id)
    {
    	/*
        for (Vertex v : predVertices)
        {            
            Predicate p = v.getPredicate();
            ArrayList<Integer> clusters = p.getClusters();
            for (int k: clusters)
            {
                if(k==id)                
                	return v;
            }
        }
        return null;
        */
    	int predId = predClusterMap.get(id);
    	return getPredVertexByID(predId);
    }    
}
