/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import java.util.ArrayList;
import java.util.Arrays;
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
    Clause(int x, ArrayList<Integer> c, ArrayList<Integer> l, double wt, CMessage m, String clr)
    {
        id = x;
        clusters = c;
        literals = l;
        
        weight = wt;
        msg = m;
        color=oldColor=clr;
        noOfIdenticalMsgs = new Hashtable<Integer, Integer>();        
        //Arrays.fill(noOfIdenticalMsgs, 1);
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
    	/*
        int t=0;
        if(literals.contains(j))
            t= literals.indexOf(j);
        else
        {
            int k=j*-1; //if the literal is negative...
            t= literals.indexOf(k);
        }
        */
    	j=Math.abs(j);
    	if(noOfIdenticalMsgs.keySet().contains(j))
    	{
    		return (Integer)noOfIdenticalMsgs.get(j);    		
    	}
    	else
    	{
    		noOfIdenticalMsgs.put(new Integer(j), new Integer(1));
    		return 1;
    	}
    }
    
    public void incrementIdenticalMessages(int literalId)
    {
    	literalId = Math.abs(literalId);
    	if(noOfIdenticalMsgs.keySet().contains(literalId))
    	{
    		Integer i = (Integer)noOfIdenticalMsgs.get(literalId);
    		i++;
    	}
    	else
    	{
    		noOfIdenticalMsgs.put(new Integer(literalId), new Integer(1));    		
    	}
    }
    
}
