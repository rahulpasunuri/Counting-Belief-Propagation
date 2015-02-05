/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Infer;

import CBP.Compression.Clause;
import CBP.Compression.Predicate;
import CBP.Infer.GraphStructure.Edge;
import CBP.Infer.GraphStructure.FactorGraph;
import CBP.Infer.GraphStructure.Graph;
import CBP.Infer.GraphStructure.Message;
import CBP.Infer.GraphStructure.Vertex;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author shrutika
 */
public class BeliefPropagation
{

    private final ArrayList<Predicate> preds;
    private final ArrayList<Clause> clauses;
    private int iteration = 1;
    private boolean convergence = false;
    private boolean msgChanged = true;
    private final FactorGraph fg;
    private final Graph g;
    ArrayList<Vertex> vertices;

    public BeliefPropagation(ArrayList<Predicate> p, ArrayList<Clause> c)
    {
        preds = p;
        clauses = c;
//        System.out.println("size of preds: " + preds.size());
        fg = new FactorGraph(preds, clauses);
        g = fg.getGraph();
        vertices = g.getVertices();
        run();

    }

    void factorGraphBP()
    {

        for (Vertex v : vertices)
        {
            ArrayList<Edge> neighbors = v.getNeighbors();

            if (v.getNode().isClause)
            {
                //Clauses
//                System.out.println(" Clause\t" + v.getNode().getID());
                Clause c = v.getClause();

//                                System.out.println(k);
                int j = 0;
                for (Edge n : neighbors)
                {
                    //predicates;

                    double True = 1.0;
                    double False = 1.0;
                    double productT = 1.0;
                    double productF = 1.0;
                    Predicate p = n.getNeighborVertex(v).getPredicate();
//                    int k = c.getIdenticalMsgs(j) * p.getClusterSize();

                    if (p.hasEvidence())
                    {
                        if (p.getEvidence())
                        {
                            False = 0.0;
                        } else
                        {
                            True = 0.0;
                        }
                    } else
                    {

                        if (n.getSign())
                        {
                            False = 0;
                            True = c.getweight();
                        } else
                        {
                            True = 0;
                            False = c.getweight();
                        }

                        for (Edge e : neighbors)
                        {
                            if (!n.equals(e))
                            {
//                                Vertex v2 = e.getNeighborVertex(v);
                                Predicate p2 = e.getNeighborVertex(v).getPredicate();

                                if (p2.hasEvidence())
                                {
                                    if (p2.getEvidence())
                                    {
                                        productF = 0;
                                    } else
                                    {
                                        productT = 0;

                                    }
                                } else
                                {
                                    productT *= e.getClausemsg().True;
                                    productF *= e.getClausemsg().False;
                                }
                            }
                        }
                    }

                    True *= productT;
                    False *= productF;
                    Message m2 = new Message();
                    m2.True = True;
                    m2.False = False;

                    Message oMsg = n.getPredMsg();
                    if (Math.abs(m2.False - oMsg.False) > 0.1 || Math.abs(m2.True - oMsg.True) > 0.1)
                    {
                        msgChanged = true;
                    }
                    n.setPredmsg(m2);

                }

            } else
            {
                for (Edge n : neighbors)
                {
                    Message m = new Message();
                    if (v.getPredicate().hasEvidence())
                    {
                        if (v.getPredicate().getEvidence())
                        {
                            m.False = 0.0;
                            m.True = 1.0;
                        } else
                        {
                            m.True = 0.0;
                            m.False = 1.0;
                        }
                    } else
                    {
                        if (iteration == 1)
                        {
                            m.False = 1.0;
                            m.True = 1.0;
                        } else
                        {

                            double t, f;
                            t = f = 1;

//                            int j=0;
                            for (Edge n1 : neighbors)
                            {
//                                System.out.println("size of lits: "+n1.getNeighborVertex(v).getClause().getLiterals().size()+" "+j);
                                int k = n1.getNeighborVertex(v).getClause().getIdenticalMsgs(v.getPredicate().getID()) * v.getPredicate().getClusterSize();
                                if (!n.equals(n1))
                                {
                                    t = t * n1.getClausemsg().True;
                                    f = f * n1.getClausemsg().False;
                                } else
                                {
                                    k--;
                                }
                                t = Math.pow(t, k);
                                f = Math.pow(f, k);
//                                j++;
                            }
                            m.True = t;
                            m.False = f;

                        }
                    }

                    Message oMsg = n.getClausemsg();
                    if (Math.abs(m.False - oMsg.False) > 0.1 || Math.abs(m.True - oMsg.True) > 0.1)
                    {
                        msgChanged = true;
                    }

                    n.setClauseMsg(m);
                }

            }

        }
        iteration++;
    }

    private void run()
    {
        while (msgChanged)
        {
            msgChanged = false;
            factorGraphBP();
        }
    }

    private Predicate getPred(Integer pid)
    {
        for (Predicate p : preds)
        {
            if (p.getID() == pid)
            {
                return p;
            }
        }
        return null;
    }

    private void printMsgs()
    {

        for (Vertex v : vertices)
        {
            ArrayList<Edge> neighbors = v.getNeighbors();
            for (Edge n : neighbors)
            {
                System.out.println(n.getPredMsg().True + "\t" + n.getPredMsg().False + "");
            }

        }

    }

    
    public void printPredIds()
    {
        for (Vertex v : vertices)
        {
           if(!v.getNode().isClause)
           {
               Predicate p = v.getPredicate();
               if(p.hasEvidence())
               {
                   System.out.println(v.getNode().getID()+" "+p.getID());
               }
           }
        }
    }
    
    public void computeProbabilities() throws IOException
    {
        ArrayList<Query> iQuerys = null;
        File file = new File("results.txt");

        // if file doesn't exists, then create it
        if (!file.exists())
        {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        //TODO
        //set the value of iQuerys
        if(iQuerys!=null)
        for (Query q : iQuerys)
        {
            System.out.println(q.id);
            Vertex v = g.getClusteredVertexByID(q.id);
            Predicate p = v.getPredicate();
            if (p.hasEvidence())
            {
                double pro=1.0;
                if(!p.getEvidence())
                    pro=0.0;
                String temp = q.query+" "+pro;
                bw.write(temp);
                bw.newLine();
            } 
            else
            {
                ArrayList<Edge> neighbors = v.getNeighbors();
                double True = 0.0;
                double False = 0.0;

                for (Edge e : neighbors)
                {
                    int k = e.getNeighborVertex(v).getClause().getIdenticalMsgs(p.getID()) * p.getClusterSize();
                    Message m = e.getPredMsg();
                    True += Math.pow(m.True, k);
                    False += Math.pow(m.False, k);
                }

                double num = Math.exp(True);
                double denom = Math.exp((True)) + Math.exp((False));

                String temp = q.query+" "+num / denom;
                bw.write(temp);
                bw.newLine();
            }

        }
        
        bw.close();
    }
}
