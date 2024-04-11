package fluid.test.adapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import fluid.test.adapter.RestAdapter.MediaType;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.FieldDefinition;
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

/**
 * Provides methods that allow access to the server resources. These methods
 * retrieve the data using the RequestBuilder
 * XXXX
 * 
 * @author
 */
public abstract class AppServerAdapter {
	
	private static final ThreadLocal<AppServerAdapter> THREAD_ADAPTER = new ThreadLocal<AppServerAdapter>();
	
	public static AppServerAdapter getInstance() {
		return THREAD_ADAPTER.get();
	}
	public static void setInstance(AppServerAdapter customAdapter) {
		THREAD_ADAPTER.set(customAdapter);
	}
	
	public AppServerAdapter() {}
	
	private String user;
	private String password;
	private String domain;
	private String extra;
	
	private String dateFormat;
	private String contextId;
	private String host;
	
	public String getUser() {return user;}						public AppServerAdapter setUser(String user) {this.user = user; return this;}	
	public String getPassword() {return password;}				public AppServerAdapter setPassword(String password) {this.password = password; return this;}
    public String getExtra() {return extra;}					public AppServerAdapter setExtra(String extra) {this.extra = extra; return this;}
	public String getDomain() {return domain;}					public AppServerAdapter setDomain(String domain) {this.domain = domain; return this;}	
	
	public String getContextId() {return contextId;}			public AppServerAdapter setContextId(String contextId) {this.contextId = contextId; return this;}				
    public String getDateFormat() {return dateFormat;}			public AppServerAdapter setDateFormat(String dateFormat) {this.dateFormat = dateFormat; return this;}
    public String getHost() {return host;}						public AppServerAdapter setHost(String host) {this.host = host; return this;}
    
	public void fluidLogin() throws Throwable {
		if (getContextId() != null && !getContextId().isEmpty()) {
			logout();
		}
		login();
		userContext();
	}
    
	public LoadActivityResponse loadActivityDef(ActivityDefinition activityDefinition, LoadActivityRequest loadActivityRequest) throws Throwable {
		LoadActivityResponse loadActivityResponse = (LoadActivityResponse) sendAppServerApiRequest(AppServerApiEnum.loadActivityDef, activityDefinition, null, loadActivityRequest);
		loadActivityResponse.setCallingRequest(loadActivityRequest);
		activityDefinition.initializeModel(loadActivityResponse);
		return loadActivityResponse;
	}
	
	public GetResultsResponse getResults(ActivityDefinition activityDefinition, GetResultsRequest getResultsRequest) throws Throwable {
		GetResultsResponse getResultsResponse = (GetResultsResponse) sendAppServerApiRequest(AppServerApiEnum.getResults, activityDefinition, null, getResultsRequest);
		getResultsResponse.setCallingRequest(getResultsRequest);
		activityDefinition.processGetResultsResponse(getResultsResponse);
		return getResultsResponse;
	}
	
	public LoadDataResponse loadData(ActivityDefinition activityDefinition, LoadDataRequest loadDataRequest) throws Throwable {
		activityDefinition.setLastLoadDataRequest(loadDataRequest);
		LoadDataResponse loadDataResponse = (LoadDataResponse) sendAppServerApiRequest(AppServerApiEnum.loadData, activityDefinition, null, loadDataRequest);
		loadDataResponse.setCallingRequest(loadDataRequest);
		activityDefinition.processLoadDataResponse(loadDataResponse);
		return loadDataResponse;
	}
	
	public SaveDataResponse saveData(ActivityDefinition activityDefinition, SaveDataRequest saveDataRequest) throws Throwable {
		SaveDataResponse saveDataResponse = (SaveDataResponse) sendAppServerApiRequest(AppServerApiEnum.saveData, activityDefinition, null, saveDataRequest);
		saveDataResponse.setCallingRequest(saveDataRequest);
		activityDefinition.processSaveDataResponse(saveDataResponse);
		return saveDataResponse;
	}
	
	public PresentationMessage presenter(ActivityDefinition activityDefinition, String ipFunction, PresentationMessage ipRequest) throws Throwable {
		PresentationMessage oResponse = (PresentationMessage) sendAppServerApiRequest(AppServerApiEnum.presenter, activityDefinition, ipFunction, ipRequest);
		oResponse.setCallingField(ipRequest.getCallingField());
		oResponse.setCallingRequest(ipRequest);
		activityDefinition.processResponse(oResponse);
		return oResponse;
	}
	
	public ActivityTaskResponse submitActivityTask(ActivityDefinition activityDefinition, ActivityTaskRequest activityTaskRequest) throws Throwable {
		ActivityTaskResponse activityTaskResponse = (ActivityTaskResponse) sendAppServerApiRequest(AppServerApiEnum.submitActivityTask, activityDefinition, null, activityTaskRequest);
		activityTaskResponse.setCallingRequest(activityTaskRequest);
		activityDefinition.processLoadDataResponse(activityTaskResponse);
		return activityTaskResponse;
	}
	
	public Response sendAppServerApiRequest(AppServerApiEnum appServerApi, ActivityDefinition activityDefinition, String ipFunction, AbstractMessage request) throws Throwable {
		activityDefinition.setCommonParams(request);
		
		Response response;
		String url = buildUrlForApp(appServerApi, activityDefinition.getService(), ipFunction);
		RestAdapter.Response restResponse = RestAdapter.getInstance().post(url, request.serializeMessage(), headers(), MediaType.JSON);
		
		String responseBody = processResponse(restResponse);
		
		switch (appServerApi) {
			case getResults:
				response = new GetResultsResponse(responseBody);
				break;
			case loadActivityDef:
				response = new LoadActivityResponse(responseBody);
				break;
			case loadData:
				response = new LoadDataResponse(responseBody, ((LoadDataRequest) request).getLoadMode());
				break;
			case presenter:
				response = new PresentationMessage(responseBody);
				break;
			case saveData:
				response = new SaveDataResponse(responseBody);
				break;
			case submitActivityTask:
				response = new ActivityTaskResponse(responseBody);
				break;
			default:
				throw new IllegalArgumentException("Unhandled AppServerApi!");
		}
		
/*		System.out.println(appServerApi + " request: " + request.serializeMessage());
		System.out.println(appServerApi + " response: " + response.serializeMessage() + "\n");*/
		
		setResponseMetaData(restResponse, response);
		
		return response;
	}
	
	public Response fluidVersion() throws Throwable {
		return new Response(RestAdapter.getInstance().get(getHost() + "/resources/fluid-version.nocache.json", headers()));
	}
	public Response appConstants() throws Throwable {
		return new Response(RestAdapter.getInstance().post(MediaType.JSON,
				getHost() + "/fluid/FluidAppServerProxy?ipApi=appConstants", "", headers()));
	}
	
	public void userContext() throws Throwable{
		LoadDataRequest loadDataRequest = userContextRequest();
		
		Response details = new Response(RestAdapter.getInstance().get(getHost() + "/fluid/FluidAppServerProxy/appConstants/details.nocache.json", headers()));
		String menuService = details.getParamAsString("menuService");
		
		String url = buildUrlForApp(AppServerApiEnum.loadData, menuService, null);
		String response = RestAdapter.getInstance().post(MediaType.JSON, url, loadDataRequest.serializeMessage(), headers());
		LoadDataResponse loadDataResponse = new LoadDataResponse(response, loadDataRequest.getLoadMode());
		
		if (loadDataResponse.hasStopErrors()) {
	         Throwable e = new Throwable("Error in UserContext response: " +loadDataResponse.getErrorMsg());
	         e.fillInStackTrace();
	         throw e;
		}
		
		DataSourceDefinition ttUserContext = new DataSourceDefinition();
		ttUserContext.setTableName("ttUserContext");
		ttUserContext.setDataSetName("dsUserContext");
		
		ttUserContext.addFieldDefinition(new FieldDefinition("sysMenuLabel", "text"));
		ttUserContext.addFieldDefinition(new FieldDefinition("sessionInfo", "text"));
		ttUserContext.addFieldDefinition(new FieldDefinition("dateFormat", "text"));
		ttUserContext.addFieldDefinition(new FieldDefinition("fldId", "integer", true));
		
		loadDataResponse.populateRecordList(ttUserContext);
		
		StringBuilder dateFormat = new StringBuilder();
		char[] dateCode = ttUserContext.getCurrentRecord().getFieldValueAsString("dateFormat").toCharArray();
		char seperator = '/';

		for (char span : dateCode) {
			if (!dateFormat.toString().isEmpty())
				dateFormat.append(seperator);

			switch (span) {
				case ('d'):
					dateFormat.append("dd");
					break;
				case ('m'):
					dateFormat.append("MM");
					break;
				case ('y'):
					dateFormat.append("yyyy");
					break;
			}
		}
		
		setDateFormat(dateFormat.toString());
	}
	
	static String stringToSHA1(String toShaify) {
		final String sha1;
		try {
			MessageDigest msgDigest = MessageDigest.getInstance("SHA-1");
			msgDigest.update(toShaify.getBytes(StandardCharsets.UTF_8), 0, toShaify.length());
			sha1 = bytesToHex(msgDigest.digest());
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalArgumentException("Unknown algorithm supplied.");
		}
		
		return sha1.toLowerCase();
	}
	
	
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
	/**
	 * <p>Javax DatatypeConverter is removed in future java versions, so as an alternative.</p>
	 * @param bytes An array of bytes
	 * @return A string containing a lexical representation of xsd:hexBinary
	 */
	private static String bytesToHex(byte[] bytes) {
	    byte[] hexChars = new byte[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars, StandardCharsets.UTF_8);
	}
	
	public static void setResponseMetaData(RestAdapter.Response restResponse, Response fluidResponse) {
		fluidResponse.setSentTime(restResponse.getSentRequestAtMillis());
		fluidResponse.setReceivedTime(restResponse.getReceivedResponseAtMillis());
	}
	
	public String processResponse(RestAdapter.Response response) throws IOException {
		final int code = response.getCode();
		switch ((int) code / 100) {
			case 1: // Informational
			case 2: // Success
			case 3: // Redirection
				// Do nothing;
				break;
			case 4: // Client Error
				throw new IllegalArgumentException("Client error HTTP status code "+code+".\r\n\""+response+"\"");
			case 5: // Server Error
				throw new RuntimeException("Server error HTTP status code "+code+".\r\n\""+response+"\"");
			default:
				// Do nothing and be scared
				break;
		}
		return response.getResponseBody();
	}
	
	public abstract boolean allowOrphanFields();
	
	public abstract AppServerAdapter copyInstance();
	public abstract AppServerAdapter newInstance();
	public abstract String buildUrlForApp(AppServerApiEnum ipApi, String ipService, String ipFunction);
	public abstract Map<String, String> headers();
	public abstract void login() throws Throwable;
	public abstract void logout() throws Throwable;
	public abstract LoadDataRequest userContextRequest()throws Throwable;
}
