package fluid.test.adapter;

import java.io.IOException;

import fluid.test.adapter.RestAdapter.CustomCookieJar;

/**
 * Fluid authentication with cookies for PASOE environments, carrying
 * the cookie from the landing HTML page.
 * @author vercin
 */
public abstract class PasoeHtmlAdapter extends PasoeAdapter {
	
	public abstract String getHtml();
	
	/**
	 * <p>
	 * Save the cookie from the landing html page to the super
	 * j_spring_security_check.
	 * </p>
	 */
	@Override
	protected synchronized String loginPost(RestAdapter rest, String host, String user, String password, String extra) throws IOException {
		
		final String url = host + getHtml();
		final CustomCookieJar cookieJar = rest.getCookieJar();
		
		cookieJar.saveFromResponseFilters().add(CustomCookieJar.filterHttpUrlStartsWith(host));
		cookieJar.loadForRequestFilters().add(CustomCookieJar.FILTER_ALLOW_ALL);
		
		String contextId = null;
		try {
			rest.get(url);
			contextId = cookieJar.latestCookieValue("JSESSIONID");
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
		
		return super.loginPost(rest, host, user, password, extra);
	}
}