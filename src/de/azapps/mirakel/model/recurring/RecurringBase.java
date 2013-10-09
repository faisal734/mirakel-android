package de.azapps.mirakel.model.recurring;

import android.content.ContentValues;

public class RecurringBase {
	private String label;
	private int _id;
	private int minutes; 
	private int hours;
	private int days;
	private int months;
	private int years;
	private boolean forDue;
	
	public RecurringBase(int _id,String label, int minutes, int hours, int days, int months, int years, boolean forDue) {
		super();
		this.days=days;
		this.forDue=forDue;
		this.hours=hours;
		this.label=label;
		this.minutes=minutes;
		this.months=months;
		this.years=years;
		this.setId(_id);
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getYears() {
		return years;
	}
	public void setYears(int years) {
		this.years = years;
	}
	public boolean isForDue() {
		return forDue;
	}
	public void setForDue(boolean for_due) {
		this.forDue = for_due;
	}
	public int getMonths() {
		return months;
	}
	public void setMonths(int months) {
		this.months = months;
	}
	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}
	public int getHours() {
		return hours;
	}
	public void setHours(int hours) {
		this.hours = hours;
	}
	public int getMinutes() {
		return minutes;
	}
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	@Override
	public String toString() {
		return label;
	}

	public int getId() {
		return _id;
	}

	public void setId(int _id) {
		this._id = _id;
	}
	
	public ContentValues getContentValues(){
		ContentValues cv = new ContentValues();
		cv.put("_id", _id);
		cv.put("minutes", minutes);
		cv.put("hours", hours);
		cv.put("days", days);
		cv.put("months", months);
		cv.put("years", years);
		cv.put("for_due", forDue);
		cv.put("label", label);
		return cv;
	}

}
