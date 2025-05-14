package auto.utils.fluid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import auto.utils.ValidationTypeEnum;
import auto.utils.step.LazyProcedure;
import auto.utils.step.Step;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.ChildActivityDefinition;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonException;
import fluid.test.json.JsonObject;
import fluid.test.json.Jsoner;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.CtrlPropPair;

/**
 * <p>
 * Automation implementation of the Fluid notification feature. Provides a way
 * of accessing NOTIFY CtrlProp settings and their possible actions.
 * </p>
 * 
 * @see #getNotifications(ActivityDefinition)
 * @see NotificationAction
 * @author vercin
 */
public class Notification {

   private String ctrlValue; // The non-unique notification ctrlProp value
   private JsonObject settings; // All the other settings.
   private Map<String, NotificationAction> actions = new HashMap<>(); // The title of the action and its function

   public Notification(String ctrlValue) {
      this.ctrlValue = ctrlValue;
   }

   public String getCtrlValue() {
      return this.ctrlValue;
   }

   public JsonObject getSettings() {
      return this.settings;
   }

   private void setSettings(JsonObject settings) {
      this.settings = settings;
   }

   public Map<String, NotificationAction> getActions() {
      return this.actions;
   }


   /**
    * <p>A clickable action within a notification.</p>
    * @see #click(LazyProcedure)
    */
   @FunctionalInterface
   public static interface NotificationAction {
      /**
       * <p>
       * Actuate the defined action. This may include clicking a button in the activity
       * that created this notification, and/or may open a separate activity entirely.
       * <ul>
       * <li>Button clicks will be processed against the activity that created the
       * notification. The Step return is only for convenience for this.</li>
       * <li>Child ActivityDefinitions will be inserted into the passed
       * LazyProcedure's child list to be conventionally handled.</li>
       * </ul>
       * </p>
       * 
       * @param procedure LazyProcedure to process the button step and/or child
       *                  ActivityDefinition.
       * @return Step object of the button response (if applicable) for convenience.
       *         Can be null if no button was used in the action.
       */
      Step click(LazyProcedure procedure);
   }

   /**
    * <p>
    * Processes an ActivityDefinition for any NOTIFY CtrlProps, decodes them, and
    * returns an actionable set of {@link Notification} objects.
    * </p>
    * 
    * @param activityDef ActivityDefinition potentially containing NOTIFY CtrlProps
    *                    in the model.
    * @return Set of Notifications
    */
   public static Set<Notification> getNotifications(final ActivityDefinition activityDef) {
      
      Set<Notification> notifications = new HashSet<>();
      
      Set<CtrlPropPair> ctrlPropPairs = activityDef.getModel().getCtrlProperties().keySet();
      for (CtrlPropPair ctrlPropPair : ctrlPropPairs) {
         if (CtrlPropEnum.NOTIFY == ctrlPropPair.getProp()) {
            
            JsonObject ctrlPropName;
            try {
               ctrlPropName = (JsonObject) Jsoner.deserialize(ctrlPropPair.getName());
            } catch (JsonException ex) {
               ex.printStackTrace();
               continue;
            }
            
            Notification notification = new Notification(activityDef.getModel().getCtrlProperties().get(ctrlPropPair));
            
            notification.setSettings((JsonObject) ctrlPropName.get("settings"));
            
            JsonArray actions = (JsonArray) ctrlPropName.get("actions");
            if (actions != null) {
               
               for (Object actionObj : actions) {
                  
                  JsonObject action = (JsonObject) actionObj;
                  
                  final String launchActivity = (String) action.get("launchActivity");
                  final String launchContract = (String) action.get("launchContract");
                  final String buttonClick = (String) action.get("buttonClick");
                  
                  final String title = (String) action.get("title");
                  
                  notification.getActions().put(title, new Notification.NotificationAction() {
                     @Override
                     public Step click(LazyProcedure procedure) {
                        
                        Step step = null;
                        
                        if (buttonClick != null) {
                           boolean launch = (activityDef.getModel().getCtrlProp(buttonClick, CtrlPropEnum.BUTTONACTIVITY) != null
                                 || activityDef.getModel().getCtrlProp(buttonClick, CtrlPropEnum.LOOKUPFINDER) != null);
                           step = procedure.step(launch, activityDef, ValidationTypeEnum.VALIDATE_ALL, buttonClick);
                        }
                        
                        if (launchActivity != null) {
                           ChildActivityDefinition childActivityDefinition = ChildActivityDefinition.newInstance(activityDef);
                           childActivityDefinition.createLaunchContract(launchContract, launchActivity, false);
                           ChildActivityDefinition.loadChildActivity(childActivityDefinition);
                           
                           procedure.getChildList().add(childActivityDefinition);
                        }
                        
                        return step;
                     }
                  });
               }
            }
            notifications.add(notification);
         }
      }
      return notifications;
   }
}