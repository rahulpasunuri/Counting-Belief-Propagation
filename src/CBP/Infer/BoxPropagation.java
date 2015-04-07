package CBP.Infer;

import CBP.Compression.BoxMessage;
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
import java.io.FileWriter;

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
    }
    
    public void run() throws IOException
    {
        System.out.println("Computing Probabilities");
        String fileName = "results.txt";
        File file = new File(fileName);

        // if file doesn't exists, then create it
        if (!file.exists())
        {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);        
        
    	//loop over queries..
    	for(Query q : this.queries)
    	{
    		//create a tree for every query..
    		Tree t = new Tree(this.fg, q.id);
    		BoxMessage bm = t.runBoxPropagation();    		
    		String temp = q.query+": ["+bm.getLowerBound()+", "+bm.getUpperBound()+"]\n";
    		bw.write(temp);
    		//break; // TODO: remove this..
    	}
    	bw.flush();
        bw.close();    
        System.out.println("Results have been saved in: "+fileName);
    }
}

