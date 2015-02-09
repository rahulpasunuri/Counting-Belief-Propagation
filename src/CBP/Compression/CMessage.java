/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CBP.Compression;

/**
 *
 * @author shrutika
 */
class CMessage 
{
    private String s;    
    
    public void clear()
    {
    	s="";    	
    }
    
    public String getMessage()
    {
    	return s;    	
    }
    
    void addliteralMessage(String temp) 
    {
        if(s == null)
            s=temp;
        else
            s = s+ " "+ temp;
    }       
}
