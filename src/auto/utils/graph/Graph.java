package auto.utils.graph;

import java.util.LinkedHashSet;
import java.util.Set;

public class Graph{
	private Set<String> entries = new LinkedHashSet<String>();
	
	public Graph() {}
	
	 Set<String> getEntrySet(){
		return this.entries;
	}
	
	public String build() {
		StringBuilder strBldr = new StringBuilder();
		strBldr.append(GraphViz.start_graph() + "\r\n");
		for (String entry: getEntrySet()) {
			strBldr.append(entry + "\r\n");
		}
		strBldr.append(GraphViz.end_graph());
		return strBldr.toString();
	}
	
	private int subgraphClusterIDCounter = 1;
	private int getUniqueClusterID() {
		return this.subgraphClusterIDCounter++;
	}
	
	public class SubGraph extends Graph{	
		private int clusterID;
		
		public SubGraph() {
			super();
			this.clusterID = getUniqueClusterID();
		}
		
		public int getClusterID() {
			return this.clusterID;
		}
		
		public String getClusterName() {
			return "cluster_" + getClusterID();
		}
		
		@Override
		public String build() {
			StringBuilder strBldr = new StringBuilder();
			strBldr.append(GraphViz.start_subgraph(getClusterID()) + "\r\n");
			for (String entry: getEntrySet()) {
				strBldr.append(entry + "\r\n");
			}
			strBldr.append(GraphViz.end_subgraph());
			return strBldr.toString();
		}
	}
}