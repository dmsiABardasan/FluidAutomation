package fluid.test.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import auto.utils.fluid.FluidEvent;
import fluid.test.message.CtrlProp;
import fluid.test.message.PresentationMessage;

/**
 * <p>
 * The <code>ActivityDefinitionResponseCascader</code> class extends
 * <code>ActivityDefinition</code> to override two methods that previously
 * returned the <code>PresentationMessage</code> response for only the initial
 * request and none for any subsequent requests that could be created based on
 * the content of the initial response. This class prevents subsequent requests
 * from being sent but records them so that the initial response is kept, of
 * which the recorded requests can be independently sent and have their
 * responses returned.
 * </p>
 * 
 * <p>
 * While this can be manually implemented, this class provides methods to assist
 * in execution of requests and collection of responses.
 * </p>
 * 
 * @author vercin
 */
public class ActivityDefinitionResponseCascader extends ActivityDefinition {
	
	private List<String[]> nextTableFields = new ArrayList<String[]>();
	private List<PresentationMessage> asyncValidations = new ArrayList<PresentationMessage>(); 
	private ActivityDefinition activityReference;
	private boolean runOnce = false;
	
	/**
	 * <p>
	 * To be used as a normal ActivityDefinition but with access to the provided
	 * methods
	 * </p>
	 * 
	 * @see fluid.test.data.ActivityDefinition#ActivityDefinition()
	 */
	public ActivityDefinitionResponseCascader(){
		super();
	}
	
	/**
	 * Checks if the passed ActivityDefintiino is already an instance of
	 * ActivityDefinitionResponseCascader, if so, returns the param with no
	 * operation, otherwise:
	 * 
	 * Creates a new ActivityDefinitionResponseCascader and runs
	 * {@link #pointToActivity(ActivityDefinition)} with the passed
	 * ActivityDefinition.
	 * 
	 * @param activityDef
	 *            ActivityDefinition to convert to
	 *            ActivityDefinitionResponseCascader
	 */
	public static ActivityDefinitionResponseCascader getInstance(ActivityDefinition activityDef) {
		ActivityDefinitionResponseCascader activityDefRes;
		if (activityDef instanceof ActivityDefinitionResponseCascader) {
			activityDefRes = (ActivityDefinitionResponseCascader) activityDef;
		}else {
			activityDefRes = new ActivityDefinitionResponseCascader();
			activityDefRes.pointToActivity(activityDef);
		}
		return activityDefRes;
	}
	
	/**
	 * Gets the top most value in a recursive stack for the field
	 * 'activityReference', or itself if there isn't any references
	 * 
	 * @throws StackOverflowError if you have an infinite recursive reference
	 * @return ActivityDefinition
	 */
	public ActivityDefinition getOriginalReference() {
		ActivityDefinition origRef = getActivityReference();
		if (origRef != null && origRef instanceof ActivityDefinitionResponseCascader) {
			origRef = ((ActivityDefinitionResponseCascader) origRef).getOriginalReference();
		}
		return origRef != null ? origRef : this ;
	}
	
	public Stack<ActivityDefinition> getReferenceStack() {
		Stack<ActivityDefinition> stack = new Stack<ActivityDefinition>();
		getReferenceStack(stack);
		return stack;
	}
	
	private ActivityDefinition getReferenceStack(Stack<ActivityDefinition> stack) {
		ActivityDefinition origRef = getActivityReference();
		if (origRef != null && origRef instanceof ActivityDefinitionResponseCascader) {
			origRef = ((ActivityDefinitionResponseCascader) origRef).getReferenceStack(stack);
		}
		return stack.push(origRef != null ? origRef : this);
	}
	
	/**
	 * <p>
	 * Assigns the passed <code>ActivityDefinition</code> values: service, model,
	 * dataSourceDefinition, resultsDataSourceDefinition and messageKeyPin to a this
	 * <code>ActivityDefinitionResponseCascader</code>.
	 * </p>
	 * <p>
	 * Be aware and utilise that the above listed objects (Excluding service and
	 * messageKeyPin) have had their references passed by value and any changes to
	 * this new object will subsequently mutate the passed
	 * <code>ActivityDefinition</code>.
	 * </p>
	 * 
	 * The reference to the original ActivityDefinition is held
	 * {@link #getActivityReference() here}
	 * 
	 * @param activityDef
	 *            <code>ActivityDefinition</code> to convert to
	 *            <code>ActivityDefinitionResponseCascader</code>
	 */
	void pointToActivity(ActivityDefinition activityDef) {
		this.mlDebugTrace = activityDef.mlDebugTrace;
		
		setActivityReference(activityDef);
		
		setStopOnWarning(activityDef.isStopOnWarnining());
		setValidateOnly(activityDef.isValidateOnly());
		setRepeatButtons(activityDef.getRepeatButtons());
		setClosed(activityDef.isClosed());
		setLastLoadActivityRequest(activityDef.getLastLoadActivityRequest());
		setLastLoadDataRequest(activityDef.getLastLoadDataRequest());
		setLastLoadDataResponse(activityDef.getLastLoadDataResponse());
		setChildSet(activityDef.getChildSet());
		setService(activityDef.getService());
		setModel(activityDef.getModel());
		setDataSourceDefinitionMap(activityDef.getDataSourceDefinitionMap());
		setResultsDataSourceDefinition(activityDef.getResultsDataSourceDefinition());
		setGenSearchDataSourceDefinition(activityDef.getGenSearchDataSourceDefinition());
		setAdvSearchDataSourceDefinition(activityDef.getAdvSearchDataSourceDefinition());
		setMessageKeyPin(activityDef.getMessageKeyPin());
		setOperatorID(activityDef.getOperatorID());
		setPin(activityDef.getPin());
		
		if (activityDef instanceof ActivityDefinitionResponseCascader) {
			setNextTableFields(((ActivityDefinitionResponseCascader) activityDef).getNextTableFields());
		}
	}
	
	/**
	 * @return ActivityDefinition that was used to convert to this
	 *         ActivityDefinitionResponseCascader, or null if there was no
	 *         conversion.
	 */
	public ActivityDefinition getActivityReference() {
		return this.activityReference;
	}
	
	private void setActivityReference(ActivityDefinition activityReference) {
		this.activityReference = activityReference;
	}
	
	public List<PresentationMessage> getAsyncValidations() {
		return this.asyncValidations;
	}

	public void setAsyncValidations(List<PresentationMessage> asyncValidations) {
		this.asyncValidations = asyncValidations;
	}
	
	/**
	 * <p>
	 * Using the overridden {@link #processResponse(PresentationMessage)
	 * processResponse} or {@link #validateField(String, String) validateField}
	 * methods will validate/process a response, in which more requests can
	 * potentially be made - specifically any ctrlProps with type
	 * {@link fluid.test.message.CtrlPropEnum#BUTTONCLICK BUTTONCLICK}. This List
	 * retains those potential requests as a list.
	 * </p>
	 * 
	 * <p>
	 * Use caution if you're planning on iterating this list over
	 * {@link #validateField(String, String)} as this list may mutate in the
	 * process, causing a {@link java.util.ConcurrentModificationException
	 * ConcurrentModificationException}. Either use an indexed loop, use the
	 * provided static methods or see the implementation of how those methods work.
	 * 
	 * You may need to implicitly clear this list to stop duplication.
	 * </p>
	 * 
	 * @see #nextTableFieldCascadeSink()
	 * @see #nextTableFieldCascadeDive()
	 * @see #cascadeSink(ActivityDefinition, String, String)
	 * @see #cascadeDive(ActivityDefinition, String, String)
	 * @return List of String arrays that contain two values - one representing the
	 *         table name and the other the field name
	 */
	public List<String[]> getNextTableFields() {
		return this.nextTableFields;
	}
	
	public void setNextTableFields(List<String[]> nextTableFields) {
		this.nextTableFields = nextTableFields;
	}

	/**
	 * @return <code>boolean</code> indicating that
	 *         {@link fluid.test.data.ActivityDefinition#processResponse(PresentationMessage)}
	 *         has run only once (true) or not (false)
	 */
	private boolean isRunOnce() {
		return this.runOnce;
	}

	/**
	 * @param runOnce
	 *            <code>boolean</code> indicating that
	 *            {@link #processResponse(PresentationMessage)}
	 *            has run only once (true) or not (false)
	 */
	private void setRunOnce(boolean runOnce) {
		this.runOnce = runOnce;
	}
	
	@Override
	protected void applyClick(List<CtrlProp> buttonClickEventForList) throws Throwable {
		setRunOnce(true);
		try {
			super.applyClick(buttonClickEventForList);
		} finally {
			setRunOnce(false);
		}
	}
	
	@Override
	public PresentationMessage validateField(String tableName, String fieldName, FluidEvent fluidEvent) throws Throwable {
		
		PresentationMessage oResponse = null;
		
		if (isRunOnce()) {
			String[] tableField = { tableName, fieldName };
			getNextTableFields().add(tableField);
		} else {
			oResponse = super.validateField(tableName, fieldName, fluidEvent);
		}
		
		return oResponse;
	}
	
	@Override
	public List<PresentationMessage> validateAsynchronously(String[][] tableFields) throws Throwable {
		List<PresentationMessage> oResponses = super.validateAsynchronously(tableFields);
		this.asyncValidations = oResponses;
		return oResponses;
	}
	
	/**
	 * Instance version of {@link #cascadeSink(ActivityDefinition, String, String)}
	 * that utilises the local {@link #getNextTableFields()} List
	 * 
	 * @return List of PresentationMessage
	 * @throws Throwable
	 */
	public List<PresentationMessage> nextTableFieldCascadeSink() throws Throwable {
		List<PresentationMessage> oResponses = new ArrayList<PresentationMessage>();
		for (String[] nextTableField : getNextTableFields()) {
			oResponses = ActivityDefinitionResponseCascader.cascadeSink(this, nextTableField[0], nextTableField[1]);
		}
		
		return oResponses;
	}

	/**
	 * Instance version of {@link #cascadeDive(ActivityDefinition, String, String)}
	 * that utilises the local {@link #getNextTableFields()} List
	 * 
	 * @return List of PresentationMessage
	 * @throws Throwable
	 */
	public List<PresentationMessage> nextTableFieldCascadeDive() throws Throwable {
		List<PresentationMessage> oResponses = new ArrayList<PresentationMessage>();
		for (String[] nextTableField : getNextTableFields()) {
			oResponses = ActivityDefinitionResponseCascader.cascadeDive(this, nextTableField[0], nextTableField[1]);
		}
		
		return oResponses;
	}
	
	/**
	 * <p>
	 * This attempts validation of the table and field String parameters to the
	 * passed activityDef with an execution of "<i>Read-first - Process-First</i>"
	 * to ctrlProps on the response. Where if a response contains multiple calls to
	 * new requests, it will process the first (ordinal) request and anything in the
	 * response to that (and so on) before addressing the next request on the
	 * initial response(s).
	 * </p>
	 * 
	 * <p>
	 * The following image displays the execution order (1 to 6) where each
	 * singularity (joining/base of line(s)) is the processing of a response to a
	 * request:
	 * </p>
	 * <img alt="Order of processing requests" src="./doc-files/cascadeDive.png">
	 * 
	 * <p>
	 * To do this, this method recursively calls itself while collecting responses
	 * until there are no more requests.
	 * </p>
	 * 
	 * @see #cascadeSink(ActivityDefinition, String, String)
	 * @param activityDef
	 *            <code>ActivityDefinition</code> to validate against
	 * @param tableName
	 *            <code>String</code> of the table to validate
	 * @param fieldName
	 *            <code>String</code> of the field to validate
	 * @return <code>List</code> of <code>PresentationMessage</code> in the order
	 *         the requests were executed
	 * @throws Throwable
	 */
	public static List<PresentationMessage> cascadeDive(ActivityDefinition activityDef, String tableName,
			String fieldName) throws Throwable {
		
		ActivityDefinitionResponseCascader activityDefRes = ActivityDefinitionResponseCascader.getInstance(activityDef);
		activityDefRes.setNextTableFields(new ArrayList<String[]>());
		List<PresentationMessage> responseList = new ArrayList<PresentationMessage>();
		
		responseList.add(activityDefRes.validateField(tableName, fieldName));
		
		for (String[] nextTableField : activityDefRes.getNextTableFields()) {
			// System.out.println(nextTableField[0] + " "+ nextTableField[1]);
			responseList.addAll(cascadeDive(activityDef, nextTableField[0], nextTableField[1]));
		}
		
		return responseList;
	}
	
	/**
	 * <p>
	 * This attempts validation of the table and field String parameters to the
	 * passed activityDef with an execution of "<i>Read-All - Process-Later</i>" to
	 * ctrlProps on the response. Where if a response contains multiple calls to new
	 * requests, it will process each request (in order) in the initial response
	 * before processing the response of the subsequent requests.
	 * </p>
	 * 
	 * <p>
	 * The following image displays the execution order (1 to 6) where each
	 * singularity (joining/base of line(s)) is the processing of a response to a
	 * request:
	 * </p>
	 * <img alt="Order of processing requests" src="./doc-files/cascadeSink.png">
	 * 
	 * <p>
	 * To do this, this <i>adds</i> requests to the end of
	 * <code>ActivityDefinitionPresentationResponseCascader</code>
	 * {@link #getNextTableFields()} List concurrently while iterating.
	 * </p>
	 * 
	 * @see #cascadeDive(ActivityDefinition, String, String)
	 * @param activityDef
	 *            <code>ActivityDefinition</code> to validate against
	 * @param tableName
	 *            <code>String</code> of the table to validate
	 * @param fieldName
	 *            <code>String</code> of the field to validate
	 * @return <code>List</code> of <code>PresentationMessage</code> in the order
	 *         the requests were executed
	 * @throws Throwable
	 */
	public static List<PresentationMessage> cascadeSink(ActivityDefinition activityDef, String tableName,
			String fieldName) throws Throwable {
		
		ActivityDefinitionResponseCascader activityDefRes = ActivityDefinitionResponseCascader.getInstance(activityDef);
		activityDefRes.setNextTableFields(new ArrayList<String[]>());
		List<PresentationMessage> responseList = new ArrayList<PresentationMessage>();
		
		activityDefRes.getNextTableFields().add(new String[] { tableName, fieldName });
		
		for (int i = 0; i < activityDefRes.getNextTableFields().size(); i++) {
			
			String[] nextTableField = activityDefRes.getNextTableFields().get(i);
			// System.out.println(nextTableField[0] + " "+ nextTableField[1]);
			responseList.add(activityDefRes.validateField(nextTableField[0], nextTableField[1]));
		}
		
		return responseList;
	}
}
