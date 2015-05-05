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
    //private final String[] color; 
    private ArrayList<Clause> clauses;
    private ArrayList<Predicate> predicates;
    private boolean colorChanged = true;
    
    //this is the variable which holds the list of all compressed predicates..
    ArrayList<Predicate> comPredicates = new ArrayList<Predicate>();
    private MarkovLogicNetwork mln;
    //this is the variable which holds the list of all compressed clauses..
    ArrayList<Clause> comClauses = new ArrayList<Clause>();
    int k;
    String progFileName;
    public Compress(RDB db1, MarkovLogicNetwork mln,int noOfIterations, String progFileName, boolean debug)
    {
        db = db1;
        k = noOfIterations;
        this.mln = mln;
        this.progFileName = progFileName;
        
        //init all clauses and predicates..
        init(debug);               
    }

    private void init(boolean debug)
    {
        initializeClauses();
        System.out.println("Clauses initialized");
        initilalizePredicates();
        System.out.println("IPreds initialised");
        if(debug)
        {
        	//disabling these two to debug BP code..
	        colorPassing();
	        compression();
        }
        else
        {
        	for(Clause c : clauses)
            {
            	for(int l : c.getLiterals())
            	{
            		c.incrementIdenticalMessages(l);
            	}
            }
        	comPredicates=predicates;
        	comClauses=clauses;
        }
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
                String temp = getPred((Math.abs(i))).color;
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
    	//used to check the presence of hash
    	HashMap<Integer, Boolean> ids = new HashMap<Integer, Boolean>();
        //ArrayList<Integer> ids = new ArrayList<Integer>();

        int id = 0;
        //HashMap<String, Boolean> colors_list = new HashMap<String, Boolean>();
        //maps hash code to color
        HashMap<Integer, String> colors = new HashMap<Integer, String>();
        int cID = 65;       
        
        for (Clause c : clauses)
        {
            int hash = c.msg.getMessage().hashCode();
            //if (!ids.contains(hash))
            if(ids.get(hash) == null)
            {
//                This implies that the message is not seen before
//                so u have to assign a new color here
            	
                String nColor = Character.toString((char) cID);
                nColor = "C" + nColor;
                cID++;
                id = id + 1;

                c.color = nColor;
                ids.put(hash, true);
                colors.put(hash, nColor);
                //colors_list.put(nColor, true);
                if (!c.oldColor.equals(c.color))
                {
                    colorChanged = true;
                }
                c.oldColor = c.color;
            } 
            else
            {
            	//the clause's color already exists..
            	//get the corresponding color
                String nColor = colors.get(hash);
                c.color = nColor;
                if (!c.oldColor.equals(c.color))
                {
                    colorChanged = true;
                }
                c.oldColor = c.color;
            }

            c.msg.clear();
        }
    }

    private void assignNewPredColors()
    {
    	//assigns new colors to the predicate vertices..
    	//used to check the presence of hash
    	HashMap<Integer, Boolean> ids = new HashMap<Integer, Boolean>();
        //ArrayList<Integer> ids = new ArrayList<Integer>();

        int id = 0;
        //HashMap<String, Boolean> colors_list = new HashMap<String, Boolean>();
        //maps hash code to color
        HashMap<Integer, String> colors = new HashMap<Integer, String>();
    	
    	
        //ArrayList<Integer> ids = new ArrayList<Integer>();;

        //ArrayList<String> colors = new ArrayList<String>();
        char cID = 0;
        for (Predicate p : predicates)
        {
            cID++;
            ArrayList<String> msg = p.msg.getMessage();
            Collections.sort(msg);
            int hash = msg.hashCode();

            if (ids.get(hash)==null)
            {
                String nColor = String.valueOf((cID));
                nColor = "P" + nColor;
                int t = 0;
      
                id = id + 1;

                //ids.add(hash);
                ids.put(hash, true);
                colors.put(hash, nColor);
                //colors_list.put(nColor,true);
                p.color = nColor;
                if (!p.oldColor.equals(p.color))
                {
                    colorChanged = true;
                }
                p.oldColor = p.color;
            } 
            else
            {
                String nColor = colors.get(hash);
                p.color = nColor;
                if (!p.oldColor.equals(p.color))
                {
                    colorChanged = true;
                }
                p.oldColor = p.color;
            }

            p.msg.clear();

        }

    }

    private void colorPassing()
    {
        int i = 0;
        while (colorChanged)
        {

            if(i==k)
            	break;
            i++;
            
            colorChanged = false;
            //System.out.println("updating messages");
            updateMessages();
            //System.out.println("assigning new colors");
            assignNewPredColors();
            assignNewClauseColors();
            
            System.out.println("Current Iteration #"+i);
        }
        System.out.println("# of iterations: " + i);    
    }

    private Predicate getPred(Integer pid)
    {
        for (Predicate p : predicates)
        {
            if (p.id == pid)
            {
                return p;
            }
        }
        return null;
    }

    //This step is the last step for compressing the graph... 
    private void compression()
    {
        System.out.println("Running Compression");
        int index=0;
        HashMap<String, Integer> colors= new HashMap<String, Integer>();
        //ArrayList<String> colors = new ArrayList<String>();
        for (Predicate p : predicates)
        {
        	//if the color is a new color
            if (!colors.containsKey(p.color))
            {
                colors.put(p.color, index);
                index++;
                comPredicates.add(p); //add it to the list of compressed predicates..
                if (!p.clusters.contains(p.id))
                {
                    p.clusters.add(p.id);
                }
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
            }
        }

        colors.clear();
        index=0;
        for (Clause c : clauses)
        {
            Clause clause = c;
            //if we encounter a new color
            if (!colors.containsKey(clause.color))
            {
                colors.put(clause.color, index);
                index++;
                if (!clause.clusters.contains(clause.id))
                {
                    clause.clusters.add(clause.id);
                }

                ArrayList<Integer> newLits = new ArrayList<Integer>();
                for (int k : clause.literals)
                {
                    for (Predicate p : comPredicates)
                    {
                        if (p.clusters.contains(Math.abs(k)))
                        {
                            int t = p.id;
                            if (k < 0)
                            {
                                t = t * -1;
                            }
                        	
                            if (!newLits.contains(t))
                            {
                                newLits.add(t);                                
                            }                                                                                   
                            clause.incrementIdenticalMessages(k);
                            
                            break;
                        }
                    }
                }
        
                clause.literals = newLits;
                comClauses.add(clause);
            } 
            else
            {
                int i = colors.get(clause.color);
                Clause c1 = comClauses.get(i);
                if (!c1.clusters.contains(clause.id))
                {
                    c1.clusters.add(clause.id);
                }
                
                
                //add the current clusters literal information..
                for (int k : clause.literals)
                {
                    for (Predicate p : comPredicates)
                    {
                        if (p.clusters.contains(Math.abs(k)))
                        {
                            int t = p.id;
                            if (k < 0)
                            {
                                t = t * -1;
                            }
                        	
                            if (!c1.literals.contains(t))
                            {
                            	c1.literals.add(t);
                            } 
                            else
                            {                            	
                                c1.incrementIdenticalMessages(k);
                            }
                            break;
                        }
                    }
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
