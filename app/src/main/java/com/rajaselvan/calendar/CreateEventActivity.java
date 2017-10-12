package com.rajaselvan.calendar;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
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
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener {

    private static TextView mTxtStartDate, mTxtEndDate;
    private static TextView mTxtStartTime, mTxtEndTime;
    private DatePickerDialogFragment mDatePickerDialogFragment;
    private TimePickerDialogFragment mTimePickerDialogFragment;
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        mTxtStartDate = (TextView) findViewById(R.id.tv_start_date_value);
        mTxtEndDate = (TextView) findViewById(R.id.tv_end_date_value);
        mTxtStartTime = (TextView) findViewById(R.id.tv_start_time_value);
        mTxtEndTime = (TextView) findViewById(R.id.tv_end_time_value);
        mTxtStartDate.setOnClickListener(this);
        mTxtEndDate.setOnClickListener(this);
        mTxtStartTime.setOnClickListener(this);
        mTxtEndTime.setOnClickListener(this);
    }


    @Override
    protected void onDestroy(){
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
        }
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
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            if (flag == FLAG_START_DATE) {
                mTxtStartDate.setText(format.format(calendar.getTime()));
            } else if (flag == FLAG_END_DATE) {
                mTxtEndDate.setText(format.format(calendar.getTime()));
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
            if(hourOfDay >11)
            {
                aMpM = "PM";
            }

            if(hourOfDay>11)
            {
                currentHour = hourOfDay - 12;
            }
            else
            {
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
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }


        private void postNewEvent() throws IOException {
            Event event = new Event()
                    .setSummary("Google I/O 2015")
                    .setLocation("800 Howard St., San Francisco, CA 94103")
                    .setDescription("A chance to hear more about Google's developer products.");

            DateTime startDateTime = new DateTime("2017-10-13T09:00:00-07:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setStart(start);

            DateTime endDateTime = new DateTime("2017-10-13T17:00:00-07:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setEnd(end);

            String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
            event.setRecurrence(Arrays.asList(recurrence));

            EventAttendee[] attendees = new EventAttendee[] {
                    new EventAttendee().setEmail("lpage@example.com"),
                    new EventAttendee().setEmail("sbrin@example.com"),
            };
            event.setAttendees(Arrays.asList(attendees));

            EventReminder[] reminderOverrides = new EventReminder[] {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            String calendarId = "primary";
            event = mService.events().insert(calendarId, event).execute();
        }



        @Override
        protected Void doInBackground(Void... params) {
            try {
                postNewEvent();
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
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            EventListActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(),"The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_LONG);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Request cancelled.", Toast.LENGTH_LONG);
            }
        }
    }
}
