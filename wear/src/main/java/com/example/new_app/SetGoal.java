package com.example.new_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SetGoal extends WearableActivity {

    private TextView mTextView;
    int goalFromDatabase;
    Button decrease, increase, submission;
    //Local SQLite Database
    InsertedData insertedData;
    //For local db
    int goalInt = 1000;
    String hourSetGoal = "";//hour for the database
    String now = "";//date for database
    public Context myContext;
    int othersSteps = 0;

    String serverURL = "http://192.168.10.19/app/db_config.php";
    AlertDialog.Builder builder;
    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goal);

        myContext = SetGoal.this;
        mTextView = (TextView) findViewById(R.id.dailyNumber);
        decrease = (Button) findViewById(R.id.dailyMinus);
        increase = (Button) findViewById(R.id.dailyPlus);
        submission = (Button) findViewById(R.id.submit);

        //create the SQLite database
        insertedData = new InsertedData(this);
        builder = new AlertDialog.Builder(this);
        // Enables Always-on
        setAmbientEnabled();

        //set previously set goal as text
        //Get the target goal set by the user
        Cursor cursorGetGoal = insertedData.getAllData1();

        if (cursorGetGoal.moveToFirst()) {
            do {
                goalFromDatabase = Integer.parseInt(cursorGetGoal.getString(cursorGetGoal.getColumnIndex("VALUE")));
            } while (cursorGetGoal.moveToNext());
        }else {
            goalFromDatabase = Integer.parseInt("10000");
        }

        mTextView.setText(Integer.toString(goalFromDatabase));

        //decrease the steps
        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(goalFromDatabase<=1000){

                }else{
                    goalFromDatabase-=500;
                    mTextView.setText(Integer.toString(goalFromDatabase));
                }
            }
        });

        //increase the steps
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(goalFromDatabase>=10000){

                }else{
                    goalFromDatabase+=500;
                    mTextView.setText(Integer.toString(goalFromDatabase));
                }
            }
        });

        //when a user clicks the submit button
        submission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                String finalHour = Integer.toString(hour);
                String finalMinute = Integer.toString(minute);

                if (minute < 10) {
                    finalMinute = "0" + finalMinute;
                }else {

                }

                hourSetGoal = finalHour+":"+finalMinute+":00";

                final String getValue = mTextView.getText().toString();
                goalInt = Integer.parseInt(getValue);//inserted goal
                DateFormat df = new SimpleDateFormat("dd-MM-YYYY");
                now = df.format(new Date()).toString();

                insertedData.insertData(hourSetGoal,now,goalInt, "Goal");
                insertedData.close();
                //Cursor res = insertedData.getAllData1();
                //Toast.makeText(SetGoal.this, "Goal Added",Toast.LENGTH_SHORT).show();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, serverURL, new Response.Listener<String>() {
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
                        Toast.makeText(SetGoal.this,"error",Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("first", hourSetGoal);
                        params.put("second", now);
                        params.put("third", String.valueOf(goalInt));
                        params.put("fourth", "Goal");
                        return params;
                    }
                };
                MySingleton.getInstance(SetGoal.this).addTorequestque(stringRequest);

            }
        });//end of submission button


        //ask for body sensors & storage permissions
        if (ContextCompat.checkSelfPermission(SetGoal.this,
                Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SetGoal.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(SetGoal.this, "You have already granted the body and storage permission",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
            //requestStoragePermission2();
        }

    }//end of onCreate



    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.BODY_SENSORS)) {

            new AlertDialog.Builder(this)
                    //.setTitle("Permission needed")
                    //.setMessage("This permission is needed to measure your activities")
                    .setPositiveButton("Click Here", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetGoal.this,
                                    new String[] {Manifest.permission.BODY_SENSORS}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    /*.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })*/
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.BODY_SENSORS}, STORAGE_PERMISSION_CODE);
        }
    }


    //function for external storage
    private void requestStoragePermission2() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to save the activities")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetGoal.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }


}