package fluid.test.adapter;

/**
 * Fluid authentication with cookies for Frameworks PASOE environments, carrying
 * the cookie from the landing HTML page.
 * 
 * @author vercin
 */
public class PasoeHtmlFrameworksAdapter extends PasoeHtmlAdapter {
	@Override
	public String getHtml() {
		return "/Frameworks.html";
	}
	
	@Override
	public PasoeHtmlFrameworksAdapter copyInstance() {
		PasoeHtmlFrameworksAdapter pasoe = new PasoeHtmlFrameworksAdapter();
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
	public PasoeHtmlFrameworksAdapter newInstance() {
		return new PasoeHtmlFrameworksAdapter();
	}
}