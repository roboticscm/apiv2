package vn.com.sky.sys.model;

public enum NotifyType {
	CHAT("CHAT"),
	FUNCTIONAL("FUNCTIONAL"),
	ALARM("ALARM");
	
	
	private final String text;
	NotifyType(final String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return text;
    }
}
