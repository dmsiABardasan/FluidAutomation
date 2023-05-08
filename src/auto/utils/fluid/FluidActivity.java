package auto.utils.fluid;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import auto.utils.ActivityDefinitionUtils;
import auto.utils.step.LazyProcedure;
import auto.utils.step.Step;
import fluid.test.adapter.AppServerAdapter;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.ChildActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.message.CtrlPropPair;

/**
 * Parent of Fluid Utility classes, contains basic methods and assures that a
 * ipService is supplied and the activity is loaded
 * 
 * @author vercin
 */
public abstract class FluidActivity {

	private ActivityDefinition activityDef;
	
	public FluidActivity() {
		this(new ActivityDefinition());
	}
	
	/**
	 * If the passed ActivityDefinition is of the relevant ipService and has a
	 * non-null model it will not attempt a LoadActivityRequest and assumes it has
	 * already loaded the activity, providing access to the given methods (And
	 * subclass implementations). Otherwise it will attempt to load the
	 * ActivityDefinition.
	 * 
	 * Be aware that changing the ipService while it contains a model for a
	 * different service may have unforeseen consequences.
	 * 
	 * @throws IllegalArgumentException
	 *             if a non-null model contains an ipService that is not equal to
	 *             the implementing class service
	 * @param activityDef
	 *            ActivityDefinition that is to be the FluidActivity base
	 */
	public FluidActivity(ActivityDefinition activityDef) {
		this.activityDef = activityDef;
		if (activityDef.getModel() != null) {
			if (!getService().equals(activityDef.getService())) {
				throw new IllegalArgumentException(
						"The passed ActivityDefinition did not have the a matching ipService for this FluidActivity");
			}
		} else {
			loadActivity();
		}
	}
	
	public ActivityDefinition getActivityDefinition() {
		return this.activityDef;
	}
	
	public void closeAllChildren() {
		ActivityDefinition activityDef = getActivityDefinition();
		Set<ChildActivityDefinition> childSet = new HashSet<ChildActivityDefinition>(activityDef.getChildSet());
		for (ActivityDefinition childActivityDef : childSet) {
			childActivityDef.setClosed(true);
		}
	}
	
	public void loadActivity() {
		ActivityDefinitionUtils.loadActivity(getActivityDefinition(), getService());
	}
	
	/**
	 * Reloads the activity loaddata, if the activity did not have a loaddata previously it's a no-op
	 */
	public void refresh() {
		ActivityDefinition activityDef = getActivityDefinition();
		if (activityDef.getLastLoadDataRequest() != null) {
			try {
				AppServerAdapter.getInstance().loadData(activityDef, activityDef.getLastLoadDataRequest());
			} catch (Throwable ex) {
				ex.printStackTrace();
				fail(ex.getMessage());
			}
		}
	}
	
	public String getName() {
		return getActivityDefinition().getModel().getActivity();
	}
	
	public String getDesc() {
		return getActivityDefinition().getModel().getActivityDesc();
	}
	
	public ActivityTypeEnum getType() {
		return ActivityTypeEnum.getActivityType(getActivityDefinition().getModel().getActivityType());
	}
	
	public Map<CtrlPropPair, String> getCtrlProps() {
		return getActivityDefinition().getModel().getCtrlProperties();
	}
	
	public DataSourceDefinition getDataSourceDefinition(String table) {
		return getActivityDefinition().getDataSourceDefinitionMap().get(table);
	}
	
	public void before(Step... before) {
		LazyProcedure procedure = new LazyProcedure();
		for (Step step: before) {
			procedure.executeStep(getActivityDefinition(), step);
		}
		procedure.assertProcedure();
	}
	
	/**
	 * @return String representing an ActivityDefinition ipService
	 */
	public abstract String getService();
}
