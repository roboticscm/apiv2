package vn.com.sky.sys.model;

public enum MessageType {
	SUBMIT("SUBMIT"),
	ASSIGN("ASSIGN"),
	HOLD("HOLD"),
	REMINDER1("REMINDER1"),
	REMINDER2("REMINDER2");
	
	
	private final String text;
	MessageType(final String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return text;
    }
}
