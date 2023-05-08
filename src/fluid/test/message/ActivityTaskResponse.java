package fluid.test.message;

public class ActivityTaskResponse extends LoadDataResponse {
   
   public static final String JSON_KEY_SUBMIT_SUCCESS = "submittedSuccess";
   public static final String JSON_KEY_TASK_HAS_RUN = "taskHasRun";

   private boolean submittedSuccess = false;
   private boolean taskHasRun = false;

   public ActivityTaskResponse() {
      super(null);
   }

   public ActivityTaskResponse(String pcMessage) throws Throwable {
      super(pcMessage, null);

      submittedSuccess = getParamAsBoolean(JSON_KEY_SUBMIT_SUCCESS);
      taskHasRun = getParamAsBoolean(JSON_KEY_TASK_HAS_RUN);

      storeProperties();
   }

   @Override
   protected void storeProperties() {
      super.storeProperties();
      setParam(JSON_KEY_SUBMIT_SUCCESS, submittedSuccess);
      setParam(JSON_KEY_TASK_HAS_RUN, taskHasRun);
   }

   public boolean getSubmitSuccess() {
      return submittedSuccess;
   }

   public void setSubmitSuccess(boolean submittedSuccess) {
      this.submittedSuccess = submittedSuccess;
   }

   public boolean getTaskHasRun() {
      return taskHasRun;
   }

   public void setTaskHasRun(boolean taskHasRun) {
      this.taskHasRun = taskHasRun;
   }

}