/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Compression;

import java.util.ArrayList;

/**
 *
 * @author shrutika
 */
public class Predicate
{
    int id;
    ArrayList<Integer> clusters;
    String color;
    String oldColor;
    PMessage msg;
    boolean hasEvidence=false;
    boolean evidence;
    boolean query=false;
    public double probability;
    
    /*
     * This constructor is used for predicates evidence..
     * x -id, c-cluster ids, col=color, m- message, e-evidence, q- whether it is a query or not..
     */
    Predicate (int x, String col, boolean q, boolean e)
    {
        id = x;
        color =oldColor= col;
        clusters = new ArrayList<Integer>();
        clusters.add(x);
        msg = new PMessage();
        hasEvidence =true;
        evidence =e;
        query=q;               
    }
    
    /*
     * This constructor is used for predicates without evidence..
     * x -id, c-cluster ids, col=color, m- message, q- whether it is a query or not..
     */
    Predicate (int x, String wt,boolean q)
    {
        id = x;
        clusters = new ArrayList<Integer>();
        clusters.add(x);
        color =oldColor= wt;
        
        msg = new PMessage();
        hasEvidence =false;
        query=q;        
    }
    
    public boolean hasEvidence()
    {
        return hasEvidence;
    }
    
    public boolean getEvidence()
    {
        return evidence;
    }

    public int getID()
    {
        return id;
    }

    public int getClusterSize()
    {
        return clusters.size();
    }

    public PMessage getPMessage()
    {
        return msg;
    }

    public ArrayList<Integer> getClusters()
    {
        return clusters;
    }
}
