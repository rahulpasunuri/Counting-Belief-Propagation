package CBP.Infer;

import CBP.Compression.Clause;
import CBP.Compression.Predicate;
import CBP.Infer.GraphStructure.Edge;
import CBP.Infer.GraphStructure.FactorGraph;
import CBP.Infer.GraphStructure.Graph;
import CBP.Infer.GraphStructure.Message;
import CBP.Infer.GraphStructure.Vertex;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class BoxPropagation {
    private final ArrayList<Predicate> preds;
    private final ArrayList<Clause> clauses;
    private final FactorGraph fg;
    private final Graph g;
    ArrayList<Vertex> vertices;
    private ArrayList<Query> queries;
	
    public BoxPropagation(ArrayList<Predicate> p, ArrayList<Clause> c, ArrayList<Query> queries)
    {
        preds = p;
        clauses = c;
        fg = new FactorGraph(preds, clauses);
        g = fg.getGraph();
        vertices = g.getVertices();  
        this.queries=queries;
        run();
    }
    
    private ArrayList<String> createTFCombinations(int length)
    {
    	ArrayList<String> res = new ArrayList<String>();
    	if(length==1)
    	{
    		res.add("T");
    		res.add("F");
    		return res;
    	}
    	ArrayList<String> t = createTFCombinations(length-1);
    	for(String s : t)
    	{
    		res.add("T"+s);
    		res.add("F"+s);
    	}
    	return res;
    }
	
    private void run()
    {
    	//TODO
    	System.out.println("Printin Queries..");
    	for(Query q : this.queries)
    	{
    		//create a tree for every query..
    		System.out.println(q.query+Integer.toString(q.id));
    	}
    }
    
    public void computeBoxProbabilities()
    {
    	//TODO
    	
    }
}

