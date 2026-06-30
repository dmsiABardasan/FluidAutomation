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
	/** When true, {@link #loadActivityDef} and {@link #loadData} capture their
	 *  request/response as a single-entry HAR file via {@link HarRecorder}.
	 *  Scoped intentionally to those two calls only: they're enough to render
	 *  an activity's initial screen with its initial data in fluid-ui-test-kit,
	 *  which is the visual-comparison surface we need for the 12.1p → 14.1p
	 *  upgrade.  Other delegated methods (getResults, saveData, presenter,
	 *  submitActivityTask) deliberately don't emit HAR — see HarRecorder
	 *  javadoc for the scope rationale. */
	public boolean writeHar = false;
	final private Map<ActivityBean, Map<FunctionBean, FunctionBean>> activityFunctions = new HashMap<>();
	final private Map<ActivityBean, ActivityBean> activities = new HashMap<>();
	final private HarRecorder harRecorder = new HarRecorder(new File(FLUID_ACTIVITY_OUTPUT));
	/** Per-JUnit-test HAR capture (all 6 method types).  Idle by default;
	 *  activated by {@link HarTestRecordingExtension} bracketing each test. */
	final private HarTestRecorder harTestRecorder = new HarTestRecorder(new File(FLUID_ACTIVITY_OUTPUT + "tests/"));

	public static final String FLUID_ACTIVITY_OUTPUT = System.getProperty("user.dir") + "/activityOutput/automation/";

	public AppServerAdapterRecorder(AppServerAdapter appServerAdapter){
		super(appServerAdapter);
	}

	public HarRecorder getHarRecorder() {
		return this.harRecorder;
	}

	public HarTestRecorder getHarTestRecorder() {
		return this.harTestRecorder;
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

		// HAR files are written immediately by HarRecorder.record() — there's
		// no flushing step here.  Just emit the run summary if anything fired
		// (or was deliberately skipped because the file already existed).
		if (writeHar) {
			long w = harRecorder.writtenCount();
			long s = harRecorder.skippedCount();
			if (w > 0 || s > 0) {
				System.out.println("[AppServerAdapterRecorder] HAR (per-activity): "
					+ w + " written, " + s + " skipped (existing file)");
			}
		}
		// Per-test HAR is independently activated by HarTestRecordingExtension.
		// Report what it captured regardless of the writeHar flag.
		long tw = harTestRecorder.filesWrittenCount();
		long ts = harTestRecorder.testsSeenCount();
		if (tw > 0 || ts > 0) {
			System.out.println("[AppServerAdapterRecorder] HAR (per-test): "
				+ tw + " file(s) written across " + ts + " test(s)");
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
	
	/**
	 * Returns the activity-name key for HAR bucketing.  Prefers the
	 * activity tag from the loaded model (set after loadActivityDef returns).
	 * Falls back to the request's activity field if the model isn't ready
	 * yet — covers the loadActivityDef call itself, where the model is
	 * what we're about to fetch.
	 */
	private String activityNameFor(ActivityDefinition activityDef, String requestActivity) {
		if (activityDef != null && activityDef.getModel() != null) {
			String name = activityDef.getModel().getActivity();
			if (name != null && !name.isEmpty()) return name;
		}
		return requestActivity != null ? requestActivity : "";
	}

	@Override
	public LoadActivityResponse loadActivityDef(ActivityDefinition activityDefinition, LoadActivityRequest loadActivityRequest) throws Throwable {
		long t0 = System.nanoTime();
		LoadActivityResponse loadActivityResponse = getAppServerAdapter().loadActivityDef(activityDefinition, loadActivityRequest);
		long elapsed = System.nanoTime() - t0;
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesLoadActivityDef(activityBean.getTimesLoadActivityDef() + 1);
		}
		// loadActivityDef is the bootstrap call — activityDef.getModel() may
		// still be null when we enter this method, so prefer the response's
		// activity name (which IS the model we just fetched).
		String activityName = (loadActivityResponse != null && loadActivityResponse.getActivity() != null)
			? loadActivityResponse.getActivity()
			: (loadActivityRequest != null ? loadActivityRequest.getActivity() : null);
		if (writeHar) {
			harRecorder.record(activityDefinition.getService(), activityName,
				HarRecorder.ApiKind.STATIC, "loadActivityDef",
				loadActivityRequest, loadActivityResponse, elapsed);
		}
		harTestRecorder.record(activityDefinition.getService(), activityName,
			HarRecorder.ApiKind.STATIC, "loadActivityDef",
			loadActivityRequest, loadActivityResponse, elapsed);
		return loadActivityResponse;
	}

	@Override
	public GetResultsResponse getResults(ActivityDefinition activityDefinition, GetResultsRequest getResultsRequest) throws Throwable {
		long t0 = System.nanoTime();
		GetResultsResponse getResultsResponse = getAppServerAdapter().getResults(activityDefinition, getResultsRequest);
		long elapsed = System.nanoTime() - t0;
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesGetResults(activityBean.getTimesGetResults() + 1);
		}
		// Per-activity HarRecorder skips this call type (out of scope for the
		// 12.1p → 14.1p visual comparison).  Per-test HarTestRecorder captures
		// it if a test is active — it's the full conversation that matters
		// for HAR-driven test playback.
		harTestRecorder.record(activityDefinition.getService(), activityNameFor(activityDefinition, null),
			HarRecorder.ApiKind.STATIC, "getResults",
			getResultsRequest, getResultsResponse, elapsed);
		return getResultsResponse;
	}

	@Override
	public LoadDataResponse loadData(ActivityDefinition activityDefinition, LoadDataRequest loadDataRequest) throws Throwable {
		long t0 = System.nanoTime();
		LoadDataResponse loadDataResponse = getAppServerAdapter().loadData(activityDefinition, loadDataRequest);
		long elapsed = System.nanoTime() - t0;
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesLoadData(activityBean.getTimesLoadData() + 1);
		}
		if (writeHar) {
			harRecorder.record(activityDefinition.getService(), activityNameFor(activityDefinition, null),
				HarRecorder.ApiKind.STATIC, "loadData",
				loadDataRequest, loadDataResponse, elapsed);
		}
		harTestRecorder.record(activityDefinition.getService(), activityNameFor(activityDefinition, null),
			HarRecorder.ApiKind.STATIC, "loadData",
			loadDataRequest, loadDataResponse, elapsed);
		return loadDataResponse;
	}

	@Override
	public SaveDataResponse saveData(ActivityDefinition activityDefinition, SaveDataRequest saveDataRequest) throws Throwable {
		long t0 = System.nanoTime();
		SaveDataResponse saveDataResponse = getAppServerAdapter().saveData(activityDefinition, saveDataRequest);
		long elapsed = System.nanoTime() - t0;
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesSaveData(activityBean.getTimesSaveData() + 1);
		}
		// Per-test only — saveData mutates server state so per-activity
		// dedup'd capture would be misleading.
		harTestRecorder.record(activityDefinition.getService(), activityNameFor(activityDefinition, null),
			HarRecorder.ApiKind.STATIC, "saveData",
			saveDataRequest, saveDataResponse, elapsed);
		return saveDataResponse;
	}

	@Override
	public PresentationMessage presenter(ActivityDefinition activityDefinition, String ipFunction, PresentationMessage ipRequest) throws Throwable {
		long t0 = System.nanoTime();
		PresentationMessage oResponse = getAppServerAdapter().presenter(activityDefinition, ipFunction, ipRequest);
		long elapsed = System.nanoTime() - t0;
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesPresenter(activityBean.getTimesPresenter() + 1);
			writeIpFunction(activityDefinition, activityBean, ipFunction, ipRequest);
		}
		// Per-test only — presenter calls are user interactions, not screen
		// state, so per-activity dedup isn't meaningful.  But each test's
		// full conversation includes these and they're needed for playback.
		harTestRecorder.record(activityDefinition.getService(), activityNameFor(activityDefinition, null),
			HarRecorder.ApiKind.PRESENTER, ipFunction,
			ipRequest, oResponse, elapsed);
		return oResponse;
	}

	@Override
	public ActivityTaskResponse submitActivityTask(ActivityDefinition activityDefinition, ActivityTaskRequest activityTaskRequest) throws Throwable {
		long t0 = System.nanoTime();
		ActivityTaskResponse activityTaskResponse = getAppServerAdapter().submitActivityTask(activityDefinition, activityTaskRequest);
		long elapsed = System.nanoTime() - t0;
		if (writeServicesAndFunctions) {
			ActivityBean activityBean = writeActivity(activityDefinition);
			activityBean.setTimesSubmitTask(activityBean.getTimesSubmitTask());
		}
		// Per-test only — same rationale as saveData.
		harTestRecorder.record(activityDefinition.getService(), activityNameFor(activityDefinition, null),
			HarRecorder.ApiKind.STATIC, "submitActivityTask",
			activityTaskRequest, activityTaskResponse, elapsed);
		return activityTaskResponse;
	}

	@Override
	public boolean allowOrphanFields() {
		return getAppServerAdapter().allowOrphanFields();
	}
}
