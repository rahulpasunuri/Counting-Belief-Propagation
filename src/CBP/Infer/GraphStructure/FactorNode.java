/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Infer.GraphStructure;

import CBP.Compression.Clause;

/**
 *
 * @author shrutika
 */
public class FactorNode extends Node
{
    private Clause c;
    private int literals[];
    private double weight;
    public FactorNode(Clause c)
    {
        this.c=c;
    }
    public void setClause()
    {
        isClause=true;
    }
    @Override
    public int getID()
    {
       return c.getID();
    }
    public void setLiterals(int [] lits, double wt)
    {
        literals = lits;
        weight = wt;
    }
    public int[] getLiterals()
    {
        return literals;
    }
    
    public Clause getClause()
    {
        return c;
    }
}
