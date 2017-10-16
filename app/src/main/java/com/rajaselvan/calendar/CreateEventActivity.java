package com.rajaselvan.calendar;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import java.util.Date;
import java.util.List;


public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static TextView mTxtStartDate, mTxtEndDate;
    private static TextView mTxtStartTime, mTxtEndTime;
    private static EditText mETxtSummary, mETxtDescription, mETxtLocation, mETxtAttendees;
    private DatePickerDialogFragment mDatePickerDialogFragment;
    private TimePickerDialogFragment mTimePickerDialogFragment;
    private ProgressDialog mProgress;
    String[] reminder = {"10", "20", "30"};
    private ArrayAdapter remainderAdapter;
    private GoogleAccountCredential mCredential;
    private Button mBtnCreate;
    private Spinner selectRemainder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        mTxtStartDate = (TextView) findViewById(R.id.tv_start_date_value);
        mTxtEndDate = (TextView) findViewById(R.id.tv_end_date_value);
        mTxtStartTime = (TextView) findViewById(R.id.tv_start_time_value);
        mTxtEndTime = (TextView) findViewById(R.id.tv_end_time_value);
        mETxtSummary = (EditText) findViewById(R.id.et_event_summary);
        mETxtDescription = (EditText) findViewById(R.id.et_description);
        mETxtLocation = (EditText) findViewById(R.id.et_location_value);
        mETxtAttendees = (EditText) findViewById(R.id.et_invite_attendees);
        mTxtStartDate.setOnClickListener(this);
        mTxtEndDate.setOnClickListener(this);
        mTxtStartTime.setOnClickListener(this);
        mTxtEndTime.setOnClickListener(this);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        selectRemainder = (Spinner) findViewById(R.id.sp_select_remainder);
        selectRemainder.setOnItemSelectedListener(this);
        remainderAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, reminder);
        remainderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectRemainder.setAdapter(remainderAdapter);
        mCredential = GoogleAccountSingleton.getInstance(getApplicationContext());
        mBtnCreate = (Button) findViewById(R.id.btn_create);
        mBtnCreate.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        mDatePickerDialogFragment = new DatePickerDialogFragment();
        mTimePickerDialogFragment = new TimePickerDialogFragment();
        if (id == R.id.tv_start_date_value) {
            mDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_START_DATE);
            mDatePickerDialogFragment.show(getSupportFragmentManager(), "datePicker");
        } else if (id == R.id.tv_end_date_value) {
            mDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_END_DATE);
            mDatePickerDialogFragment.show(getSupportFragmentManager(), "datePicker");
        } else if (id == R.id.tv_start_time_value) {
            mTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_START_TIME);
            mTimePickerDialogFragment.show(getSupportFragmentManager(), "timePicker");
        } else if (id == R.id.tv_end_time_value) {
            mTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_END_TIME);
            mTimePickerDialogFragment.show(getSupportFragmentManager(), "timePicker");
        } else if (id == R.id.btn_create) {
            EventModel eventModel = new EventModel();
            eventModel.setEventSummary(mETxtSummary.getText().toString());
            eventModel.setEventDescription(mETxtDescription.getText().toString());
            eventModel.setEventStartDateTime(getDateTime(mTxtStartDate.getText().toString(), mTxtStartTime.getText().toString()));
            eventModel.setEventEndDateTime(getDateTime(mTxtEndDate.getText().toString(), mTxtEndTime.getText().toString()));
            eventModel.setEventAttendees(getListOfEvenAttendees(mETxtAttendees.getText().toString()));
            eventModel.setEventReminder(getListOfEventRemainders(selectRemainder.getSelectedItem().toString()));
            new CreateEventActivity.InsertEventTask(mCredential).execute(eventModel);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getApplicationContext(), reminder[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
            calendar.set(year, monthOfYear, dayOfMonth);
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
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void setFlag(int i) {
            flag = i;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String aMpM = "AM";
            int currentHour;
            if (hourOfDay > 11) {
                aMpM = "PM";
            }

            if (hourOfDay > 11) {
                currentHour = hourOfDay - 12;
            } else {
                currentHour = hourOfDay;
            }

            if (flag == FLAG_START_TIME) {
                mTxtStartTime.setText(String.format("%02d:%02d %s", currentHour, minute, aMpM));
            } else if (flag == FLAG_END_TIME) {
                mTxtEndTime.setText(String.format("%02d:%02d %s", currentHour, minute, aMpM));
            }
        }
    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class InsertEventTask extends AsyncTask<EventModel, Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        InsertEventTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API")
                    .build();
        }


        private void postNewEvent(EventModel eventModel) throws IOException, ParseException {
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
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(eventModel.getEventReminder());
            event.setReminders(reminders);

            String calendarId = "primary";

            event = mService.events().insert(calendarId, event).execute();

        }


        @Override
        protected Void doInBackground(EventModel... params) {
            try {
                postNewEvent(params[0]);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            mProgress.hide();
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
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
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        cal.set(Calendar.MONTH, Integer.parseInt(month));
        cal.set(Calendar.DATE, Integer.parseInt(day));
        cal.set(Calendar.HOUR, Integer.parseInt(hour));
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


    private List<EventReminder> getListOfEventRemainders(String remainder) {
        List<EventReminder> remainders = new ArrayList<EventReminder>();
        remainders.add(new EventReminder().setMethod("popup").setMinutes(Integer.parseInt(remainder)));
        return remainders;
    }
}
