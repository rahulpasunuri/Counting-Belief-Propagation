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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import tuffy.util.Config;

import com.sun.xml.internal.ws.handler.ClientMessageHandlerTube;

/**
 *
 * @author shrutika
 */
public class BeliefPropagation
{
	private static HashMap<Integer, ArrayList<String>> tfComb;
	private static int maxTFComb;
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
    	if(BeliefPropagation.tfComb==null)
    	{
    		BeliefPropagation.tfComb=new HashMap<Integer, ArrayList<String>>();
    		ArrayList<String> single = new ArrayList<String>();
    		single.add("T");
    		single.add("F");
    		BeliefPropagation.tfComb.put(1, single);
    		
    		BeliefPropagation.maxTFComb = 1;
    	}
        preds = p;
        clauses = c;
        fg = new FactorGraph(preds, clauses);
        g = fg.getGraph();
        vertices = g.getVertices();  
        this.queries=queries;
        run();
    }

    private ArrayList<String> createTFCombinations(int length)
    {
    	//System.out.println("Length is :"+Integer.toString(length));
    	//writing a iterative version instead of a recursive version to overcome the GC overhead exception.
    	if(BeliefPropagation.tfComb.containsKey(length))
    	{
    		return BeliefPropagation.tfComb.get(length);
    	}
    	ArrayList<String> res = new ArrayList<String>();
    	
    	int start=BeliefPropagation.maxTFComb;
		for(int i= start+1; i<=length; i++)
		{
			res = new ArrayList<String>();
			ArrayList<String> t = createTFCombinations(i-1);
			int size = t.size();
	    	for(int j=0; j<size; j++)
	    	{
	    		res.add("T"+t.get(j));
	    		res.add("F"+t.get(j));
	    	}		    	
	    	BeliefPropagation.tfComb.put(i, res);
		}

		BeliefPropagation.maxTFComb = length;
	
    	//System.out.println("Length is "+Integer.toString(length));
    	return res;
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
                    Predicate p = n.getNeighborVertex(v).getPredicate();                    
                    Message m = new Message();                    
                    
                    //CASE WHERE THERE IS ONLY ONE PREDICATE NODE..
    				if(c.getLiterals().size() == 1)
    				{
    					if(n.getSign())
    					{
    						m.True= Math.exp(c.getweight());
    						m.False = 1;
    					}
    					else
    					{
    						m.True = 1;
    						m.False = Math.exp(c.getweight());						
    					}
    				}
    				else
    				{
    					//System.out.println("Literal Size is "+ Integer.toString(v.getClause().getLiterals().size()-1));
    					ArrayList<String> comb = createTFCombinations(v.getClause().getLiterals().size()-1);
						m.True=0;
						m.False=0;
    					for(String s : comb)
    					{
							double potentialTrue = Math.exp(c.getweight());
							double potentialFalse = Math.exp(c.getweight());
							//double potential = f.cl.weight;
							//determine the truthness of the clause..
							int k=0, strIndex = 0;
							boolean isTrue = false;
							for(Edge e : neighbors)
							{
								if(n.equals(e))
								{									
									continue;
								}

								if(( !e.getSign() && s.charAt(strIndex) == 'F') || (e.getSign() && s.charAt(strIndex) == 'T'))
								{									
									isTrue = true;
									break;	
								}								
								strIndex++;							
							}
					
							if(n.getSign())
							{								
								if(!isTrue)
								{
									potentialFalse = 1;
								}			 								 
							}
							else
							{								
								if(!isTrue)
								{
									potentialTrue = 1;
								}			 		
							}   
							double tempTrue = potentialTrue;
							double tempFalse = potentialFalse;
							//System.out.println("Potentials are"+potentialTrue+"\t"+potentialFalse);
							//System.out.println("Neighbors count#"+neighbors.size());						
							strIndex = 0;;
							for(Edge e  : neighbors)
							{
								if(!n.equals(e))
								{
									Message pmsg = e.getPredMsg();
									if(s.charAt(strIndex) == 'T')
									{
										tempTrue *= pmsg.True;
										tempFalse *= pmsg.True; 
									}
									else
									{
										tempTrue *= pmsg.False;
										tempFalse *= pmsg.False;								
									}									
									strIndex++;
								}								
							}
							m.True += tempTrue;
							m.False += tempFalse;													
    					}    					
    				}                   
    				//System.out.println("Priting Clause Message "+m.True+"\t"+m.False);
    				/*
    				Message oMsg = n.getClausemsg();
                    if (Math.abs(m.False - oMsg.False) > 0.1 || Math.abs(m.True - oMsg.True) > 0.1)
                    {
                        msgChanged = true;
                    }
                    */
                    n.setClauseMsg(m);
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
                    
                    //normalize the message
                    double sum = m.True+m.False;
                    if(sum!=0)
                    {
                    	m.True /= sum;
                    	m.False /=sum;
                    }
    				//System.out.println("Priting Predicate Message "+m.True+"\t"+m.False+"\t"+iteration);
                    /*
    				Message oMsg = n.getPredMsg();
                    if (Math.abs(m.False - oMsg.False) > 0.1 || Math.abs(m.True - oMsg.True) > 0.1)
                    {
                        msgChanged = true;
                    }
					*/
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
            //check for convergence..
            for(Vertex v : vertices)
            {
            	if(!v.getNode().isClause)
            	{
                    Predicate p = v.getPredicate();
                    ArrayList<Edge> neighbors = v.getNeighbors();
                    double True = 1;
                    double False = 1;

                    for (Edge e : neighbors)
                    {
                        //int k = e.getNeighborVertex(v).getClause().getIdenticalMsgs(p.getID()) * p.getClusterSize();
                    	int k = e.getNeighborVertex(v).getClause().getIdenticalMsgs(p.getID()); //RAHUL
                        Message m = e.getClausemsg();
                        True *= Math.pow(m.True, k);
                        False *= Math.pow(m.False, k);
                    }
                    
                    double newProb=0;
                    if(True+False!=0)
                    {
                    	newProb=True / (True+False);                    	
                    }
                    
                    if(!msgChanged && Math.abs(newProb-p.probability) > 0.1)
                    {
                    	msgChanged=true;
                    }
                    p.probability=newProb;
                    //System.out.println(newProb+"\t"+p.probability);
            	}                	
            }            	            	
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
        int nan=0;
        for (Query q : queries)
        {   
        	//System.out.println("what????");
            Vertex v = g.getClusteredPredicateVertexByID(q.id);
            Predicate p = v.getPredicate();
            
            //handle the case where probability is nan..
            if(p.probability != p.probability)
            {	
            	nan++;
            	p.probability=0;
            	//TODO
            	//we are getting nan's because of really high clause weights..
            	//have to look at the reason for high weights generated by tuffy..
            }
            else
            {
            	q.probability=p.probability;
            }            		
        }
        
        //sort the queries based on their probabilities
        //from highest to lowest..
        Collections.sort(queries, new Comparator<Query>()
		{
			public int compare(Query o1, Query o2)
			{
				if(o1.probability > o2.probability)
				{
					return -1;
				}
				else if(o1.probability == o2.probability)
				{
					return 0;
				}
				return 1;
			}
		});
        
        for(Query q : queries)
        {  
        	//format the probabilities to 4 decimal points..
        	//doing this to match tuffy's output..
        	//new DecimalFormat("#.##").format(q.probability)
        	String prob = new DecimalFormat("#.####").format(q.probability);
        	
        	if( prob.length() < 6)
        	{
        		if(prob.length()==1)
        		{
        			prob+=".";
        		}
        		int l = prob.length();
        		for(int i=0; i<6-l; i++)
        		{
        			prob+="0";
        		}
        	}
        	
        	bw.write(prob+"\t"+q.query);
        	bw.newLine();
        }
        
        bw.flush();
        bw.close();    
        if(nan>0)
        System.out.println("Number of skipped cases: "+nan);
        System.out.println("Results have been saved in: "+fileName);
    }
}
