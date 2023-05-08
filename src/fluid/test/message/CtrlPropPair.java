package fluid.test.message;

import java.util.Locale;

public class CtrlPropPair {
	private String name;
	private CtrlPropEnum prop;

	public CtrlPropPair(String name, CtrlPropEnum prop) {
		this.name = name;
		this.prop = prop;
	}

	public String getName() {
		return name;
	}

	public CtrlPropEnum getProp() {
		return prop;
	}

	@Override
	public String toString() {
		return this.name + " " + this.prop;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.toLowerCase(Locale.ROOT).hashCode());
		result = prime * result + ((prop == null) ? 0 : prop.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof CtrlPropPair) {
			CtrlPropPair ctrlPropPair = (CtrlPropPair) obj;
			if (((getName() == ctrlPropPair.getName()) 
						|| (getName() != null 
							&& ctrlPropPair.getName() != null
							&& getName().toLowerCase(Locale.ROOT).equals(ctrlPropPair.getName().toLowerCase(Locale.ROOT))))
					&& getProp() == ctrlPropPair.getProp()) {
				isEqual = true;
			}
		}
		return isEqual;
	}
}