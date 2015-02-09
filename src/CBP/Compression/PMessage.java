/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Compression;

import java.util.ArrayList;

/**
 *
 * @author shrutika
 */
class PMessage 
{
    private ArrayList<String> msg = new ArrayList<String>();

    public ArrayList<String> getMessage()
    {
    	return msg;
    }
    
    public void clear()
    {    	
    	msg.clear();
    }
    
    void addClauseMsgToPredicate(String s) 
    {
        msg.add(s);
    }
    
}
