package auto.utils.coverage;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class FunctionBean extends Bean{
	private static final long serialVersionUID = 1L;
	private Set<String> values = new TreeSet<String>();
	private String ipFunction;
	private String tableField;
	private Boolean visible;
	private int timesCalled;
	
	public FunctionBean(String tableField, String ipFunction) {
		this.ipFunction = ipFunction;
		this.tableField = tableField;
	}
	
	public FunctionBean(String tableField, String ipFunction, boolean isVisible) {
		this.ipFunction = ipFunction;
		this.tableField = tableField;
		this.visible = isVisible;
	}
	
	@Override
	public String toString() {
		return this.ipFunction + "\t" + this.tableField/*+ "\t" + this.values.size() + "\t" + this.values*/;
	}
	
	public static int totalUniqueValues(Collection<FunctionBean> functionBeans) {
		int i = 0;
		for (FunctionBean functionBean : functionBeans) {
			i += functionBean.getValues().size();
		}
		return i;
	}
	
	public Set<String> getValues() {
		return values;
	}
	public String getIpFunction() {
		return ipFunction;
	}
	public String getTableField() {
		return tableField;
	}
	public Boolean isVisible() {
		return this.visible;
	}
	public int getTimesCalled() {
		return timesCalled;
	}
	public void setTimesCalled(int timesCalled) {
		this.timesCalled = timesCalled;
	}
	
	public static class FunctionBeanComparator implements Comparator<FunctionBean>, Serializable {
		private static final long serialVersionUID = 1L;
		@Override
		public int compare(FunctionBean o1, FunctionBean o2) {
			int comp = o1.getIpFunction().compareTo(o2.getIpFunction());
			if (comp == 0) {
				comp = o1.getTableField().compareTo(o2.getTableField());
			}
			return comp;
		}
	}
}