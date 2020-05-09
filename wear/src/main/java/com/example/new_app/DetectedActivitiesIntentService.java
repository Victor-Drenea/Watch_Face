package com.example.new_app;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetectedActivitiesIntentService  extends IntentService {

    int hour = 0;
    String dateToStr, hourSetGoal = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();
    Stance_Database myDb;

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myDb = new Stance_Database(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        for (DetectedActivity activity : detectedActivities) {
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY");
            dateToStr = format.format(today);

            String dateOldCompare =pref.getString("dateOld", "null");

            if(dateOldCompare.equals(dateToStr)){

            }else{
                editor.putString("dateOld", dateToStr);
                editor.apply();
            }//end of else

            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            String finalHour = Integer.toString(hour);
            String finalMinute = Integer.toString(minute);
            hourSetGoal = finalHour+":"+finalMinute+":00";

            String[] labelByDetected = handleUserActivity(activity.getType(), activity.getConfidence());
            //System.out.println(labelByDetected[0] +" + " + labelByDetected[1]);

            //This is the part which decides what goes in the database or not (we base it on confidence)
            if (activity.getConfidence() > Constants.CONFIDENCE) { //if the confidence is satisfying
                myDb.insertData(hourSetGoal, dateToStr, activity.getConfidence(), labelByDetected[0], labelByDetected[1], "yes", finalHour);
                System.out.println("Satisfying confidence");
            }else{ //
                System.out.println("Not satisfying confidence");
            }

            //if we are close in entering a new hour: if the system doesn't see an activity in this hour remind him to stand up
            if(minute>50 && minute<56){
                Cursor cursorGetStepValue = myDb.getActivitiesStoodHour(dateToStr, finalHour);

                //there are rows- user stood in the hour
                if (cursorGetStepValue.moveToFirst()) {

                }else {
                    //there are no rows (the user didn't stand up in an hour), so remind
                    callNotification("You have been sedentary for almost one hour.\n Why don't you take an active break?");
                }
            }//finish of if statement (check minutes)

        }

        Cursor checkifMore4 = myDb.getWalking2(dateToStr, hourSetGoal);
        if (checkifMore4.moveToFirst()) {

            int countRows = checkifMore4.getCount();

            //if the user was active for more than 4 times
            if(countRows>4){
                callNotification("You have been active for more than 2 minutes!");
            }

        }else {
            //there are no rows (the user wan't active in this hour yet)
        }

        myDb.close();
    }

    private String[] handleUserActivity(int type, final int confidence) {
        String label = getString(R.string.activity_unknown);
        String booleanCheck = "";

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                booleanCheck = "false";//user is inactive
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                booleanCheck = "true";//user is active
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                booleanCheck = "true";//user is active
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                booleanCheck = "true";//user is active
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                booleanCheck = "false";//user is inactive
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                booleanCheck = "false";//user is inactive
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                booleanCheck = "true";//user is active
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                booleanCheck = "false";//user is inactive
                break;
            }
        }//end of switch

        return new String[] {label, booleanCheck};
    }

    public void callNotification(String outputText){

        Context context = com.example.new_app.DetectedActivitiesIntentService.this;
        String CHANNEL_ID = "my_channel_01";
        int notificationId = 001;
        // The channel ID of the notification.
        String id = "my_channel_01";
        int NOTIFICATION_ID = 234;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification")
                .setContentText(outputText);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}