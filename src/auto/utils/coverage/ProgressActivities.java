package auto.utils.coverage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ProgressActivities {
	
	public static final String PROGRESS_ACTIVITY_OUTPUT = System.getProperty("user.dir") + "/activityOutput/progress/";
	
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException("Requires 2 arguments representing Fluid's \"UiDef Data\" and Sonarqube's \".pct\" directories");
		}
		
		System.out.println(args[0]);
		System.out.println(args[1]);
		
		File dir1 = new File(args[0]); 
		File dir2 = new File(args[1]);
		
		if (!dir1.isDirectory()) {
			throw new IllegalArgumentException("The supplied Fluid \"UiDef Data\" directory is invalid.");
		} else if (!dir2.isDirectory()) {
			throw new IllegalArgumentException("The supplied Sonarqube \".pct\" directory is invalid.");
		}
		
		Set<ActivityBean> activities = uiDefDirCollection(dir1.toPath());
		TreeMap<ActivityBean, TreeSet<FunctionBean>> entries = new TreeMap<>();
		
		for (ActivityBean activityBean : activities) {
			String pathToFile = dir2.getPath() + "/" + activityBean.getActivityService().replaceAll("\\.", "/") + "PR.cls.xref";
			
			File prXref = new File(pathToFile);
			final TreeSet<FunctionBean> methods = new TreeSet<FunctionBean>();
			if (prXref.isFile()) {
				Document xref = fileToXMLDocument(prXref);
				
				/*/frameworks/build/sonarqube/workspace/sonar/.pct/Frameworks/Activity/Pos
				nano VoidPosSuspendedSalesSchedPR.cls.xref*/
				
				processXref(xref, (refType, reference) -> {
					if ("METHOD".equals(refType)){
						boolean isOverridden = false;
						boolean isPrivate = false;
						
						for (int y = 0 ; y < reference.getChildNodes().getLength() ; y++) {
							Node methodValue = reference.getChildNodes().item(y);
							//<Access-mode>PUBLIC</Access-mode>
							//<Object-context>OVERRIDE</Object-context>
							switch (methodValue.getNodeName()) {
								case "Access-mode":
									isPrivate = "PRIVATE".equalsIgnoreCase(methodValue.getTextContent());
									break;
								case "Object-context":
									if (methodValue.getTextContent() != null) {
										isOverridden = methodValue.getTextContent().toUpperCase().contains("OVERRIDE");
									}
									break;
								default:
									// Do nothing
									break;
							}
						}
						if (!isPrivate && !isOverridden) {
							FunctionBean functionBean = new FunctionBean("", reference.getAttributes().getNamedItem("Object-identifier").getNodeValue());
							methods.add(functionBean);
						}
					}
				});
			}
			entries.put(activityBean, methods);
		}
		
		Bean.serialize(entries, new File(PROGRESS_ACTIVITY_OUTPUT + "_Activities.dat"));
	}
	
	public static void processXref(Document xref, BiConsumer<String, Node> criteria) {
		NodeList nodeList = xref.getChildNodes().item(0).getChildNodes();
		for (int i = 0 ; i < nodeList.getLength() ; i++) {
			Node source = nodeList.item(i);
			for (int x = 0 ; x < source.getChildNodes().getLength() ; x++) {
				Node reference = source.getChildNodes().item(x);
				if ("Reference".equals(reference.getNodeName())) {
					final String refType = reference.getAttributes().getNamedItem("Reference-type").getNodeValue();
					
					criteria.accept(refType, reference);
				}
			}
		}
	}
	
	public static Set<ActivityBean> uiDefDirCollection(Path path) {
		Stream<Path> stream;
		try {
			 stream = Files.walk(path)
				.filter(Files::isRegularFile);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IllegalStateException(ex);
		}
		
		final Set<ActivityBean> activities = new TreeSet<ActivityBean>();
		try {
			stream.forEach(p -> {
				Document document = ProgressActivities.fileToXMLDocument(p.toFile());
				NodeList nodeList = document.getFirstChild().getChildNodes();
				
				for (int i = 0 ; i < nodeList.getLength() ; i++) {
					Node node = nodeList.item(i);
					
					if ("activityDef".equals(node.getNodeName())) {
						NodeList activityDef = node.getChildNodes();
						
						String activity = "";
						String activityDesc = "";
						String activityService = "";
						String activityType = "";
						String keyType = "";
						for (int y = 0 ; y < activityDef.getLength() ; y++) {
							Node activityDefNode = activityDef.item(y);
							switch (activityDefNode.getNodeName()) {
								case "activity":
									activity = activityDefNode.getTextContent();
									break;
								case "activityDesc":
									activityDesc = activityDefNode.getTextContent();
									break;
								case "activityService":
									activityService = activityDefNode.getTextContent();
									break;
								case "activityType":
									activityType = activityDefNode.getTextContent();
									break;
								case "keyType":
									keyType = activityDefNode.getTextContent();
									break;
								default:
									// Do nothing
									break;
							}
						}
						if (activityService != null && !activityService.isEmpty()) {
							activities.add(new ActivityBean(activity, activityDesc, activityService.replaceAll("\\/", "."), activityType, keyType));
						}
					}
				}
			});
		} finally {
			stream.close();
		}
		
		return activities;
	}
	
	public static Document fileToXMLDocument(File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);
		Document doc;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException(ex);
		}
		return doc;
	}
	
}
