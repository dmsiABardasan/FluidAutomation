package auto.utils.fluid;

public enum ActivityTypeEnum {
	DASHBOARD ("dash"),
	DASHBOARD_SECTION ("dashSection"),
	SEARCH_RESULTS_DETAILS ("srd"),
	HEADER_TABS_FOOTER ("htf"),
	SCHED ("sched"),
	FILE_UPLOAD ("fileUpdate"),
	URL_DISPLAY ("urlDisplay"),
	IMG_DISPLAY ("imgDisplay"),
	POPUP_WINDOW ("popupWindow"),
	POPUP_FINDER ("popupFinder"),
	POPUP_SLIDE("popupSlide"),
	INCLUDE("include");
	
	private String value;
	
	private ActivityTypeEnum(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public static boolean isActivityType(String type) {
		boolean exists = false;
		
		for (ActivityTypeEnum activityType: ActivityTypeEnum.values()) {
			if (activityType.getValue().equals(type)) {
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	public static ActivityTypeEnum getActivityType(String value) {
		ActivityTypeEnum foundActivityType = null;
		
		for (ActivityTypeEnum activityType: ActivityTypeEnum.values()) {
			if (activityType.getValue().equals(value)) {
				foundActivityType = activityType;
				break;
			}
		}
		if (foundActivityType == null) {
			throw new IllegalArgumentException("Invalid value for Enum");
		}
		
		return foundActivityType;
	}

}
