/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Infer.GraphStructure;

import CBP.Compression.Predicate;
import java.util.ArrayList;

/**
 *
 * @author shrutika
 */
public class Graph
{

    private final ArrayList<Vertex> vertices;
    private final ArrayList<Edge> edges;
    private ArrayList<Vertex> predVertices = new ArrayList();
//    public ArrayList<CopyClusters> copyNodes = new ArrayList();
    private ArrayList<Vertex> randomVertexCluster= new ArrayList();
    private int count=1;
    private Vertex randomVertex;
    public Graph()
    {
        vertices = new ArrayList();
        edges = new ArrayList();
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
        } else
        {
//            Vertex v1 = new Vertex(v);
            v.setNodeID(count);
                count++;
            vertices.add(v);
            return true;
        }
    }

    /*
     Have to write a methode for this
     */
    public boolean addPredVertices(ArrayList<Node> nodes)
    {
        if (nodes == null)
        {
            throw new NullPointerException();
        } else
        {
            for (Node n : nodes)
            {
                Vertex v = new Vertex(n);
                v.setNodeID(count);
                count++;
                vertices.add(v);
                predVertices.add(v);
            }

            return true;
        }
    }

    public boolean addEdge(Vertex v1, Vertex v2, boolean t)
    {
        if (v1.equals(v2))
        {
            System.out.println("Tried adding edge"
                    + " between identical vertices");
            return false;
        } else
        {
            Edge temp = new Edge(v1, v2);
            temp.setSign(t);
            edges.add(temp);
            v1.addNeighbor(temp);
            v2.addNeighbor(temp);
            return true;

        }
        //return v1.addNeighbor(v2) && v2.addNeighbor(v1);
    }

    public Vertex getVertexByID(int id)
    {
//        System.out.println("out id: "+id);
        for (Vertex v : predVertices)
        {
            Node temp = v.getNode();
            if (temp.getID() == id)
            {
                
//                System.out.println("id: "+temp.getID());
                return v;
            }
        }
        return null;
    }

    public void printVertices()
    {
        for (Vertex v : vertices)
        {
            
            if (v.getNode().isClause)
            {
                System.out.print(" Clause\t" + v.getNode().getID());
            } else
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
//            System.out.print("\nVertice"+v.getNode().getClass());
        }

        System.out.println("\n" + vertices.size() + "\n\n\n");
    }

   

    void addEdge(Edge e)
    {
        edges.add(e);
    }

    public Vertex getRandomVertex()
    {
        randomVertex=vertices.get(0);
//        randomVertexCluster();
        return randomVertex;
    }


    public Edge getEdge(Vertex v, Vertex v1)
    {
        
        
            ArrayList<Edge> n = v.getNeighbors();
//            System.out.println(n.size());
            for(Edge e : n)
            {
                if(v1.equals(e.getNeighborVertex(v)))
                    return e;
            }
        
        return null;
    }

    
    public Vertex getClusteredVertexByID(int id)
    {
        System.out.println("out id: "+id);
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
    }
    
}
