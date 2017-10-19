package com.rajaselvan.calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class EditListItemActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static TextView mTxtStartDate, mTxtEndDate;
    private static TextView mTxtStartTime, mTxtEndTime;
    private static EditText mETxtSummary, mETxtDescription, mETxtLocation, mETxtAttendees;
    String[] reminder = {"10", "20", "30"};
    private ArrayAdapter remainderAdapter;
    private GoogleAccountCredential mCredential;
    private Button mBtnUpdate;
    private Spinner selectRemainder;
    private Bundle extras;
    private String calendarId ="phll02ughcj3qk6v61pp9ok5k4@group.calendar.google.com";

    private DatePickerDialogFragment mDatePickerDialogFragment;
    private TimePickerDialogFragment mTimePickerDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list_item);
        selectRemainder = (Spinner) findViewById(R.id.sp_select_remainder);
        selectRemainder.setOnItemSelectedListener(this);
        remainderAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, reminder);
        remainderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectRemainder.setAdapter(remainderAdapter);
        extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getSerializable("summary") != null) {
                mETxtSummary = (EditText) findViewById(R.id.et_event_summary);
                mETxtSummary.setText(extras.getSerializable("summary").toString(), TextView.BufferType.EDITABLE);
            }
            if (extras.getSerializable("description") != null) {
                mETxtDescription = (EditText) findViewById(R.id.et_description);
                mETxtDescription.setText(extras.getSerializable("description").toString(), TextView.BufferType.EDITABLE);
            }
            if (extras.getSerializable("location") != null) {
                mETxtLocation = (EditText) findViewById(R.id.et_location_value);
                mETxtLocation.setText(extras.getSerializable("location").toString(), TextView.BufferType.EDITABLE);
            }
            if (extras.getSerializable("startDate") != null) {
                mTxtStartDate = (TextView) findViewById(R.id.new_event_tv_start_date_value);
                mTxtStartDate.setText(extras.getSerializable("startDate").toString());
            }
            if (extras.getSerializable("endDate") != null) {
                mTxtEndDate = (TextView) findViewById(R.id.new_event_tv_end_date_value);
                mTxtEndDate.setText(extras.getSerializable("endDate").toString());
            }
            if (extras.getSerializable("startTime") != null) {
                mTxtStartTime = (TextView) findViewById(R.id.new_event_tv_start_time_value);
                mTxtStartTime.setText(extras.getSerializable("startTime").toString());
            }
            if (extras.getSerializable("endTime") != null) {
                mTxtEndTime = (TextView) findViewById(R.id.new_event_tv_end_time_value);
                mTxtEndTime.setText(extras.getSerializable("endTime").toString());
            }
            if (extras.getSerializable("attendees") != null) {
                mETxtAttendees = (EditText) findViewById(R.id.et_invite_attendees);
                mETxtAttendees.setText(extras.getSerializable("attendees").toString(), TextView.BufferType.EDITABLE);
            }
            if (extras.getSerializable("remainders")!=null) {
                int spinnerPosition = remainderAdapter.getPosition(extras.getSerializable("remainders").toString().replace(";", "").trim());
                selectRemainder.setSelection(spinnerPosition);
            }
            mCredential = GoogleAccountSingleton.getInstance(getApplicationContext());
            mBtnUpdate = (Button) findViewById(R.id.btn_update);
            mTxtStartDate.setOnClickListener(this);
            mTxtEndDate.setOnClickListener(this);
            mTxtStartTime.setOnClickListener(this);
            mTxtEndTime.setOnClickListener(this);
            mBtnUpdate.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        mDatePickerDialogFragment = new DatePickerDialogFragment();
        mTimePickerDialogFragment = new TimePickerDialogFragment();
        if (id == R.id.new_event_tv_start_date_value) {
            mDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_START_DATE);
            mDatePickerDialogFragment.show(getSupportFragmentManager(), "datePicker");
        } else if (id == R.id.new_event_tv_end_date_value) {
            mDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_END_DATE);
            mDatePickerDialogFragment.show(getSupportFragmentManager(), "datePicker");
        } else if (id == R.id.new_event_tv_start_time_value) {
            mTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_START_TIME);
            mTimePickerDialogFragment.show(getSupportFragmentManager(), "timePicker");
        } else if (id == R.id.new_event_tv_end_time_value) {
            mTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_END_TIME);
            mTimePickerDialogFragment.show(getSupportFragmentManager(), "timePicker");
        } else if (id == R.id.btn_update) {
            EventModel eventModel = new EventModel();
            eventModel.setEventId(extras.getSerializable("id").toString());
            eventModel.setEventSummary(mETxtSummary.getText().toString());
            eventModel.setEventDescription(mETxtDescription.getText().toString());
            eventModel.setEventLocation(mETxtLocation.getText().toString());
            eventModel.setEventStartDateTime(getDateTime(mTxtStartDate.getText().toString(), mTxtStartTime.getText().toString()));
            eventModel.setEventEndDateTime(getDateTime(mTxtEndDate.getText().toString(), mTxtEndTime.getText().toString()));
            eventModel.setEventAttendees(getListOfEvenAttendees(mETxtAttendees.getText().toString()));
            eventModel.setEventReminder(getEventRemainders(selectRemainder.getSelectedItem().toString()));
            new EditListItemActivity.UpdateEventTask(mCredential).execute(eventModel);
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class UpdateEventTask extends AsyncTask<EventModel, Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        UpdateEventTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API")
                    .build();
        }


        private void updateOldEvent(EventModel eventModel) throws IOException, ParseException {
            Event event = new Event()
                    .setSummary(eventModel.getEventSummary())
                    .setLocation(eventModel.getEventLocation())
                    .setDescription(eventModel.getEventDescription());

            DateTime startDateTime = DateTime.parseRfc3339(eventModel.getEventStartDateTime().toStringRfc3339());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);
            event.setStart(start);
            DateTime endDateTime = DateTime.parseRfc3339(eventModel.getEventEndDateTime().toStringRfc3339());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);
            event.setEnd(end);
            event.setAttendees(eventModel.getEventAttendees());
            event.setReminders(eventModel.getEventReminder());
            event = mService.events().update(calendarId, eventModel.getEventId(), event).execute();
        }


        @Override
        protected Void doInBackground(EventModel... params) {
            try {
                updateOldEvent(params[0]);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    GoogleAccountSingleton.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleAccountSingleton.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(), "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private DateTime getDateTime(String date, String time) {
        String year = date.split("-")[0];
        String month = date.split("-")[1];
        String day = date.split("-")[2];
        String hour = time.split(":")[0];
        String minute = time.split(":")[1].replace("AM", "").replace("PM", "").trim();
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        cal.set(Calendar.MINUTE, Integer.parseInt(minute));
        cal.set(Calendar.SECOND, 0);
        return new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(cal.getTime()));
    }


    private List<EventAttendee> getListOfEvenAttendees(String eventAttendees) {
        List<EventAttendee> attendees = new ArrayList<EventAttendee>();
        for (String eventAttendee : eventAttendees.split(";")) {
            attendees.add(new EventAttendee().setEmail(eventAttendee));
        }
        return attendees;
    }


    private Event.Reminders getEventRemainders(String remainder) {
        List<EventReminder> reminderOverrides = new ArrayList<EventReminder>();
        reminderOverrides.add(new EventReminder().setMethod("popup").setMinutes(Integer.parseInt(remainder)));
        return new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(reminderOverrides);
    }


    public static class DatePickerDialogFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        public static final int FLAG_START_DATE = 0;
        public static final int FLAG_END_DATE = 1;

        private int flag = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void setFlag(int i) {
            flag = i;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear-1, dayOfMonth);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (flag == FLAG_START_DATE) {
                mTxtStartDate.setText(simpleDateFormat.format(calendar.getTime()));
            } else if (flag == FLAG_END_DATE) {
                mTxtEndDate.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }
    }


    public static class TimePickerDialogFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {
        public static final int FLAG_START_TIME = 0;
        public static final int FLAG_END_TIME = 1;

        private int flag = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void setFlag(int i) {
            flag = i;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (flag == FLAG_START_TIME) {
                mTxtStartTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            } else if (flag == FLAG_END_TIME) {
                mTxtEndTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            }
        }
    }
}
