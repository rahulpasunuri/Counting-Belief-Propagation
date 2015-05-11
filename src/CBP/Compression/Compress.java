/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

//import CBP.Infer.BeliefPropagation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import tuffy.db.RDB;
import tuffy.mln.MarkovLogicNetwork;
/**
 *
 * @author shrutika
 */
public class Compress
{

    private final RDB db;
    private ArrayList<Clause> clauses;
    private ArrayList<Predicate> predicates;
    private boolean colorChanged = true;
    private int changePred=0;
    private int changeClause=0;
    private HashMap<Integer, Predicate> PredicateMap=new HashMap<Integer, Predicate>();
    //this is the variable which holds the list of all compressed predicates..
    ArrayList<Predicate> comPredicates = new ArrayList<Predicate>();    
    //this is the variable which holds the list of all compressed clauses..
    ArrayList<Clause> comClauses = new ArrayList<Clause>();
    int k;
    String progFileName;
    
    private boolean disableCompression=false;
    
    public Compress(RDB db1, int noOfIterations, String progFileName)
    {
        db = db1;
        k = noOfIterations;
        this.progFileName = progFileName;
        disableCompression=true;
        //init all clauses and predicates..
        init();               
        if(disableCompression)
        {
	        //do the color passing..
	        long start_color = System.nanoTime();        
	        colorPassing();
	        long end_color = System.nanoTime();
	        
	        System.out.println("Time taken(in milli seconds) for Color Passing is "+Long.toString((end_color-start_color)/ (long)Math.pow(10, 6)) );
        }
        //compress the factor graph
        compression();        
    }

    private void init()
    {
        initializeClauses();
        System.out.println("Clauses initialized");
        initilalizePredicates();
        System.out.println("Predicates initialised");                      
    }

    //INIT clauses..fetches clauses from data base..
    private void initializeClauses()
    {
        clauses = new ArrayList<Clause>();
        try
        {
            ResultSet rs = db.query("Select cid,lits,weight from mln0_clauses");
            while (rs.next())
            {
                int id = rs.getInt("cid");
                double weight = rs.getDouble("weight");
                String lits = rs.getString("lits");

                ArrayList<Integer> lit = parseLiterals(lits);
                Clause temp;
                //System.out.println("Weights are "+weight);
                temp = new Clause(id, lit, weight);
                clauses.add(temp);
            }
        } 
        catch (SQLException e)
        {
            System.out.println(e);
        }
    }

    private ArrayList<Integer> parseLiterals(String x)
    {
        ArrayList<Integer> al = new ArrayList<Integer>();
        String temp[] = x.split("\\{");
        temp = temp[1].split("\\}");

        temp[0] = temp[0].replace(",", " ");
        String lits[] = temp[0].split(" ");
        for (String lit : lits)
        {
            al.add(Integer.parseInt(lit));
        }
        return al;
    }

    /*
     * Used to init Predicates.
     * Fetches predicates from database.
     * color is init to "R" for unknown predicates, "T" for true evidence and "F" for false evidence
     */
    private void initilalizePredicates()
    {
        predicates = new ArrayList<Predicate>();
        try
        {
            ResultSet rs = db.query("Select isquery,atomid,truth from mln0_atoms order by atomid");
            while (rs.next())
            {
                int aid = rs.getInt("atomid");
                String truth = rs.getString("truth");
                String c;
                boolean iquery = rs.getBoolean("isquery");
                boolean isEvidence = false;
                boolean evidenceVal = false;
                if (truth == null)
                {
                    c = "R";
                } 
                else
                {
                	//the truth value is not null..
                	//so this code doesn't happen.. 
                	isEvidence=true;
                    if (truth.equalsIgnoreCase("t"))
                    {
                        c = "T";
                        evidenceVal = true;
                    }
                    else
                    {
                        c = "F";
                        evidenceVal = false;
                    }
                }

                Predicate ptemp;
                if (isEvidence)
                {
                	//this never happens, as the evidence is not stored in the data base..
                    ptemp = new Predicate(aid, c, iquery, evidenceVal);
                } 
                else
                {
                    ptemp = new Predicate(aid, c, iquery);
                }
                predicates.add(ptemp);
            }
            
            for (Predicate p : predicates)
            {
            	PredicateMap.put(p.id, p);
            }
        } 
        catch (Exception e)
        {
            System.out.println(e);
        }
        
    }

    private void updateMessages()
    {
        //clear the list of messages..each predicate has..
        for(Predicate p : predicates)
        {            	
        	p.msg.clear();
        }
    	    	
    	//update clause messages here..
        for (Clause c : clauses)
        {
            c.msg.clear();
            ArrayList<Integer> lits = c.literals;
            for (int i : lits)
            {
                String temp =  getPred((Math.abs(i))).color;
                if (i < 0)
                {
                    temp = "N" + temp;
                }
                c.msg.addliteralMessage(temp);
            }
            
            //append clause's color at the end.
            c.msg.addliteralMessage(c.color);
                                    
            for (int i : lits)
            {
            	//add the clause message to the list of predicate messages..
                getPred((Math.abs(i))).msg.addClauseMsgToPredicate(c.msg.getMessage());
            }
        }        
    }

	//updates colors of all the clauses..
    private void assignNewClauseColors()
    {
    	changeClause=0;
    	//used to check the presence of hash
    	HashMap<Integer, Boolean> ids = new HashMap<Integer, Boolean>();
        //ArrayList<Integer> ids = new ArrayList<Integer>();

        int id = 0;
        //HashMap<String, Boolean> colors_list = new HashMap<String, Boolean>();
        //maps hash code to color
        HashMap<Integer, String> colors = new HashMap<Integer, String>();
        for (Clause c : clauses)
        {
            int hash = c.msg.getMessage().hashCode();
            //if (!ids.contains(hash))
            if(ids.get(hash) == null)
            {
//                This implies that the message is not seen before
//                so u have to assign a new color here
            	c.oldColor = c.color;
            	
            	c.color  = Character.toString((char) id);                
                id = id + 1;
                
                ids.put(hash, true);
                colors.put(hash, c.color);
                /*
                 * color change here should not be counted..
                //colors_list.put(nColor, true);
                if (!c.oldColor.equals(c.color))
                {
                    colorChanged = true;
                    changeClause++;
                }
                */                
                changeClause++;
            } 
            else
            {
            	//the clause's color already exists..
            	//get the corresponding color
            	c.oldColor=c.color;
            	c.color = colors.get(hash);                
            }
        }
    }

    private void assignNewPredColors()
    {
    	changePred=0;
    	//assigns new colors to the predicate vertices..
    	//used to check the presence of hash
    	HashMap<Integer, Boolean> ids = new HashMap<Integer, Boolean>();
        //ArrayList<Integer> ids = new ArrayList<Integer>();

        int id = 0;
        //HashMap<String, Boolean> colors_list = new HashMap<String, Boolean>();
        //maps hash code to color
        HashMap<Integer, String> colors = new HashMap<Integer, String>();
        for (Predicate p : predicates)
        {        	
            p.msg.sort();
            ArrayList<String> msg = p.msg.getMessage();
            Collections.sort(msg);
            
            int hash = msg.hashCode();

            if (ids.get(hash)==null)
            {
            	p.oldColor = p.color;
                String nColor = Character.toString((char) id);
                nColor = "P" + nColor;
      
                id = id + 1;

                //ids.add(hash);
                ids.put(hash, true);
                colors.put(hash, nColor);
                //colors_list.put(nColor,true);
                p.color = nColor;
                changePred++;
            } 
            else
            {
            	p.oldColor = p.color;
            	String nColor = colors.get(hash);
                p.color = nColor;            	          
            }
        }
    }

    private void colorPassing()
    {
    	System.out.println("Performing message passing");
        int i = 0;
        int max_iterations=10;
        
        changePred = predicates.size();
        changeClause = clauses.size();
        k=6;        
        //while ( (colorChanged || i<=2) && i<max_iterations)
        while(i<k)
        {
            float oldPred = changePred;
            float oldClause = changeClause;
        
        	//if(i==k)
            //	break;
            i++;
            
            colorChanged = false;
            //System.out.println("updating messages");
            updateMessages();
            //System.out.println("assigning new colors");
            assignNewPredColors();
            assignNewClauseColors();
            //System.out.println("Current iteration#: " + i);  
            
            
            float percentChangePred = ((float)((oldPred-changePred)*100))/predicates.size();
            float percentChangeClause = ((float)(oldClause-changeClause*100))/clauses.size();
            float minLimit=6;
            //System.out.println(percentChangePred);
            //System.out.println(percentChangeClause);
            System.out.println(changePred);
            System.out.println(changeClause);
            if( i>=2 && Math.abs(percentChangePred)  < minLimit && Math.abs(percentChangeClause) < minLimit)
            {
            	break;
            } 
                       
        }
        System.out.println("Total Number of color passing iterations: " + i);    
    }

    private Predicate getPred(Integer pid)
    {
    	return PredicateMap.get(pid);
    }
    
  //This step is the last step for compressing the graph... 
    private void compression()
    {
    	if(disableCompression)
    	{
    		for(Predicate p: predicates)
    		{                
                if (!p.clusters.contains(p.id))
                {
                    p.clusters.add(p.id);
                }
                comPredicates.add(p); //add it to the list of compressed predicates..    			
    		}
    		
    		for(Clause c : clauses)
    		{
                for (int k : c.literals)
                {                                                                                             
                    c.incrementIdenticalMessages(k);                        
                }    
                               
                if (!c.clusters.contains(c.id))
                {
                    c.clusters.add(c.id);
                }
                comClauses.add(c);                
    		}
    		
    		return;
    	}
    	
    	int max_size =-1;
    	
    	for(Clause c : clauses)
    	{
    		if (c.literals.size() > max_size)
    		{
    			max_size=c.literals.size();
    		}    		
    	}
    	System.out.println("Max size is:"+Integer.toString(max_size));
    	
        System.out.println("Running Compression");
        int index=0;
        HashMap<String, Integer> colors= new HashMap<String, Integer>();

        HashMap<Integer,Integer> predIdMap=new HashMap<Integer, Integer>(); 
        for (Predicate p : predicates)
        {
        	//if the color is a new color
            if (!colors.containsKey(p.color))
            {
                colors.put(p.color, index);
                index++;
                if (!p.clusters.contains(p.id))
                {
                    p.clusters.add(p.id);
                }
                predIdMap.put(p.id, p.id);
                comPredicates.add(p); //add it to the list of compressed predicates..
            } 
            else
            {
            	//this color has already been seen.
                int i = colors.get(p.color);
                Predicate p1 = comPredicates.get(i);
                if (!p1.clusters.contains(p.id))
                {
                    p1.clusters.add(p.id);
                }
                predIdMap.put(p.id, p1.id);
            }
        }

        colors.clear();        
        //map the old literal ids to new literal ids..
        for(Clause clause : clauses)
        {
            ArrayList<Integer> newLits = new ArrayList<Integer>();
            for (int k : clause.literals)
            {               
                int t = predIdMap.get(Math.abs(k));
                if (k < 0)
                {
                    t = t * -1;
                }
            	
                if (!newLits.contains(t))
                {
                    newLits.add(t);                                
                }                                                                                   
                clause.incrementIdenticalMessages(t);                        
            }    
            clause.literals = newLits;     	        	
        }
        
        HashMap<String, ArrayList<Clause>> colorMap=new HashMap<String, ArrayList<Clause>>();
        index=0;        
        for (Clause clause  : clauses)
        {
            //Clause clause = c;
            //if we encounter a new color
            if (!colorMap.containsKey(clause.color))
            {
                index++;
                if (!clause.clusters.contains(clause.id))
                {
                    clause.clusters.add(clause.id);
                }
                comClauses.add(clause);
            	ArrayList<Clause> temp = new ArrayList<Clause>();
            	temp.add(clause);
                colorMap.put(clause.color, temp);
            } 
            else
            {
                ArrayList<Clause> l = colorMap.get(clause.color);
                Clause final_clause=null;
                for(Clause c : l)
                {
                	if(c.literals.size() != clause.literals.size())
                	{
                		continue;
                	}
                	for(int h=0; h<c.literals.size(); h++)
                	{
                		if(c.literals.get(h) != clause.literals.get(h))
                		{
                			continue;
                		}                	
                	}
                	final_clause=c;
                	break;
                }
                
                if(final_clause!=null)
                {	                
                	final_clause.clusters.add(clause.id);
	                	               
	                //add the current clusters literal information..
	                for (int t : clause.literals)
	                {	                	                    	
	                	final_clause.incrementIdenticalMessages(t);	                                    
	                }
                }
                else
                {
                    index++;
                    if (!clause.clusters.contains(clause.id))
                    {
                        clause.clusters.add(clause.id);
                    }
                    comClauses.add(clause);
                	ArrayList<Clause> temp = new ArrayList<Clause>();
                	temp.add(clause);
                    colorMap.put(clause.color, temp);	                	
                }
            }
        }

        System.out.println("Predicates are compressed from "+predicates.size() + " predicates to  " + comPredicates.size()+" predicates");
        System.out.println("Clauses are compressed from "+clauses.size() + " clauses to " + comClauses.size()+" clauses");
    }
    ArrayList<Clause> getCompressedClauses()
    {
        return comClauses;
    }

    ArrayList<Predicate> getCompressedPreds()
    {
        return comPredicates;
    }

}
