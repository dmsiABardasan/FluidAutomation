package fluid.test.adapter;

public enum AppServerApiEnum {

   loadActivityDef(1), getResults(2), loadData(3), saveData(4), presenter(5), userLogin(6), submitActivityTask(9);

   private int value;

   private AppServerApiEnum(int api) {
      this.value = api;
   }

   public int getValue() {
      return value;
   }

   public static AppServerApiEnum GetEnum(String s) {
      if (loadActivityDef.name().equalsIgnoreCase(s)) {
         return loadActivityDef;
      } else if (getResults.name().equalsIgnoreCase(s)) {
         return getResults;
      } else if (loadData.name().equalsIgnoreCase(s)) {
         return loadData;
      } else if (saveData.name().equalsIgnoreCase(s)) {
         return saveData;
      } else if (presenter.name().equalsIgnoreCase(s)) {
         return presenter;
      } else if (userLogin.name().equalsIgnoreCase(s)) {
         return userLogin;
      } else if (submitActivityTask.name().equalsIgnoreCase(s)) {
         return submitActivityTask;
      }
      throw new IllegalArgumentException("No Enum specified for this string");
   }
}
