package com.rajaselvan.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by rajaselvan on 10/12/17.
 */

public class EventModel implements Serializable {

    private String eventId;
    private String eventSummary;
    private String eventDescription;
    private String eventLocation;
    private DateTime eventStartDateTime;
    private DateTime eventEndDateTime;
    private List<EventAttendee> eventAttendees;
    private Event.Reminders eventReminder;


    public EventModel(){
    }

    public EventModel(String eventSummary, String eventDescription, String eventLocation, DateTime eventStartDateTime, DateTime eventEndDateTime, List<EventAttendee> eventAttendees, Event.Reminders eventReminder) {
        this.eventSummary = eventSummary;
        this.eventDescription = eventDescription;
        this.eventLocation = eventLocation;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
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

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public DateTime getEventStartDateTime() {
        return eventStartDateTime;
    }

    public void setEventStartDateTime(DateTime eventStartDateTime) {
        this.eventStartDateTime = eventStartDateTime;
    }

    public DateTime getEventEndDateTime() {
        return eventEndDateTime;
    }

    public void setEventEndDateTime(DateTime eventEndDateTime) {
        this.eventEndDateTime = eventEndDateTime;
    }

    public List<EventAttendee> getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(List<EventAttendee> eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    public Event.Reminders getEventReminder() {
        return eventReminder;
    }

    public void setEventReminder(Event.Reminders eventReminder) {
        this.eventReminder = eventReminder;
    }
}
