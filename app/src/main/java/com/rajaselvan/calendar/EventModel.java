package com.rajaselvan.calendar;

import java.util.List;

/**
 * Created by rajaselvan on 10/12/17.
 */

public class EventModel {
    private String eventId;
    private String eventSummary;
    private String eventDescription;
    private String eventStartDate;
    private String eventEndDate;
    private String eventStartTime;
    private String eventEndTime;
    private List<String> eventAttendees;
    private String eventReminder;


    public EventModel(String eventId, String eventSummary, String eventDescription, String eventStartDate, String eventEndDate, String eventStartTime, String eventEndTime, List<String> eventAttendees, String eventReminder) {
        this.eventId = eventId;
        this.eventSummary = eventSummary;
        this.eventDescription = eventDescription;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.eventAttendees = eventAttendees;
        this.eventReminder = eventReminder;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventSummary() {
        return eventSummary;
    }

    public void setEventSummary(String eventSummary) {
        this.eventSummary = eventSummary;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public List<String> getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(List<String> eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    public String getEventReminder() {
        return eventReminder;
    }

    public void setEventReminder(String eventReminder) {
        this.eventReminder = eventReminder;
    }

}
