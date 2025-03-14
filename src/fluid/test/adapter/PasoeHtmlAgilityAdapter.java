package fluid.test.adapter;

/**
 * Fluid authentication with cookies for Agility PASOE environments, carrying
 * the cookie from the landing HTML page.
 * 
 * @author vercin
 */
public class PasoeHtmlAgilityAdapter extends PasoeHtmlAdapter {
	@Override
	public String getHtml() {
		return "/Agility.html";
	}
	
	@Override
	public PasoeHtmlAgilityAdapter copyInstance() {
		PasoeHtmlAgilityAdapter pasoe = new PasoeHtmlAgilityAdapter();
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
	public PasoeHtmlAgilityAdapter newInstance() {
		return new PasoeHtmlAgilityAdapter();
	}
}