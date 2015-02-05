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
//        System.out.println("\n\nhello\n\n");
        db = db1;
        mln = mln1;
        k = noOfIterations;
        init();
        //initializeTables();
//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void init()
    {
        System.out.println("In compressing class");
        initializeClauses();
        System.out.println("Clauses initialized");
        initilalizePredicates();
        System.out.println("IPreds initialised");
//        testGraph();
        long startTime = System.nanoTime();

        colorPassing();
        long endTime = System.nanoTime();

//        System.out.println("Time for color passing: "+Math.abs(startTime-endTime)/1000000000.0);
        compression();

    }

    private void initializeClauses()
    {
        clauses = new ArrayList();
        try
        {
            String evi = "";
            int count = 0;
            ResultSet rs = db.query("Select cid,lits,weight from mln0_clauses");
            while (rs.next())
            {
                int id = rs.getInt("cid");
                double weight = rs.getDouble("weight");
                //System.out.println(id+" ");
                //String hasEvidence=rs.getString("");
                String lits = rs.getString("lits");

                ArrayList<Integer> lit = parseLiterals(lits);
                ArrayList<Integer> tempCluster = new ArrayList();
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
        ArrayList<Integer> al = new ArrayList();
        String temp[] = x.split("\\{");
        temp = temp[1].split("\\}");

        temp[0] = temp[0].replace(",", " ");

        //System.out.println("\nlits: "+temp[0]);
        String lits[] = temp[0].split(" ");
        for (String lit : lits)
        {
            //System.out.println(lit);
            al.add(Integer.parseInt(lit));
        }
        return al;
    }

    private void initilalizePredicates()
    {
        predicates = new ArrayList();

        try
        {
            String evi = "";
            int count = 0;
            //int flag=0;
            ResultSet rs = db.query("Select isquery,atomid,truth from mln0_atoms order by atomid");
            while (rs.next())
            {
                //System.out.println("asd");
//                int id = rs.getInt("predid");
//                System.out.println(id);
                int aid = rs.getInt("atomid");
                String truth = rs.getString("truth");
                String c;
                boolean iquery = rs.getBoolean("isquery");
//                if(iquery)
//                {
//                    System.out.println("yes query is "+iquery);
//                }
                //System.out.println(truth);
                //evi = rs1.getString("truth");
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
//                        System.out.println("Tr");
                    } else
                    {
                        c = "F";
                        e = false;
//                        System.out.println("Hello False" + aid);
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

//        System.out.println(predicates.size());
    }

    private void updateMessages()
    {

        for (Clause c : clauses)
        {
            c.msg.s = "";
            ArrayList<Integer> lits = c.literals;
            //String m = "";
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
                //System.out.println(temp);
                c.msg.addliteralMessage(temp);
            }

//            String temp1 = clause.color;
            c.msg.addliteralMessage(c.color);
//            System.out.println(c.msg.s);
            for (int i : lits)
            {
//                System.out.println(i);
                getPred((Math.abs(i))).msg.msg.add(c.msg.s);

//                for(String s: getPred((Math.abs(i))).msg.msg)
//                    System.out.print("\t"+s+" ");
//            System.out.println();
            }
            //addClauseMsgToPredicate();
        }

//        System.out.println("Printing messages");
//        
//        for(Clause c: clauses)
//        {
//            System.out.println(c.id+"\t"+c.msg.s);
//        }
//        
//        
//        for(Predicate c: predicates)
//        {
//            System.out.print(c.id+"\t");
//            for(String s: c.msg.msg)
//                System.out.print(s+" ");
//            System.out.println();
//        }
//        
//        
    }

    private void assignNewClauseColors()
    {
//        currentClauseList.clear();

//        System.out.println("Assigning new Clause Colors");
        ArrayList<Integer> ids = new ArrayList();

        int id = 0;
//        ids.add(0);
        String msg = " ";
        ArrayList<String> colors = new ArrayList();
        int cID = 65;

        for (Clause c : clauses)
        {
//            System.out.println(c.msg.s);
            int hash = c.msg.s.hashCode();
//            System.out.println(hash);
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
//                            System.out.println("I am the problem"+nColor);
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
//                        System.out.println(nColor);
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

//        System.out.println("end of assign");
    }

    private void assignNewPredColors()
    {

//        ASCII Code for A = 65, Z=90
        //Collections.sort(predicates);
        ArrayList<Integer> ids = new ArrayList();
//        ids.add(0);
        int id = 0;
        ArrayList<String> colors = new ArrayList();
        char cID = 64;
//        System.out.println("preds");
        for (Predicate p : predicates)
        {
            cID++;
            ArrayList<String> msg = p.msg.msg;
            Collections.sort(msg);

//            for(String s: msg)
//                System.out.print(s+"  X  ");
//            System.out.println();
            int hash = msg.hashCode();
//            System.out.println(hash);

            if (!ids.contains(hash))
            {
//                ids.add(hash);

                String nColor = String.valueOf((cID));
                nColor = "P" + nColor;
//                System.out.println(absghf);

                int t = 0;
                while (true)
                {

                    t++;
                    if (colors.contains(nColor))
                    {
                        if (!nColor.contains("[0-9") && cID <= 91)
                        {
//                            System.out.println("I am the problem"+nColor);
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
//                        System.out.println(nColor);
                    } else
                    {

                        break;
                    }
                }

                id = id + 1;

                ids.add(hash);
                colors.add(nColor);
//                System.out.println(absghf);
//                absghf++;
                p.color = nColor;
                if (!p.oldColor.equals(p.color))
                {
                    colorChanged = true;
                }
                p.oldColor = p.color;
            } else
            {
//                System.out.println(ids.indexOf(h));
                int x = ids.indexOf(hash);
//                System.out.println("index "+x+"Size: "+ids.size());
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
//        System.out.println("In color passing");
        if (k >= 10)
        {
            while (colorChanged)
            {
                colorChanged = false;
                updateMessages();
//            System.out.println("Messages updates");
                assignNewPredColors();
                assignNewClauseColors();
//            System.out.println(colorChanged);
//            if(i==1)
//                break;
                i++;
            }
            System.out.println("# of iterations: " + i);
        } else
        {
            while (colorChanged)
            {
                colorChanged = false;
                updateMessages();
//            System.out.println("Messages updates");
                assignNewPredColors();
                assignNewClauseColors();
//            System.out.println(colorChanged);
            if(i==k)
                break;
                i++;
            }
            System.out.println("# of iterations: " + i);
        }

//        compression();
    }

    private Predicate getPred(Integer pid)
    {
//        System.out.println("PID: "+pid);
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

//       int id=0;
        for (Predicate p : predicates)
        {
//            System.out.println(p.color);
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

                ArrayList<Integer> newLits = new ArrayList();
                int l = 0;
                ArrayList<Predicate> lits = new ArrayList();
                for (int k : clause.literals)
                {
//                    Predicate pTemp = getPred(Math.abs(k));
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
//                System.out.println(cl.size()+"  "+i);
                Clause c1 = cl.get(i);
                if (!c1.clusters.contains(clause.id))
                {
                    c1.clusters.add(clause.id);
                }
//                c1.noOfIdenticalMsgs++;
            }
        }

        System.out.println(predicates.size() + "   " + preds.size());
        System.out.println(clauses.size() + "   " + cl.size());

        System.out.println("\n\n\n\n\nStarting BP\n\n\n\n\n");

    }

    private void testGraph()
    {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        clauses = new ArrayList();
        predicates = new ArrayList();
        ArrayList<Integer> al = new ArrayList();
        al.add(1);
        ArrayList<Integer> al1 = new ArrayList();
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

//        System.out.println("Clauses");
//        for(Clause c: clauses)
//        {
//            System.out.println(c.id+"\t"+c.color);
//        }
//        
//        
//        System.out.println("Predicates");
//        for(Predicate c: predicates)
//        {
//            System.out.println(c.id+"\t"+c.color);
//        }
//        System.out.println("\n\n\n\n\n");
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
