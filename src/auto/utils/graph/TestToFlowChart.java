package auto.utils.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import auto.utils.Reader;
import auto.utils.step.Step;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.ChildActivityDefinition;

public class TestToFlowChart {
	
	public TestToFlowChart() {}
	
	public static List<ActivityDefinition> hierarchyRecurser(ActivityDefinition activityDef){
		return hierarchyRecurser(activityDef, new ArrayList<ActivityDefinition>());
	}
	
	public static List<ActivityDefinition> hierarchyRecurser(ActivityDefinition activityDef, List<ActivityDefinition> order) {
		order.add(0, activityDef);
		if (activityDef instanceof ChildActivityDefinition) {
			ActivityDefinition parentActivityDef = ((ChildActivityDefinition) activityDef).getParentActivityDefinition();
			hierarchyRecurser(parentActivityDef, order);
		}
		return order;
	}
	
	public static boolean doesNodeExist(Collection<Graph.SubGraph> subGraphs, String node) {
		boolean exists = false;
		for (Graph.SubGraph subGraph : subGraphs) {
			if (subGraph.getEntrySet().contains(node)) {
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	private static Graph.SubGraph subGraphResolver(Graph graph, Map<List<ActivityDefinition>, Graph.SubGraph> depthSubGraphs, List<ActivityDefinition> newDepth) {
		Graph.SubGraph subGraph = null;
		for (List<ActivityDefinition> knownDepth : depthSubGraphs.keySet()) {
			if (knownDepth.size() != newDepth.size()) {
				continue; //Not a match
			}
			boolean isMatch = true;
			for (int i = 0 ; i < newDepth.size() ; i++) {
				ActivityDefinition newer = newDepth.get(i); 
				ActivityDefinition known = knownDepth.get(i);
				//if (newer != known) {
				if (!newer.getService().equals(known.getService())) {
					isMatch = false;
					break;
				}
			}
			if (isMatch) {
				subGraph = depthSubGraphs.get(knownDepth);
				break;
			}
		}
		if (subGraph == null) {
			subGraph = graph.new SubGraph();
			depthSubGraphs.put(newDepth, subGraph);
		}
		return subGraph;
	}
	
	public static void writeTestToFile(Set<List<Step>> tests, String fileName) {
		Graph graph = new Graph();
		//graph.getEntrySet().add("layout=fdp");
		Map<List<ActivityDefinition>, Graph.SubGraph> depthSubGraphs = new HashMap<List<ActivityDefinition>, Graph.SubGraph>();
		
		for (List<Step> test : tests) {
			for (int i = 0 ; i < test.size() ; i++) {
			//for (Step step: test) {
				Step step = test.get(i);
				
				ActivityDefinition stepActivity = step.getCalledActivityDefinition();
				List<ActivityDefinition> newDepth = hierarchyRecurser(stepActivity);
				
				Graph subGraph = subGraphResolver(graph, depthSubGraphs, newDepth);
				Set<String> dotContent = subGraph.getEntrySet();
				
				dotContent.add("label = \"" + stepActivity.getService() + "\";");
				//dotContent.add("label = \"" + stepActivity.getService() + " (" + i + ")\";"); // Don't use this
				
				
				// 1 Don't separate and allow recursive flow
				//dotContent.add("\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\";");
				
				// 2 Separate each step
				//dotContent.add("\"" + step.getCalledActivityName() + ":" + step.getTableField() + " (" + i + ")\";");
				
				// 3 Only separate if duplicate/exists
				String node = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\";";
				int dupe = 0;
				while (doesNodeExist(depthSubGraphs.values(), node)) {
					if (subGraph.getEntrySet().contains(node)) {
						break;
					}
					dupe++;
					node = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + " (" + dupe + ")\";";
				}
				
				dotContent.add(node);
				
				
				// Really just a backup for minimal functionality, well surpassed by now
/*				for (ChildActivityDefinition childActivityDef : step.getChildren()) {
					List<ActivityDefinition> shallowClone = new ArrayList<ActivityDefinition>(newDepth);
					shallowClone.add(childActivityDef);
					Graph.SubGraph nextSubGraph = subGraphResolver(graph, depthSubGraphs, shallowClone); 
					nextSubGraph.getEntrySet().add("label = \"" + childActivityDef.getService() + "\";");
					dotContent.add("\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\" -> " + nextSubGraph.getClusterName() + ";");
				}*/
/*				for (Step subStep: step.getSubSteps()) {
					dotContent.add("\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\" -> \"" + subStep.getCalledActivityName() + ":" + subStep.getTableField() + "\";");
				}*/
	/*			if (i < test.size() - 1) {
					Step nextStep = test.get(1 + i);
					String dot = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\" -> \"" + nextStep.getCalledActivityName() + ":" + nextStep.getTableField() + "\";"; 
					dotContent.add(dot);
				}*/
			}
		}
		
		List<List<ActivityDefinition>> shortestToLongest = new ArrayList<>(depthSubGraphs.keySet());
		
		Map<List<ActivityDefinition>, Graph.SubGraph> cascade = new HashMap<>(depthSubGraphs);
		shortestToLongest.sort(Comparator.comparing(List::size)); //Don't have time to reverse this crap
		for (int i = shortestToLongest.size() - 1 ; i >= 0 ; i--) {
			List<ActivityDefinition> depth = shortestToLongest.get(i);
			Graph subGraph = cascade.get(depth);
			
			// Style graphs based on odd/even numbers
/*			if (i % 2 == 0) {
				subGraph.getEntrySet().add("node [style=filled,color=white];\r\n" + 
						"style=filled;\r\n" + 
						"color=lightgrey;");
			} else {
				subGraph.getEntrySet().add("node [style=filled,color=lightgrey];\r\n" + 
						"style=filled;\r\n" + 
						"color=white;");
			}*/
			
			List<ActivityDefinition> parentDepth = new ArrayList<ActivityDefinition>(depth);
			parentDepth.remove(parentDepth.size() - 1);
			
			if (!parentDepth.isEmpty()) {
				Graph.SubGraph parentSubGraph = subGraphResolver(graph, depthSubGraphs, parentDepth);
				parentSubGraph.getEntrySet().add(subGraph.build());
				
				cascade.remove(depth);
				//subGraph.getEntrySet().clear();
			}
		}
		
		for (Graph.SubGraph subGraph : cascade.values()) {
			graph.getEntrySet().add(subGraph.build());
		}
		
		for (List<Step> test : tests) {
			for (int i = 0 ; i < test.size() ; i++) {
			//for (Step step: test) {
				Step step = test.get(i);
				
				// Code for pointing directly at clusters, not as useful as you may think
/*				for (ChildActivityDefinition childActivityDef : step.getChildren()) {
					ActivityDefinition stepActivity = step.getCalledActivityDefinition();
					List<ActivityDefinition> newDepth = hierarchyRecurser(stepActivity);
					
					List<ActivityDefinition> shallowClone = new ArrayList<ActivityDefinition>(newDepth);
					shallowClone.add(childActivityDef);
					Graph.SubGraph nextSubGraph = subGraphResolver(graph, depthSubGraphs, shallowClone); 
					nextSubGraph.getEntrySet().add("label = \"" + childActivityDef.getService() + "\";");
					graph.getEntrySet().add("\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\" -> " + nextSubGraph.getClusterName() + ";");
				}*/
				
/*				for (Step subStep: step.getSubSteps()) {
					graph.getEntrySet().add("\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\" -> \"" + subStep.getCalledActivityName() + ":" + subStep.getTableField() + "\";");
				}*/
				
				if (i < test.size() - 1) {
					Step nextStep = test.get(1 + i);
					
					
					// 1
					//String dot = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\" -> \"" + nextStep.getCalledActivityName() + ":" + nextStep.getTableField() + "\";"; 
					// 2
					//String dot = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + " (" + i + ")\" -> \"" + nextStep.getCalledActivityName() + ":" + nextStep.getTableField() + " (" + (1 + i) + ")\";"; 
					// 3
					ActivityDefinition stepActivity;
					List<ActivityDefinition> newDepth;
					
					stepActivity = step.getCalledActivityDefinition();
					newDepth = hierarchyRecurser(stepActivity);
					Graph currentSubGraph = subGraphResolver(graph, depthSubGraphs, newDepth);
					String currentNode = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + "\";";
					int dupe = 0;
					while (doesNodeExist(depthSubGraphs.values(), currentNode)) {
						if (currentSubGraph.getEntrySet().contains(currentNode)) {
							currentNode = currentNode.substring(0, currentNode.length() - 1); // Remove semi-colon
							break;
						}
						dupe++;
						currentNode = "\"" + step.getCalledActivityName() + ":" + step.getTableField() + " (" + dupe + ")\";";
					}
					
					stepActivity = nextStep.getCalledActivityDefinition();
					newDepth = hierarchyRecurser(stepActivity);
					Graph nextSubGraph = subGraphResolver(graph, depthSubGraphs, newDepth);
					String nextNode = "\"" + nextStep.getCalledActivityName() + ":" + nextStep.getTableField() + "\";";
					dupe = 0;
					while (doesNodeExist(depthSubGraphs.values(), nextNode)) {
						if (nextSubGraph.getEntrySet().contains(nextNode)) {
							//nextNode = nextNode.substring(0, nextNode.length() - 1); // Remove semi-colon
							break;
						}
						dupe++;
						nextNode = "\"" + nextStep.getCalledActivityName() + ":" + nextStep.getTableField() + " (" + dupe + ")\";";
					}
					
					//String dot = currentNode + " -> " + nextNode + " [ label = \"" + (1 + i) + "\" ];";
					String dot = currentNode + " -> " + nextNode;
					
					
					graph.getEntrySet().add(dot);
				}
			}
		}
		
		buildGraph(graph, fileName);
	}
	
	private static void buildGraph(Graph graph, String fileName) {
		GraphViz gv = new GraphViz();
		//gv.addln(GraphViz.start_graph());
		gv.addln(graph.build());
/*		for (String path : paths) {
			gv.addln(path);
		}*/
/*		gv.addln("A -> B;");
		gv.addln("A -> C;");*/
		//gv.addln(GraphViz.end_graph());
		System.out.println(gv.getDotSource());
		
		gv.increaseDpi(); // 106 dpi

		String type = "gif";
		// String type = "dot";
		// String type = "fig"; // open with xfig
		// String type = "pdf";
		// String type = "ps";
		// String type = "svg"; // open with inkscape
		// String type = "png";
		 //String type = "plain";

		String repesentationType = "dot";
		// String repesentationType= "neato";
		//String repesentationType= "fdp";
		// String repesentationType= "sfdp";
		// String repesentationType= "twopi";
		// String repesentationType= "circo";

		// Linux
		// File out = new File("/tmp/out"+gv.getImageDpi()+"."+ type); 
		// Windows
		File out = new File(Reader.USER_DIRECTORY + "/graph/" + fileName + "." + type); 
		System.out.println(gv.getDotSource());
		int i = gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, repesentationType), out);
		System.out.println(i);
	}

}
