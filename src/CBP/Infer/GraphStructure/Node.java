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
	// dfgdf
    private double message =1.0;
    public boolean isClause=false;
    
    /**
     * 
     * @return
     */
    public int getID()
    {
        //System.out.print("''");
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
//        System.out.println("My name ");
        return null; //To change body of generated methods, choose Tools | Templates.
    }

    Clause getClause()
    {
        return null; //To change body of generated methods, choose Tools | Templates.
    }
    
}
