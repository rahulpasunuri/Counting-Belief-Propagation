/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CBP.main;

import java.io.IOException;
import java.sql.SQLException;

import tuffy.parse.CommandOptions;
import tuffy.util.Config;
import tuffy.util.UIMan;
import CBP.main.Grounding;

/**
 *
 * @author shrutika
 */
public class Main
{

    public static void main(String[] args) throws SQLException, IOException
    {    	    
    	//parse the input options..
        CommandOptions options = UIMan.parseCommand(args);
        UIMan.println("*** Welcome to " + Config.product_name + "!");
        if (options == null)
        {
            return;
        }

        Config.keep_db_data =true;//TODO: Remove this.
        
        //mark all atoms as active for CBP
        Config.mark_all_atoms_active=true;
        
        //we should not partition for CBP
        Config.disable_partition =true;
        
        if (!options.isDLearningMode)
        {            
            long startTime = System.nanoTime();
            new Grounding().run(options,startTime);            
        } 
    }

}
