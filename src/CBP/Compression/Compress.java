/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import CBP.Infer.BeliefPropagation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
    private int iteration = 1;
    private boolean colorChanged = true;
    
    //this is the variable which holds the list of all compressed predicates..
    ArrayList<Predicate> comPredicates = new ArrayList<Predicate>();
    
    //this is the variable which holds the list of all compressed clauses..
    ArrayList<Clause> comClauses = new ArrayList<Clause>();
    int k;

    public Compress(RDB db1, int noOfIterations)
    {
        db = db1;
        k = noOfIterations;
        init();
    }

    private void init()
    {
        initializeClauses();
        System.out.println("Clauses initialized");
        initilalizePredicates();
        System.out.println("IPreds initialised");
        colorPassing();
        compression();
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
                ArrayList<Integer> tempCluster = new ArrayList<Integer>();
                tempCluster.add(id);
                CMessage m = new CMessage();
                Clause temp;
                
                //initial value of color is the weight.
                String color = "" + weight;
                temp = new Clause(id, tempCluster, lit, weight, m, color);
                clauses.add(temp);
            }

        } catch (SQLException e)
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
                ArrayList<Integer> temp = new ArrayList<Integer>();
                temp.add(aid);
                PMessage pmsg = new PMessage();
                Predicate ptemp;
                if (isEvidence)
                {
                    ptemp = new Predicate(aid, temp, c, pmsg, evidenceVal, iquery);
                } 
                else
                {
                    ptemp = new Predicate(aid, temp, c, pmsg,iquery);
                }
                predicates.add(ptemp);
            }

        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    private void updateMessages()
    {
    	//update clause messages here..
        for (Clause c : clauses)
        {
            c.msg.clear();
            ArrayList<Integer> lits = c.literals;
            for (int i : lits)
            {
                String temp = getPred((Math.abs(i))).color;
                if (i < 0 && iteration == 1)
                {
                	//negate the messages, as the literal is negative.
                    if (temp == "T")
                    {
                        temp = "F";
                    } 
                    else if (temp == "F")
                    {
                        temp = "T";
                    }
                } 
                else if (i < 0)
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
            	//should we not clear all the predicate messages first ???
                getPred((Math.abs(i))).msg.addClauseMsgToPredicate(c.msg.getMessage());
            }
        }        
    }

    private void assignNewClauseColors()
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        int id = 0;
        ArrayList<String> colors = new ArrayList<String>();
        int cID = 65;

        for (Clause c : clauses)
        {
            int hash = c.msg.getMessage().hashCode();
            if (!ids.contains(hash))
            {
//                This implies that the message is not seen before
//                so u have to assign a new color here
//                ids.add(hash);
                if (cID == 91)
                {
                    cID = 65;
                }

                String nColor = Character.toString((char) cID);
                nColor = "C" + nColor;
                cID++;
                int t = 0;
                while (true)
                {

                    t++;
                    if (colors.contains(nColor))
                    {
                        if (!nColor.contains("[0-9") && cID <= 91)
                        {
                            nColor = Character.toString((char) cID);
                            nColor = "C" + nColor;
                            cID++;

                        } else
                        {
                            cID = 65;
                            nColor = Character.toString((char) cID);
                            nColor = "C" + nColor;
                            nColor = nColor + t;
                        }

                    } else
                    {

                        break;
                    }
                }

                id = id + 1;

                c.color = nColor;
                ids.add(hash);
                colors.add(nColor);

                if (!c.oldColor.equals(c.color))
                {
                    colorChanged = true;
                }
                c.oldColor = c.color;

            } else
            {
                int x = ids.indexOf(hash);
                String nColor = colors.get(x);
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

//        ASCII Code for A = 65, Z=90
        ArrayList<Integer> ids = new ArrayList<Integer>();;
        int id = 0;
        ArrayList<String> colors = new ArrayList<String>();
        char cID = 64;
        for (Predicate p : predicates)
        {
            cID++;
            ArrayList<String> msg = p.msg.getMessage();
            Collections.sort(msg);
            int hash = msg.hashCode();

            if (!ids.contains(hash))
            {
                String nColor = String.valueOf((cID));
                nColor = "P" + nColor;
                int t = 0;
                while (true)
                {

                    t++;
                    if (colors.contains(nColor))
                    {
                        if (!nColor.contains("[0-9") && cID <= 91)
                        {
                            nColor = Character.toString((char) cID);
                            nColor = "C" + nColor;
                            cID++;

                        } else
                        {
                            cID = 65;
                            nColor = Character.toString((char) cID);
                            nColor = "C" + nColor;
                            nColor = nColor + t;
                        }
                    } 
                    else
                    {
                        break;
                    }
                }

                id = id + 1;

                ids.add(hash);
                colors.add(nColor);
                p.color = nColor;
                if (!p.oldColor.equals(p.color))
                {
                    colorChanged = true;
                }
                p.oldColor = p.color;
            } 
            else
            {
                int x = ids.indexOf(hash);
                String nColor = colors.get(x);
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
        int i = 1;
        if (k >= 10)
        {
            while (colorChanged)
            {
                colorChanged = false;
                updateMessages();
                assignNewPredColors();
                assignNewClauseColors();
                i++;
            }
            System.out.println("# of iterations: " + i);
        } 
        else
        {
            while (colorChanged)
            {
                colorChanged = false;
                updateMessages();
                assignNewPredColors();
                assignNewClauseColors();
                if(i==k)
                	break;
                i++;
            }
            System.out.println("# of iterations: " + i);
        }
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
        ArrayList<String> colors = new ArrayList<String>();
        for (Predicate p : predicates)
        {
        	//if the color is a new color
            if (!colors.contains(p.color))
            {
                colors.add(p.color);
                comPredicates.add(p); //add it to the list of compressed predicates..
                if (!p.clusters.contains(p.id))
                {
                    p.clusters.add(p.id);
                }
            } 
            else
            {
            	//this color has already been seen.
                int i = colors.indexOf(p.color);
                Predicate p1 = comPredicates.get(i);
                if (!p1.clusters.contains(p.id))
                {
                    p1.clusters.add(p.id);
                }
            }
        }

        colors.clear();

        for (Clause c : clauses)
        {
            Clause clause = c;
            //if we encounter a new color
            if (!colors.contains(clause.color))
            {
                colors.add(clause.color);

                if (!clause.clusters.contains(clause.id))
                {
                    clause.clusters.add(clause.id);
                }

                ArrayList<Integer> newLits = new ArrayList<Integer>();
                ArrayList<Predicate> lits = new ArrayList<Predicate>();
                for (int k : clause.literals)
                {
                    for (Predicate p : comPredicates)
                    {
                        if (p.clusters.contains(Math.abs(k)))
                        {
                        	//we are never adding to this variable..
                        	//so this variable is always empty..and the below condition is always false..??
                            if (!lits.contains(p))
                            {
                                int t = p.id;
                                if (k < 0)
                                {
                                    t = t * -1;
                                }
                                newLits.add(t);
                                break;
                            } 
                            else
                            {
                                clause.noOfIdenticalMsgs[k]++;
                            }
                        }
                    }
                }
                clause.literals = newLits;
                comClauses.add(clause);
            } 
            else
            {
                int i = colors.indexOf(clause.color);
                Clause c1 = comClauses.get(i);
                if (!c1.clusters.contains(clause.id))
                {
                    c1.clusters.add(clause.id);
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
