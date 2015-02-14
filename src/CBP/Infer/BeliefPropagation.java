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

import tuffy.db.RDB;
import tuffy.mln.MarkovLogicNetwork;

/**
 *
 * @author shrutika
 */
public class BeliefPropagation
{

    private final ArrayList<Predicate> preds;
    private final ArrayList<Clause> clauses;
    private int iteration = 1;
    private boolean msgChanged = true;
    private final FactorGraph fg;
    private final Graph g;
    ArrayList<Vertex> vertices;
    private ArrayList<Query> queries;
    public BeliefPropagation(ArrayList<Predicate> p, ArrayList<Clause> c, ArrayList<Query> queries)
    {
        preds = p;
        clauses = c;
        fg = new FactorGraph(preds, clauses);
        g = fg.getGraph();
        vertices = g.getVertices();  
        this.queries=queries;
        run();
    }

    void factorGraphBP()
    {
        for (Vertex v : vertices)
        {
            ArrayList<Edge> neighbors = v.getNeighbors();
            if (v.getNode().isClause)
            {
            	//message from clauses to predicates..
                //Clauses
                Clause c = v.getClause();
                for (Edge n : neighbors)
                {
                    //predicates;
                    double True = 1.0;
                    double False = 1.0;
                    double productT = 1.0;
                    double productF = 1.0;
                    Predicate p = n.getNeighborVertex(v).getPredicate();

                    if (p.hasEvidence())
                    {
                        if (p.getEvidence())
                        {
                            False = 0.0;
                        } 
                        else
                        {
                            True = 0.0;
                        }
                    } 
                    else
                    {
                        if (n.getSign())
                        {
                        	//System.out.println("what???");
                            False = 0;
                            True = c.getweight();
                        } 
                        else
                        {
                            True = 0;
                            False = c.getweight();
                        }

                        for (Edge e : neighbors)
                        {
                            if (!n.equals(e))
                            {
                                Predicate p2 = e.getNeighborVertex(v).getPredicate();

                                if (p2.hasEvidence())
                                {
                                    if (p2.getEvidence())
                                    {
                                        productF = 0;
                                    } 
                                    else
                                    {
                                        productT = 0;
                                    }
                                } 
                                else
                                {
                                    productT *= e.getPredMsg().True;
                                    productF *= e.getPredMsg().False;
                                }
                            }
                        }
                    }

                    True *= productT;
                    False *= productF;
                    Message m2 = new Message();
                    m2.True = True;
                    m2.False = False;

                    Message oMsg = n.getClausemsg();
                    if (Math.abs(m2.False - oMsg.False) > 0.1 || Math.abs(m2.True - oMsg.True) > 0.1)
                    {
                        msgChanged = true;
                    }
                    n.setClauseMsg(m2);
                }

            } 
            else
            {
            	//message from predicates to clauses..
                for (Edge n : neighbors)
                {
                    Message m = new Message();
                    if (v.getPredicate().hasEvidence())
                    {
                    	//this never happens...
                        if (v.getPredicate().getEvidence())
                        {
                            m.False = 0.0;
                            m.True = 1.0;
                        } 
                        else
                        {
                            m.True = 0.0;
                            m.False = 1.0;
                        }
                    } 
                    else
                    {
                        if (iteration == 1)
                        {
                            m.False = 1.0;
                            m.True = 1.0;
                        } 
                        else
                        {

                            double t, f;
                            
                            int k = n.getNeighborVertex(v).getClause().getIdenticalMsgs(v.getPredicate().getID())-1; //RAHUL
                            t = Math.pow(n.getClausemsg().True, k);
                        	f = Math.pow(n.getClausemsg().False, k);
                            for (Edge n1 : neighbors)
                            {
                                //int k = n1.getNeighborVertex(v).getClause().getIdenticalMsgs(v.getPredicate().getID()) * v.getPredicate().getClusterSize();                            	
                            	if (!n.equals(n1))
                                {
                            		int k2 = n1.getNeighborVertex(v).getClause().getIdenticalMsgs(v.getPredicate().getID())-1; //RAHUL
                                    t *= Math.pow(n1.getClausemsg().True, k2);
                                    f *= Math.pow(n1.getClausemsg().False, k2);
                                }
                            }
                            m.True = t;
                            m.False = f;
                        }
                    }

                    Message oMsg = n.getPredMsg();
                    if (Math.abs(m.False - oMsg.False) > 0.1 || Math.abs(m.True - oMsg.True) > 0.1)
                    {
                        msgChanged = true;
                    }

                    n.setPredmsg(m);
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
/*
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
*/
    /*
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
    */
    
    public void computeProbabilities() throws IOException
    {
        System.out.println("Computing Probabilities");
        String fileName = "results.txt";
        File file = new File(fileName);

        // if file doesn't exists, then create it
        if (!file.exists())
        {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
                         

        //TODO
        //set the value of iQuerys
        for (Query q : queries)
        {            
            Vertex v = g.getClusteredPredicateVertexByID(q.id);
            Predicate p = v.getPredicate();
            if (p.hasEvidence())
            {
            	//predicate already has evidence..
                double pro=1.0;
                if(!p.getEvidence())
                    pro=0.0;
                String temp="";
            	temp += q.query+": "+pro+"\n";	                	
                                
                bw.write(temp);
            } 
            else
            {
                ArrayList<Edge> neighbors = v.getNeighbors();
                double True = 1;
                double False = 1;

                for (Edge e : neighbors)
                {
                    //int k = e.getNeighborVertex(v).getClause().getIdenticalMsgs(p.getID()) * p.getClusterSize();
                	int k = e.getNeighborVertex(v).getClause().getIdenticalMsgs(p.getID()); //RAHUL
                    Message m = e.getPredMsg();
                    True *= Math.pow(m.True, k);
                    False *= Math.pow(m.False, k);
                    System.out.println(True+"\t"+False);
                }

                String temp = q.query+": "+(True / (True+False))+"\n";;                                
                bw.write(temp);
            }

        }
        bw.flush();
        bw.close();    
        System.out.println("Results have been saved in: "+fileName);
    }
}
