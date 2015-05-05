/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author shrutika
 */
public class Clause
{

    int id;
    CMessage msg;
    ArrayList <Integer> clusters;
    ArrayList <Integer> literals; //actual ids
    double weight;
    String color="";
    String oldColor ="";
    Hashtable<Integer, Integer> noOfIdenticalMsgs; //map from predicate id to count of identical messages..
    
    /*
     * x- clause id, c-cluster ids, l - literal ids, wt- weight, 
     */
    Clause(int x, ArrayList<Integer> l, double wt)
    {
        id = x;
        clusters = new ArrayList<Integer>();
        clusters.add(id);
        literals = l;
        
        weight = wt;
        msg = new CMessage();
        
        //init value for color is the weight of the clause..
        color=oldColor=Double.toString(wt);
        noOfIdenticalMsgs = new Hashtable<Integer, Integer>();        
    }
          
    public ArrayList<Integer> getLiterals()
    {
        return literals;
    }

    public double getweight()
    {
        return weight;
    }

    public int getID()
    {
        return id;
    }

    public int getIdenticalMsgs(int j)
    {
    	j=Math.abs(j);
    	if(noOfIdenticalMsgs.keySet().contains(j))
    	{
    		return (Integer)noOfIdenticalMsgs.get(j);    		
    	}
    	
    	return -1;
    	//else
    	//{
    	//	noOfIdenticalMsgs.put(new Integer(j), new Integer(1));
    	//	return 1;
    	//}
    }
    
    public void incrementIdenticalMessages(int literalId)
    {
    	literalId = Math.abs(literalId);
    	if(noOfIdenticalMsgs.keySet().contains(literalId))
    	{
    		int i = noOfIdenticalMsgs.get(literalId);
    		noOfIdenticalMsgs.put(literalId, i+1);    		    		
    	}
    	else
    	{
    		noOfIdenticalMsgs.put(new Integer(literalId), new Integer(1));    		
    	}
    }
    
}
