package auto.utils;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import fluid.test.adapter.AppServerAdapter;
import fluid.test.adapter.AppServerApiEnum;
import fluid.test.adapter.RestAdapter;
import fluid.test.adapter.RestAdapter.MediaType;
import fluid.test.data.ActivityDefinition;
import fluid.test.message.ActivityTaskRequest;
import fluid.test.message.ActivityTaskResponse;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadActivityResponse;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;
import fluid.test.message.SaveDataRequest;
import fluid.test.message.SaveDataResponse;

public class RetryAdapter extends AppServerAdapterSock{
		
	private Integer retryLimit = 10;
	private Map<String, Integer> retries = new HashMap<String, Integer>();
	private boolean retryRecordLock = false;
	private boolean countRetries = false;
	
	public RetryAdapter(AppServerAdapter appServerAdapter) {
		super(appServerAdapter);
	}
	
	public boolean isRetryRecordLock() {
		return this.retryRecordLock;
	}
	public RetryAdapter setRetryRecordLock(boolean retryRecordLock) {
		this.retryRecordLock = retryRecordLock;
		return this;
	}
	
	public boolean isCountRetries() {
		return countRetries;
	}

	public RetryAdapter setCountRetries(boolean countRetries) {
		this.countRetries = countRetries;
		return this;
	}

	public Map<String, Integer> getRetries(){
		return this.retries;
	}
	
	public Integer getRetryLimit() {
		return this.retryLimit;
	}

	public RetryAdapter setRetryLimit(Integer retryLimit) {
		this.retryLimit = retryLimit;
		return this;
	}

	public boolean retryRecordLock(String service, PresentationMessage oResponse) {
		boolean retry;
		if (retry = oResponse.getErrorMsg().contains("Another user is currently making changes to this data so your changes can not be saved. Please try again.")) {
			if (this.countRetries) {
				StringBuilder strbldr = new StringBuilder();
				strbldr.append(service+":");
				strbldr.append(oResponse.getClass().getSimpleName()+":");
				strbldr.append(oResponse.getCallingField());
				String key = strbldr.toString();
				if (getRetries().containsKey(key)) {
					Integer currentRetries = getRetries().get(key);
					if (getRetryLimit().equals(currentRetries)) {
						getRetries().clear();
						//Exit
						fail(key + "-Record locked, retried " + getRetryLimit() + " times (The retry limit), exiting test now.");
					}
					getRetries().put(key,  + 1);
				} else {
					getRetries().put(key, 1);
				}
			}
		}
		return retry;
	}
	
	@Override
	public LoadActivityResponse loadActivityDef(ActivityDefinition activityDefinition, LoadActivityRequest loadActivityRequest) throws Throwable {
		activityDefinition.setCommonParams(loadActivityRequest);
		
		String url = buildUrlForApp(AppServerApiEnum.loadActivityDef, activityDefinition.getService(), null);
		String response = processResponse(RestAdapter.getInstance().post(url, loadActivityRequest.serializeMessage(), headers(), MediaType.JSON));
		LoadActivityResponse loadActivityResponse = new LoadActivityResponse(response);
		
		if (isRetryRecordLock() && retryRecordLock(activityDefinition.getService(), loadActivityResponse)) {
			loadActivityResponse = loadActivityDef(activityDefinition, loadActivityRequest);
		} else {
			activityDefinition.initializeModel(loadActivityResponse);
		}
		
		return loadActivityResponse;
	}
	
	@Override
	public GetResultsResponse getResults(ActivityDefinition activityDefinition, GetResultsRequest getResultsRequest) throws Throwable {
		activityDefinition.setCommonParams(getResultsRequest);
		
		String url = buildUrlForApp(AppServerApiEnum.getResults, activityDefinition.getService(), null);
		String response = processResponse(RestAdapter.getInstance().post(url, getResultsRequest.serializeMessage(), headers(), MediaType.JSON));
		GetResultsResponse getResultsResponse = new GetResultsResponse(response);
		
		if (isRetryRecordLock() && retryRecordLock(activityDefinition.getService(), getResultsResponse)) {
			getResultsResponse = getResults(activityDefinition, getResultsRequest);
		} else {
			activityDefinition.processGetResultsResponse(getResultsResponse);
		}
		
		return getResultsResponse;
	}
	
	@Override
	public LoadDataResponse loadData(ActivityDefinition activityDefinition, LoadDataRequest loadDataRequest) throws Throwable {
		activityDefinition.setCommonParams(loadDataRequest);
		activityDefinition.setLastLoadDataRequest(loadDataRequest);
		
		String url = buildUrlForApp(AppServerApiEnum.loadData, activityDefinition.getService(), null);
		String response = processResponse(RestAdapter.getInstance().post(url, loadDataRequest.serializeMessage(), headers(), MediaType.JSON));
		LoadDataResponse loadDataResponse = new LoadDataResponse(response, loadDataRequest.getLoadMode());
		
		if (isRetryRecordLock() && retryRecordLock(activityDefinition.getService(), loadDataResponse)) {
			loadDataResponse = loadData(activityDefinition, loadDataRequest);
		} else {
			activityDefinition.processLoadDataResponse(loadDataResponse);
		}
		
		return loadDataResponse;
	}
	
	@Override
	public SaveDataResponse saveData(ActivityDefinition activityDefinition, SaveDataRequest saveDataRequest) throws Throwable {
		activityDefinition.setCommonParams(saveDataRequest);

		String url = buildUrlForApp(AppServerApiEnum.saveData, activityDefinition.getService(), null);
		String response = processResponse(RestAdapter.getInstance().post(url, saveDataRequest.serializeMessage(), headers(), MediaType.JSON));
		SaveDataResponse saveDataResponse = new SaveDataResponse(response);
		
		if (isRetryRecordLock() && retryRecordLock(activityDefinition.getService(), saveDataResponse)) {
			saveDataResponse = saveData(activityDefinition, saveDataRequest);
		} else {
			activityDefinition.processSaveDataResponse(saveDataResponse);
		}
		
		return saveDataResponse;
	}
	
	@Override
	public PresentationMessage presenter(ActivityDefinition activityDefinition, String ipFunction, PresentationMessage ipRequest) throws Throwable {
		activityDefinition.setCommonParams(ipRequest);
		
		String url = buildUrlForApp(AppServerApiEnum.presenter, activityDefinition.getService(), ipFunction);
		String response = processResponse(RestAdapter.getInstance().post(url, ipRequest.serializeMessage(), headers(), MediaType.JSON));
		PresentationMessage oResponse = new PresentationMessage(response);
		
		oResponse.setCallingField(ipRequest.getCallingField());
		
		if (isRetryRecordLock() && retryRecordLock(activityDefinition.getService(), oResponse)) { 
			oResponse = presenter(activityDefinition, ipFunction, ipRequest);
		} else {
			activityDefinition.processResponse(oResponse);
		}

		return oResponse;
	}
	
	@Override
	public ActivityTaskResponse submitActivityTask(ActivityDefinition activityDefinition, ActivityTaskRequest activityTaskRequest) throws Throwable {
		activityDefinition.setCommonParams(activityTaskRequest);
		
		String url = buildUrlForApp(AppServerApiEnum.submitActivityTask, activityDefinition.getService(), null);
		String response = processResponse(RestAdapter.getInstance().post(url, activityTaskRequest.serializeMessage(), headers(), MediaType.JSON));
		ActivityTaskResponse activityTaskResponse = new ActivityTaskResponse(response);
		
		if (isRetryRecordLock() && retryRecordLock(activityDefinition.getService(), activityTaskResponse)) { 
			activityTaskResponse = submitActivityTask(activityDefinition, activityTaskRequest);
		} else {
			activityDefinition.processLoadDataResponse(activityTaskResponse);
		}
		
		return activityTaskResponse;
	}
	
}