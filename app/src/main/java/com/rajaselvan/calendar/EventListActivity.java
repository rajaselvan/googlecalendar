package com.rajaselvan.calendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.MutableDateTime;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class EventListActivity extends AppCompatActivity implements OnMonthChangedListener,
        OnDateSelectedListener, EasyPermissions.PermissionCallbacks {

    private MaterialCalendarView mMaterialCalendarView;
    private RecyclerView mRecyclerView;
    private EventListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private GoogleAccountCredential mCredential;
    private Map<String, String> dateRange = new HashMap<String, String>();
    private List<EventModel> listOfValues = new ArrayList<EventModel>();
    private String calendarId ="phll02ughcj3qk6v61pp9ok5k4@group.calendar.google.com";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        mMaterialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_event_list);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), mRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        EventModel eventModel = listOfValues.get(position);
                        Intent detailIntent = new Intent(getApplicationContext(), EventListDetailActivity.class);
                        detailIntent.putExtra("id", eventModel.getEventId());
                        detailIntent.putExtra("summary", eventModel.getEventSummary());
                        detailIntent.putExtra("description", eventModel.getEventDescription());
                        detailIntent.putExtra("location", eventModel.getEventLocation());
                        detailIntent.putExtra("startDate", new SimpleDateFormat("yyyy-MM-dd").format(eventModel.getEventStartDateTime().getValue()));
                        detailIntent.putExtra("endDate", new SimpleDateFormat("yyyy-MM-dd").format(eventModel.getEventEndDateTime().getValue()));
                        detailIntent.putExtra("startTime", new SimpleDateFormat("hh:mm aa").format(eventModel.getEventStartDateTime().getValue()));
                        detailIntent.putExtra("endTime", new SimpleDateFormat("hh:mm aa").format(eventModel.getEventEndDateTime().getValue()));
                        if(eventModel.getEventAttendees()!= null) {
                            StringBuilder eventAttendeesList = new StringBuilder();
                            for (EventAttendee eventAttendee : eventModel.getEventAttendees()) {
                                eventAttendeesList.append(eventAttendee.getEmail());
                                eventAttendeesList.append(";");
                            }
                            detailIntent.putExtra("attendees", eventAttendeesList.toString());
                        }
                        if(eventModel.getEventReminder()!=null) {
                            StringBuilder eventRemainderList = new StringBuilder();
                            for (EventReminder eventReminder : eventModel.getEventReminder().getOverrides()) {
                                eventRemainderList.append(eventReminder.getMinutes().toString());
                                eventRemainderList.append(";");
                            }
                            detailIntent.putExtra("remainders", eventRemainderList.toString());
                        }
                        startActivity(detailIntent);
                    }

                })
        );
        mAdapter = new EventListAdapter(listOfValues);
        mRecyclerView.setAdapter(mAdapter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventListActivity.this, CreateEventActivity.class);
                startActivity(intent);
            }
        });
        mMaterialCalendarView.setOnDateChangedListener(this);
        mMaterialCalendarView.setOnMonthChangedListener(this);
        // Initialize credentials and service object.
        mCredential = GoogleAccountSingleton.getInstance(getApplicationContext());
        setUpApp();
        initCurrentMonth();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initCurrentMonth();
    }


    private void initCurrentMonth() {
        dateRange = new HashMap<String, String>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        dateRange.put("startDate", new DateTime(c.getTime()).toStringRfc3339());
        MutableDateTime firstDateOfNextMonth = new MutableDateTime(c.getTime());
        firstDateOfNextMonth.addMonths(1);
        firstDateOfNextMonth.addDays(-1);
        dateRange.put("endDate", new DateTime(firstDateOfNextMonth.toDate()).toStringRfc3339());
        getResultsFromApi();
    }

    private void setUpApp(){
        if (!GoogleAccountSingleton.isGooglePlayServicesAvailable()) {
            GoogleAccountSingleton.acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_calendar:
                toggleCalendarView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @Nullable final CalendarDay date, boolean selected) {
        dateRange = new HashMap<String, String>();
        MutableDateTime tomorrow = new MutableDateTime(date.getDate());
        tomorrow.addDays(1);
        dateRange.put("startDate", new DateTime(date.getDate()).toStringRfc3339());
        dateRange.put("endDate", new DateTime(tomorrow.toDate()).toStringRfc3339());
        getResultsFromApi();
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay firstDateOfNewMonth) {
        dateRange = new HashMap<String, String>();
        dateRange.put("startDate", new DateTime(firstDateOfNewMonth.getDate()).toStringRfc3339());
        MutableDateTime firstDateOfNextMonth = new MutableDateTime(firstDateOfNewMonth.getDate());
        firstDateOfNextMonth.addMonths(1);
        dateRange.put("endDate", new DateTime(firstDateOfNextMonth.toDate()).toStringRfc3339());
        getResultsFromApi();
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void getResultsFromApi() {
        if (!isDeviceOnline()) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            new EventListActivity.MakeRequestTask(mCredential).execute();
        }
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(GoogleAccountSingleton.REQUEST_PERMISSION_GET_ACCOUNTS)
    public void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(GoogleAccountSingleton.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        GoogleAccountSingleton.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    GoogleAccountSingleton.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GoogleAccountSingleton.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case GoogleAccountSingleton.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(GoogleAccountSingleton.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case GoogleAccountSingleton.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<EventModel>> {
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


        /**
         * Fetch a list of the next 10 events from the primary calendar.
         *
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<EventModel> getDataFromApi() throws IOException {
            Events events = mService.events().list(calendarId)
                        .setTimeMin(DateTime.parseRfc3339(dateRange.get("startDate")))
                        .setTimeMax(DateTime.parseRfc3339(dateRange.get("endDate")))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
            List<Event> items = events.getItems();
            List<EventModel> eventsList = new ArrayList<>();
            for (Event event : items) {
                EventModel eventModel = new EventModel();
                eventModel.setEventId(event.getId());
                eventModel.setEventSummary(event.getSummary());
                eventModel.setEventDescription(event.getDescription());
                eventModel.setEventLocation(event.getLocation());
                if (event.getStart().getDateTime() != null) {
                    eventModel.setEventStartDateTime(event.getStart().getDateTime());
                } else {
                    eventModel.setEventStartDateTime(event.getStart().getDate());
                }
                if (event.getEnd().getDateTime() != null) {
                    eventModel.setEventEndDateTime(event.getEnd().getDateTime());
                } else {
                    eventModel.setEventEndDateTime(event.getEnd().getDate());
                }
                if (event.getAttendees() != null) {
                    eventModel.setEventAttendees(event.getAttendees());
                }
                if (event.getReminders() != null) {
                    eventModel.setEventReminder(event.getReminders());
                }
                eventsList.add(eventModel);
            }
            return eventsList;
        }


        @Override
        protected List<EventModel> doInBackground(Void... params) {
            try {
                return getDataFromApi();
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
        protected void onPostExecute(List<EventModel> results) {
            mAdapter.updateList(results);
//            toggleCalendarView();
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

    private void toggleCalendarView(){
        if(mMaterialCalendarView.getVisibility()==View.VISIBLE)
            mMaterialCalendarView.setVisibility(View.GONE);
        else
            mMaterialCalendarView.setVisibility(View.VISIBLE);
    }
}
