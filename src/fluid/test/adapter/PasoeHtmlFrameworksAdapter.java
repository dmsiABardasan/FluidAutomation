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
}