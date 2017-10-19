package com.rajaselvan.calendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.text.ParseException;

public class EventListDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTxtStartDate, mTxtEndDate;
    private TextView mTxtStartTime, mTxtEndTime;
    private TextView mTxtSummary, mTxtDescription, mTxtLocation, mTxtAttendees, mTxtRemainders;
    private Button mEdit, mDelete;
    private GoogleAccountCredential mCredential;
    private Bundle extras;
    private String calendarId ="phll02ughcj3qk6v61pp9ok5k4@group.calendar.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_detail);
        extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getSerializable("summary") != null) {
                mTxtSummary = (TextView) findViewById(R.id.tv_event_summary_value);
                mTxtSummary.setText(extras.getSerializable("summary").toString());
            }
            if (extras.getSerializable("description") != null) {
                mTxtDescription = (TextView) findViewById(R.id.tv_description_value);
                mTxtDescription.setText(extras.getSerializable("description").toString());
            }
            if (extras.getSerializable("location") != null) {
                mTxtLocation = (TextView) findViewById(R.id.tv_location_value);
                mTxtLocation.setText(extras.getSerializable("location").toString());
            }
            if (extras.getSerializable("startDate") != null) {
                mTxtStartDate = (TextView) findViewById(R.id.display_event_tv_start_date_value);
                mTxtStartDate.setText(extras.getSerializable("startDate").toString());
            }
            if (extras.getSerializable("endDate") != null) {
                mTxtEndDate = (TextView) findViewById(R.id.display_event_tv_end_date_value);
                mTxtEndDate.setText(extras.getSerializable("endDate").toString());
            }
            if (extras.getSerializable("startTime") != null) {
                mTxtStartTime = (TextView) findViewById(R.id.display_event_tv_start_time_value);
                mTxtStartTime.setText(extras.getSerializable("startTime").toString());
            }
            if (extras.getSerializable("endTime") != null) {
                mTxtEndTime = (TextView) findViewById(R.id.display_event_tv_end_time_value);
                mTxtEndTime.setText(extras.getSerializable("endTime").toString());
            }
            if (extras.getSerializable("attendees") != null) {
                mTxtAttendees = (TextView) findViewById(R.id.tv_invite_attendees_value);
                mTxtAttendees.setText(extras.getSerializable("attendees").toString());
            }
            if (extras.getSerializable("remainders") != null) {
                mTxtRemainders = (TextView) findViewById(R.id.tv_select_remainder_value);
                mTxtRemainders.setText(extras.getSerializable("remainders").toString());
            }
            mCredential = GoogleAccountSingleton.getInstance(getApplicationContext());
            mEdit = (Button) findViewById(R.id.btn_edit);
            mEdit.setOnClickListener(this);
            mDelete = (Button) findViewById(R.id.btn_delete);
            mDelete.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_delete) {
            new EventListDetailActivity.DeleteEventTask(mCredential).execute(extras.getSerializable("id").toString());
            finish();
        } else if (id == R.id.btn_edit) {
            Intent detailIntent = new Intent(getApplicationContext(), EditListItemActivity.class);
            detailIntent.putExtras(extras);
            startActivity(detailIntent);
        }
    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class DeleteEventTask extends AsyncTask<String, Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        DeleteEventTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API")
                    .build();
        }


        private void removeEvent(String eventId) throws IOException, ParseException {
            mService.events().delete(calendarId, eventId).execute();
        }


        @Override
        protected Void doInBackground(String... params) {
            try {
                removeEvent(params[0]);
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
            Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_LONG).show();
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
}

