package auto.utils.fluid;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import auto.utils.ActivityDefinitionUtils;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.ChildActivityDefinition;
import fluid.test.data.ChildActivityDefinition.LaunchContractType;
import fluid.test.data.Record;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.PresentationMessage;

public abstract class Dashboard extends FluidActivity {
	
	public Dashboard() {
		super();
	}
	
	public Dashboard(ActivityDefinition activityDef) {
		super(activityDef);
	}
	
	public Set<String> explodeDashboardServices(){
		return explodeDashboardServices(getActivityDefinition());
	}

	public Set<ChildActivityDefinition> explodeDashboardBySearch(Object searchValue){
		return explodeDashboardBySearch(getActivityDefinition(), searchValue);
	}
	
	public Set<ChildActivityDefinition> explodeDashboardBySearch(String searchField, Object searchValue){
		return explodeDashboardBySearch(getActivityDefinition(), searchField, searchValue);
	}
	
	public static Set<ChildActivityDefinition> explodeDashboardByRecord(ActivityDefinition activityDef, Record record){
		String primaryKey = activityDef.getResultsDataSourceDefinition().getPrimaryKey();
		String primaryKeyValue = activityDef.getResultsDataSourceDefinition().getCurrentRecord().getFieldValueAsString(primaryKey);
		
		return explodeDashboard(activityDef, primaryKeyValue);
	}
	
	@SuppressWarnings("unchecked")
	public static Set<String> explodeDashboardServices(ActivityDefinition activityDef){
		
		if (ActivityTypeEnum.DASHBOARD != ActivityTypeEnum.getActivityType(activityDef.getModel().getActivityType())) {
			throw new IllegalArgumentException("The passed ActivityDefinition was not of Type DASHBOARD");
		}
		
		Set<String> services = new LinkedHashSet<String>();
		Map<String, Object> viewDetails = activityDef.getModel().getViewDetails(); 
		List<Object> dashboardLayout = (List<Object>) viewDetails.get("dashboardLayout");
		for (Object layoutObj: dashboardLayout) {
			Map<String, Object> layout = (Map<String, Object>) layoutObj;
			List<Object> sections = (List<Object>) layout.get("sections");
			for (Object sectionObj: sections) {
				Map<String, Object> section = (Map<String, Object>) sectionObj;
				services.add((String) section.get("service"));
			}
		}
		
		return services;
	}
	
	public static Set<ChildActivityDefinition> explodeDashboard(ActivityDefinition activityDef, String maintRowid){
		Set<String> services = explodeDashboardServices(activityDef);
		
		final boolean substitute = activityDef.getResultsDataSourceDefinition() == null;
		
		Set<ChildActivityDefinition> activityList = new LinkedHashSet<ChildActivityDefinition>();
		for (String service: services) {
			ChildActivityDefinition childActivityDefinition = ChildActivityDefinition.newInstance(activityDef); 
			childActivityDefinition.setType(LaunchContractType.LOADDATA);
			
			LoadActivityRequest loadActivityRequest = new LoadActivityRequest();
			LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.VIEW);
			
			if (activityDef.getInjectedParams() != null) {
				PresentationMessage injectedParams = activityDef.getInjectedParams();
				childActivityDefinition.setInjectedParams(injectedParams);
				loadActivityRequest.getMoMsgParams().putAll(injectedParams.getMoMsgParams());
				loadDataRequest.getMoMsgParams().putAll(injectedParams.getMoMsgParams());
			}
			
			if (substitute) {
				loadActivityRequest.setParam("activity", activityDef.getModel().getActivity());
				loadActivityRequest.setParam("activityType", activityDef.getModel().getActivityType());
				loadDataRequest.setParam("activity", activityDef.getModel().getActivity());
				loadDataRequest.setParam("activityType", activityDef.getModel().getActivityType());
			}
			
			loadActivityRequest.setCtrlProp("dummy", CtrlPropEnum.LOADDATA, maintRowid);
			loadActivityRequest.setParam("parentActivity", activityDef.getModel().getActivity());
			loadDataRequest.setParam("parentActivity", activityDef.getModel().getActivity());
			
			childActivityDefinition.resolveLaunchActivity(loadActivityRequest, ",," + service);
			
			childActivityDefinition.setLastLoadActivityRequest(loadActivityRequest);
			childActivityDefinition.setLastLoadDataRequest(loadDataRequest);
			
			ActivityDefinitionUtils.loadActivity(childActivityDefinition, loadActivityRequest, service);
			ActivityDefinitionUtils.loadData(childActivityDefinition, loadDataRequest, maintRowid);
			
			activityList.add(childActivityDefinition);
		}
		
		return activityList;
	}
	
	/**
	 * Functionally the same as
	 * {@link #explodeDashboard(ActivityDefinition, String, Object)} but assumes
	 * that the searchField is the getResultsData primaryKey
	 * 
	 * @param activityDef
	 *            ActivityDefinition of the dashboard activityType
	 * @param searchValue 	The value that is equal to the value in the record
	 * @return List of ActivityDefinition dashboardSections
	 */
	public static Set<ChildActivityDefinition> explodeDashboardBySearch(ActivityDefinition activityDef, Object searchValue){
		return explodeDashboardBySearch(activityDef, activityDef.getResultsDataSourceDefinition().getPrimaryKey(), searchValue);
	}
	

	/**
	 * For an ActivityDefinition with activityType "Dashboard", will attempt to load
	 * the passed search values (given if they exist and are unique) and then
	 * initialises each "dashboardSection" as an ActivityDefinition and returns them
	 * as a Set or null if record could not be found.
	 * 
	 * @param activityDef
	 *            ActivityDefinition of the dashboard activityType
	 * @param searchField
	 *            The field to search in the record
	 * @param searchValue
	 *            The value that is equal to the value in the record
	 * @return Set of ActivityDefinition or null if searchField and searchValue cannot be resolved 
	 */
	public static Set<ChildActivityDefinition> explodeDashboardBySearch(ActivityDefinition activityDef, String searchField, Object searchValue){
		
		Set<ChildActivityDefinition> activitySet = null;
		Record selectedRecord = ActivityDefinitionUtils.selectResults(activityDef, searchField, searchValue);
		
		if (selectedRecord != null) {
			String maintRowid = selectedRecord.getFieldValueAsString(activityDef.getResultsDataSourceDefinition().getPrimaryKey());
			activitySet = explodeDashboard(activityDef, maintRowid);
		}
		
		return activitySet;
	}
}
