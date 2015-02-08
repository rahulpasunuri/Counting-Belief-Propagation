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

    int abc = -1;
    private final RDB db;
    //private final String[] color;
    private final MarkovLogicNetwork mln;
    private int iteration_count;
    private ArrayList<Clause> clauses;
    private ArrayList<Predicate> predicates;
    private int iteration = 1;
    private boolean colorChanged = true;
    ArrayList<Predicate> preds = new ArrayList<Predicate>();
    ArrayList<Clause> cl = new ArrayList<Clause>();
    int k;

    public Compress(RDB db1, MarkovLogicNetwork mln1, int noOfIterations)
    {
        db = db1;
        mln = mln1;
        k = noOfIterations;
        init();
    }

    private void init()
    {
        System.out.println("In compressing class");
        initializeClauses();
        System.out.println("Clauses initialized");
        initilalizePredicates();
        System.out.println("IPreds initialised");
        colorPassing();
        compression();
    }

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
                boolean ae = false;
                boolean e = false;
                if (truth == null)
                {
                    c = "R";
                } else
                {
                    ae=true;
                    if (truth.equalsIgnoreCase("t"))
                    {
                        c = "T";
                        e = true;
                    } else
                    {
                        c = "F";
                        e = false;
                        abc = aid;
                    }
                }
                ArrayList<Integer> temp = new ArrayList<Integer>();
                temp.add(aid);
                PMessage pmsg = new PMessage();
                Predicate ptemp;
                if (ae)
                {
                    ptemp = new Predicate(aid, temp, c, pmsg, e,iquery);
                } else

                {
                    ptemp = new Predicate(aid, temp, c, pmsg,iquery);
                }
                predicates.add(ptemp);

            }
            //p.getID()

        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    private void updateMessages()
    {

        for (Clause c : clauses)
        {
            c.msg.s = "";
            ArrayList<Integer> lits = c.literals;
            for (int i : lits)
            {
                String temp = getPred((Math.abs(i))).color;
                if (i < 0 && iteration == 1)
                {
                    if (temp == "T")
                    {
                        temp = "F";
                    } else if (temp == "F")
                    {
                        temp = "T";
                    }

                } else if (i < 0)
                {
                    temp = "N" + temp;
                }
                c.msg.addliteralMessage(temp);
            }

            c.msg.addliteralMessage(c.color);
            for (int i : lits)
            {
                getPred((Math.abs(i))).msg.msg.add(c.msg.s);
            }
            //addClauseMsgToPredicate();
        }        
    }

    private void assignNewClauseColors()
    {
        ArrayList<Integer> ids = new ArrayList();

        int id = 0;
        String msg = " ";
        ArrayList<String> colors = new ArrayList();
        int cID = 65;

        for (Clause c : clauses)
        {
            int hash = c.msg.s.hashCode();
            if (!ids.contains(hash))
            {
//                This implies that the messageis not seen before
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

            c.msg.s = "";

        }
    }

    private void assignNewPredColors()
    {

//        ASCII Code for A = 65, Z=90
        //Collections.sort(predicates);
        ArrayList<Integer> ids = new ArrayList();;
        int id = 0;
        ArrayList<String> colors = new ArrayList();
        char cID = 64;
        for (Predicate p : predicates)
        {
            cID++;
            ArrayList<String> msg = p.msg.msg;
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

//                        nColor = nColor.replaceAll("[0-9]", "");
                    } else
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
            } else
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

            p.msg.msg.clear();

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
        } else
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
        System.out.println("In Compression");

        ArrayList<String> colors = new ArrayList();
        ArrayList<String> cColors = new ArrayList();

        for (Predicate p : predicates)
        {
            if (!colors.contains(p.color))
            {
                colors.add(p.color);
                preds.add(p);
                if (!p.clusters.contains(p.id))
                {
                    p.clusters.add(p.id);
                }
            } else
            {
                int i = colors.indexOf(p.color);
                Predicate p1 = preds.get(i);
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
            if (!cColors.contains(clause.color))
            {
                cColors.add(clause.color);

                if (!clause.clusters.contains(clause.id))
                {
                    clause.clusters.add(clause.id);
                }

                ArrayList<Integer> newLits = new ArrayList<Integer>();
                int l = 0;
                ArrayList<Predicate> lits = new ArrayList<Predicate>();
                for (int k : clause.literals)
                {
                    for (Predicate p : preds)
                    {
                        if (p.clusters.contains(Math.abs(k)))
                        {
                            if (!lits.contains(p))
                            {
                                int t = p.id;
                                if (k < 0)
                                {
                                    t = t * -1;
                                }
                                newLits.add(t);
                                break;
                            } else
                            {
                                clause.noOfIdenticalMsgs[k]++;
                            }
                        }
                    }
                    l++;
                }
                clause.literals = newLits;
                cl.add(clause);
            } else
            {
                int i = cColors.indexOf(clause.color);
                Clause c1 = cl.get(i);
                if (!c1.clusters.contains(clause.id))
                {
                    c1.clusters.add(clause.id);
                }
            }
        }

        System.out.println(predicates.size() + "   " + preds.size());
        System.out.println(clauses.size() + "   " + cl.size());

        System.out.println("\n\n\n\n\nStarting BP\n\n\n\n\n");

    }

    private void testGraph()
    {
        clauses = new ArrayList<Clause>();
        predicates = new ArrayList<Predicate>();
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(1);
        ArrayList<Integer> al1 = new ArrayList<Integer>();
        al1.add(1);
        al1.add(2);
        al1.add(3);
        CMessage m = new CMessage();
        CMessage m1 = new CMessage();

        Clause temp = new Clause(1, al, al1, 2.1, m, "2.1");

        clauses.add(temp);

        al.clear();
        al.add(2);
        Clause temp1 = new Clause(2, al, al1, 2.1, m1, "2.1");

        clauses.add(temp1);

        al.clear();
        al.add(1);
        PMessage pm = new PMessage();
        PMessage pm1 = new PMessage();
        PMessage pm2 = new PMessage();

        Predicate p = new Predicate(1, al, "PA", pm,false);
        predicates.add(p);
        al.clear();
        al.add(2);
        Predicate p1 = new Predicate(2, al, "PA", pm1,false);
        predicates.add(p1);

        al.clear();
        al.add(3);

        Predicate p2 = new Predicate(3, al, "PA", pm2,false);
        predicates.add(p2);
    }

    ArrayList<Clause> getCompressedClauses()
    {
        return cl;
    }

    ArrayList<Predicate> getCompressedPreds()
    {
        return preds;
    }

}
