package vn.com.sky.task.model;

public enum AssignPosition {
	ASSIGNER("ASSIGNER"),
	ASSIGNEE("ASSIGNEE"),
	EVALUATOR("EVALUATOR"),
	CHAR("CHAR"),
	TARGET_PERSON("TARGET_PERSON"),
	TARGET_TEAM("TARGET_TEAM");
	
	
	private final String text;
	AssignPosition(final String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return text;
    }
}