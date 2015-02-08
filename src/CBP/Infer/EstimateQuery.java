/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Infer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import tuffy.db.RDB;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;

/**
 *
 * @author shrutika
 */

public class EstimateQuery
{
    
    RDB db;
    MarkovLogicNetwork mln;

    public EstimateQuery(RDB db1)
    {
        db = db1;
    }

    public ArrayList<Query> parse(String s, MarkovLogicNetwork mln1)
    {
        mln = mln1;
        
        ArrayList<Query> ids = new ArrayList<Query>();
        BufferedReader br = null;

        try
        {

            String sCurrentLine;
            if(s!=null)
            {
                br = new BufferedReader(new FileReader(s));
	            while ((sCurrentLine = br.readLine()) != null)
	            {
	               sCurrentLine=sCurrentLine.trim();
	               int k= getPredname(sCurrentLine);
	               Query temp = new Query();
	               temp.id=k;
	               temp.query=sCurrentLine;
	               ids.add(temp);
	            }
            }

        } 
        catch (IOException e)
        {
        	//TODO:throw exception here..
        } 
        finally
        {
            try
            {
                if (br != null)
                {
                    br.close();
                }
            } 
            catch (IOException ex)
            {
            	//TODO:throw exception here..
            }
        }
        //updateEvidence(a);
        return ids;
    }

    private int getPredname(String line)
    {
        String k[] = line.split("\\(");
        String k1[] = k[1].split("\\)");

        ArrayList<Integer> ids = new ArrayList<Integer>();

        String strings[];
        /*
         Over here you need to get the ids from the table constants
         where the string is constantname
         */
        if (k1[0].contains(","))
        {
            strings = k1[0].split(",");
        } 
        else
        {
            strings = new String[1];
            strings[0] = k1[0];
        }
        ids = getIds(strings);
        
        
        int id=  getQueryIds(k[0], "t", ids);
        return id;        
    }

    private ArrayList<Integer> getIds(String strings[])
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (int i = 0; i < strings.length; i++)
        {
            try
            {
                strings[i] = strings[i].trim();
                String sql = "SELECT id FROM constants WHERE string='" + strings[i] + "'";
                ResultSet rs = db.query(sql);
                
                while (rs.next())
                {
                    int id = Integer.parseInt(rs.getString("id"));
                    ids.add(id);
                }
            } catch (Exception e)
            {
                System.out.println(e);
            }
        }

        return ids;
    }

    private int getQueryIds(String a, String b, ArrayList<Integer> id)
    {
        int ids =0;

        String knownColumns[] =
        {
            "id", "truth", "prior", "club", "atomid", "itruth", "prob", "useful"
        };
        Predicate p = mln.getPredByName(a);
        
        ArrayList<String> cols = new ArrayList<String>();
        try
        {
            String sql1 = "Select * from " + p.getRelName();
            System.out.println(sql1);
            ResultSet rs1 = db.query(sql1);
            ResultSetMetaData rsmd = rs1.getMetaData();
            int n = rsmd.getColumnCount();
            for (int i = 9; i <= n; i++)
            {
                cols.add(rsmd.getColumnName(i));
            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

        if (p.getName().equalsIgnoreCase(a))
        {
            String sql1 = "Select atomid from "+ p.getRelName() + " where "; 
            for (int i = 0; i<cols.size(); i++)
            {
                sql1 = sql1 + cols.get(i) + " = " + id.get(i);
                if (i == cols.size() - 1)
                {
                    continue;
                }
                sql1 = sql1 + " AND ";
            }            

           
            try
            {
                ResultSet rs1 = db.query(sql1);
                while(rs1.next())
                {                                       
                        int k = rs1.getInt(1);
                        ids=k;                    
                    
                }               
            } catch (Exception e)
            {
                System.out.println(e);
            }
        }
        return ids;
    }

}
