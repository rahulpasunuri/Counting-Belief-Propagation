package CBP.Infer;

import CBP.Compression.Clause;
import CBP.Compression.Predicate;
import CBP.Infer.GraphStructure.Edge;
import CBP.Infer.GraphStructure.FactorGraph;
import CBP.Infer.GraphStructure.Graph;
import CBP.Infer.GraphStructure.Tree;
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
    

	
    private void run()
    {
    	//loop over queries..
    	for(Query q : this.queries)
    	{
    		//create a tree for every query..
    		Tree t = new Tree(this.fg, q.id);
    		
    		
    		
    		break; // TODO: remove this..
    	}
    }
    
    public void computeBoxProbabilities()
    {
    	//TODO
    	
    }
}

