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
	
	/**
	 * Static resource used to (re)fetch the CSRF token cookie — the same
	 * permitAll resource the Fluid GWT client refreshes from
	 * (RequestBuilder.CSRF_REFRESH_URL in FluidUI).
	 */
	private static final String CSRF_SEED_PATH = "/images/blank.gif";

	protected synchronized String loginPost(RestAdapter rest, String host, String user, String password, String extra) throws IOException {
		final String url = host + "/static/auth/j_spring_security_check";
		final CustomCookieJar cookieJar = rest.getCookieJar();

		String contextId = null;

		cookieJar.saveFromResponseFilters().add(CustomCookieJar.filterHttpUrlStartsWith(host));
		try {
			// With CSRF protection enabled (http.spring.csrf=true) the login
			// POST itself is protected, so seed a pre-login session first: the
			// GET answers with an XSRF-TOKEN cookie bound to a JSESSIONID, and
			// the RestAdapter echoes it as X-CSRF-TOKEN on the POST below.
			// Harmless when CSRF protection is disabled.
			rest.get(host + CSRF_SEED_PATH);
			String preLoginSession = cookieJar.latestCookieValue("JSESSIONID");
			String loginCookies = (preLoginSession == null ? "" : "JSESSIONID=" + preLoginSession + "; ") + "FID=" + extra;

			RestAdapter.Response loginResponse = rest.post(url,
					"j_username="+ URLEncoder.encode(user, StandardCharsets.UTF_8.name())+"&" +
					"j_password="+ URLEncoder.encode(password, StandardCharsets.UTF_8.name())+"&" +
					"SubmitButton=Submit",
					Collections.singletonMap("Cookie", loginCookies),
					MediaType.APPLICATION);
			if (loginResponse.getCode() == 403) {
				throw new IOException("Login rejected with HTTP 403 — CSRF token missing or stale for " + url);
			}
			// Session fixation protection rotates the id during login; the
			// authenticated session is the newest JSESSIONID, not the first.
			contextId = cookieJar.latestCookieValue("JSESSIONID");
			if (contextId == null || contextId.isEmpty()) {
				throw new NullPointerException("ContextId was null or empty.");
			}
			cookieJar.retainNewestCookies();
			// Login also rotates the CSRF token; fetch the replacement for the
			// authenticated session before the first state-changing request.
			rest.get(host + CSRF_SEED_PATH, Collections.singletonMap("Cookie", "JSESSIONID=" + contextId + "; FID=" + extra));
			cookieJar.retainNewestCookies();
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
		RestAdapter.getInstance().getCookieJar().getCookies().clear();
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