package fluid.test.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class ViewDefinition {
	
	private Map<String, Map<String, Object>> fields = new HashMap<>();
	private Map<String, Object> table = new HashMap<>();
	private Map<String, Object> definition = new HashMap<>();
	
	public ViewDefinition() {}
	
	public Map<String, Object> getFieldViewDefinition(String name){
		return this.fields.get(name);
	}
	public Map<String, Object> getTableView(){
		return this.table;
	}
	public Map<String, Object> getDefinition(){
		return this.definition;
	}
	
	public void populateViewDefinition(JsonObject table) {
		this.table = table;

		final String defWord = "def";
		final String fieldsWord = "fields";
		final String nameWord = "name";
		
		if (table.containsKey(defWord) && table.get(defWord) instanceof JsonObject) {
			JsonObject definition = (JsonObject) table.get(defWord);
			
			this.definition = definition;
			
			if (definition.containsKey(fieldsWord) && definition.get(fieldsWord) instanceof JsonArray) {
				JsonArray fields = (JsonArray) definition.get(fieldsWord);
				
				for (Object fieldDef : fields) {
					if (fieldDef instanceof JsonObject) {
						JsonObject fieldDefJsonObj = (JsonObject) fieldDef;
						if (fieldDefJsonObj.containsKey(nameWord)) {
							
							String name = Objects.toString(fieldDefJsonObj.get(nameWord), null);
							this.fields.put(name, fieldDefJsonObj);
						}
					}
				}
			}	
		}
	}
	
	public static void setViewDefinitions(Map<String, DataSourceDefinition> dsDefMap, JsonObject jsonObj) {
		if (jsonObj != null && !jsonObj.isEmpty()) {
			for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
				
				if (("def".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof JsonObject)
						&& (jsonObj.containsKey("tableName") && dsDefMap.containsKey(jsonObj.get("tableName")))) {
					
					DataSourceDefinition dsDef = dsDefMap.get(jsonObj.get("tableName"));
					dsDef.getViewDefinition().populateViewDefinition(jsonObj);
					
				} else {
					if (entry.getValue() instanceof JsonObject) {
						 setViewDefinitions(dsDefMap, (JsonObject) entry.getValue());
					} else if (entry.getValue() instanceof JsonArray) {
						JsonArray jsonArr = (JsonArray) entry.getValue();
						for (Object arrObj : jsonArr) {
							if (arrObj instanceof JsonObject) {
								setViewDefinitions(dsDefMap, (JsonObject) arrObj);
							}
						}
					}
				}
			}
		}
	}
}
