package auto.utils.coverage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import auto.utils.AppServerAdapterSock;
import fluid.test.adapter.AppServerAdapter;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.message.ActivityTaskRequest;
import fluid.test.message.ActivityTaskResponse;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.CtrlPropPair;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadActivityResponse;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;
import fluid.test.message.SaveDataRequest;
import fluid.test.message.SaveDataResponse;

/**
 * Chuck in an AppServerAdapter instance and have it record fluid network traffic and write to file.
 * @author vercin
 */
public class AppServerAdapterRecorder extends AppServerAdapterSock {

	public boolean writeServicesAndFunctions = true;
	public boolean writeToProgress = false;
	final private Map<ActivityBean, Map<FunctionBean, FunctionBean>> activityFunctions = new HashMap<>();
	final private Map<ActivityBean, ActivityBean> activities = new HashMap<>();
	
	public static final String FLUID_ACTIVITY_OUTPUT = System.getProperty("user.dir") + "/activityOutput/automation/";
	
	public AppServerAdapterRecorder(AppServerAdapter appServerAdapter){
		super(appServerAdapter);
	}
	
	public Map<ActivityBean, ActivityBean> getActivities(){
		return this.activities;
	}
	
	public Map<ActivityBean, Map<FunctionBean, FunctionBean>> getRecordedActivityFunctions(){
		return this.activityFunctions;
	}
	
	public void writeToFile() {
		File activityFile = new File(FLUID_ACTIVITY_OUTPUT + "_Activities.dat");
		
		TreeMap<ActivityBean, TreeSet<FunctionBean>> map = new TreeMap<>();
		for (Map.Entry<ActivityBean, Map<FunctionBean, FunctionBean>> line : getRecordedActivityFunctions().entrySet()) {
			map.put(line.getKey(), new TreeSet<>(line.getValue().values()));
		}
		Bean.serialize(map, activityFile);
		
		if (writeToProgress) {
			File progressFile = new File(ProgressActivities.PROGRESS_ACTIVITY_OUTPUT+"_Activities.dat");
			Bean.serialize(map, progressFile);
		}
	}
	
	private void writeIpFunction(ActivityDefinition activityDef, ActivityBean activityBean, String ipFunction, PresentationMessage ipRequest) {
		Map<FunctionBean, FunctionBean> activityFunctions = getRecordedActivityFunctions().get(activityBean);
		FunctionBean searchBean = new FunctionBean(ipRequest.getCallingField(), ipFunction);
		
		String value;
		String callingField = ipRequest.getCallingField();
		if (callingField != null && !callingField.isEmpty() && !callingField.startsWith("button")) {
			String[] tableField = callingField.split("\\.");
			value = activityDef.getDataSourceDefinitionMap().get(tableField[0]).getCurrentRecord().getFieldValueAsString(tableField[1]);
		} else {
			value = ipRequest.getCtrlProperties().toString();
		}
		
		if (activityFunctions.containsKey(searchBean)) {
			FunctionBean functionBean = activityFunctions.get(searchBean);
			functionBean.getValues().add(value);
			functionBean.setTimesCalled(functionBean.getTimesCalled() + 1);
		} else {
			searchBean.getValues().add(value);
			searchBean.setTimesCalled(searchBean.getTimesCalled() + 1);
			activityFunctions.put(searchBean, searchBean);
		}
	}
	
	private ActivityBean writeActivity(ActivityDefinition activityDef) {

		final LoadActivityResponse model = activityDef.getModel();
		
		String activity = model != null ? model.getActivity() : "";
		String activityService = activityDef.getService();
		String keyType = "System";
		String activityDesc = model != null ? model.getActivityDesc() : "";
		String activityType = model != null ? model.getActivityType() : "";
		
		ActivityBean activityBean = new ActivityBean(activity, activityDesc, activityService, activityType, keyType);
		if (getActivities().containsKey(activityBean)) {
			activityBean = getActivities().get(activityBean);
		}else {
			getActivities().put(activityBean, activityBean);
		}
		
		Map<FunctionBean, FunctionBean> functions;
		if (getRecordedActivityFunctions().containsKey(activityBean)) {
			functions = getRecordedActivityFunctions().get(activityBean);
		} else {
			functions = new HashMap<FunctionBean, FunctionBean>();
			getRecordedActivityFunctions().put(activityBean, functions);
		}
		
		if (model != null) {
			for (Map.Entry<CtrlPropPair, String> ctrlProp : model.getCtrlProperties().entrySet()) {
				String name = ctrlProp.getKey().getName();
				CtrlPropEnum prop = ctrlProp.getKey().getProp();
				String value = ctrlProp.getValue();
				
				if (CtrlPropEnum.VALIDATIONREQ == prop && "yes".equals(value)) {
					String ipFunction = model.getCtrlProp(name, CtrlPropEnum.VALIDATIONFUNCTION);
					
					if (ipFunction == null/* || ipFunction.isEmpty() */) {
						ipFunction = name.replaceAll("\\.", "");
					}
					
					final FunctionBean functionBean;
					String tableField[] = name.split("\\.");
					if (tableField.length == 2) {
						String table = tableField[0];
						String field = tableField[1];
						
						Map<String, DataSourceDefinition> dsDefMap = activityDef.getDataSourceDefinitionMap();
						boolean isVisible = false;
						if ("button".equals(table)) {
							for (DataSourceDefinition dsDef : dsDefMap.values()) {
								if (dsDef.getViewDefinition().getTableView().containsValue(field)) {
									isVisible = true;
									break;
								}
								if (dsDef.getViewDefinition().getFieldViewDefinition(field) != null) {
									isVisible = true;
									break;
								}
							}
						} else {
							if (dsDefMap.containsKey(table)) {
								isVisible = dsDefMap.get(table).getViewDefinition().getFieldViewDefinition(field) != null;
							}
						}
						functionBean = new FunctionBean(name, ipFunction, isVisible);
					} else {
						functionBean = new FunctionBean(name, ipFunction);
					}
					if (!functions.containsKey(functionBean)) {
						functions.put(functionBean, functionBean);
					}
				}
			}
		}
		return activityBean;
	}
	
	@Override
	public LoadActivityResponse loadActivityDef(ActivityDefinition activityDefinition, LoadActivityRequest loadActivityRequest) throws Throwable {
		LoadActivityResponse loadActivityResponse = getAppServerAdapter().loadActivityDef(activityDefinition, loadActivityRequest);
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesLoadActivityDef(activityBean.getTimesLoadActivityDef() + 1);
		}
		return loadActivityResponse;
	}

	@Override
	public GetResultsResponse getResults(ActivityDefinition activityDefinition, GetResultsRequest getResultsRequest) throws Throwable {
		GetResultsResponse getResultsResponse = getAppServerAdapter().getResults(activityDefinition, getResultsRequest);
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesGetResults(activityBean.getTimesGetResults() + 1);
		}
		return getResultsResponse;
	}

	@Override
	public LoadDataResponse loadData(ActivityDefinition activityDefinition, LoadDataRequest loadDataRequest) throws Throwable {
		LoadDataResponse loadDataResponse = getAppServerAdapter().loadData(activityDefinition, loadDataRequest);
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesLoadData(activityBean.getTimesLoadData() + 1);
		}
		return loadDataResponse;
	}

	@Override
	public SaveDataResponse saveData(ActivityDefinition activityDefinition, SaveDataRequest saveDataRequest) throws Throwable {
		SaveDataResponse saveDataResponse = getAppServerAdapter().saveData(activityDefinition, saveDataRequest);
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesSaveData(activityBean.getTimesSaveData() + 1);
		}
		return saveDataResponse;
	}

	@Override
	public PresentationMessage presenter(ActivityDefinition activityDefinition, String ipFunction, PresentationMessage ipRequest) throws Throwable {
		PresentationMessage oResponse = getAppServerAdapter().presenter(activityDefinition, ipFunction, ipRequest);
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesPresenter(activityBean.getTimesPresenter() + 1);
			writeIpFunction(activityDefinition, activityBean, ipFunction, ipRequest);
		}
		return oResponse;
	}

	@Override
	public ActivityTaskResponse submitActivityTask(ActivityDefinition activityDefinition, ActivityTaskRequest activityTaskRequest) throws Throwable {
		ActivityTaskResponse activityTaskResponse = getAppServerAdapter().submitActivityTask(activityDefinition, activityTaskRequest);
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesSubmitTask(activityBean.getTimesSubmitTask());
		}
		return activityTaskResponse;
	}

	@Override
	public boolean allowOrphanFields() {
		return getAppServerAdapter().allowOrphanFields();
	}
}
