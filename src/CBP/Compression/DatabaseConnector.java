/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import tuffy.db.RDB;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;

/**
 *
 * @author shrutika
 */
public class DatabaseConnector
{

    private final RDB db;
    private final String[] color;
    private final MarkovLogicNetwork mln;
    private int iteration_count;

    DatabaseConnector(RDB db1, MarkovLogicNetwork mln1)
    {
        this.color = new String[]
        {
            "R", "Y", "G", "Bl", "Blk", "O", "Br", "Wh", "M", "P"
        };
        db = db1;
        mln = mln1;
        initializeTables();
    }

    void createTable(String sql)
    {
        try
        {
            db.execute(sql);
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    void updateClauseMessage()
    {

    }

    void initilalizePredicates()
    {

        try
        {
            String evi = "";
            ResultSet rs = db.query("Select predid,atomid from mln0_atoms");
            while (rs.next())
            {
                int id = rs.getInt("predid");
                int aid = rs.getInt("atomid");
                //String evidence=rs.getString("");
                for (Predicate p : mln.getAllPred())
                {
                    if (p.getID() == id)
                    {
                       
                        String sql = "Select truth from " + p.getRelName() + " where atomid = " + aid;

                        ResultSet rs1 = db.query(sql);

                        while (rs1.next())
                        {
                            String c = "RED";

                            evi = rs1.getString("truth");

                            if (evi == null)
                            {
                                c = color[2];
                            } else
                            {
                                if (evi.equalsIgnoreCase("t"))
                                {
                                    c = "T";
                                } else
                                {
                                    c = "F";
                                }
                            }
                            sql = "Insert into clusteredPredicate_0 values(" + aid + "," + aid + "," + aid + ",'" + c + "','null')";

                            db.update(sql);

                        }
                    }
                }
                //p.getID()

            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

    }

    private void initializeClauses()
    {
        try
        {
            String evi = "";
            int count = 0;
            ResultSet rs = db.query("Select cid,lits,weight from mln0_clauses");
            while (rs.next())
            {
                int id = rs.getInt("cid");
                double weight = rs.getDouble("weight");

                //String evidence=rs.getString("");
                String lits = rs.getString("lits");

                String lit = parseLiterals(lits);

                String sql = "Insert into clusteredClause_0 values(" + id + ",'" + id + "','" + lit + "'," + weight + ",'null')";
                db.update(sql);

            }

        } catch (SQLException e)
        {
            System.out.println(e);
        }
    }

    private String parseLiterals(String x)
    {
        String temp[] = x.split("\\{");
        temp = temp[1].split("\\}");

        temp[0] = temp[0].replace(",", " ");
        return temp[0];
    }

    private void initializeTables()
    {
        String sql = "CREATE TABLE clusteredClause_0(ID int,Clusters varchar,literals varchar,weight float,message varchar)";

        createTable(sql);

        sql = "CREATE TABLE clusteredPredicate_0(ID int,Clusters varchar,allPredicates varchar,color varchar,message varchar)";

        createTable(sql);
        System.out.println("Inililize table");
        initilalizePredicates();
        System.out.println("Inililize table");
        initializeClauses();
        System.out.println("Clauses Done");

    }

    private String selectQuery(String tableName)
    {
        return "Select * from " + tableName;
    }

    void computeClauseMessages(int iteration)
    {
        iteration_count = iteration;
        String tableName = "clusteredClause_" + iteration;
        try
        {
            ResultSet rs = db.query(selectQuery(tableName));

            while (rs.next())
            {
                ArrayList<Integer> atoms = getAtoms(rs.getString("literals"));
                updateClauseMessage(atoms, rs.getInt("ID"), rs.getDouble("weight"), tableName);
            }
        } catch (SQLException e)
        {
            System.out.println(e);

        }

        clusterClauses(tableName);
    }

    public int countQuery(String tableName)
    {
        String sql = "Select count(*) from " + tableName;
        ResultSet rs = db.query(sql);
        try
        {
            while (rs.next())
            {
                int count = rs.getInt(1);
                return count;
            }

        } catch (SQLException e)
        {
            System.out.println(e);
        }
        return 0;
    }

    void clusterClauses(String tableName)
    {//Remember to increase iteration
        //int count = countQuery(tableName);
        int x = iteration_count + 1;
        String newTableName = "clusteredClause_" + x;
        //ArrayList<Clause> c = new ArrayList<Clause>();
        //Clause c[]=new Clause[count];
        System.out.println("Iteration #" + x);
        int i = 0;
        try
        {
            String sql = "Select * from " + tableName + " order by message";
            //System.out.println(sql);
            ResultSet rs = db.query(sql);
            String msg = "";
            ArrayList<Integer> atoms;
            String wholeCluster = "";
            while (rs.next())
            {
                atoms = getAtoms(rs.getString("literals"));
                String message = rs.getString("message");
                if (!msg.equalsIgnoreCase(message))
                {
                    i++;
                    wholeCluster = rs.getString("clusters");
                    sql = "Insert into " + newTableName + " values(" + i + ",'" + rs.getString("Clusters") + "','" + rs.getString("literals") + "'," + rs.getDouble("weight") + ",'null')";
                    db.update(sql);
                    msg = message;
                } else
                {
                    //ArrayList<Integer> atoms = getAtoms(rs.getString("literals"));
                    //ResultSet rs1=db.query(sql);
                    String clusters = rs.getString("Clusters");
                    String id = rs.getString("ID");
                    String clust[] = clusters.split(" ");
                    for (String clust1 : clust)
                    {
                        String temp[]=wholeCluster.split(" ");
                        boolean flag=false;
                        for(String temp1:temp)
                        {
                            temp1=temp1.trim();
                            clust1=clust1.trim();
                            if(temp1.equalsIgnoreCase(clust1))
                            {                              
                                flag=true;
                                break;
                            }
                            
         
                        }
                        if(!flag)
                        {
                            wholeCluster = wholeCluster + " " + clust1;
                        }
                        
                    }

                    sql = "Update " + newTableName + " SET Clusters = '" + wholeCluster + "' where ID = " + i;
                    db.update(sql);
                }
                //c[i]=new Clause();
                //int id, String clus, String lit,double wt, String msg
                sendPredicateMessages(atoms, message);
            }
        } catch (SQLException e)
        {
            System.out.println(e);

        }

        clusterPredicates();
        addLiterals(newTableName, tableName);

    }

    private ArrayList<Integer> getAtoms(String literals)
    {
        /*
         This method parses the literals and returns an array of integers
         */
        String x[] = literals.split(" ");

        ArrayList<Integer> atoms = new ArrayList<Integer>();
        for (String x1 : x)
        {
            atoms.add(Integer.parseInt(x1));
        }

        return atoms;       
    }

    private void updateClauseMessage(ArrayList<Integer> atoms, int ID, double weight, String tableName)
    {
        /*
         In this method you compute the messages and then update the clause table
         */
        String msg = "";
        for (int i = 0; i < atoms.size(); i++)
        {
            String c = getColorOfPredicate(atoms.get(i));
            msg = msg + c + " ";

        }

        msg = msg + weight;

        String sql = "Update " + tableName + " SET message = '" + msg + "' where ID =" + ID;
        db.update(sql);

    }

    private String getColorOfPredicate(int id)
    {
        String c = "";
        boolean notSign = false;
        if (id < 0)
        {
            notSign = true;
            id = -id;
        }
        String tableName = "clusteredPredicate_" + iteration_count;
        String sql = "Select color from " + tableName + " where ID = " + id;
        try
        {
            ResultSet rs = db.query(sql);
            while (rs.next())
            {
                c = rs.getString("color");
                if (notSign == true)
                {
                    if (c.equalsIgnoreCase("T"))
                    {
                        return "F";
                    } else if (c.equalsIgnoreCase("F"))
                    {
                        return "T";
                    }

                }
                return c;
            }
        } catch (SQLException e)
        {
            System.out.println(e);
        }
        return c;

    }

    private void sendPredicateMessages(ArrayList<Integer> atoms, String message1)
    {
        String tableName = "clusteredPredicate_" + iteration_count;
        try
        {
            //ResultSet rs = db.query(selectQuery(tableName));
            for (int i = 0; i < atoms.size(); i++)
            {
                String message = message1;
                String sql = selectQuery(tableName) + " where id = " + Math.abs(atoms.get(i));
                ResultSet rs = db.query(sql);
                while (rs.next())
                {
                    String msg = rs.getString("message");
                    if (msg != null)
                    {
                        String temp = msg.trim();
                        if (!temp.equalsIgnoreCase("null"))
                        {
                            message = message + " " + msg;
                        }
                    }

                    sql = "Update " + tableName + " SET message = '" + message + "'where ID =" + Math.abs(atoms.get(i));
                    db.update(sql);
                }
            }
        } catch (SQLException e)
        {
            System.out.println(e);
        }

    }

    private void clusterPredicates()
    {
        String tableName = "clusteredPredicate_" + iteration_count;
        String newTableName = "clusteredPredicate_" + (iteration_count + 1);

        try
        {
            String sql = selectQuery(tableName) + " order by message";
            ResultSet rs = db.query(sql);

            String msg = "";
            ArrayList<Integer> atoms;
            int i = 0;
            String cluster = "";
            String c = "";
            String wholeClusters = "";
            while (rs.next())
            {
                //atoms = getAtoms(rs.getString("literals"));
                String message = rs.getString("message");

                if (!msg.equalsIgnoreCase(message))
                {
                    i++;
                    wholeClusters = rs.getString("clusters");
                    c = rs.getString("color");
                    if (!c.equalsIgnoreCase("T") && !c.equalsIgnoreCase("F"))
                    {
                        c = color[i];
                    }
                    sql = "Insert into " + newTableName + " values( " + i + ",'" + wholeClusters + "'," + i + ",'" + c + "','null')";

                    db.update(sql);
                    msg = message;
                } else
                {

                    //ArrayList<Integer> atoms = getAtoms(rs.getString("literals"));
                    String clusters = rs.getString("clusters");
                    //ArrayList<Integer> atoms;
                    String clust[] = clusters.split(" ");
                    String id = rs.getString("ID");
                    for (String clust1 : clust)
                    {
                        String temp[] = wholeClusters.split(" ");
                        for (String temp1 : temp)
                        {
                            if (Integer.parseInt(temp1) != Integer.parseInt(clust1))
                            {
                                wholeClusters = wholeClusters + " " + clust1;
                                break;
                            }
                        }
                    }

                    sql = "Update " + newTableName + " SET Clusters = '" + wholeClusters + "' where ID = " + i;
                    db.update(sql);
                }
                //c[i]=new Clause();
                //int id, String clus, String lit,double wt, String msg
                //sendPredicateMessages(atoms, message);
            }

        } catch (SQLException e)
        {
            System.out.println(e);
        }
    }

    private void addLiterals(String newTableName, String tableName)
    {
        ResultSet rs, rs1;
        String predTableName = "clusteredPredicate_" + (iteration_count + 1);

        String sql = selectQuery(tableName);
        try
        {
            rs = db.query(sql);
            while (rs.next())
            {
                ArrayList<Integer> atoms = getAtoms(rs.getString("literals"));
                String newAtoms = "";
                for (int i = 0; i < atoms.size(); i++)
                {
                    int atom = atoms.get(i);
                    sql = selectQuery(predTableName);
                    rs1 = db.query(sql);
                    String id = "";
                    while (rs1.next())
                    {
                        String clusters = rs1.getString("clusters");
                        String temp[] = clusters.split(" ");
                        for (int l = 0; l < temp.length; l++)
                        {
                            temp[l] = temp[l].trim();
                            if (Integer.parseInt(temp[l]) == Math.abs(atom))
                            {
                                id = rs1.getString("ID");
                                break;
                            }
                        }

                    }

                    if (atom < 0)
                    {
                        id = "-" + id;
                    }
                    //newAtoms = id + " " + newAtoms;
                    if (newAtoms == "")
                    {
                        newAtoms = id;
                    } else
                    {
                        newAtoms = newAtoms + " " + id;
                    }

                }

                sql = "Select ID,Clusters from " + newTableName;
                rs1 = db.query(sql);

                while (rs1.next())
                {
                    String clusters = rs1.getString("clusters");
                    String temp[] = clusters.split(" ");
                    for (String temp1 : temp)
                    {
                        if (temp1.equalsIgnoreCase(rs.getString("ID")))
                        {
                            String ID = rs1.getString("id");
                            sql = "Update " + newTableName + " SET literals ='" + newAtoms + "' where id=" + ID;
                            db.update(sql);
                            break;
                        }
                    }

                }
                //Now get the ID of new clustertable

            }

        } catch (SQLException e)
        {

        }
    }

}
