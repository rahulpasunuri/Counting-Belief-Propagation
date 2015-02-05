/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import java.util.ArrayList;
import java.util.Arrays;

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
    int []noOfIdenticalMsgs;
//    ArrayList <Integer> lits;
    
//    public ArrayList <ActualMessage> msgToPredicate= new ArrayList();

    Clause(int x, ArrayList<Integer> c, ArrayList<Integer> l, double wt, CMessage m, String clr)
    {
        id = x;
        clusters = c;
        literals = l;
        
        weight = wt;
        msg = m;
        color=oldColor=clr;
        noOfIdenticalMsgs = new int[literals.size()];
        
        Arrays.fill(noOfIdenticalMsgs, 1);
//        initmsgToPredciate();
       

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
        int t=0;
        if(literals.contains(j))
            t= literals.indexOf(j);
        else
        {
            int k=j*-1;
            t= literals.indexOf(k);
        }
        return noOfIdenticalMsgs[t];
    }
}
