package auto.utils.step;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import auto.utils.ActivityDefinitionUtils;
import auto.utils.ApiUtils;
import auto.utils.ValidationTypeEnum;
import auto.utils.fluid.ActivityTypeEnum;
import auto.utils.fluid.CrudButtonEnum;
import auto.utils.fluid.Dashboard;
import fluid.test.adapter.AppServerAdapter;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.ActivityDefinitionResponseCascader;
import fluid.test.data.ChildActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.ViewDefinition;
import fluid.test.message.AbstractMessage;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.CtrlPropPair;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;

/**
 * <p>
 * The <code>Step</code> class is an object to hold values about what and how a
 * <i>Fluid</i> field validation is to be handled and executed.</p>
 * 
 * @author vercin
 */
public class Step {
	
	private List<ChildActivityDefinition> children = new ArrayList<ChildActivityDefinition>();
	private List<Step> subSteps = new ArrayList<Step>();
	private Integer recordIndex;
	private List<String> label = new ArrayList<String>();
	private String field;
	private String table;
	private String tableField;
	private Object value;
	private boolean valueAssigned;
	private boolean complete;
	private boolean auto;
	private boolean launch;
	private ValidationTypeEnum validationType;
	private PresentationMessage response;
	private String calledActivityName;
	private String calledActivityService;
	private ActivityDefinition calledActivityDefinition;
	private CtrlPropEnum contractType;
	
	public static boolean mlDebugTrace;
	
	private Step() {}
	
	public Step(ValidationTypeEnum validationType, String table, String field) {
		this.table = table;
		this.field = field;
		this.tableField = table + "." + field;
		this.validationType = validationType;
	}
	
	public Step(ValidationTypeEnum validationType, String table, String field, Object value) {
		this(validationType, table, field);
		this.value = value;
		this.valueAssigned = true;
	}
	
	public Step(ValidationTypeEnum validationType, String[] tableField) {
		this(validationType, tableField[0], tableField[1]);
	}
	
	public Step(ValidationTypeEnum validationType, String[] tableField, Object value) {
		this(validationType, tableField[0], tableField[1], value);
	}
	
	private void setCalledActivityDefinition(ActivityDefinition activityDef) {
		this.calledActivityDefinition = activityDef;
		setCalledActivityName(activityDef.getModel().getActivity());
		setCalledActivityService(activityDef.getService());
	}
	
	public ActivityDefinition getCalledActivityDefinition() {
		return this.calledActivityDefinition;
	}
	
	private void setChildren(List<ChildActivityDefinition> children) {
		this.children = children;
	}
	
	public List<ChildActivityDefinition> getChildren() {
		return this.children;
	}
	
	/**
	 * @param launch
	 *            Boolean that signifies that this Step is to attempt generation of
	 *            a ChildActivityDefinition (true) or not (false)
	 * @return This Step (builder)
	 */
	public Step setLaunch(boolean launch) {
		this.launch = launch;
		return this;
	}
	
	/**
	 * @param validationType How this Step is to be validated
	 * @return This Step (builder)
	 */
	public Step setValidationType(ValidationTypeEnum validationType) {
		this.validationType = validationType;
		return this;
	}

	/**
	 * @param field Name of the field to be validated
	 * @return This Step (builder)
	 */
	public Step setField(String field) {
		this.field = field;
		setTableField(getTable(), field);
		return this;
	}
	 
	 /**
	 * @param table Name of the table to be validated
	 * @return This Step (builder)
	 */
	public Step setTable(String table) {
		this.table = table;
		setTableField(table, getField());
		return this;
	}
	
	private void setTableField(String table, String field) {
		this.tableField = table + "." + field;
	}
	
	/**
	 * Assigning a value to this will mark this object as having a
	 * {@link #isValueAssigned() value assigned}
	 * 
	 * @param value
	 *            <code>Object</code> value to be set for this
	 *            {@link #setTable(String) table} and {@link #setField(String) field}
	 * @return This Step (builder)
	 */
	public Step setValue(Object value) {
		this.value = value;
		setValueAssigned(true);
		return this;
	}
	
	/**
	 * @param index Integer Index of a table object you wish to modify, null will refer to the currentRow of the DataSourceDefinition
	 */
	public Step setRecordIndex(Integer index) {
		if (index != null && index < 0) throw new IndexOutOfBoundsException("You can not have a negative index for a record");
		this.recordIndex = index;
		return this;
	}
	
	/**
	 * <p>
	 * Assigning a value to this will mark this Step as being {@link #isComplete()
	 * complete}.
	 * </p>
	 * <p>
	 * Also sets {@link #setCalledActivityName(String)} and
	 * {@link #setCalledActivityService(String)} to their respective immutable
	 * values from the passed ActivityDefintion that the response was processed
	 * with.
	 * </p>
	 * @param response
	 *            PresentationMessage response that was created from this Step
	 */
	private void setResponse(PresentationMessage response) {
		this.response = response;
		setIsComplete(true);
	}

	/**
	 * @param isValueSet <code>boolean</code> to indicate that this step has had a value assigned (true) or not (false)
	 */
	private void setValueAssigned(boolean isValueSet) {
		this.valueAssigned = isValueSet;
	}

	/**
	 * @param complete <code>boolean</code> to indicate that this step has received a PresentationMessage response (true) or not (false)
	 */
	private void setIsComplete(boolean complete) {
		this.complete = complete;
	}
	
	/**
	 * @param auto <code>boolean</code> to indicate that this step has been automatically generated (true) or not (false)
	 */
	private void setAuto(boolean auto) {
		this.auto = auto;
	}
	
	/** 
	 * @param calledActivityName The activity name that completed this step
	 */
	private void setCalledActivityName(String calledActivityName) {
		this.calledActivityName = calledActivityName;
	}
	
	/**
	 * @param calledActivityService The activity service that completed this step
	 */
	private void setCalledActivityService(String calledActivityService) {
		this.calledActivityService = calledActivityService;
	}
	
	/**
	 * @param contractType
	 *            The contract type this step was run by, either VALIDATIONCONTRACT
	 *            if validation occurred or LAUNCHCONTRACT if attempted to create a
	 *            ChildActivityDefinition or exceptionally LOADDATA.
	 * @return This Step (Builder)
	 */
	private void setContractType(CtrlPropEnum contractType) {
		this.contractType = contractType;
	}

	/**
	 * @return Boolean that signifies that this Step is to attempt generation of a
	 *         ChildActivityDefinition (true) or not (false)
	 */
	public boolean isLaunch() {
		return this.launch;
	}
	
	/**
	 * @return <code>ValidationTypeEnum</code> indicating how this step is to be validated
	 */
	public ValidationTypeEnum getValidationType() {
		return this.validationType;
	}

	/**
	 * @return Set Optional unique identifier, description or use for debugging
	 */
	public List<String> getLabel() {
		return this.label;
	}
	
	/**
	 * @return String representing the field for validation
	 */
	public String getField() {
		return this.field;
	}
	
	/**
	 * @return String representing the table for validation
	 */
	public String getTable() {
		return this.table;
	}
	
	/**
	 * @return Object representing the value to be set and validated
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * @return Integer representing the index of the DataSourceDefinition Record to be validated, null will refer to the 'currentRow' of the DataSourceDefinition
	 */
	public Integer getRecordIndex() {
		return this.recordIndex;
	}

	/**
	 * @return Response of the validation performed on this step
	 */
	public PresentationMessage getResponse() {
		return response;
	}
	
	public AbstractMessage getRequest() {
		AbstractMessage request = null;
		if (this.response != null) {
			request = this.response.getCallingRequest();
		}
		return request;
	}
	
	/**
	 * @see #setValue(Object)
	 * @return <code>boolean</code> indicating if a value has been assigned (true) or not (false)
	 */
	public boolean isValueAssigned() {
		return this.valueAssigned;
	}

	/**
	 * @return <code>boolean</code> indicating if a response has been returned to this step (true) or not (false)
	 */
	public boolean isComplete() {
		return this.complete;
	}

	/**
	 * @return <code>boolean</code> indicating if this step was generated without direct user intervention (true) or not (false)
	 */
	public boolean isAuto() {
		return auto;
	}
	
	/**
	 * @return The name of the activity that this Step was validated against
	 */
	public String getCalledActivityName() {
		return this.calledActivityName;
	}
	
	/**
	 * @return The service of the activity that this Step was validated against
	 */
	public String getCalledActivityService() {
		return this.calledActivityService;
	}
	
	/**
	 * @return The contract type this step was run by, either VALIDATIONCONTRACT if
	 *         validation occurred or LAUNCHCONTRACT if attempted to create a
	 *         ChildActivityDefinition or exceptionally LOADDATA.
	 */
	public CtrlPropEnum getContractType() {
		return this.contractType;
	}

	/**
	 * @return concatenation of {@link #getTable() table} and {@link #getField() field}, separated by a point
	 */
	public String getTableField() {
		return this.tableField;
	}
	
	/**
	 * @return List of Steps that cascaded from <code>this</code> Step
	 */
	public List<Step> getSubSteps() {
		return this.subSteps;
	}

	/**
	 * Deep-copy for reuse - this only passes primitives and immutable types to the
	 * new Step, making mutation of the original unlikely. The response and values
	 * of the Step indicating whether the step is completed or automatically
	 * generated are excluded from the copy.
	 * 
	 * @return New copy of this Step
	 */
	public Step cloneStep() {
		Step step = new Step();
		
		step.setRecordIndex(this.recordIndex); 
		step.setField(this.field);
		step.setTable(this.table);
		step.setLaunch(this.launch);
		if (this.valueAssigned) {
			step.setValue(this.value);
		}
		step.setValidationType(this.validationType);
		
		return step;
	}
	
	/**
	 * Checks that {@link #getTableField() this String} exists in the passed
	 * activity definition and is not-null
	 * 
	 * @param activityDef
	 *            ActivityDefinition to check this step's existence in
	 */
	public void validateStepTableField(ActivityDefinition activityDef) {
		
		if (getTable() == null) {
			throw new NullPointerException(
					"Step contained a null table value");
		}
		
		if (getField() == null) {
			throw new NullPointerException(
					"Step contained a null field value");
		}
		
		boolean exists = false;
		
		StringBuilder strBldr = new StringBuilder();
		if ("button".equalsIgnoreCase(getTable()) || "grid".equalsIgnoreCase(getField()) ){
			if (!CrudButtonEnum.isCrudButton(getTableField())) {
				for (CtrlPropPair ctrlProp: activityDef.getModel().getCtrlProperties().keySet()) {
					if (getTableField().equals(ctrlProp.getName())) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					// Perhaps an activitySaveButton
					for (DataSourceDefinition dsDef : activityDef.getDataSourceDefinitionMap().values()) {
						ViewDefinition viewDef = dsDef.getViewDefinition();
						Map<String, Object> fieldViewDef = viewDef.getFieldViewDefinition(getField());
						if (fieldViewDef != null) {
							exists = true;
							break;
						}
					}
					
					if (!exists) {
						strBldr.append("Button <" + getTableField());
					}
				}
			} else {
				exists = true;
			}
		} else if (!activityDef.getDataSourceDefinitionMap().containsKey(getTable())) {
			strBldr.append("Table <" + getTable());
		} else if ((!activityDef.getDataSourceDefinitionMap().get(getTable()).getFieldDefinitionMap().containsKey(getField()) 
				&& !(activityDef.getDataSourceDefinitionMap().get(getTable()).getCurrentRecord() != null && activityDef
						.getDataSourceDefinitionMap().get(getTable()).getCurrentRecord().containsKey(getField()))) && !"search".equals(getTable())) {
			strBldr.append("Field <" + getField());
		}else {
			exists = true;
		}
		
		if (!exists) {
			strBldr.append("> did not exist in the passed ActivityDefinition.");
			if (isAuto()) {
				System.err.println("WARNING: " + strBldr.toString());
			} else {
				throw new NoSuchElementException(strBldr.toString());
			}
		}
	}
	
	/**
	 * Executes this Step instance to
	 * {@link #executeProcedure(ActivityDefinition, List, boolean, boolean)}. Use
	 * {@link #getSubSteps()} to see any Steps that may have spawned from this Step.
	 * 
	 * @param activityDef
	 *            ActivityDefinition to apply this Step to
	 * @param haltOnChildActivity
	 *            <code>boolean</code> determining if the method will exit
	 *            immediately if a <code>ChildActivityDefinition</code> is detected
	 *            (true) or return only after <code>List</code> of <code>Step</code>
	 *            has completely iterated (false).
	 * @param clickCrudButtons
	 *            <code>boolean</code> that determines if when any
	 *            {@link CrudButtonEnum crud buttons} are clicked in any response
	 *            that they will be processed (true) or not (false)
	 * @return List possibly populated with ChildActivityDefinitions
	 */
	public List<ChildActivityDefinition> execute(ActivityDefinition activityDef, boolean haltOnChildActivity, boolean clickCrudButtons){
		return executeProcedure(activityDef, new ArrayList<Step>(Collections.singletonList(this)), haltOnChildActivity, clickCrudButtons);
	}

	/**
	 * <p>
	 * Iterates a <code>List</code> of <code>Step</code> containing and executing
	 * instructions of what and how to validate fields against the passed
	 * <code>ActivityDefinition</code>.
	 * </p>
	 * <p>
	 * Features using this method include:
	 * </p>
	 * <ul>
	 * <li>Execution of Steps and how they are to be validated, while keeping
	 * reference as to what activity they were validated against and what the
	 * response was</li>
	 * <li>Automatic collection and generation of ChildActivityDefintions based on
	 * responses to Steps</li>
	 * <li>Automatic continuation of requests based on responses containing
	 * BUTTONCLICK Control Properties ("cascading")</li>
	 * <li>Automatic continuation of requests based on responses from a LoadData
	 * ChildActivityDefintion to its parent activity</li>
	 * <li>Integrated handling of hidden Crud Buttons (clickCrudButtons param)</li>
	 * <li>Automatic generation of new Steps to reference the above features to help
	 * in documentation/debugging</li>
	 * <li>Handling of LOADDATA control properties, in which an activity is
	 * loaded</li>
	 * <li>Handling of SRD activity types, in which when an SRD activity is saved, a
	 * GetResultsRequest with the 'maintRowid' String of the saved activity as a
	 * param is called and then loads the returned record in a LoadDataRequest.
	 * </ul>
	 * 
	 * <p>
	 * The same list can be applied again if prematurely returned, as any steps up
	 * to that point will have a variable indicating if they have been completed or
	 * not.
	 * </p>
	 * 
	 * 
	 * @param activityDef
	 *            <code>ActivityDefinition</code> to apply a <code>List</code> of
	 *            <code>Step</code> against
	 * @param procedure
	 *            <code>List</code> of <code>Step</code> to apply to the
	 *            ActivityDefinition
	 * @param haltOnChildActivity
	 *            <code>boolean</code> determining if the method will exit
	 *            immediately if a <code>ChildActivityDefinition</code> is detected
	 *            (true) or return only after <code>List</code> of <code>Step</code>
	 *            has completely iterated (false).
	 * @param clickCrudButtons
	 *            <code>boolean</code> that determines if when any
	 *            {@link CrudButtonEnum crud buttons} are clicked in any response
	 *            that they will be processed (true) or not (false)
	 * 
	 * @return List of ChildActivityDefinitions
	 */
	public static List<ChildActivityDefinition> executeProcedure(ActivityDefinition activityDef, final List<Step> procedure,
			boolean haltOnChildActivity, boolean clickCrudButtons) {
		List<ChildActivityDefinition> childActivityDefList = new ArrayList<ChildActivityDefinition>();
		
		ActivityDefinitionResponseCascader activityDefRes = ActivityDefinitionResponseCascader.getInstance(activityDef);
		
		childActivityDefList.addAll(insertNextTableFieldsIntoProcedure(procedure, activityDefRes, haltOnChildActivity, clickCrudButtons));
		
		for (int i = 0; i < procedure.size(); i++) {
			final Step step = procedure.get(i);
			
			if (!step.isComplete()) {
				
				step.setCalledActivityDefinition(activityDefRes);
				
				if (!clickCrudButtons) {
					if (CrudButtonEnum.isCrudButton(step.getTableField())) {
						procedure.remove(i);
						i--;
						continue;
					}
				}
				
				step.validateStepTableField(activityDefRes);
				
				if (!"button".equalsIgnoreCase(step.getTable())) {
					if (step.getRecordIndex() != null) {
						activityDefRes.getDataSourceDefinitionMap().get(step.getTable()).setCurrentRow(step.getRecordIndex());
					}else {
						step.setRecordIndex(activityDefRes.getDataSourceDefinitionMap().get(step.getTable()).getCurrentRow());
					}
				}
				
				if (step.isValueAssigned()) {
					activityDefRes.getDataSourceDefinitionMap().get(step.getTable()).getCurrentRecord()
							.setFieldValue(step.getField(), step.getValue());
				}
				
				try {
					assertFalse("Activity is closed.", activityDefRes.isClosed());
					
					if (!step.isLaunch()) {
						step.validationHandling(activityDefRes, childActivityDefList, procedure, haltOnChildActivity, clickCrudButtons);
						
					} else {
						activityDefRes = step.childHandling(childActivityDefList, activityDefRes, procedure, haltOnChildActivity, clickCrudButtons);
					}

					PresentationMessage stepResponse = step.getResponse();
					if (activityDefRes.isClosed()) {
						step.getLabel().add("Closed activity \"" + step.getCalledActivityName() + "\"");
						if (activityDefRes instanceof ChildActivityDefinition) {
							ChildActivityDefinition childActivityDef = (ChildActivityDefinition) activityDefRes;
							ActivityDefinitionResponseCascader parentActivityDef = ActivityDefinitionResponseCascader.getInstance(childActivityDef.getParentActivityDefinition());
							activityDefRes = (ActivityDefinitionResponseCascader) parentActivityDef;
						}else {
							break;
						}
					} else if (stepResponse != null && stepResponse.getCtrlProp("window.parent", CtrlPropEnum.ACTIVITYCLOSE) != null) {
						if (activityDefRes instanceof ChildActivityDefinition) {
							step.getLabel().add("Closed parent activity \"" + ((ChildActivityDefinition) activityDefRes).getParentActivityName() + "\"");
						}
					}
					
					List<PresentationMessage> asyncValidations;
					if (!(asyncValidations = activityDefRes.getAsyncValidations()).isEmpty()) {
						List<Step> asyncSteps = new ArrayList<Step>();
						for (PresentationMessage oResponse : asyncValidations) {
							if (oResponse.getCallingField() != null) {
								String[] tableField = oResponse.getCallingField().split("\\.");
								
								Step asyncStep = new Step(step.getValidationType(), tableField[0], tableField[1]);
								asyncStep.setAuto(true);
								asyncStep.setContractType(CtrlPropEnum.VALIDATIONCONTRACT);
								asyncStep.getLabel().add("WARNING - Asynchronous step");
								validationResponseHandling(activityDefRes, asyncStep, oResponse);
								
								asyncSteps.add(asyncStep);
							}
						}
						asyncValidations.clear();
						procedure.addAll(1 + i, asyncSteps);
						step.subSteps.addAll(asyncSteps);
					}
					
				} catch (AssertionError ass) {
					stepTrace(step, procedure, ass);
				}
				
				try {
					childActivityDefList.addAll(step.cascade(activityDefRes, procedure, haltOnChildActivity, clickCrudButtons));
				} catch (AssertionError ass) {
					stepTrace(step, procedure, ass);
				}
				
				step.setChildren(childActivityDefList);
				
				if (haltOnChildActivity && !childActivityDefList.isEmpty()) {
					if (mlDebugTrace) {
						System.out.println("Procedure execution halted at step <" + (1 + i)
									+ "> from response to <" + step.getTableField()
									+ "> validation regarding ChildActivityDefinition(s):\r\n");
						for (ChildActivityDefinition outChildActDef : childActivityDefList) {
							System.out.println(outChildActDef);
						}
					}
					break;
				}
			}
		}
		return childActivityDefList;
	}
	
	private void validationHandling(ActivityDefinitionResponseCascader activityDefRes,
			List<ChildActivityDefinition> childActivityDefList, List<Step> procedure,
			boolean haltOnChildActivity, boolean clickCrudButtons) {
		
		setContractType(CtrlPropEnum.VALIDATIONCONTRACT);
		
		PresentationMessage oResponse = null;
		try {
			if (CrudButtonEnum.isCrudButton(getTableField())) {
				CrudButtonEnum crudButton = CrudButtonEnum.getCrudButton(getTableField());
				switch (crudButton) {
					case USE:
						//Handles selecting something in the dashboard and it exploding into many activities
						if (ActivityTypeEnum.DASHBOARD == ActivityTypeEnum.getActivityType(activityDefRes.getModel().getActivityType())) {
							
							final String maintRowid;
							if (activityDefRes.getResultsDataSourceDefinition() != null) {
								if (activityDefRes.getResultsDataSourceDefinition().getRecordList().isEmpty()) {
									fail("Nothing to use! (Use CrudButtonEnum.FIND or a delay may be required for server side process to complete)");
								}
								setRecordIndex(activityDefRes.getResultsDataSourceDefinition().getCurrentRow());
								
								DataSourceDefinition results = activityDefRes.getResultsDataSourceDefinition();
								maintRowid = results.getCurrentRecord().getFieldValueAsString(results.getPrimaryKey());
								
							} else {
								maintRowid = "dummy";
							}
							
							childActivityDefList.addAll(dashboardHandling(
									activityDefRes,
									procedure,
									maintRowid,
									haltOnChildActivity,
									clickCrudButtons));
							break;
						}
						//No break;
					default:
						oResponse = CrudButtonEnum.crudButtonClick(activityDefRes, crudButton);
						break;
				}
			} else {
				oResponse = ActivityDefinitionUtils.validateField(activityDefRes, getTable(), getField(), false);
			}
		} catch (Throwable ex) {
/*			System.err.println("WARNING - Error attempting to create request for activity" + getCalledActivityName() + "> field <" + getTableField() + "> in procedure at Step <"
					+ (isAuto() ? "(AUTO)>" : (1 + procedure.indexOf(this)) + ">") + ":\r\n" + ex.getMessage());*/
			getLabel().add("WARNING - Error attempting to create request: \"" + ex.getMessage() + "\"");
		}
		if (oResponse != null) {
			validationResponseHandling(activityDefRes, this, oResponse);
		} else {
			setIsComplete(true);
		}
	}
	

	private static void stepTrace(final Step step, final List<Step> procedure, final AssertionError ass) {
		if (mlDebugTrace) {
			step.toString();
		}
/*		String[] miniStrings = prettyPrintProcedureMini(procedure);
		final int history = 5;
		String[] stepHistory = Arrays.copyOfRange(miniStrings,
				procedure.size() - history > 0 ? procedure.size() - history : 0,
				procedure.size() >= history ? procedure.size() : history - procedure.size());
		for (String stepTrace : stepHistory) {
			if (stepTrace != null) {
				System.err.println(stepTrace);
			}
		}*/
		fail("Error from validation for activity <" + step.getCalledActivityName() + "> field <" + step.getTableField() + "> in procedure at Step <"
				+ (step.isAuto() ? "(AUTO)>" : (1 + procedure.indexOf(step)) + ">") + ":\r\n" + ass.getMessage());
	}
	
	/**
	 * @param activityDefRes
	 * @param step
	 * @param oResponse
	 */
	private static void validationResponseHandling(ActivityDefinitionResponseCascader activityDefRes, final Step step,
			PresentationMessage oResponse) {
		if (oResponse.isEmpty()) {
			step.getLabel().add("Validation was not required");
		}
		step.setCalledActivityDefinition(activityDefRes);
		step.setResponse(oResponse);
		validateStepResponse(step);
	}

	/**
	 * @param childActivityDefList
	 * @param activityDefRes
	 * @param step
	 * @return
	 */
	private ActivityDefinitionResponseCascader childHandling(
			List<ChildActivityDefinition> childActivityDefList,
			ActivityDefinitionResponseCascader activityDefRes,
			List<Step> procedure,
			boolean haltOnChildActivity, boolean clickCrudButtons) {

		ChildActivityDefinition child;
		LoadActivityRequest loadActivityRequest;
		try {
			child = ChildActivityDefinition.newInstance(activityDefRes, getTable(), getField());
			loadActivityRequest = child.getLastLoadActivityRequest();
			setContractType(CtrlPropEnum.LAUNCHCONTRACT);
		} catch(NullPointerException ex) { // Added due to bad passThroughActivity ctrlprops on server responses
			setResponse(new PresentationMessage());
			this.getLabel().add(ex.getMessage());
			return activityDefRes; // Creating a child was a dud (Server error), carry on and better luck next time.
		}
		
		// Captures special LoadDataMode/CrudButton overrides 
		if (CrudButtonEnum.isCrudButton(getTableField())) {
			CrudButtonEnum crudButton = CrudButtonEnum.getCrudButton(getTableField());
			for (LoadDataModeEnum enumValue : LoadDataModeEnum.values()) {
				if (enumValue.toString().equals(crudButton.toString())) {
					loadActivityRequest.setParam("loadDataMode", enumValue.getValue());
					break;
				}
			}
		}
		
		LoadDataRequest loadDataRequest = null; 
		if (child.getLastLoadDataRequest() != null) {
			loadDataRequest = child.getLastLoadDataRequest();
		}
		
		// If it has a reference, it found a pre-existing activity through its hierarchy and copied that instead of making a new child
		// That child should be handled externally
		if (child.getActivityReference() != null) {
			activityDefRes =  ActivityDefinitionResponseCascader.getInstance(child.getActivityReference());
		}else {
			childActivityDefList.add(child);
			activityDefRes = child;
		}
		
		StringBuilder strBldr = new StringBuilder();
		try {
			if (activityDefRes.getModel() == null || !child.getService().equals(activityDefRes.getService())) {
				if (!child.getService().equals(activityDefRes.getService())) {
					activityDefRes.setService(child.getService());
					strBldr.append("Replaced \"" + activityDefRes.getModel().getActivity() + "\"");
				}
				AppServerAdapter.getInstance().loadActivityDef(activityDefRes, loadActivityRequest);
				setResponse(activityDefRes.getModel());
				if (strBldr.toString() != null && !strBldr.toString().isEmpty()) {
					strBldr.append(", launched");
				}else {
					strBldr.append("Launched");
				}
			}
			if (loadDataRequest != null) {
				// Handles dashboards being loaded from buttonclicks
				if (ActivityTypeEnum.DASHBOARD == ActivityTypeEnum
						.getActivityType(activityDefRes.getModel().getActivityType())) {
					childActivityDefList.addAll(dashboardHandling(
							activityDefRes,
							procedure,
							loadDataRequest.getLoadRowid(),
							haltOnChildActivity, clickCrudButtons));
				}
				
				LoadDataResponse loadDataResponse = AppServerAdapter.getInstance().loadData(activityDefRes, loadDataRequest);
				setResponse(loadDataResponse);
				if (strBldr.toString() != null && !strBldr.toString().trim().isEmpty()) {
					strBldr.append(" and loaded");
				}else {
					strBldr.append("Loaded");
				}
			}
		} catch (Throwable ex) {
			if (getValidationType() != ValidationTypeEnum.IGNORE_ERRORS) {
				ex.printStackTrace();
				fail(ex.getMessage());
			}
		}
		
		getLabel().add(strBldr + " activity \"" + activityDefRes.getModel().getActivity() + "\"");
		return activityDefRes;
	}

	private List<ChildActivityDefinition> dashboardHandling(
			ActivityDefinitionResponseCascader activityDefRes,
			List<Step> procedure,
			final String maintRowid,
			boolean haltOnChildActivity,
			boolean clickCrudButtons) {
		
		List<ChildActivityDefinition> childActivityDefList = new ArrayList<ChildActivityDefinition>();
		
		Set<ChildActivityDefinition> childActivities = Dashboard.explodeDashboard(activityDefRes, maintRowid);
		
		childActivityDefList.addAll(childActivities);
		
		getLabel().add("Dashboard launched and loaded: " + activitiesToNameString(childActivities));
		
		for (ChildActivityDefinition childActivityDef : childActivities) {
			
			List<Step> dashSteps = nextTableFieldStepListLaunch(getValidationType(), childActivityDef);
			childActivityDef.getNextTableFields().clear();
			
			childActivityDefList.addAll(executeProcedure(childActivityDef, dashSteps, haltOnChildActivity, clickCrudButtons));
			
			procedure.addAll(1 + procedure.indexOf(this), dashSteps);
			this.subSteps.addAll(dashSteps);
		}
		
		return childActivityDefList;
		
	}

	/**
	 * @param procedure
	 * @param activityDefRes
	 */
	private static List<ChildActivityDefinition> insertNextTableFieldsIntoProcedure(final List<Step> procedure,
			ActivityDefinitionResponseCascader activityDefRes,
			boolean haltOnChildActivity, boolean clickCrudButtons) {
		
		List<ChildActivityDefinition> childActivityDefList = new ArrayList<ChildActivityDefinition>();
		
		if (activityDefRes.getNextTableFields().size() >= 1) {
			System.err.println("WARNING: ActivityDefinition was entered with pre-existing nextTableFields");
			List<Step> emergencyProcedure = Step.nextTableFieldStepListLaunch(ValidationTypeEnum.VALIDATE_ALL, activityDefRes);
			activityDefRes.getNextTableFields().clear();
			
			childActivityDefList.addAll(executeProcedure(activityDefRes, emergencyProcedure, haltOnChildActivity, clickCrudButtons));
			
			for (int i = 0 ; i < procedure.size() ; i++) {
				Step step = procedure.get(i);
				if (!step.isComplete()) {
					procedure.addAll(i, emergencyProcedure);
					break;
				}else if (i == procedure.size() - 1) {
					procedure.addAll(emergencyProcedure);
					break;
				}
			}
		}
		
		return childActivityDefList; 
	}
	
	public List<ChildActivityDefinition> cascade(ActivityDefinitionResponseCascader activityDefRes, final List<Step> procedure, boolean haltOnChildActivity, boolean clickCrudButtons) {
		List<ChildActivityDefinition> childActivityDefList = new ArrayList<ChildActivityDefinition>();
		
		List<Step> subProcedure = nextTableFieldStepListLaunch(getValidationType(), activityDefRes);
		activityDefRes.getNextTableFields().clear();
		
		List<Step> validate = new ArrayList<Step>();
		for (Step subStep : subProcedure) {
			if (!subStep.isLaunch()) {
				validate.add(subStep);
			}
		}
		childActivityDefList.addAll(executeProcedure(activityDefRes, validate, haltOnChildActivity, clickCrudButtons));
		
		List<Step> launch = new ArrayList<Step>();
		for (Step subStep : subProcedure) {
			if (subStep.isLaunch()) {
				launch.add(subStep);
				childActivityDefList.addAll(executeProcedure(activityDefRes, launch, haltOnChildActivity, clickCrudButtons));
			}
		}
		subProcedure = validate;
		subProcedure.addAll(launch);
		
		procedure.addAll(1 + procedure.indexOf(this), subProcedure); 
		this.subSteps.addAll(subProcedure);

		return childActivityDefList;
	}
	
	public static String activitiesToNameString(Iterable<? extends ActivityDefinition> activities) {
		final StringBuilder strbldr = new StringBuilder();
		activities.forEach((activity) ->  strbldr.append(activity.getModel().getActivity() + ", "));
		strbldr.replace(strbldr.length() - 2, strbldr.length(), ""); // remove last comma and space
		return strbldr.toString();
	}
	
	public static void validateStepResponse(Step step) {
		ValidationTypeEnum.validateResponse(step.getValidationType(), step.getResponse(), step.getTable(), step.getField());
	}
	
	/**
	 * Creates a new List of Step from
	 * <code>ActivityDefinitionResponseCascader</code>'s
	 * {@link fluid.test.data.ActivityDefinitionResponseCascader#getNextTableFields()
	 * nextTableFields} value and sets each generated step as
	 * {@link #isAuto() auto}
	 * 
	 * @see ActivityDefinitionResponseCascader
	 * @param activityDefRes
	 * @return List of Step
	 */
	public static List<Step> nextTableFieldStepList(ValidationTypeEnum validationType, List<String[]> nextTableFields) {
		List<Step> procedure = new ArrayList<Step>();
		for (String[] nextTableField : nextTableFields) {
			Step autoStep = new Step(validationType, nextTableField[0], nextTableField[1]);
			autoStep.setAuto(true);
			procedure.add(autoStep);
		}
		return procedure;
	}
	
	/**
	 * Similar to
	 * {@link #nextTableFieldStepList(ValidationTypeEnum, ActivityDefinitionResponseCascader)}
	 * but if a value in the nextTableField list has a {@link CtrlPropEnum
	 * LAUNCHCONTRACT} assigned to it in the ActivityDefinition, it will set the
	 * generated Step generated from this to {@link #setLaunch(boolean) launch}.
	 * 
	 * @param activityDefRes
	 * @return List of Step
	 */
	public static List<Step> nextTableFieldStepListLaunch(ValidationTypeEnum validationType, ActivityDefinitionResponseCascader activityDefRes) {
		List<Step> procedure = new ArrayList<Step>();
		for (String[] nextTableField : activityDefRes.getNextTableFields()) {
			Step autoStep = new Step(validationType, nextTableField[0], nextTableField[1]);
			autoStep.setAuto(true);
			if (activityDefRes.getModel().getCtrlProp(autoStep.getTableField(), CtrlPropEnum.BUTTONACTIVITY) != null
					|| activityDefRes.getModel().getCtrlProp(autoStep.getTableField(), CtrlPropEnum.LOOKUPFINDER) != null) {
				autoStep.setLaunch(true);
			}
			procedure.add(autoStep);
		}
		return procedure;
	}
	
	/**
	 * Asserts the results of
	 * {@link #executeProcedure(ActivityDefinition, List, boolean, boolean, boolean)
	 * executeProcedure} for any unhandled <code>ChildActivityDefinition</code>
	 * returns and any incomplete steps. Also has a neat debug procedure printout when using mlDebugTrace.
	 *
	 * 
	 * @param childActDefList
	 *            result of
	 *            {@link #executeProcedure(ActivityDefinition, List, boolean, boolean)
	 *            executeProcedure}
	 * @param procedure
	 *            List passed to
	 *            {@link #executeProcedure(ActivityDefinition, List, boolean, boolean)
	 *            executeProcedure}
	 */
	public static void assertProcedure(List<ChildActivityDefinition> childActDefList, List<Step> procedure) {
		
		List<String> errorList = new ArrayList<String>(); 
		for (int i = 0 ; i < procedure.size() ; i++) {
			if (!procedure.get(i).isComplete()) {
				String error = String.valueOf(1 + i) + ((procedure.get(i).isAuto()) ? (" (AUTO)") : (""));
				errorList.add(error);
			}
		}
		
		if (mlDebugTrace) {
			for (String stepString : prettyPrintProcedure(procedure)) {
				System.out.println(stepString);
			}
		}
		
		assertTrue("Unhandled ChildActivityDefinition(s) returned" + (errorList.isEmpty() ? ":" : " at step <" + errorList.get(0) + ">") + "\r\n" + (childActDefList.isEmpty() ? null : childActDefList.get(0).toString()), childActDefList.isEmpty());
		assertTrue("Not all steps were completed, steps: " + errorList.toString() + " were not complete (" + errorList.size() + " total)", errorList.isEmpty());
	}
	
	/**
	 * Converts the parsed list of steps to their respective toString representation
	 * but presented in such a way that each comma separated value is spaced to
	 * match whichever step has a longer respective length.
	 * 
	 * @param procedure
	 *            List of Steps to stringify.
	 * @return String array containing each individual step toString representation
	 *         but appropriately spaced.
	 */
	public static String[] prettyPrintProcedure(List<Step> procedure) {
		String[] foo = new String[procedure.size()];
		for (int i = 0 ; i < procedure.size() ; i++) {
			foo[i] = procedure.get(i).toString();
		}
		return ApiUtils.prettyPrintStrings(foo, "\t");
	}
	
	public static String[] prettyPrintProcedureMini(List<Step> procedure) {
		String[] foo = new String[procedure.size()];
		for (int i = 0 ; i < procedure.size() ; i++) {
			foo[i] = (1 + i) + procedure.get(i).toStringMini();
		}
		return ApiUtils.prettyPrintStrings(foo, "\t");
	}
	
	public static void assertExpectedChildren(Collection<ChildActivityDefinition> childActivities, String expectedService, int expectedNumberOfChildren) {
		assertExpectedChildren(childActivities, new String[] {expectedService}, expectedNumberOfChildren);
	}

	public static void assertExpectedChildren(Collection<ChildActivityDefinition> childActivities, String expectedService, String expectedVariant, int expectedNumberOfChildren) {
		assertExpectedChildren(childActivities, new String[] {expectedService}, new String[] {expectedVariant}, expectedNumberOfChildren);
	}
	
	public static void assertExpectedChildren(Collection<ChildActivityDefinition> childActivities, String[] expectedServices, int expectedNumberOfChildren) {
		assertExpectedChildren(childActivities, expectedServices, new String[0], expectedNumberOfChildren);
	}
	
	public static void assertExpectedChildren(Collection<ChildActivityDefinition> childActivities, String[] expectedServices, String[] expectedVariants, int expectedNumberOfChildren) {
		StringBuilder errors = new StringBuilder();
		
		if (childActivities.size() != expectedNumberOfChildren) {
			if (childActivities.size() > expectedNumberOfChildren) {
				errors.append("\r\nMore ");
			}else if (childActivities.size() < expectedNumberOfChildren) {
				errors.append("\r\nLess ");
			}
			errors.append("than the expected amount of ChildActivityDefinitions were returned: Expected <" + expectedNumberOfChildren + "> Actual<" + childActivities.size() + ">");
		}
		
		for (String service : expectedServices) {
			if (service != null) {
				boolean exists = false; 
				for (ChildActivityDefinition child : childActivities) {
					if (service.equals(child.getService())) {
						exists = true;
					}
				}
				if (!exists) errors.append("\r\nService was not found in the child collection <" + service + ">");
			}
		}
		for (String variant : expectedVariants) {
			if (variant != null) {
				boolean exists = false;
				for (ChildActivityDefinition child : childActivities) {
					if (child.getModel() != null && variant.equals(child.getModel().getActivity())) {
						exists = true;
					}
				}
				if (!exists) errors.append("\r\nVariant was not found in the child collection <" + variant + ">"); 
			}
		}
		
		String error = errors.toString();
		if (error != null && !error.isEmpty()) {
			fail("Errors returned from validation:\r\n"+ error);
		}
	}
	
	/**
	 * Clones a List of Step, implementing the method {@link #cloneStep()}
	 * 
	 * 
	 * @param procedure
	 *             List of Step to clone
	 * @return new List of new Steps
	 */
	public static List<Step> cloneProcedure(List<Step> procedure) {
		List<Step> clonedProcedure = new ArrayList<Step>();
		for (Step step: procedure) {
			clonedProcedure.add(step.cloneStep());
		}
		return clonedProcedure;
	}
	
	/**
	 * Creates a new List from the passed Step List where any Step members that have
	 * {@link #isAuto()} set to true are excluded. The original List will still
	 * contain all its auto members, however the steps in the new stripped List
	 * still reference the non-auto members in the original list, use this method in
	 * conjunction with {@link #cloneProcedure(List)} for a fresh List of new Steps.
	 * 
	 * @param procedure
	 *            Step List to strip
	 * @return New List of Step without any {@link #isAuto()} members
	 */
	public static List<Step> stripAutoStep(List<Step> procedure){
		List<Step> newProcedure = new ArrayList<Step>();
		for (Step step: procedure) {
			if (!step.isAuto()) {
				newProcedure.add(step);
			}
		}
		return newProcedure;
	}
	
	/**
	 * Transforms a list of <code>ChildActivityDefinition</code> to a HashMap with
	 * the key being the IpService of the respective ChildActivityDefinition
	 * 
	 * @param childActivityDefIterable
	 * @return HashMap of ChildActivityDefinitions with their IpService string as
	 *         key
	 */
	public static Map<String, ChildActivityDefinition> childListToServiceMap(Iterable<ChildActivityDefinition> childActivityDefIterable) {
		Map<String, ChildActivityDefinition> map = new HashMap<String, ChildActivityDefinition>();
		for (ChildActivityDefinition child : childActivityDefIterable) {
			map.put(child.getService(), child);
		}
		return map;
	}
	
	@Override
	public String toString() {
		String description = "{\"isComplete\":" + this.complete + ",\t\"validationType\":" + this.validationType
				+ ",\t\"contractType\":" + this.contractType + ",\t\"isAuto\":" + this.auto
				+ ",\t\"recordIndex\":" + this.recordIndex + ",\t\"activity\":\"" + this.calledActivityName + "\",\t"
				+ (this.table != null && this.field != null 
					? "\"" + getTableField() + "\"" 
					: "")
				+ (this.valueAssigned
					? (this.value instanceof String 
						? ":\"" + this.value + "\"" 
						: ":" + this.value)
					: "")
				+ "}\t " + (!this.label.isEmpty() 
					? this.label.toString() 
					: "");
		return description;
	}
	
	public String toStringMini() {
		return "{\"validationType\":" + this.validationType + ",\t\"activity\":\"" + this.calledActivityName + "\",\t"
				+ (this.table != null && this.field != null ? "\"" + getTableField() + "\"" : "")
				+ (this.valueAssigned
						? (this.value instanceof String
								? ":\"" + this.value + "\""
								: ":" + this.value)
						: "")
				+ "}\t " + (!this.label.isEmpty() ? this.label.toString() : "");
	}
	
}

