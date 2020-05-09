package com.example.new_app;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.palette.graphics.Palette;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.google.android.gms.location.DetectedActivity;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MyWatchFace extends CanvasWatchFaceService implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accel;
    public Context myContext;
    SharedPreferences.Editor prefsforValuefinalComparison;
    SharedPreferences myPrefsForFinalComparisonValue;
    float OthersFinalPreviousValue = 0;
    int othersSteps = 0;
    float finalValueSteps = 0;
    float finalValueStepsSetTextAndChart = 0; //official steps recorded and saved (after the subtraction is done)
    SharedPreferences.Editor editor;
    SharedPreferences myPrefs;
    String comparisonDate = "";
    Calendar c, calndr;
    SimpleDateFormat sdf, date2, date3;
    String getCurrentDateTime2;//global variable to compare dates
    private TextView stepsText;
    SharedPreferences preferences;//to get the dailyGoal value
    int userStood, userStood2 = 0;// how many times the user stood
    boolean didUserStand = false; //goes hand in hand with userStood
    public int countTheActivities = 0;//+1 if the user is sedentary; 0 if he stood up
    InsertedData insertedData1, insertedData2;
    Stance_Database getData3;
    String serverURL = "http://192.168.10.4/android/insert.php";
    String serverURL2 = "http://192.168.10.4/android/insert2.php";
    AlertDialog.Builder builder;
    String now22, hourSetGoal22, goalFromDatabase, setStepsChart; //the setStepsChart is the value that will retrieve the recorded finalValueStepsSetTextAndChart from the database
    int userActiveActivities;
    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float comparisonpreviousvalue2 = myPrefsForFinalComparisonValue.getFloat("OTHERSFINALPREVIOUSVALUE",othersSteps);
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("dd-MM-YYYY");
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = myPrefs.edit();

        String getCurrentDateTime = sdf.format(c.getTime());
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String mDate =  myPrefs.getString("Date","nothing");

        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];//accumulated steps (from the beginning)
        }


        if (getCurrentDateTime.compareTo(mDate) < 0)
        {
            comparisonDate = getCurrentDateTime;
            System.out.println("CHOICE 1");
            editor.putString("Date", comparisonDate);
            finalValueSteps = value;//this keeps adding (accumulated steps)
            editor.putFloat("Steps to abstract",finalValueSteps);
            editor.commit();
            float comparisonpreviousvalue = myPrefsForFinalComparisonValue.getFloat("OTHERSFINALPREVIOUSVALUE", 0);
            //userStood = 0;
        }
        else if(getCurrentDateTime.compareTo(mDate) == 0){
            System.out.println("CHOICE 2");
        }else
        {
            System.out.println("CHOICE 3");
            comparisonDate = getCurrentDateTime;
            editor.putString("Date", comparisonDate);//NEWEST CHANGE
            finalValueSteps = value;
            editor.putFloat("Steps to abstract",finalValueSteps);
            editor.commit();
            float comparisonpreviousvalue = myPrefsForFinalComparisonValue.getFloat("OTHERSFINALPREVIOUSVALUE", 0);
        }


        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            float getSavedSteps =  myPrefs.getFloat("Steps to abstract",0);//get all accumulated steps
            finalValueStepsSetTextAndChart = value - getSavedSteps;//find the steps
        }


        else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

        }else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

        }

        Calendar calendar2 = Calendar.getInstance(Locale.getDefault());
        int hour2 = calendar2.get(Calendar.HOUR_OF_DAY);
        int minute2 = calendar2.get(Calendar.MINUTE);
        String finalHour2 = Integer.toString(hour2);
        String finalMinute2 = Integer.toString(minute2);
        String hourSetGoal2 = finalHour2+":"+finalMinute2+":00";
        DateFormat df2 = new SimpleDateFormat("dd-MM-YYYY");
        String now2 = df2.format(new Date()).toString();
        insertedData1.insertData(hourSetGoal2,now2, (int) finalValueStepsSetTextAndChart, "Start of Walk");

        now22 = now2;
        hourSetGoal22 = hourSetGoal2;
        insertedData1.close();

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, serverURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                builder.setTitle("Server Response");
                builder.setMessage("Response: " + response);
                AlertDialog alertDialog = builder.create();
                //alertDialog.show();
            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(com.example.placemly.MyWatchFace.this,"error",Toast.LENGTH_LONG).show();
                //error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("first", hourSetGoal22);
                params.put("second", now22);
                params.put("three", String.valueOf(finalValueStepsSetTextAndChart));
                params.put("four", "Start of Walk");
                return params;
            }
        };

        MySingleton.getInstance(com.example.placemly.MyWatchFace.this).addTorequestque(stringRequest);*/

    }//end of sensor change


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;
        private Bitmap mGrayBackgroundBitmap;
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        private int specW, specH;
        private View myLayout;
        private final Point displaySize = new Point();
        Time mTime;

        float mXOffset = 0;
        float mYOffset = 0;

        CircularProgressBar progressBar, progressBar2;

        int hour24hrs, minutes, seconds;
        String offihour = "";

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mTime = new Time();

            LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myLayout = inflater.inflate(R.layout.main, null);

            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            display.getSize(displaySize);

            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x,
                    View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y,
                    View.MeasureSpec.EXACTLY);

            progressBar = myLayout.findViewById(R.id.progress_bar);//external
            progressBar2 = myLayout.findViewById(R.id.progress_bar2);//internal

            // Get an instance of the SensorManager
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            //accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            sensorManager.registerListener(com.example.new_app.MyWatchFace.this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            if(accel!=null){
                //System.out.println("found");
                //sensorManager.registerListener(this,countSensor,SensorManager.SENSOR_DELAY_UI);
            }else{
                //Toast.makeText(this,"not found",Toast.LENGTH_LONG).show();
                //System.out.println("not found");
            }

            myContext = com.example.new_app.MyWatchFace.this;

            myPrefsForFinalComparisonValue = getApplicationContext().getSharedPreferences("MyPrefFORPREVIOUSFINALVALUECOMPARISON", 0);
            prefsforValuefinalComparison = myPrefsForFinalComparisonValue.edit();
            prefsforValuefinalComparison.putFloat("OTHERSFINALPREVIOUSVALUE", OthersFinalPreviousValue);
            prefsforValuefinalComparison.commit();

            mCalendar = Calendar.getInstance();
            startTracking();

            insertedData1 = new InsertedData(com.example.new_app.MyWatchFace.this);
            insertedData2 = new InsertedData(com.example.new_app.MyWatchFace.this);
            sensorManager.registerListener(com.example.new_app.MyWatchFace.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

            getData3 = new Stance_Database(com.example.new_app.MyWatchFace.this);

            date2 = new SimpleDateFormat("dd-MM-YYYY");
            calndr = Calendar.getInstance();
            getCurrentDateTime2 = date2.format(calndr.getTime());
            //System.out.println(getCurrentDateTime2);
            builder = new AlertDialog.Builder(com.example.new_app.MyWatchFace.this);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;

                // Show/hide the seconds fields
                if (inAmbientMode) {
                    //second.setVisibility(View.GONE);
                    //myLayout.findViewById(R.id.second_label).setVisibility(View.GONE);
                    myLayout.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    myLayout.findViewById(R.id.progress_bar2).setVisibility(View.GONE);
                    myLayout.findViewById(R.id.textsteps).setVisibility(View.GONE);

                } else {
                    //second.setVisibility(View.VISIBLE);
                    //myLayout.findViewById(R.id.second_label).setVisibility(View.VISIBLE);
                    myLayout.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    myLayout.findViewById(R.id.progress_bar2).setVisibility(View.VISIBLE);
                    myLayout.findViewById(R.id.textsteps).setVisibility(View.VISIBLE);
                }

                // Switch between bold & normal font
                Typeface font = Typeface.create("sans-serif-condensed",
                        inAmbientMode ? Typeface.NORMAL : Typeface.BOLD);
                ViewGroup group = (ViewGroup) myLayout;
                for (int i = group.getChildCount() - 1; i >= 0; i--) {
                    // We only get away with this because every child is a TextView
                    //((TextView) group.getChildAt(i)).setTypeface(font);
                }
                invalidate();
            }
            //updateWatchHandStyle();
            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            if (insets.isRound()) {
                // Shrink the face to fit on a round screen
                //mYOffset = mXOffset = displaySize.x * 0.1f;
                //displaySize.y -= 2 * mXOffset;
                //displaySize.x -= 2 * mXOffset;
            } else {
                //mXOffset = mYOffset = 0;
            }
            // Recompute the MeasureSpec fields - these determine the actual size of the layout
            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.EXACTLY);
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                invalidate();
            }
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    Intent intent = new Intent(MyWatchFace.this, SetGoal.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            date3 = new SimpleDateFormat("dd-MM-YYYY");
            Calendar calendar3 = Calendar.getInstance();
            String getCurrentDateTime3 = date3.format(calendar3.getTime());

            //If there's a date change, 0 all progress bars and notification (we start from the beginning)
            if(getCurrentDateTime2.compareTo(getCurrentDateTime3) == 0){

            }else{
                //When there's a date change, change the date
                getCurrentDateTime2 = getCurrentDateTime3;
            }

            //Get the target goal set by the user
            Cursor cursorGetGoal = insertedData2.getAllData1();

            if (cursorGetGoal.moveToLast()) {
                goalFromDatabase = cursorGetGoal.getString(cursorGetGoal.getColumnIndex("VALUE"));
                System.out.println("goalFromDatabase");
                System.out.println(goalFromDatabase);
            }else {
                goalFromDatabase = "10000";
            }

            //Get the latest recorded steps
            Cursor cursorGetStepValue = insertedData2.getAllData2();
            if (cursorGetStepValue.moveToLast()) {
                setStepsChart = cursorGetStepValue.getString(cursorGetStepValue.getColumnIndex("VALUE"));
            }else {
                setStepsChart = "0";
            }

            insertedData2.close();

            //get all the instances that user stood up for the day
            Cursor cursor2 = getData3.getActivitiesStood(getCurrentDateTime3);
            if (cursor2.moveToFirst()) {
                userActiveActivities = cursor2.getCount();
            }else {
                userActiveActivities = 0;
            }

            getData3.close();

            //user has stood up for more than 12 times per hour in a day
            if(userActiveActivities>=12){
                Context context = com.example.new_app.MyWatchFace.this;
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
                        .setContentTitle("Warning!")
                        .setContentText("You have stood up enough times in 12 hours");

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }

            if (!mAmbient) {
                float comparisonpreviousvalue = myPrefsForFinalComparisonValue.getFloat("OTHERSFINALPREVIOUSVALUE",othersSteps);

                //My Steps
                progressBar.setProgress(Float.parseFloat(setStepsChart));
                progressBar.setMaximum(Float.parseFloat(goalFromDatabase));

                //for standing up throughout the day
                //progressBar2.setProgress(userStood);
                progressBar2.setProgress(userActiveActivities);
                progressBar2.setMaximum(12);

                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                String finalHour = Integer.toString(hour);
                String finalMinute = Integer.toString(minute);

                if (minute < 10) {
                    finalMinute = "0" + finalMinute;
                }else {

                }
                stepsText.setText(finalHour + ":"+ finalMinute);
            }//end of visible

            myLayout.measure(specW, specH);
            myLayout.layout(0, 0, myLayout.getMeasuredWidth(),
                    myLayout.getMeasuredHeight());

            // Update the layout
            myLayout.measure(specW, specH);
            myLayout.layout(0, 0, myLayout.getMeasuredWidth(), myLayout.getMeasuredHeight());
            //sensorManager.registerListener(com.example.placemly.MyWatchFace.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
            // Draw it to the Canvas
            canvas.drawColor(Color.BLACK);
            canvas.translate(mXOffset, mYOffset);
            myLayout.draw(canvas);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {

                //Get goal from setgoal
                preferences = PreferenceManager.getDefaultSharedPreferences(com.example.new_app.MyWatchFace.this);
                stepsText  = myLayout.findViewById(R.id.textsteps);
                // Get the current Time
                mTime.setToNow();

                float comparisonpreviousvalue = myPrefsForFinalComparisonValue.getFloat("OTHERSFINALPREVIOUSVALUE",othersSteps);
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();

            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

    }

    private void startTracking() {
        Intent intent = new Intent(MyWatchFace.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

}
