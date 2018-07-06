package application;
public class TimeEvent implements Comparable<TimeEvent>{
	private Integer date;
	private String event;
	
	public TimeEvent(int date, String event) {
		this.date = date;
		this.event = event;
	}
	
	public int getDate() {
		return this.date;
	}
	
	public String getEvent() {
		return this.event;
	}

	@Override
	/**
	 * Will be used to sort arrayList of TimeEvent objects according to date
	 */
	public int compareTo(TimeEvent o) {
		return date.compareTo(o.date);
	}
}
