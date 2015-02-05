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
//    public ActualMessage aMsg = new ActualMessage();
    Predicate (int x, ArrayList<Integer> c, String col, PMessage m, boolean e, boolean q)
    {
        id = x;
        clusters = c;
        color =oldColor= col;
        
        msg = m;
        hasEvidence =true;
        evidence =e;
        query=q;
        
        
    }
    
    Predicate (int x, ArrayList<Integer> c, String wt, PMessage m, boolean q)
    {
        id = x;
        clusters = c;
        color =oldColor= wt;
        
        msg = m;
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
