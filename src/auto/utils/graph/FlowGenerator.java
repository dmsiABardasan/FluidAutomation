package auto.utils.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import auto.utils.Reader;
import auto.utils.ValidationTypeEnum;
import auto.utils.fluid.CrudButtonEnum;
import auto.utils.step.LazyProcedure;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;

public class FlowGenerator {

	private final static String VIEW = "Frameworks.Activity.System.SecurityGroup.SecurityGroupView";
	private final static String MAIN = "Frameworks.Activity.System.SecurityGroup.SecurityGroupMain";
	private final static String MENU_VIEW = "Frameworks.Activity.System.SecurityGroup.SecurityGroupMenuView";
	private final static String MENU_MAIN = "Frameworks.Activity.System.SecurityGroup.SecurityGroupMenuMain";

	public static void main(String[] args) {
		//AllSuites.setup(); // Login

		LazyProcedure procedure = new LazyProcedure();
		try {
			ActivityDefinition securityGroupView = procedure.lazyLoadActivity(VIEW);
			procedure.step(securityGroupView, ValidationTypeEnum.VALIDATE_ALL, CrudButtonEnum.FIND.getValue());
			procedure.step(securityGroupView, ValidationTypeEnum.VALIDATE_ALL, CrudButtonEnum.USE.getValue());
			procedure.assertExpectedChildren(2, MAIN, MENU_VIEW);

			ActivityDefinition securityGroupMenuView = procedure.cutChildFromList(MENU_VIEW);
			procedure.getChildList().clear(); //Remove other unwanted activities

			final DataSourceDefinition mastMenu = securityGroupMenuView.getDataSourceDefinitionMap().get("ttMastMenu");
			for (Record mastMenuRecord : mastMenu.getRecordList()) {
				mastMenu.setCurrentRow(mastMenu.getRecordList().indexOf(mastMenuRecord));

				procedure.step(true, securityGroupMenuView, ValidationTypeEnum.VALIDATE_ALL,
						"button.ttMastMenu_menuDesc");
				procedure.assertExpectedChildren(1, MENU_MAIN);

				ActivityDefinition securityGroupMenuMain = procedure.getChildList().remove(0);
				List<Record> recordList = securityGroupMenuMain.getDataSourceDefinitionMap().get("ttMastMenu")
						.getRecordList();

				createGraphs(mastMenuRecord, recordList);
				
			}
		} finally {
			System.out.println(procedure.prettyPrintProcedure());
		}
	}
	
	
	private static void createGraphs(Record rootRecord, Iterable<Record> childRecords) {
		
		GraphViz gv = new GraphViz();
		gv.addln(GraphViz.start_graph());
		//gv.addln("node [shape=\"box\"];");
		gv.addln("rankdir = LR;\r\n" + "node [shape=record];");

		//------------

		Set<String> activities = new HashSet<String>();
		Set<String> connections = new HashSet<String>();

		List<Record> foo = new ArrayList<Record>();
		final String topActivity = rootRecord.getFieldValueAsString("activity");
		final String topMenuDesc = rootRecord.getFieldValueAsString("menuDesc"); //topActivity.split("\\|")[0];
		final String fileName = topActivity.replace("|", "");
		foo.add(rootRecord);
		//activities.add("\"" + topActivity + "\" [label=\"" + topMenuDesc + "\", shape=cds];");
		activities.add("\"" + topActivity + "\" [target=\"_top\", href=\"" + topActivity.replace("|", "")
				+ ".html\",label=\"" + topMenuDesc + "\", shape=cds];");

		//-------------

		for (int i = 0; i < foo.size(); i++) {
			final Record parentActivity = foo.get(i);

			GraphViz stubGraph = new GraphViz();
			stubGraph.addln(GraphViz.start_graph());
			//gv.addln("node [shape=\"box\"];");
			stubGraph.addln("rankdir = LR;\r\n" + "node [shape=record];");
			Set<String> activitiesStub = new HashSet<String>();
			Set<String> connectionsStub = new HashSet<String>();

			String backHref;
			if (parentActivity.getFieldValueAsString("activity").equals(topActivity)) {
				backHref = "_Master_" + topActivity.replace("|", "") + ".svg";
			} else {
				backHref = parentActivity.getFieldValueAsString("menuParent").replace("|", "") + ".html";
			}

			activitiesStub.add("\"" + parentActivity.getFieldValueAsString("activity")
					+ "\" [target=\"_top\", href=\"" + backHref + "\",label=\""
					+ parentActivity.getFieldValueAsString("menuDesc").replaceAll("<[^>]*>", "") + "\", shape=cds];");

			String[] parentTypeArr = parentActivity.getFieldValueAsString("nodeActivity").split(",");
			//String parentType = "";
			String parentName = "";
			String parentService = "";
			if (parentTypeArr.length == 3) {
				//parentType = parentTypeArr[0];
				parentName = parentTypeArr[1];
				parentService = parentTypeArr[2];
			}

			for (Record record : childRecords) {
				final String childActivity = record.getFieldValueAsString("activity");
				final String childParent = record.getFieldValueAsString("menuParent");
				final String childType = record.getFieldValueAsString("nodeActivity");
				final String childDesc = record.getFieldValueAsString("menuDesc").replaceAll("<[^>]*>", "");

				if (parentActivity.getFieldValueAsString("activity").equals(childParent)) {
					final String shape;
					final String color;

					if (!childType.isEmpty()) {
						String[] typeArr = childType.split(",");
						String type = "";
						String name = "";
						String service = "";
						if (typeArr.length == 3) {
							type = typeArr[0];
							name = typeArr[1];
							service = typeArr[2];
						}

						switch (type) {
						case "htf":
							shape = ", shape=tab"; // Looks like a browser tab
							break;
						case "srd":
							shape = ", shape=folder";
							break;
						case "popupWindow":
							shape = ", shape=note"; // The dog ear'd rectangle
							break;
						case "sched":
							shape = ", shape=signature"; // The underline with x
							break;
						case "dash":
							shape = ", shape=box3d";
							break;
						case "dashSection":
							shape = ", shape=component"; // The thing with two tiny rectangles on the left side
							break;
						default:
							shape = ""; // Plain rectangle
							break;
						}

						File childCoverageFile = new File(
								Reader.USER_DIRECTORY + "/activityOutput/automation/" + service + "-" + name + ".txt");
						if (childCoverageFile.exists()) {
							color = ", color=green, style=filled, fillcolor=\"#ccffcc\"";
						} else {
							color = ", color=red";
							//color = ", color=red, style=filled, fillcolor=lightyellow";
						}
					} else {
						shape = "";
						color = "";
					}

					activitiesStub.add("\"" + childActivity + "\" [target=\"_top\", href=\""
							+ childActivity.replace("|", "") + ".html\",label=\"" + childDesc + "\"" + shape
							+ color + "];");
					connectionsStub.add("\"" + parentActivity.getFieldValueAsString("activity") + "\" -> \""
							+ childActivity + "\";");

					activities.add("\"" + childActivity + "\" [target=\"_top\", href=\""
							+ childActivity.replace("|", "") + ".html\",label=\"" + childDesc + "\"" + shape
							+ color + "];");
					connections.add("\"" + parentActivity.getFieldValueAsString("activity") + "\" -> \""
							+ childActivity + "\";");
					foo.add(record);
				}
			}

			final String stubGraphFileName = parentActivity.getFieldValueAsString("activity").replace("|", "");
			final String coverageFileName;
			if (parentActivity.getFieldValueAsString("activity").equals(topActivity)) {
				coverageFileName = "_ActivityStatsGeneral.txt";
			} else {
				coverageFileName = parentService + "-" + parentName + ".txt";
			}

			File coverageFile = new File(Reader.USER_DIRECTORY + "/activityOutput/" + coverageFileName);
			final String preContent;
			if (coverageFile.isFile()) {
				preContent = Reader.readFile("/activityOutput/" + coverageFileName);
			} else {
				preContent = "";
			}

			try {
				System.out.println(stubGraphFileName);
				File file = new File(Reader.USER_DIRECTORY + "/graph/html/" + topMenuDesc + "/"
						+ stubGraphFileName + ".html");
				if (!file.exists()) {
					new File(Reader.USER_DIRECTORY + "/graph/html/" + topMenuDesc + "/").mkdirs();
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("<!DOCTYPE html>\r\n" + "<html>\r\n" + "	<body>\r\n" + "		<object data=\""
						+ stubGraphFileName
						+ ".svg\" type=\"image/svg+xml\" style=\"position:absolute;top:0;left:0;width:40%;\"><!--<img src=\"fallback.jpg\" />--></object>\r\n"
						+ "		<pre style=\"position:absolute;top:0;right:0;width:60%;\">" + preContent
						+ "</pre>\r\n" + "	</body>\r\n" + "</html>");
				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			for (String activity : activitiesStub) {
				stubGraph.addln(activity);
			}
			for (String connection : connectionsStub) {
				stubGraph.addln(connection);
			}

			String type = "svg";
			String repesentationType = "dot";

			stubGraph.addln(GraphViz.end_graph());
			File out = new File(Reader.USER_DIRECTORY + "/graph/html/" + topMenuDesc + "/" + stubGraphFileName
					+ "." + type);
			int exitCode = stubGraph.writeGraphToFile(
					stubGraph.getGraph(stubGraph.getDotSource(), type, repesentationType), out);
			System.out.println(exitCode);
		}

		for (String activity : activities) {
			gv.addln(activity);
		}
		for (String connection : connections) {
			gv.addln(connection);
		}

		String type = "svg";
		String repesentationType = "dot";
		gv.addln(GraphViz.end_graph());
		//System.out.println(gv.getDotSource());

		File out = new File(Reader.USER_DIRECTORY + "/graph/html/" + topMenuDesc + "/_Master_"
				+ fileName.replace("|", "") + "." + type);
		int i = gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, repesentationType), out);
		System.out.println(i);
	}

}
