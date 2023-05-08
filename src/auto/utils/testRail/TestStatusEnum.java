package auto.utils.testRail;

public enum TestStatusEnum {
	PASS		(1),
	BLOCKED		(2),
	UNTESTED	(3),
	RETEST		(4),
	FAIL		(5),
	CONFIRMED	(6),
	IN_PROGRESS	(7);
	
	private int id;
	
	private TestStatusEnum(int id) {
		this.id = id;
	}
	
	public int id() {
		return this.id;
	}
	
	public static TestStatusEnum getTestStatusEnum(int id) {
		
		TestStatusEnum testStatusEnum = null;
		
		for (TestStatusEnum testStatus: TestStatusEnum.values()) {
			if (testStatus.id() == id) {
				testStatusEnum = testStatus;
				break;
			}
		}
		if (testStatusEnum == null) {
			throw new IllegalArgumentException("Invalid value for Enum");
		}
		
		return testStatusEnum;
	
	}
}
