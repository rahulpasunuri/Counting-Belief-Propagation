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
class CMessage {
    String s;
    
//    int hashCode;

    void addliteralMessage(String temp) 
    {
         //To change body of generated methods, choose Tools | Templates.
        //System.out.println("here");
        if(s == null)
            s=temp;
        else
            s = s+ " "+ temp;
    }
    
    
}
