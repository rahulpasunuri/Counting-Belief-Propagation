/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.Compression;

import CBP.AddEvidence.ParseEvidence;
import CBP.Infer.BeliefPropagation;
import CBP.Infer.EstimateQuery;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import tuffy.db.RDB;
import tuffy.ground.Grounding;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;
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
    private int iterationCount=0;
    //private DatabaseConnector dc;
    private ArrayList<Clause> currentCLauseList;
    private ArrayList<Predicate> currentPredicateList;
    private ArrayList<Clause> previousCLauseList;
    private ArrayList<Predicate> previousPredicateList;
    private final Compress c;
    private String queryAtoms;
    
    

    public Compression(Grounding g, String s, long stat, int noOfIterations)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        System.out.println("Started0");
        //long startTime = System.nanoTime();
        grounding = g;
        mln = g.getMLN();
        db = mln.getRDB();
        db.schema = Config.db_schema;
//        printAllTablesinSchema();
        new ParseEvidence(db).parse(s,mln);
//        System.out.println("Evidence parsed");
        //dc = new DatabaseConnector(db,mln);
        
        c= new Compress(db,mln,noOfIterations);
//        System.out.println("Started");
        //printAllTablesinSchema();
        //Compress();


    }
    
    public void runBP() throws IOException
    {
        ArrayList<Clause> cl = c.getCompressedClauses();
        ArrayList<CBP.Compression.Predicate> pd = c.getCompressedPreds();
        BeliefPropagation bp;
//        bp = new BeliefPropagation(preds, cl);
//        System.out.println("Query Atoms: "+queryAtoms);
         bp = new BeliefPropagation(pd, cl);
//         ArrayList<Integer> iQuerys=new EstimateQuery(db).parse(queryAtoms,mln);
         bp.computeProbabilities();
        
    }

    public void printAllTablesinSchema()
    {
        try
        {
            //Statement statement = (Statement) connection.createStatement();
            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='" + db.schema + "'";
            ResultSet rs = db.query(sql);

            while (rs.next())
            {
                System.out.println("Table Name : " + rs.getString(1));

                String sql1 = "Select * from " + rs.getString(1);

                ResultSet rs1 = db.query(sql1);
                ResultSetMetaData rsmd = rs1.getMetaData();
                int n = rsmd.getColumnCount();
                for (int i = 1; i <= n; i++)
                {
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }

                System.out.println();
                while (rs1.next())
                {

                    for (int i = 1; i <= n; i++)
                    {
                        System.out.print(rs1.getString(i) + "\t");

                    }
                    System.out.println();
                }

                System.out.println("\n\n");

            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }
    
    public void Compress()
    {
        //mln0_clauses
        
            
            
        while(true)
        {
            System.out.println(iterationCount);
            
            
            
            if(convergence())
            {
                System.out.println(iterationCount);
                break;
            }
        }
    }
    
    private boolean convergence()
    {
        
        
        
        return false;
    }

   

    public void setQueryAtoms(String query)
    {
//        System.out.println("\n\ninside set:"+query);
        queryAtoms = query;
    }
    
}
