package auto.utils;

import java.io.IOException;
import java.util.Map;

import fluid.test.adapter.AppServerAdapter;
import fluid.test.adapter.AppServerApiEnum;
import fluid.test.adapter.RestAdapter;
import fluid.test.data.ActivityDefinition;
import fluid.test.message.AbstractMessage;
import fluid.test.message.ActivityTaskRequest;
import fluid.test.message.ActivityTaskResponse;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadActivityResponse;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;
import fluid.test.message.Response;
import fluid.test.message.SaveDataRequest;
import fluid.test.message.SaveDataResponse;

public abstract class AppServerAdapterSock extends AppServerAdapter {

	private AppServerAdapter appServerAdapter;
	
	public AppServerAdapterSock(AppServerAdapter appServerAdapter){
		this.appServerAdapter = appServerAdapter;
	}
	
	public void setAppServerAdapter(AppServerAdapter appServerAdapter) {
		this.appServerAdapter = appServerAdapter;
	}
	
	public AppServerAdapter getAppServerAdapter() {
		return this.appServerAdapter;
	}

	@Override
	public LoadActivityResponse loadActivityDef(ActivityDefinition activityDefinition, LoadActivityRequest loadActivityRequest) throws Throwable {
		return getAppServerAdapter().loadActivityDef(activityDefinition, loadActivityRequest);
	}

	@Override
	public GetResultsResponse getResults(ActivityDefinition activityDefinition, GetResultsRequest getResultsRequest) throws Throwable {
		return getAppServerAdapter().getResults(activityDefinition, getResultsRequest);
	}

	@Override
	public LoadDataResponse loadData(ActivityDefinition activityDefinition, LoadDataRequest loadDataRequest) throws Throwable {
		return getAppServerAdapter().loadData(activityDefinition, loadDataRequest);
	}

	@Override
	public SaveDataResponse saveData(ActivityDefinition activityDefinition, SaveDataRequest saveDataRequest) throws Throwable {
		return getAppServerAdapter().saveData(activityDefinition, saveDataRequest);
	}

	@Override
	public PresentationMessage presenter(ActivityDefinition activityDefinition, String ipFunction, PresentationMessage ipRequest) throws Throwable {
		return getAppServerAdapter().presenter(activityDefinition, ipFunction, ipRequest);
	}

	@Override
	public ActivityTaskResponse submitActivityTask(ActivityDefinition activityDefinition, ActivityTaskRequest activityTaskRequest) throws Throwable {
		return getAppServerAdapter().submitActivityTask(activityDefinition, activityTaskRequest);
	}

	@Override
	public String buildUrlForApp(AppServerApiEnum ipApi, String ipService, String ipFunction) {
		return getAppServerAdapter().buildUrlForApp(ipApi, ipService, ipFunction);
	}
	@Override
	public void login() throws Throwable {
		getAppServerAdapter().login();
	}
	@Override
	public void logout() throws Throwable {
		getAppServerAdapter().logout();
	}
	@Override
	public Map<String, String> headers() {
		return getAppServerAdapter().headers();
	}
	@Override
	public LoadDataRequest userContextRequest() throws Throwable {
		return getAppServerAdapter().userContextRequest();
	}
	@Override
	public AppServerAdapter copyInstance() {
		return getAppServerAdapter().copyInstance();
	}
	@Override
	public AppServerAdapter newInstance() {
		return getAppServerAdapter().newInstance();
	}
	@Override
	public String getUser() {
		return getAppServerAdapter().getUser();
	}
	@Override
	public AppServerAdapter setUser(String user) {
		return getAppServerAdapter().setUser(user);
	}
	@Override
	public String getPassword() {
		return getAppServerAdapter().getPassword();
	}
	@Override
	public AppServerAdapter setPassword(String password) {
		return getAppServerAdapter().setPassword(password);
	}
	@Override
	public String getExtra() {
		return getAppServerAdapter().getExtra();
	}
	@Override
	public AppServerAdapter setExtra(String extra) {
		return getAppServerAdapter().setExtra(extra);
	}
	@Override
	public String getDomain() {
		return getAppServerAdapter().getDomain();
	}
	@Override
	public AppServerAdapter setDomain(String domain) {
		return getAppServerAdapter().setDomain(domain);
	}
	@Override
	public String getContextId() {
		return getAppServerAdapter().getContextId();
	}
	@Override
	public AppServerAdapter setContextId(String contextId) {
		return getAppServerAdapter().setContextId(contextId);
	}
	@Override
	public String getDateFormat() {
		return getAppServerAdapter().getDateFormat();
	}
	@Override
	public AppServerAdapter setDateFormat(String dateFormat) {
		return getAppServerAdapter().setDateFormat(dateFormat);
	}
	@Override
	public String getHost() {
		return getAppServerAdapter().getHost();
	}
	@Override
	public AppServerAdapter setHost(String host) {
		return getAppServerAdapter().setHost(host);
	}
	@Override
	public void fluidLogin() throws Throwable {
		getAppServerAdapter().fluidLogin();
	}
	@Override
	public Response fluidVersion() throws Throwable {
		return getAppServerAdapter().fluidVersion();
	}
	@Override
	public Response appConstants() throws Throwable {
		return getAppServerAdapter().appConstants();
	}
	@Override
	public void userContext() throws Throwable {
		getAppServerAdapter().userContext();
	}
	@Override
	public String processResponse(RestAdapter.Response response) throws IOException  {
		return getAppServerAdapter().processResponse(response);
	}
	@Override
	public boolean allowOrphanFields() {
		return getAppServerAdapter().allowOrphanFields();
	}
	@Override
	public Response sendAppServerApiRequest(AppServerApiEnum appServerApi, ActivityDefinition activityDefinition,String ipFunction, AbstractMessage request) throws Throwable {
		return super.sendAppServerApiRequest(appServerApi, activityDefinition, ipFunction, request);
	}
}
