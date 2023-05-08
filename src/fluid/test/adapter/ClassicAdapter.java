package fluid.test.adapter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import fluid.test.adapter.RestAdapter.MediaType;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.Request;
import fluid.test.message.Response;

/**
 * Classic Fluid authentication.
 * @author vercin
 */
public class ClassicAdapter extends AppServerAdapter{
	protected static final String FLUID_APP_SERVER_PROXY = "/fluid/FluidAppServerProxy?ipApi=";
	
	@Override
	public String buildUrlForApp(AppServerApiEnum ipApi, String ipService, String ipFunction) {
		String foo = getHost() + FLUID_APP_SERVER_PROXY + ipApi.toString() + "&ipService=" + ipService
				+ (ipFunction != null && !ipFunction.isEmpty() ? "&ipFunction=" + ipFunction : "");
		//System.out.println(foo);
		return  foo;
	}
	@Override
	public Map<String, String> headers() {
		return Collections.singletonMap("X-FLUID-CONTEXT", getContextId());
	}
	@Override
	public void login() throws Throwable {
		Response response = loginPost(RestAdapter.getInstance(), getHost(), getUser(), getPassword(), getDomain(), getExtra());
		
		String sessionID = response.getParamAsString("sessionID");
		if (sessionID != null && !sessionID.isEmpty()) {
			setContextId(sessionID);
		} else {
			String msg = response.getParamAsString("msg");
			Throwable e = new Throwable(msg);
			e.fillInStackTrace();
			throw e;
		}
	}
	
	private static synchronized Response loginPost(RestAdapter rest, String host, String user, String password, String domain, String extra) throws Throwable {
		Request requestBody = new Request();
		requestBody.setParam("userid",	 user);
		requestBody.setParam("password", password);
		requestBody.setParam("domain",	 domain);
		requestBody.setParam("extra",	 extra);
		
		String url = host + FLUID_APP_SERVER_PROXY + AppServerApiEnum.userLogin.toString();
		String res = rest.post(MediaType.JSON, url, requestBody.serializeMessage());
		Response response = new Response(res);
		
		return response;
	}
	
	@Override
	public void logout() throws Throwable {
		
		Request requestBody = new Request();
		requestBody.setParam("userid", "LOGOUT");
		requestBody.setParam("password", getContextId());
		requestBody.setParam("domain", "");
		requestBody.setParam("extra", "");
		
		String url = getHost() + FLUID_APP_SERVER_PROXY + AppServerApiEnum.userLogin.toString();
		String res = RestAdapter.getInstance().post(MediaType.JSON, url, requestBody.serializeMessage());
		Response response = new Response(res);
		
		String sessionID = response.getParamAsString("sessionID");
		String msg = response.getParamAsString("msg");
		if ((sessionID == null || !sessionID.isEmpty()) || (msg == null || !msg.isEmpty())) {
			Throwable e = new Throwable(msg);
			e.fillInStackTrace();
			throw e;
		}
		setContextId("");
	}
	@Override
	public LoadDataRequest userContextRequest() throws Throwable {
		LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.EDIT);
		
		final String contextId = getContextId();
		loadDataRequest.setLoadRowid(AppServerAdapter.stringToSHA1(contextId));
		
		return loadDataRequest;
	}
	@Override
	public String processResponse(RestAdapter.Response response) throws IOException {
		
		String responseStr = super.processResponse(response);
		
		if (response.getRequestUrl().startsWith(getHost() + FLUID_APP_SERVER_PROXY)) {
			final String contentType = response.getHeaders().get("Content-Type").get(0);
			if (contentType != null && !"application/json;charset=UTF-8".equalsIgnoreCase(contentType)) {
				throw new IllegalArgumentException("Expecting UTF-8, instead received: " + contentType);
			}
		}
		
		return responseStr;
	}
	@Override
	public ClassicAdapter newInstance() {
		return new ClassicAdapter();
	}
	@Override
	public ClassicAdapter copyInstance() {
		ClassicAdapter classic = new ClassicAdapter();
		classic.setHost(getHost());
		
		classic.setUser(getUser());
		classic.setPassword(getPassword());
		classic.setDomain(getDomain());
		classic.setExtra(getExtra());
		
		classic.setContextId(getContextId());
		classic.setDateFormat(getDateFormat());
		
		return classic;
	}
	@Override
	public boolean allowOrphanFields() {
		return false;
	}
}