package auto.utils.fluid;

import fluid.test.data.ActivityDefinition;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.SaveDataResponse;

public abstract class DashboardSection extends FluidActivity implements Main {
	
	public DashboardSection() {
		super();
	}
	
	public DashboardSection(ActivityDefinition activityDef) {
		super(activityDef);
	}
	
	@Override
	public LoadDataResponse loadMain(String maintRowid) {
		return Main.loadMain(getActivityDefinition(), maintRowid, LoadDataModeEnum.VIEW);
	}
	
	@Override
	public SaveDataResponse saveMain(boolean stopOnWarning, boolean validateOnly) {
		throw new UnsupportedOperationException("This activity does not support this operation.");
	}
	
	@Override
	public SaveDataResponse deleteMain(boolean stopOnWarning, boolean validateOnly) {
		throw new UnsupportedOperationException("This activity does not support this operation.");
	}
	
}
