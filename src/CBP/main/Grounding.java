/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.main;

import CBP.Compression.Compression;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import tuffy.db.RDB;
import tuffy.ground.partition.PartitionScheme;
import tuffy.infer.InferPartitioned;
import tuffy.mln.Clause;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;

import tuffy.parse.CommandOptions;
import tuffy.util.Config;
import tuffy.util.Settings;
import tuffy.util.UIMan;

/**
 *
 * @author shrutika
 */
public class Grounding extends Ground
{
    //private RDB db;

    //MarkovLogicNetwork mln1;
    public void run(CommandOptions opt,long start) throws SQLException, IOException
    {
        
        UIMan.println(">>> Parsing the Files for Grounding.....");

        setUp(opt);
        UIMan.println("\n\n>>> Before Ground.....");
        ground();
        
        
        
        Compression cp = new Compression(grounding, opt.addEvidence,start, opt.noOfIterations);
        cp.setQueryAtoms(opt.fquery);
        cp.runBP();
        
        
        cleanUp();
    }

}
