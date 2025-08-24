package fluid.test.adapter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import fluid.test.adapter.RestAdapter.CustomCookieJar;
import fluid.test.adapter.RestAdapter.MediaType;
import okhttp3.Cookie;

/**
 * Frameworks authentication with cookies for Auth0 Frameworks environments using the legacy fallback.
 * @author vercin
 */
public class AuthLegacyAdapter extends PasoeAdapter {
	
	@Override
	public Map<String, String> headers() {
		return Collections.singletonMap("Cookie", "JSESSIONID="+getContextId());
	}
	
	@Override
	public void login() throws Throwable {
		String contextId = loginPost(RestAdapter.getInstance(), getHost(), getUser(), getPassword(), getExtra());
		setContextId(contextId);
	}
	
	private static String checkCookieJsessionId(CustomCookieJar cookieJar) {
		String contextId = null;
		for (Cookie cookie : cookieJar.getCookies()) {
			if ("JSESSIONID".equals(cookie.name())) {
				contextId = cookie.value();
				break;
			}
		}
		if (contextId == null || contextId.isEmpty()) {
			throw new NullPointerException("ContextId was null or empty.");
		}
		
		return contextId;
	}
	
	@Override
	protected synchronized String loginPost(RestAdapter rest, String host, String user, String password, String extra) throws IOException {
		
		final CustomCookieJar cookieJar = rest.getCookieJar();
		
		String contextId = null;
		
		cookieJar.saveFromResponseFilters().add(CustomCookieJar.filterHttpUrlStartsWith(host));
		try {
			rest.get(host + "/Frameworks.html");
			String foo = checkCookieJsessionId(cookieJar);
			cookieJar.getCookies().clear();
			
			rest.post(MediaType.APPLICATION, host + "/static/auth/j_spring_security_check", 
					"j_username="+ URLEncoder.encode(user, StandardCharsets.UTF_8.name())+"&" + 
					"j_password="+ URLEncoder.encode(password, StandardCharsets.UTF_8.name()),
					Collections.singletonMap("Cookie", "JSESSIONID="+foo));
			
			contextId = checkCookieJsessionId(cookieJar);
			cookieJar.getCookies().clear();
			
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
	public AuthLegacyAdapter copyInstance() {
		AuthLegacyAdapter pasoe = new AuthLegacyAdapter();
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
	public AuthLegacyAdapter newInstance() {
		return new AuthLegacyAdapter();
	}
}