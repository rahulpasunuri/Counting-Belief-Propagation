/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Infer.GraphStructure;

import CBP.Compression.Clause;
import CBP.Compression.Predicate;

/**
 * Node.java 
 * @author shrutika
 */
public class Node
{
    private double message =1.0;
    public boolean isClause=false;
    /**
     * 
     * @return
     */
    public int getID()
    {
       return 0; 
    }
    
    /**
     * 
     */
    public void printIfClause()
    {
        if(isClause)
        {
            
            System.out.print("Clause: ");
        }
        else
            System.out.print("Predicate: ");
    }

    Predicate getPredicate()
    {
        return null; 
    }

    Clause getClause()
    {
        return null;
    }
    
}
