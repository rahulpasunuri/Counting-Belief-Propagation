/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import CBP.AddEvidence.ParseEvidence;
import CBP.Infer.BeliefPropagation;
import CBP.Infer.BoxPropagation; // does box propagation instead of belief propagation
import CBP.Infer.EstimateQuery;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import tuffy.db.RDB;
import tuffy.ground.Grounding;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.util.Config;

/**
 *
 * @author shrutika
 */
public class Compression
{

    MarkovLogicNetwork mln;
    RDB db;
    Grounding grounding;
    private final Compress c;       
    String queryFileName;
    public Compression(Grounding g, String s, long stat, int noOfIterations, String queryFileName, String progFileName)
    {
        grounding = g;
        mln = g.getMLN();
        db = mln.getRDB();
        db.schema = Config.db_schema;
        this.queryFileName=queryFileName;
        new ParseEvidence(db).parse(s,mln);
        c= new Compress(db, noOfIterations, progFileName);
    }
    
    public void runBP() throws IOException, SQLException
    {
    	//return; //TODO
    	///*
        ArrayList<Clause> cl = c.getCompressedClauses();
        ArrayList<CBP.Compression.Predicate> pd = c.getCompressedPreds();
        BeliefPropagation bp;
        
        System.out.println("Starting BP");
        bp = new BeliefPropagation(pd, cl, EstimateQuery.getQueryAtomIds(queryFileName, db, mln));        
        bp.computeProbabilities();        
    	//*/
    }
    
    public void runBoxPropagation() throws SQLException, IOException
    {
    	System.out.println("Starting Box Propagation");
        ArrayList<Clause> cl = c.getCompressedClauses();
        ArrayList<CBP.Compression.Predicate> pd = c.getCompressedPreds();
        BoxPropagation bp;        
        bp = new BoxPropagation(pd, cl, EstimateQuery.getQueryAtomIds(queryFileName, db, mln));        
        bp.run(); //runs box propagation
    }   
}
