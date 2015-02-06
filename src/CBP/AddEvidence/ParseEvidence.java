/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.AddEvidence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import tuffy.db.RDB;
import tuffy.mln.Atom;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;

/**
 *
 * @author shrutika
 */
public class ParseEvidence
{

    RDB db;
    MarkovLogicNetwork mln;

    public ParseEvidence(RDB db1)
    {
        db = db1;
//        System.out.println(db.schema);
    }

    
    
    public void markQueryAtoms()
    {
        
    }
    
    
    public void parse(String s, MarkovLogicNetwork mln1)
    {
        mln = mln1;
        

        BufferedReader br = null;

        try
        {

            String sCurrentLine;
//            System.out.println("zee "+s);
            if(s!=null)
            {
                br = new BufferedReader(new FileReader(s));
            //System.out.println("\n\nread File\n\n");
            while ((sCurrentLine = br.readLine()) != null)
            {
                sCurrentLine=sCurrentLine.trim();
//                System.out.println(sCurrentLine);
                getPredname(sCurrentLine);
                
            }
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (br != null)
                {
                    br.close();
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        //updateEvidence(a);
//            System.out.println(p.getRelName());
//            System.out.println(p.getRelName());
    }

    private void getPredname(String line)
    {
        //System.out.println("\nLine: "+line);
        String k[] = line.split("\\(");

//        System.out.println(line+" "+k[0]+" "+k.length+" "+k[1]);
        String k1[] = k[1].split("\\)");

//        System.out.println(k1[0]);
        ArrayList<Integer> ids = new ArrayList<Integer>();

        String strings[];
        /*
         Over here you need to get the ids from the table constants
         where the string is constantname
         */
        if (k1[0].contains(","))
        {
            strings = k1[0].split(",");
        } else
        {
            strings = new String[1];
            strings[0] = k1[0];
        }
        ids = getIds(strings);
        
        if (k[0].contains("!"))
        {
            
            updateEvidence((k[0].replace("!", "")), "f", ids);
        } else
        {
//            System.out.println("true evi");
            updateEvidence(k[0], "t", ids);
        }
        //return k[0];
        
    }

    private ArrayList<Integer> getIds(String strings[])
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (int i = 0; i < strings.length; i++)
        {
            try
            {
                strings[i] = strings[i].trim();
                //Statement statement = (Statement) connection.createStatement();
                String sql = "SELECT id FROM constants WHERE string='" + strings[i] + "'";
                ResultSet rs = db.query(sql);

//                System.out.println("Strings  "+strings[i]);
                //System.out.println("sql  "+sql);
                
                while (rs.next())
                {
//                    System.out.println("Table Name : " + rs.getString(1));

                    //String sql1 = "Select * from " + rs.getString(1);
                    int id = Integer.parseInt(rs.getString("id"));
//                    System.out.println(id);
                    ids.add(id);

                    //System.out.println(id);

                }
            } catch (Exception e)
            {
                System.out.println(e);
            }
        }

        return ids;
    }

    private void updateEvidence(String a, String b, ArrayList<Integer> id)
    {
//        System.out.println("In update");

        String knownColumns[] =
        {
            "id", "truth", "prior", "club", "atomid", "itruth", "prob", "useful"
        };
        Predicate p = mln.getPredByName(a);

        //System.out.println(p.getName());
        ArrayList<String> cols = new ArrayList<String>();
        try
        {
            String sql1 = "Select * from " + p.getRelName();
            //System.out.println(sql1);
            ResultSet rs1 = db.query(sql1);
            ResultSetMetaData rsmd = rs1.getMetaData();
            int n = rsmd.getColumnCount();
            for (int i = 9; i <= n; i++)
            {
                cols.add(rsmd.getColumnName(i));
//                System.out.println(rsmd.getColumnName(i));
            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

        if (p!=null && p.getName().equalsIgnoreCase(a))
        {
            //System.out.println(p.getName()+" "+b);
            String sql = "UPDATE " + p.getRelName() + " set truth = '" + b + "' where ";
            String sql1 = "Select atomid from "+ p.getRelName() + " where "; 
               //System.out.println(cols.size());
            for (int i = 0; i<cols.size(); i++)
            {
                
                sql = sql + cols.get(i) + " = " + id.get(i);
                //System.out.println(sql);
                sql1 = sql1 + cols.get(i) + " = " + id.get(i);
                if (i == cols.size() - 1)
                {
                    continue;
                }
                sql = sql + " AND ";
                sql1 = sql1 + " AND ";
            }

            //sql = sql + id.get(i);
            try
            {
//                System.out.println(sql1);
                ResultSet rs1 = db.query(sql1);
                while(rs1.next())
                {
                    String sql0="Update mln0_atoms set truth = '"+b+"' where atomid = "+rs1.getInt("atomid");
//                    System.out.println(sql0);
                    db.update(sql0);
                }
                db.update(sql);
            } catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }

}
