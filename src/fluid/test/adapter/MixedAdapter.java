package fluid.test.adapter;

import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;

public class MixedAdapter extends ClassicAdapter{
	
	@Override
	public LoadDataRequest userContextRequest() throws Throwable {
		LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.EDIT);
		
		final String contextId = fluidVersion().getParamAsString("version");
		loadDataRequest.setParam("fluidUiVersion", contextId);
		loadDataRequest.setLoadRowid(AppServerAdapter.stringToSHA1(contextId));
		
		loadDataRequest.setParam("apiURL", "fluid/FluidAppServerProxy");
		loadDataRequest.setParam("applicationURL", getHost()+"/");
		
		return loadDataRequest;
	}
	
	@Override
	public MixedAdapter newInstance() {
		return new MixedAdapter();
	}
	@Override
	public MixedAdapter copyInstance() {
		MixedAdapter mixed = new MixedAdapter();
		mixed.setHost(getHost());
		
		mixed.setUser(getUser());
		mixed.setPassword(getPassword());
		mixed.setDomain(getDomain());
		mixed.setExtra(getExtra());
		
		mixed.setContextId(getContextId());
		mixed.setDateFormat(getDateFormat());
		
		return mixed;
	}
	@Override
	public boolean allowOrphanFields() {
		return true;
	}
}