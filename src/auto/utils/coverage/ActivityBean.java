package auto.utils.coverage;

public class ActivityBean extends Bean{
	private static final long serialVersionUID = 1L;
	private int timesSaveData;
	private int timesLoadData;
	private int timesGetResults;
	private int timesPresenter;
	private int timesLoadActivityDef;
	private int timesSubmitTask;
	
	private String activity = "";
	private String activityDesc = "";
	private String activityService = "";
	private String activityType = "";
	private String keyType = "";
	
	public ActivityBean(String activity, String activityDesc, String activityService, String activityType, String keyType) {
		this.activity = activity;
		this.activityDesc = activityDesc;
		this.activityService = activityService;
		this.activityType = activityType;
		this.keyType = keyType;
	}
	
	@Override
	public String toString() {
		return this.activity.toLowerCase() + "\t" + this.activityService + /*"\t" + this.keyType +*/ "\t"
				+ this.activityType/* + "\t" + this.activityDesc*//* + "\t" + timesLoaded + "\t" + timesGetResults + "\t" + timesPresenter + "\t"+ timesSaved*/;
	}
	
	public int getTimesSaveData() {
		return this.timesSaveData;
	}
	public int getTimesLoadData() {
		return this.timesLoadData;
	}
	public int getTimesGetResults() {
		return this.timesGetResults;
	}
	public int getTimesPresenter() {
		return this.timesPresenter;
	}
	public int getTimesLoadActivityDef() {
		return this.timesLoadActivityDef;
	}
	public int getTimesSubmitTask() {
		return this.timesSubmitTask;
	}
	public String getActivity() {
		return this.activity;
	}
	public String getActivityDesc() {
		return this.activityDesc;
	}
	public String getActivityService() {
		return this.activityService;
	}
	public String getActivityType() {
		return this.activityType;
	}
	public String getKeyType() {
		return this.keyType;
	}
	public void setTimesSaveData(int timesSaved) {
		this.timesSaveData = timesSaved;
	}
	public void setTimesLoadData(int timesLoaded) {
		this.timesLoadData = timesLoaded;
	}
	public void setTimesGetResults(int timesGetResults) {
		this.timesGetResults = timesGetResults;
	}
	public void setTimesPresenter(int timesPresenter) {
		this.timesPresenter = timesPresenter;
	}
	public void setTimesLoadActivityDef(int timesPresenter) {
		this.timesLoadActivityDef = timesPresenter;
	}
	public void setTimesSubmitTask(int timesSubmitTask) {
		this.timesSubmitTask = timesSubmitTask;
	}
}
