package auto.utils.fluid;

import static org.junit.Assert.fail;

import auto.utils.ActivityDefinitionUtils;
import fluid.test.data.ActivityDefinition;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.PresentationMessage;

/**
 * Implementation of Fluids baked in buttons that are in each activity. 
 * 
 * @author vercin
 */
public enum CrudButtonEnum {
	
	SAVE		("Save"),
	EDIT		("Edit"),
	DELETE		("Delete"),
	NEW			("New"),
	FIND		("Find"),
	ADV_FIND 	("AdvFind"),
	USE			("Use"), // Sometimes used as a macguffin
	CLOSE		("Close"),
	COPY		("Copy"),
	REFRESH		("Refresh"),
	SUBMIT		("Submit");
	//There are more but these are staple, add as required
	
	private final String value;
	public static boolean mlDebugTrace;
	
	private CrudButtonEnum(String value) {
		this.value = "button." + value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public static boolean isCrudButton(String tableField) {
		boolean exists = false;
		
		for (CrudButtonEnum crudButton: CrudButtonEnum.values()) {
			if (crudButton.getValue().equalsIgnoreCase(tableField)) {
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	public static CrudButtonEnum getCrudButton(String crudButtonValue) {
		CrudButtonEnum foundCrudButton = null;
		
		for (CrudButtonEnum crudButton: CrudButtonEnum.values()) {
			if (crudButton.getValue().equalsIgnoreCase(crudButtonValue)) {
				foundCrudButton = crudButton;
				break;
			}
		}
		if (foundCrudButton == null) {
			throw new IllegalArgumentException("Invalid value for Enum");
		}
		
		return foundCrudButton;
	}
	
	public static PresentationMessage crudButtonClick(ActivityDefinition activityDef, CrudButtonEnum crudButton) {
		
		if (mlDebugTrace) {
			System.out.println("Crud button <" + crudButton.toString() + "> clicked");
		}
		
		final PresentationMessage oResponse;
		
		switch (crudButton) {
			case NEW:
				//this$static.presenter.loadActivityData('ADD', '');
				oResponse = ActivityDefinitionUtils.loadData(activityDef, LoadDataModeEnum.ADD, "");
				break;
			case DELETE:
				//$saveActivityData(this$static.presenter, true, true);
				oResponse = ActivityDefinitionUtils.saveDataDelete(activityDef);
				break;
			case EDIT:
				//this$static.editNewDetail('EDIT', this$static.searchGridResults);
/*				activityDef.getResultsDataSourceDefinition().getCurrentRecord()
				.getFieldValueAsString(activityDef.getResultsDataSourceDefinition().getPrimaryKey());*/
				oResponse = ActivityDefinitionUtils.loadData(activityDef, LoadDataModeEnum.EDIT,
						activityDef.getLastLoadDataRequest() != null
								? activityDef.getLastLoadDataRequest().getLoadRowid()
								: "");
				break;
			case COPY:
				if (!activityDef.getResultsDataSourceDefinition().getRecordList().isEmpty()) {
					String loadRowid = activityDef.getResultsDataSourceDefinition().getCurrentRecord().getFieldValueAsString(activityDef.getResultsDataSourceDefinition().getPrimaryKey());
					oResponse = ActivityDefinitionUtils.loadData(activityDef, LoadDataModeEnum.COPY, loadRowid);
				} else {
					final String reason = "At least one record needs to be in the search results before it can be copied.";
					fail(reason);
					throw new IllegalArgumentException(reason);
				}
				break;
			case SAVE:
				//$saveActivityData(this$static.presenter, true, false);
				oResponse = ActivityDefinitionUtils.saveData(activityDef, activityDef.isStopOnWarnining(), activityDef.isValidateOnly());
				break;
			case FIND:
				GetResultsRequest genGetResults;
				if ((genGetResults = ViewGenSearch.searchRequest(activityDef)) != null) { 
					oResponse = ActivityDefinitionUtils.getResults(activityDef, genGetResults);
				} else {
					final String reason = "Cannot use \"find\", as the search DataSourceDefinition does not exist.";
					fail(reason);
					throw new IllegalArgumentException(reason);
				}
				break;
			case ADV_FIND:
				GetResultsRequest advGetResults;
				if ((advGetResults = ViewAdvSearch.searchRequest(activityDef)) != null) {
					oResponse = ActivityDefinitionUtils.getResults(activityDef, advGetResults);
				} else {
					final String reason = "Cannot use \"advFind\", as the advSearch DataSourceDefinition does not exist.";
					fail(reason);
					throw new IllegalArgumentException(reason);
				}
				break;
			case USE:
				try {
					oResponse = activityDef.loadCurrentResultRecord();
				} catch (Throwable ex) {
					fail(ex.getMessage());
					throw new IllegalArgumentException(ex);
				}
				break;
			case CLOSE:
				activityDef.close();
				oResponse = null;
				break;
			case REFRESH:
				oResponse = ActivityDefinitionUtils.loadData(activityDef, activityDef.getLastLoadDataRequest(), activityDef.getLastLoadDataRequest().getLoadRowid());
				break;
			case SUBMIT:
				oResponse = ActivityDefinitionUtils.submitActivityTask(activityDef, false, false);
				break;
			default:
				throw new IllegalArgumentException("Unhandled CRUD button");
		}
		
		return oResponse;
	}
}
	
/*
//CRUD button names
public static final String CRUD_C = "New";
public static final String CRUD_C_LABEL = "<U>N</U>ew";
public static final String CRUD_C_ACCESS_KEY = "N";
public static final String CRUD_R = "Edit";
public static final String CRUD_R_LABEL = "<U>E</U>dit";
public static final String CRUD_R_ACCESS_KEY = "E";
public static final String CRUD_FR_LABEL = "Find/<U>E</U>dit";
public static final String CRUD_FR_ACCESS_KEY = "E";
public static final String CRUD_Y = "Copy";
public static final String CRUD_Y_LABEL = "<U>C</U>opy";
public static final String CRUD_Y_ACCESS_KEY = "C";
public static final String CRUD_FY_LABEL = "Find/<U>C</U>opy";
public static final String CRUD_FY_ACCESS_KEY = "C";
public static final String CRUD_Z = "Cancel";
public static final String CRUD_Z_LABEL = "Cancel (<U>Z</U>)";
public static final String CRUD_Z_LABEL_2 = "Refresh (<U>Z</U>)";
public static final String CRUD_Z_ACCESS_KEY = "Z";
public static final String CRUD_X = "Close";
public static final String CRUD_X_LABEL = "Close (<U>X</U>)";
public static final String CRUD_X_ACCESS_KEY = "X";
public static final String CRUD_U = "Save";
public static final String CRUD_U_LABEL = "<U>S</U>ave";
public static final String CRUD_U_ACCESS_KEY = "S";
public static final String CRUD_D = "Delete";
public static final String CRUD_D_LABEL = "<U>D</U>elete";
public static final String CRUD_D_ACCESS_KEY = "D";

public static final String ROW_C = "Add";
public static final String ROW_C_LABEL = "<U>A</U>dd";
public static final String ROW_C_ACCESS_KEY = "A";
public static final String ROW_M = "Modify";
public static final String ROW_M_LABEL = "<U>M</U>odify";
public static final String ROW_M_ACCESS_KEY = "M";
public static final String ROW_F = "Finish";
public static final String ROW_F_LABEL = "<U>F</U>inish";
public static final String ROW_F_ACCESS_KEY = "F";
public static final String ROW_R = "Remove";
public static final String ROW_R_LABEL = "<U>R</U>emove";
public static final String ROW_R_ACCESS_KEY = "R";

//Other Control buttons 
public static final String CTRL_S = "Submit";
public static final String CTRL_S_LABEL = "<U>S</U>ubmit";
public static final String CTRL_S_ACCESS_KEY = "S";
public static final String CTRL_U = "Use";
public static final String CTRL_U_LABEL = "<U>U</U>se";
public static final String CTRL_U_ACCESS_KEY = "U";
public static final String CTRL_I = "Import";
public static final String CTRL_I_LABEL = "<U>I</U>mport";
public static final String CTRL_I_ACCESS_KEY = "I";
public static final String CTRL_F = "Find";
public static final String CTRL_F_LABEL = "<U>F</U>ind";
public static final String CTRL_F_ACCESS_KEY = "F";
public static final String CTRL_AF = "AdvFind";
public static final String CTRL_ADD_LINE = "AddLine";
public static final String CTRL_DEL_LINE = "DelLine";
public static final String CTRL_BREADCRUMB = "breadcrumb";

public static final String CTRL_FIRST_PROMPT = "Fetch the first page";
public static final String CTRL_PREV_PROMPT = "Fetch the previous page";
public static final String CTRL_NEXT_PROMPT = "Fetch the next page";
public static final String CTRL_LAST_PROMPT = "Fetch the last page";
*/


	
	

