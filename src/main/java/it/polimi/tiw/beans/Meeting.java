package it.polimi.tiw.beans;

import java.sql.Time;
import java.util.Date;

public class Meeting {
	private int idMeeting;
	private String title;
	private Date date;
	private Time time; //TODO capire se cambiare questo tipo per renderlo compatibile con Time di mysql
	private int duration;
	private int maxParticipant;
	
	public int getIdMeeting() {
		return idMeeting;
	}

	public void setIdMeeting(int idMeeting) {
		this.idMeeting = idMeeting;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Time getTime() {
		return time;
	}
	
	public void setTime(Time time) {
		this.time = time;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getMaxParticipant() {
		return maxParticipant;
	}
	
	public void setMaxParticipant(int maxParticipant) {
		this.maxParticipant = maxParticipant;
	}

}
