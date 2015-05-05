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
import java.sql.SQLException;
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
    /*
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
	*/
	/*
    private int getPredname(String line)
    {
        String k[] = line.split("\\(");
        String k1[] = k[1].split("\\)");

        ArrayList<Integer> ids = new ArrayList<Integer>();

        String strings[];
        //
         Over here you need to get the ids from the table constants
         where the string is constantname
        // 
        System.out.println("Problem lines is " + line); 
        
        if (k1[0].contains(","))
        {
            strings = k1[0].split(",");
        } 
        else
        {
            strings = new String[1];
            strings[0] = k1[0];
        }
        
        System.out.println();
        System.out.println("args are ");
        for(String s : strings)
        {
        	System.out.println(s);
        }
        
        ids = getIds(strings);
        int id=  getQuerysAtomId(k[0], ids);
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
            } 
            catch (Exception e)
            {
                System.out.println(e);
            }
        }

        return ids;
    }

    private int getQuerysAtomId(String predName, ArrayList<Integer> id)
    {
        int ids =0;
        Predicate p = mln.getPredByName(predName);
        
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
        } 
        catch (Exception e)
        {
            System.out.println(e);
        }

        if(cols.size() != id.size())
        {
        	//exception..
        	System.out.println("Num Columns != Num Paramters for predicate "+predName);
        	System.out.println("Column Size:"+ cols.size()+ " Ids Size: "+id.size());
        }
        
        if (p.getName().equalsIgnoreCase(predName))
        {
            String sql1 = "Select atomid from "+ p.getRelName() + " where "; 
            for (int i = 0; i<cols.size(); i++)
            {
                sql1 = sql1 + cols.get(i) ;
                sql1 = sql1 +" = " + id.get(i);
                if (i == cols.size() - 1)
                {
                    continue;
                }
                if(i!=cols.size()-1)
                {
                	sql1 = sql1 + " AND ";
                }    
            }

           
            try
            {
                ResultSet rs1 = db.query(sql1);
                while(rs1.next())
                {                                       
                        int k = rs1.getInt(1);
                        ids=k;                    
                    
                }               
            } 
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        //System.out.println("Atom id of query is "+ids);
        return ids;
    }
    */
    /*
     * returns the atom ids of all the queries which are not evidence..
     */
    
    public static ArrayList<Query> getQueryAtomIds(String queryFile, RDB db, MarkovLogicNetwork mln) throws SQLException
    {
    	//parse the query file and get predicate names..
    	ArrayList<String> liPredNames = new ArrayList<String>();
        BufferedReader br = null;
        try
        {
            String line;
            if(queryFile!=null)
            {
                br = new BufferedReader(new FileReader(queryFile));
	            while ((line = br.readLine()) != null)
	            {
	            	line=line.trim();
	            	if(line!="" && line.length()>=2)
	            	{
		            	if(line.substring(0,2).equals("//") || line.substring(0,2).equals("\\"))
		            	{
		            		continue; // these lines are comments placed in the query file..
		            	}
		            	liPredNames.add(line.split("\\(")[0].trim());
	            	}            			            			              
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
        
        
        //the below variable will hold the list of all queries..
    	ArrayList<Query> liQueries = new ArrayList<Query>();
    	    	
    	//ArrayList<Integer> liAtomIds = new ArrayList<Integer>();
    	
    	//get all query atom ids..
    	/*
    	String s="";
    	try
    	{
	    	String q = "Select atomid from mln0_atoms where isquery = TRUE and isqueryevid = FALSE and atomid is not NULL";
	    	ResultSet rs = db.query(q);

        	boolean isEmpty=true;
	        while(rs.next())
	        {                                       
                int k = rs.getInt(1);
                if(isEmpty)
                {
                	s = "atomid="+k;
                }
                else
                {
                	s=s+" or atomid="+k;
                }
                isEmpty = false;
                
	        }
    		if(isEmpty)
    		{
    			//throw exception here.. TODO
    			return null;
    		}
    	}
    	catch(Exception e)
    	{
    		//throw exception here..
    		System.out.println(e);    		
    	}
    	*/
    	
    	for(String predName : liPredNames)
    	{  
    		Predicate pred = mln.getPredByName(predName);
	    	//get column names for the predicate..
            String sql1 = "Select * from " + pred.getRelName()+" LIMIT 1";
            ArrayList<String> cols = new ArrayList<String>();
            ResultSet rs1 = db.query(sql1);
            ResultSetMetaData rsmd = rs1.getMetaData();
            int n = rsmd.getColumnCount();
            String colString=" ";
            for (int i = 9; i <= n; i++)
            {
                cols.add(rsmd.getColumnName(i));
                colString+=rsmd.getColumnName(i);
                if(i!=n)
                {
                	colString +=", ";                	
                }
            }
    		    		    		    		    		    		    		    		       		
	        String q2 = "Select atomid, "+colString+" from "+pred.getRelName()+" where atomid in (Select atomid from mln0_atoms where isquery = TRUE and isqueryevid = FALSE and atomid is not NULL)";	        	        	        	     	        
	        try
	        {
	            ResultSet rs = db.query(q2);	        	
		        while(rs.next())
		        {                                       	                
	                Query q = new Query();
	                q.id = rs.getInt(1);
	                //get the query string.
	                String p=predName+"(";
	                //as 0th column is atom id, we should start from 1..
	                for(int k=2; k<=cols.size()+1; k++)
	                {
	                	int cId = rs.getInt(k);
	                	String sql = "Select string from constants where id="+cId;	                	
	                    ResultSet rs2 = db.query(sql);
	                    while(rs2.next())
	                    {
	                    	p+= ("\""+rs2.getString(1)+"\"");
	                    	if(k!=cols.size()+1)
	                    	{
	                    		p+=", ";
	                    	}
	                    	break;
	                    }
	                }
	                //close with the ')'
	                p+=")";
	                q.query = p;
	                liQueries.add(q);
		        }
	        }
	        catch(Exception e)
	        {
	        	//throw exception here.. TODO
	        	System.out.println(e);
	        }
    	}
    	    	    
    	return liQueries;
    }

}
