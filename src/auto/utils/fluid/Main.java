package auto.utils.fluid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import auto.utils.ActivityDefinitionUtils;
import auto.utils.Assertions;
import fluid.test.data.ActivityDefinition;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.SaveDataResponse;

/**
 * <p>
 * For Fluid utility classes that have an activity with a "maintenance"
 * functionality - as in data can be loaded, manipulated and saved. May also be
 * used in conjunction with the {@link View} interface for "SRD" activity types,
 * as they can both search and be maintained.
 * </p>
 * <ul>
 * <li><code>loadMain</code> - String parameter of an applicable record
 * maintRowid String for an activity</li>
 * <li><code>saveMain</code> - Intended to be implemented with
 * {@link auto.utils.ActivityDefinitionUtils#saveData(ActivityDefinition, boolean, boolean)}
 * but with customised error handling</li>
 * </ul>
 * 
 * @author vercin
 *
 */
public interface Main{

	public ActivityDefinition getActivityDefinition();
	
	/**
	 * Assumes that if maintRowid is null or empty that the LoadDataModeEnum value will be ADD, otherwise EDIT
	 * 
	 * @param maintRowid String ID record to load the activity with
	 * @return LoadDataResponse
	 */
	default LoadDataResponse loadMain(String maintRowid) {
		return loadMain(getActivityDefinition(), maintRowid,
				(maintRowid != null && !maintRowid.isEmpty() ? LoadDataModeEnum.EDIT : LoadDataModeEnum.ADD));
	}
	
	default SaveDataResponse saveMain(boolean stopOnWarning, boolean validateOnly) {
		return saveMain(getActivityDefinition(), stopOnWarning, validateOnly);
	}

	default SaveDataResponse deleteMain(boolean stopOnWarning, boolean validateOnly) {
		return deleteMain(getActivityDefinition(), stopOnWarning, validateOnly);
	}
	
	public static LoadDataResponse loadMain(ActivityDefinition activityDef, String maintRowid, LoadDataModeEnum mode) {
		return ActivityDefinitionUtils.loadData(activityDef, mode, maintRowid);
	}

	public static SaveDataResponse saveMain(ActivityDefinition activityDef, boolean stopOnWarning, boolean validateOnly) {
		SaveDataResponse saveDataResponse = ActivityDefinitionUtils.saveData(activityDef, stopOnWarning, validateOnly);
		return saveDataResponse;
	}
	
	public static SaveDataResponse deleteMain(ActivityDefinition activityDef, boolean stopOnWarning, boolean validateOnly) {
		return ActivityDefinitionUtils.saveDataDelete(activityDef, stopOnWarning, validateOnly);
	}
	
	public static void assertSaveDataResponse(SaveDataResponse saveDataResponse) {
		Assertions.assertGroup(
				() -> assertFalse("Activity had errors attempting to save:\n" + saveDataResponse.getErrorMsg(), saveDataResponse.hasStopErrors()),
				() -> assertTrue("Activity failed to save:" + saveDataResponse.getErrorMsg(), saveDataResponse.getSaveSuccess()));
	}
}
