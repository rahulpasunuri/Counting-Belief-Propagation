/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.main;

import java.io.File;
import java.util.HashMap;
import tuffy.db.RDB;
import tuffy.ground.Grounding;
import tuffy.ground.KBMC;
import tuffy.infer.DataMover;
import tuffy.mln.Clause;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;
import tuffy.util.ExceptionMan;
import tuffy.util.FileMan;
import tuffy.util.Timer;
import tuffy.util.UIMan;

/**
 *
 * @author shrutika
 */
public abstract class Ground {
	
	/**
	 * The DB.
	 */
	protected RDB db = null;
	
	public DataMover dmover = null;
	
	/**
	 * The MLN.
	 */
	public MarkovLogicNetwork mln = null;
	
	/**
	 * Command line options.
	 */
	protected CommandOptions options = null;
	
	/**
	 * Grounding worker.
	 */
	protected Grounding grounding = null;

	/**
	 * Ground the MLN into an MRF.
	 */
	protected void ground(){
		grounding = new Grounding(mln);
		grounding.constructMRF();
               UIMan.println("After Construction");
                
	}

	/**
	 * Set up MLN inference, including the following steps:
	 * 
	 * 1) loadMLN {@link Infer#loadMLN};
	 * 2) store symbols and evidence {@link MarkovLogicNetwork#materializeTables};
	 * 3) run KBMC;
	 * 4) apply scoping rules;
	 * 5) mark query atoms in the database {@link MarkovLogicNetwork#storeAllQueries()}.
	 * 
	 * @param opt command line options.
	 */
	protected void setUp(CommandOptions opt){
		options = opt;
		Timer.resetClock();

		Clause.mappingFromID2Const = new HashMap<Integer, String>();
		Clause.mappingFromID2Desc = new HashMap<String, String>();
		
		UIMan.println(">>> Connecting to RDBMS at " + Config.db_url);
                //System.out.println("I am here");
		db = RDB.getRDBbyConfig();
		//System.out.println("I am here");
		db.resetSchema(Config.db_schema);
                //System.out.println("I am here3");
		mln = new MarkovLogicNetwork();
                //System.out.println("I am here");
		loadMLN(mln, db, options);
                //System.out.println("I am here5");
		mln.materializeTables();
		
		KBMC kbmc = new KBMC(mln);
		kbmc.run();
		mln.executeAllDatalogRules();
		mln.applyAllScopes();
		UIMan.verbose(1, ">>> Marking queries...");
		mln.storeAllQueries();
		
		for(Clause c : mln.listClauses){
			if(c.isHardClause()){
				c.isFixedWeight = true;
			}else{
				c.isFixedWeight = false;
			}
		}
                
               // System.out.println(db.schema);
		
	}
	
	protected void setUp_noloading(CommandOptions opt){
		options = opt;
		Timer.resetClock();

		//Clause.mappingFromID2Const = new HashMap<Integer, String>();
		//Clause.mappingFromID2Desc = new HashMap<String, String>();
		
		UIMan.println(">>> Connecting to RDBMS at " + Config.db_url);
		db = RDB.getRDBbyConfig();
		
	}
	
	/**
	 * Clean up temporary data: the schema in PostgreSQL and the working directory.
	 */
	protected void cleanUp(){		
		Config.exiting_mode = true;
		UIMan.println(">>> Cleaning up temporary data");
		if(!Config.keep_db_data){
			UIMan.print("    Removing database schema '" + Config.db_schema + "'...");
			UIMan.println(db.dropSchema(Config.db_schema)?"OK" : "FAILED");
		}else{
			UIMan.println("    Data remains in schema '" + Config.db_schema + "'.");
		}
		db.close();

		UIMan.print("    Removing temporary dir '" + Config.getWorkingDir() + "'...");
		UIMan.println(FileMan.removeDirectory(new File(Config.getWorkingDir()))?"OK" : "FAILED");

		UIMan.println("*** Tuffy exited at " + Timer.getDateTime() + " after running for " + Timer.elapsed());
		UIMan.closeDribbleFile();
	}
	
	/**
	 * Load the rules and data of the MLN program.
	 * 
	 * 1) {@link MarkovLogicNetwork#loadPrograms(String[])};
	 * 2) {@link MarkovLogicNetwork#loadQueries(String[])};
	 * 3) {@link MarkovLogicNetwork#parseQueryCommaList(String)};
	 * 4) Mark closed-world predicate specified by {@link CommandOptions#cwaPreds};
	 * 5) {@link MarkovLogicNetwork#prepareDB(RDB)};
	 * 6) {@link MarkovLogicNetwork#loadEvidences(String[])}.
	 * 
	 * @param mln the target MLN
	 * @param adb database object used for this MLN
	 * @param opt command line options
	 */
	protected void loadMLN(MarkovLogicNetwork mln, RDB adb, CommandOptions opt){
		//System.out.println("HELLO1");
		String[] progFiles = opt.fprog.split(",");
                //System.out.println("HELLO1.5");
		mln.loadPrograms(progFiles);
                //System.out.println("HELLO1.9");
                
		if(opt.fquery != null){
                        //System.out.println("HELLO2");
			String[] queryFiles = opt.fquery.split(",");
                           //System.out.println("HELLO2.2");
			mln.loadQueries(queryFiles);
                        //System.out.println("HELLO3");
		}
		
		if(opt.queryAtoms != null){
                        //System.out.println("HELLO4");
			UIMan.verbose(2, ">>> Parsing query atoms in command line");
                        //System.out.println("HELLO1.5");
			mln.parseQueryCommaList(opt.queryAtoms);
                        //System.out.println("HELLO3");
		}

		if(opt.cwaPreds != null){
			String[] preds = opt.cwaPreds.split(",");
			for(String ps : preds){
				Predicate p = mln.getPredByName(ps);
				if(p == null){
					mln.closeFiles();
					ExceptionMan.die("COMMAND LINE: Unknown predicate name -- " + ps);
				}else{
					p.setClosedWorld(true);
				}
			}
		}
		
		mln.prepareDB(adb);
		
		if(opt.fevid != null){
			String[] evidFiles = opt.fevid.split(",");
			mln.loadEvidences(evidFiles);
		}
		
		dmover = new DataMover(mln);
	}


	
	
}

