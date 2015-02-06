/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Infer.GraphStructure;

import CBP.Compression.Predicate;

/**
 *
 * @author shrutika
 */
public class PredicateNode extends Node
{
    Predicate p;
    public PredicateNode(Predicate p)
    {
        this.p=p;
        this.isClause = false;
    }

    public int getID()
    {
       return p.getID();
    }

    public Predicate getPredicate()
    {
        return p;
    }
}
