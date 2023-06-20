package fluid.test.adapter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import fluid.test.adapter.RestAdapter.CustomCookieJar;
import fluid.test.adapter.RestAdapter.MediaType;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import okhttp3.Cookie;

/**
 * Fluid authentication with cookies for PASOE environments.
 * @author vercin
 */
public class PasoeAdapter extends AppServerAdapter{
	@Override
	public String buildUrlForApp(AppServerApiEnum ipApi, String ipService, String ipFunction) {
		return  getHost() + "/web/fluid/" + (ipApi == AppServerApiEnum.presenter ? "presenter" : "static") + "/"
				+ ipService + (ipApi == AppServerApiEnum.getResults ? "BA" : "PR") + "/"
				+ (ipApi == AppServerApiEnum.presenter ? ipFunction : ipApi.toString());
	}
	@Override
	public Map<String, String> headers() {
		return Collections.singletonMap("Cookie", "JSESSIONID="+getContextId() + "; FID="+getExtra());
	}
	@Override
	public void login() throws Throwable {
		String contextId = loginPost(RestAdapter.getInstance(), getHost(), getUser(), getPassword(), getExtra());
		setContextId(contextId);
	}
	
	private static String loginPost(RestAdapter rest, String host, String user, String password, String extra) throws IOException {
		final String url = host + "/static/auth/j_spring_security_check";
		final CustomCookieJar cookieJar = rest.getCookieJar();
		
		String contextId = null;
		
		cookieJar.saveFromResponseFilters().add(CustomCookieJar.filterHttpUrlStartsWith(host));
		try {
			rest.post(MediaType.APPLICATION, url, 
					"j_username="+ URLEncoder.encode(user, StandardCharsets.UTF_8)+"&" + 
							"j_password=" + URLEncoder.encode(password,
									StandardCharsets.UTF_8)+"&" + 
															"SubmitButton=Submit", 
					Collections.singletonMap("Cookie", "FID="+extra));
			for (Cookie cookie : cookieJar.getCookies()) {
				if ("JSESSIONID".equals(cookie.name())) {
					contextId = cookie.value();
					break;
				}
			}
			if (contextId == null || contextId.isEmpty()) {
				throw new NullPointerException("ContextId was null or empty.");
			}
		} catch (Exception ex) {
			Throwable e = new Throwable(ex);
			e.fillInStackTrace();
			throw ex;
		} finally {
			cookieJar.saveFromResponseFilters().clear();
		}
		
		return contextId;
	}
	
	@Override
	public void logout() throws Throwable {
		try {
			RestAdapter.getInstance().post(
					MediaType.APPLICATION,
					getHost() + "/static/auth/j_spring_security_logout",
					"", headers());
		} catch(Exception ex) {
			Throwable e = new Throwable(ex);
			e.fillInStackTrace();
			throw e;
		}
		setContextId("");
	}
	@Override
	public LoadDataRequest userContextRequest() throws Throwable {
		LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.EDIT);
		
		final String contextId = fluidVersion().getParamAsString("version");
		loadDataRequest.setParam("fluidUiVersion", contextId);
		loadDataRequest.setParam("applicationURL", getHost()+"/");
		loadDataRequest.setLoadRowid(AppServerAdapter.stringToSHA1(contextId));
		
		return loadDataRequest;
	}
	@Override
	public PasoeAdapter copyInstance() {
		PasoeAdapter pasoe = new PasoeAdapter();
		pasoe.setHost(getHost());
		
		pasoe.setUser(getUser());
		pasoe.setPassword(getPassword());
		pasoe.setDomain(getDomain());
		pasoe.setExtra(getExtra());
		
		pasoe.setContextId(getContextId());
		pasoe.setDateFormat(getDateFormat());
		
		return pasoe;
	}
	@Override
	public PasoeAdapter newInstance() {
		return new PasoeAdapter();
	}
	@Override
	public boolean allowOrphanFields() {
		return true;
	}
}