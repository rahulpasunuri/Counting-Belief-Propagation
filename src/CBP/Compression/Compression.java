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
    private String queryAtoms;       
    String queryFileName;
    public Compression(Grounding g, String s, long stat, int noOfIterations, String queryFileName, String progFileName)
    {
        grounding = g;
        mln = g.getMLN();
        db = mln.getRDB();
        db.schema = Config.db_schema;
        this.queryFileName=queryFileName;
        new ParseEvidence(db).parse(s,mln);
        c= new Compress(db, mln,noOfIterations, progFileName, true); //last variable is just a debug parameter.
    }
    
    public void runBP() throws IOException, SQLException
    {
    	System.out.println("Starting BP");
        ArrayList<Clause> cl = c.getCompressedClauses();
        ArrayList<CBP.Compression.Predicate> pd = c.getCompressedPreds();
        BeliefPropagation bp;        
        bp = new BeliefPropagation(pd, cl, EstimateQuery.getQueryAtomIds(queryFileName, db, mln));        
        bp.computeProbabilities();        
    }
/*
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
*/
   

    public void setQueryAtoms(String query)
    {
        queryAtoms = query;
    }
    
}
