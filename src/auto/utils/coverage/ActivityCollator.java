package auto.utils.coverage;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import auto.utils.ApiUtils;
import auto.utils.Reader;

public class ActivityCollator {

	public static void main(String[] args) {
		
		boolean automationOnly = false;
		
		System.out.println(AppServerAdapterRecorder.FLUID_ACTIVITY_OUTPUT + "_Activities.dat");
		System.out.println(ProgressActivities.PROGRESS_ACTIVITY_OUTPUT + "_Activities.dat");
		
		File automationFile = new File(AppServerAdapterRecorder.FLUID_ACTIVITY_OUTPUT + "_Activities.dat");
		File progressFile;
		if (automationOnly) {
			progressFile = automationFile;
		} else {
			progressFile = new File(ProgressActivities.PROGRESS_ACTIVITY_OUTPUT + "_Activities.dat");
		}
		
		if (!automationFile.isFile() || !progressFile.isFile()) {
			throw new NullPointerException("Automation or Progress '_Activities.dat' file was null or missing!");
		}
		
		Map<ActivityBean, Set<FunctionBean>> collated = new TreeMap<>(new Comparator<ActivityBean>() {
			@Override
			public int compare(ActivityBean o1, ActivityBean o2) {
				return o1.toString().compareToIgnoreCase(o2.toString());
			}
		});
		
		Map<ActivityBean, Set<FunctionBean>> progress = Bean.deserialize(progressFile);
		Map<ActivityBean, Set<FunctionBean>> automation = Bean.deserialize(automationFile);
		
		collated.putAll(automation); // Don't
		collated.putAll(progress); // Even
		collated.putAll(automation); // Ask
		
		final List<ActivityBean> ordered = new LinkedList<ActivityBean>(collated.keySet());
		final Map<String, Integer> analyticsActivityPriority = fetchAnalytics();
		
		ordered.sort(new Comparator<ActivityBean>() {
			@Override
			public int compare(ActivityBean a, ActivityBean b) {
				Integer aScore = analyticsActivityPriority.get(a.getActivityService());
				Integer bScore = analyticsActivityPriority.get(b.getActivityService());
				if (aScore == null) {
					aScore = 0;
				}
				if (bScore == null) {
					bScore = 0;
				}
				return bScore.compareTo(aScore);
			}
		});
		
		int totalMethodsSize = 0;
		int totalMethodsCalled = 0;
		int totalUniqueValues = 0;
		
		int activityFiles = 0;
		//Arbitrary, change as you see fit, for all: topX = ordered.size();
		final int topX = ordered.size(); 
		
		int line = 1;
		String[] reportLines = new String[ordered.size() + 1];
		reportLines[0] = "No. \tService \tName \tDescription \tPage Views \tFunctions \tUnique Values \tIn Automation? \tLoadActivityDef # \tLoadData # \tGetResults # \tPresenter # \tSaveData #";
		for (ActivityBean activity: ordered) {
			
			int methodsSize = 0;
			int methodsCalled = 0;
			int uniqueValues = 0;
			
		 	Set<FunctionBean> functionBeans = collated.get(activity);
			methodsSize = functionBeans.size();
			for (FunctionBean functionBean : functionBeans) {
				if (!functionBean.getValues().isEmpty()) {
					uniqueValues += functionBean.getValues().size();
					methodsCalled++;
				}
			}
			
			totalUniqueValues += uniqueValues;
			totalMethodsCalled += methodsCalled;
			totalMethodsSize += methodsSize;
			
			BigDecimal methodsSizeBD = new BigDecimal(methodsSize);
			BigDecimal methodsCalledBD = new BigDecimal(methodsCalled);
			BigDecimal perc = percentageOf(methodsCalledBD, methodsSizeBD);
			
			final String methodPerc = "%" + perc.stripTrailingZeros().toPlainString()
					+ " (" + methodsCalledBD + "/" + methodsSizeBD + ")";
			final Integer pageViews = analyticsActivityPriority.get(activity.getActivityService()) != null
					? analyticsActivityPriority.get(activity.getActivityService())
					: 0;
			
			if (activityFiles < topX/* && automation.containsKey(activity)*/) {
				String[] functions = new String[collated.get(activity).size() + 1];
				functions[0] = "No. \tFunction \tTableField \tIs Visible? \tTimes Called # \tUnique Values #";
				int functionLine = 1;
				for (FunctionBean functionBean : collated.get(activity)) {
					String visible = functionBean.isVisible() == null ? "?" : functionBean.isVisible() ? "YES" : "" ;
					
					functions[functionLine] = functionLine + " \t" + functionBean.getIpFunction() + "\t" + functionBean.getTableField() + "\t" + visible + "\t" + functionBean.getTimesCalled() + "\t" + functionBean.getValues().size();
					functionLine++;
				}
				
				StringBuilder strBldr = new StringBuilder();
				strBldr.append("Name: " + activity.getActivity() + "\r\n");
				strBldr.append("Service: " + activity.getActivityService() + "\r\n");
				strBldr.append("Description: " + activity.getActivityDesc() + "\r\n");
				strBldr.append("\r\n");
				
				strBldr.append("PageViews: " + pageViews + " \t");
				strBldr.append("Functions Reached: " + methodPerc + " \t"); 
				strBldr.append("Total Unique values: " + uniqueValues + " \t");
				
				strBldr.append("LoadActivityDef: " + activity.getTimesLoadActivityDef() + " \t");
				strBldr.append("LoadData: " + activity.getTimesLoadData() + " \t");
				strBldr.append("GetResults: " + activity.getTimesGetResults() + " \t");
				strBldr.append("SaveData: " + activity.getTimesPresenter() + "\r\n");
				
				String[] finalFormat = ApiUtils.prettyPrintStrings(functions, "\t");
				String underLine = StringUtils.repeat('-', finalFormat[0].length());
				strBldr.append(underLine + "\r\n");
				strBldr.append(finalFormat[0] + "\r\n");
				strBldr.append(underLine);
				finalFormat[0] = strBldr.toString();
				
				if (automationOnly) {
					ApiUtils.writeToFile(new File(System.getProperty("user.dir") + "/activityOutput/automation/" + activity.getActivityService() + "-" + activity.getActivity() + ".txt"), finalFormat);
				} else {
					ApiUtils.writeToFile(new File(System.getProperty("user.dir") + "/activityOutput/" + activity.getActivityService() + "-" + activity.getActivity() + ".txt"), finalFormat);
				}
				activityFiles++;
			}
			
			reportLines[line] = line + " \t" + activity.getActivityService() + "\t" + activity.getActivity() + "\t"
					+ activity.getActivityDesc() + "\t" + pageViews + "\t" + methodPerc + "\t" + uniqueValues + "\t"
					+ (automation.containsKey(activity) ? "YES" : " ") + "\t" + activity.getTimesLoadActivityDef() + "\t" + activity.getTimesLoadData() + "\t"
					+ activity.getTimesGetResults() + "\t" + activity.getTimesPresenter() + "\t"
					+ activity.getTimesSaveData();
			line++;
		}
		
		BigDecimal automationSizeBD = new BigDecimal(automation.size());
		BigDecimal progressSizeBD = new BigDecimal(progress.size());
		final BigDecimal activityCoverage = percentageOf(automationSizeBD, progressSizeBD);
		
		BigDecimal totalMethodsSizeBD = new BigDecimal(totalMethodsSize);
		BigDecimal totalMethodsCalledBD = new BigDecimal(totalMethodsCalled);
		final BigDecimal totalMethodPerc = percentageOf(totalMethodsCalledBD, totalMethodsSizeBD);
		
/*		BigDecimal totalAutoItems = totalAutoMethodsBD.add(automationSize);
		BigDecimal totalProgItems = totalProgMethodsBD.add(progressSize);
		final BigDecimal totalPerc = percentageOf(totalAutoItems, totalProgItems);*/
		
		StringBuilder strBldr = new StringBuilder();
		//strBldr.append("Total Coverage: %" + totalPerc.stripTrailingZeros().toPlainString() + "\t ");
		strBldr.append("Total Activity Reach: %" + activityCoverage.stripTrailingZeros().toPlainString() + " (" + automation.size() +  "/" + progress.size() + ")\t ");
		strBldr.append("Total Function Reach: %" + totalMethodPerc.stripTrailingZeros().toPlainString() + " (" + totalMethodsCalled + "/" + totalMethodsSize + ")\t "); 
		strBldr.append("Total Unique values: " + totalUniqueValues + "\r\n");
		
		String[] finalFormat = ApiUtils.prettyPrintStrings(reportLines, "\t");
		String underLine = StringUtils.repeat('-', finalFormat[0].length());
		strBldr.append(underLine + "\r\n");
		strBldr.append(finalFormat[0] + "\r\n");
		strBldr.append(underLine);
		finalFormat[0] = strBldr.toString();
		
		if (automationOnly) {
			ApiUtils.writeToFile(new File(System.getProperty("user.dir") + "/activityOutput/automation/_ActivityStatsGeneral.txt"), finalFormat);
		} else {
			ApiUtils.writeToFile(new File(System.getProperty("user.dir") + "/activityOutput/_ActivityStatsGeneral.txt"), finalFormat);
		}
	}

	private static BigDecimal percentageOf(BigDecimal a, BigDecimal b) {
		BigDecimal perc;
		if (b.compareTo(BigDecimal.ZERO) != 0) {
			perc = a.divide(b, 10, RoundingMode.HALF_UP).scaleByPowerOfTen(2).setScale(2, RoundingMode.HALF_UP);
		} else {
			perc = BigDecimal.ZERO;
		}
		return perc;
	}
	
	static Map<String, Integer> fetchAnalytics() {
		Iterable<Object[]> analytics = Reader.inputData("/Analytics.csv");
		Map<String, Integer> analyticsActivityPriority = new HashMap<String, Integer>();
		for (Object[] obj : analytics) {
			if (obj.length != 3) {
				throw new IllegalArgumentException("Invalid analytics file.");
			}
			
			String service = String.valueOf(obj[0]);
			Integer pageViews = Integer.valueOf(String.valueOf(obj[2]));
			
			if (analyticsActivityPriority.keySet().contains(service)) {
				pageViews += analyticsActivityPriority.get(service);
			} 
			
			analyticsActivityPriority.put(service, pageViews);
		}
		return analyticsActivityPriority;
	}
	
}
