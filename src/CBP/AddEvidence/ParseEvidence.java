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
    }
    
    
    public void parse(String s, MarkovLogicNetwork mln1)
    {
    	// Add evidence from the command line parameters..
    	
        mln = mln1;
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
	                handleEvidence(sCurrentLine);	                
	            }
            }

        } 
        catch (IOException e)
        {
            e.printStackTrace();
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
                ex.printStackTrace();
            }
        }
    }

    private void handleEvidence(String line)
    {    	
    	line=line.trim();
        String k[] = line.split("(");
        if(k.length==1)
        {
        	//TODO: Throw exception..
        	return;
        }
        k[1] = k[1].trim();
        String k1[] = k[1].split(")");
        if(k1.length==1)
        {
        	//TODO: Throw exception..
        	return;
        }
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
        ids = getConstantIds(strings);
        
        if (k[0].contains("!"))
        {            
            updateEvidence((k[0].replace("!", "")), "f", ids);
        } 
        else
        {
            updateEvidence(k[0], "t", ids);
        }
        
    }

    private ArrayList<Integer> getConstantIds(String strings[])
    {
    	//return the ids of the constants
    	
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
            } 
            catch (Exception e)
            {
                System.out.println(e);
            }
        }

        return ids;
    }

    private void updateEvidence(String predName, String b, ArrayList<Integer> id)
    {
    	//b == "t" for true evidence and = "f" for false evidence
    	
        Predicate p = mln.getPredByName(predName);
    	if(p==null)
    	{
    		//TODO: Throw exception,, predicate doesn't exist..
    		return;    		
    	}
        ArrayList<String> cols = new ArrayList<String>();
        try
        {
            String sql1 = "Select * from " + p.getRelName();
            ResultSet rs1 = db.query(sql1);
            ResultSetMetaData rsmd = rs1.getMetaData();
            int n = rsmd.getColumnCount();
            
            //starting from the column 9, we have the columns for ids...
            for (int i = 9; i <= n; i++) 
            {
                cols.add(rsmd.getColumnName(i));
            }

        } catch (Exception e)
        {
            System.out.println(e);
        }


        String sql = "UPDATE " + p.getRelName() + " set truth = '" + b + "' where ";
        String sql1 = "Select atomid from "+ p.getRelName() + " where "; 
        for (int i = 0; i<cols.size(); i++)
        {
            
            sql = sql + cols.get(i) + " = " + id.get(i);
            sql1 = sql1 + cols.get(i) + " = " + id.get(i);
            if (i == cols.size() - 1)
            {
                continue;
            }
            sql = sql + " AND ";
            sql1 = sql1 + " AND ";
        }

        try
        {
            ResultSet rs1 = db.query(sql1);
            while(rs1.next())
            {
                String sql0="Update mln0_atoms set truth = '"+b+"' where atomid = "+rs1.getInt("atomid");
                db.update(sql0);
            }
            db.update(sql);
        } catch (Exception e)
        {
            System.out.println(e);
        }
    
    }

}
