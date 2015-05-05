package tuffy.main;

import java.sql.SQLException;

import tuffy.learn.DNLearner;
import tuffy.learn.MultiCoreSGDLearner;
import tuffy.learn.SGDLearner;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;
import tuffy.util.ExceptionMan;
import tuffy.util.UIMan;
/**
 * The Main.
 */
public class Main {
	public static void main(String[] args) throws SQLException {
		
		CommandOptions options = UIMan.parseCommand(args);
		
		Config.mark_all_atoms_active=true;
        Config.disable_partition =true;
		UIMan.println("*** Welcome to " + Config.product_name + "!");
		if(options == null){
			return;
		}
		long start = System.nanoTime();
		if(!options.isDLearningMode){
			// INFERENCE
			if(!options.disablePartition){
				
				new PartInfer().run(options);
			}else{
				
				new NonPartInfer().run(options);
				
			}
		}else{
			
			if(options.mle){
				//SGDLearner l = new SGDLearner();
				MultiCoreSGDLearner l = new MultiCoreSGDLearner();
				l.run(options);
				l.cleanUp();
				
			}else{
				//LEARNING
				DNLearner l = new DNLearner();
				l.run(options);
			}
		}
		long end = System.nanoTime();
		System.out.println("Total Time taken(in milli seconds) is "+Long.toString((end-start)/ (long)Math.pow(10, 6)) );
	}
	
}
