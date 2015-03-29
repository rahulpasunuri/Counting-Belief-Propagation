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
public class FactorGraph
{
    private Graph graph;

    public FactorGraph(ArrayList<Predicate> Predicates, ArrayList<Clause> Clauses)
    {
        initGraph(Predicates, Clauses);
    }

    private void initGraph(ArrayList<Predicate> Predicates, ArrayList<Clause> Clauses)
    {
        graph = new Graph();
        ArrayList<Node> preds = new ArrayList<Node>();
        for (Predicate p : Predicates)
        {
        	//System.out.println("Predicate id is "+p.getID());
            Node temp = new PredicateNode(p);
            preds.add(temp);
        }

        graph.addPredVertices(preds);

        for (Clause c : Clauses)
        {
            Node temp = new FactorNode(c);
            ArrayList<Integer> literals = c.getLiterals();
            Vertex v = new Vertex(temp);
            graph.addVertex(v);
            for (int lit : literals)
            {
                boolean sign=true;
                if(lit<0)
                    sign=false;
                Vertex v1 = graph.getPredVertexByID(Math.abs(lit));
                if (v1 != null)
                {
                    graph.addEdge(v, v1,sign);
                }
            }
        }
    }
    
    public Graph getGraph()
    {
        return graph;
    }

}
