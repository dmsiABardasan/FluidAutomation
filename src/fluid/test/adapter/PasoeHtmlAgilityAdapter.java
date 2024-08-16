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
}