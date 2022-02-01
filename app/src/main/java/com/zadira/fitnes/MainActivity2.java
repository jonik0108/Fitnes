/*
// Если этот код работает, его написал Никита Алексеев <noskiddie@yandex.com>,
// а если нет, то не знаю, кто его писал.(C)
 */

package com.zadira.fitnes;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.furkanakdemir.surroundcardview.StartPoint;
import com.furkanakdemir.surroundcardview.SurroundCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.huawei.healthkit.demo.Models.ExerciseModel;
import com.huawei.healthkit.demo.Models.HeartModel;
import com.huawei.hihealth.HiHealthAggregateQuery;
import com.huawei.hihealth.HiHealthDataKey;
import com.huawei.hihealth.HiHealthDataQuery;
import com.huawei.hihealth.error.HiHealthError;
import com.huawei.hihealth.listener.ResultCallback;
import com.huawei.hihealthkit.auth.HiHealthAuth;
import com.huawei.hihealthkit.auth.HiHealthOpenPermissionType;
import com.huawei.hihealthkit.auth.IDataAuthStatusListener;
import com.huawei.hihealthkit.data.HiHealthData;
import com.huawei.hihealthkit.data.HiHealthKitConstant;
import com.huawei.hihealthkit.data.HiHealthPointData;
import com.huawei.hihealthkit.data.store.HiHealthDataStore;
import com.huawei.hihealthkit.data.store.HiSportDataCallback;
import com.huawei.hihealthkit.data.type.HiHealthDataType;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.ActivityRecordsController;
import com.huawei.hms.hihealth.HiHealthActivities;
import com.huawei.hms.hihealth.HiHealthStatusCodes;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.SettingController;
import com.huawei.hms.hihealth.data.ActivityRecord;
import com.huawei.hms.hihealth.data.ActivitySummary;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.data.Scopes;
import com.huawei.hms.hihealth.options.ActivityRecordInsertOptions;
import com.huawei.hms.hihealth.result.HealthKitAuthResult;


import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "water_notification";
    private static final String TYPE_PLAN = "planA";
    private static final int REQUEST_AUTH = 1002;
    public ToneGenerator alarm;
    public int tv_durations;
    public FirebaseAuth firebaseAuth;
    public DatabaseReference exerciseRef, morningExerciseRef, mainExerciseRef;
    public ExerciseModel exerciseModel;
    public List<ExerciseModel> exerciseList = new ArrayList<>();
    public List<HeartModel> heartModelsList;
    public TextView tv_exercise_name_current,
            tv_exercise_name_next,
            tv_workout_state_message,
            tv_repeats,
            tv_add_weight,
            total_time_countdown,
            tv_current_exercise_count_of_panelInfo,
            tv_current_exercise_count_panelInfo;
    public ImageView imageView_CenterCircut;
    public LinearLayout ll_cardio_type_display;
    public CardView cv_image;
    public boolean isRun = false;
    public int position = 0;
    public final int relax_timer = 0;

    public int currentId;
    public int nextId;
    public int sizeId;
    public int myProgress = 0;
    public ProgressBar progressBarView;
    public int progress;
    public CountDownTimer countDownTimer, relaxTimer, onChangeTimer, repeatsTimer;
    public int endTime = 250;
    public int count = 0;

    public String type, id;
    public int sportType;
    public ImageView green, orange, blue, red, start_btn;
    public CardView pause, start, stop;
    public TextView stateBpm, titleBpm, numBpm;

    private TextView tvResults;
    private TextView tvDuration;

    private TextView tvHeart;
    private boolean waterPref;
    private TextView tvSteps;
    public int duration, steps, bpm;
    public Timer timer = new Timer();
    public Date date;
    public boolean runningThread = true;
    private final MessageHandler mHandler = new MessageHandler();
    private FitnessData fitnessData = new FitnessData(bpm, steps, duration);
    private TextView tvR;
    private Context mContext;
    public int state = 0;
    private  SurroundCardView surroundCardView;
    public int t, i;
    SettingController mSettingController;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        surroundCardView = findViewById(R.id.SurroundCardView_start);
        surroundCardView.setDuration(2000);
        surroundCardView.setSurroundStrokeWidth(R.dimen.scv_stroke_width_default);
        surroundCardView.setSurroundStrokeColor(R.color.colorGreen_900);
        surroundCardView.setStartPoint(StartPoint.TOP_START);
        surroundCardView.surround();
        surroundCardView.release();
        bindDataView();
        setOnClickListener();
        getWorkoutSets();
        createNotificationChannel();
        switchWaterNotification();
        checkHuaweiAuth();
        initService();
        // Step 2 Authorization process, which is called each time the process is started.
        requestAuth();

    }
    /**
     * Initialize SettingController.
     */
    private void initService() {
        mSettingController = HuaweiHiHealth.getSettingController(this);
    }
    private void requestAuth() {
        // Add scopes to apply for. The following only shows an example. You need to add scopes according to your specific needs.
        String[] scopes = new String[] {
                // View and store the step count in Health Kit.
                Scopes.HEALTHKIT_STEP_READ, Scopes.HEALTHKIT_STEP_WRITE,
                Scopes.HEALTHKIT_ACTIVITY_RECORD_WRITE, Scopes.HEALTHKIT_ACTIVITY_RECORD_READ,
                Scopes.HEALTHKIT_ACTIVITY_WRITE, Scopes.HEALTHKIT_ACTIVITY_READ,
                Scopes.HEALTHKIT_ACTIVITY_BOTH, Scopes.HEALTHKIT_ACTIVITY_RECORD_BOTH,
                Scopes.HEALTHKIT_CALORIES_BOTH, Scopes.HEALTHKIT_HEARTRATE_BOTH,
                Scopes.HEALTHKIT_STEP_BOTH, Scopes.HEALTHKIT_SPEED_BOTH,
                Scopes.HEALTHKIT_STEP_WRITE,Scopes.HEALTHKIT_STEP_READ,
                Scopes.HEALTHKIT_HEARTRATE_WRITE, Scopes.HEALTHKIT_HEARTRATE_READ,
                // View and store the height and weight in Health Kit.
                Scopes.HEALTHKIT_HEIGHTWEIGHT_READ, Scopes.HEALTHKIT_HEIGHTWEIGHT_WRITE,
                // View and store the heart rate data in Health Kit.
                Scopes.HEALTHKIT_HEARTRATE_READ, Scopes.HEALTHKIT_HEARTRATE_WRITE};

        // Obtain the intent of the authorization process. The value true indicates that the authorization process of the Health app is enabled, and false indicates that the authorization process is disabled.
        Intent intent = mSettingController.requestAuthorizationIntent(scopes, true);

        // Open the authorization process screen.
        Log.i(TAG, "start authorization activity");
        startActivityForResult(intent, REQUEST_AUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Process only the response result of the authorization process.
        if (requestCode == REQUEST_AUTH) {
            // Obtain the authorization response result from the intent.
            HealthKitAuthResult result = mSettingController.parseHealthKitAuthResultFromIntent(data);
            if (result == null) {
                Log.w(TAG, "authorization fail");
                return;
            }

            if (result.isSuccess()) {
                Log.i(TAG, "authorization success");
            } else {
                Log.w(TAG, "authorization fail, errorCode:" + result.getErrorCode());
            }
        }
    }

    ///// NOTIFICATIONS
        public void switchWaterNotification() {
            SharedPreferences preferences = getSharedPreferences("personalData", getApplicationContext().MODE_PRIVATE);
            waterPref = preferences.getBoolean("isWaterNot", false);
            if (waterPref) {
                timer.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendNotification();
                            }
                        });
                    }
                }, 0, 720000);
            }else {
                return;
            }
        }
        private void sendNotification() {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_aqua)
                    .setContentTitle("Человек состоит на 90% из воды!")
                    .setContentText("Выпей воды, будь человеком!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());

        }
        private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    ///// GET PARAMETERS FROM TODAY SCREEN
    private void getWorkoutSets() {
        type = getIntent().getStringExtra("type");
        id = getIntent().getStringExtra("id");
        TextView textViewWorkoutName = findViewById(R.id.cv_workout_type_title);
        if (type != null) {
            if (type.equals("1")) {
                textViewWorkoutName.setText("КАРДИО");
                setExerciseList(Integer.parseInt(type), Integer.parseInt(id));
//                ll_cardio_type_display.setVisibility(View.VISIBLE);
//                cv_image.setVisibility(View.GONE);
            } else if(type.equals("2")){
                textViewWorkoutName.setText("СИЛОВАЯ");
                setExerciseList(Integer.parseInt(type), Integer.parseInt(id));


            } else {
                textViewWorkoutName.setText("ЗАРЯДКА");
                setExerciseList(Integer.parseInt(type), Integer.parseInt(id));
            }
        }
    }


    private void checkHuaweiAuth() {
        int[] userAllowTypesToRead =
                new int[]{HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_INFORMATION,
                        HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_FEATURE,
                        HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_STEP_SUM,
                        HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_RUN_METADATA,
                        HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_WEIGHT,
                        HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_REALTIME_HEARTRATE,
                        HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_REAL_TIME_SPORT};
        int[] userAllowTypesToWrite =
                new int[]{HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_WRITE_DATA_SET_WEIGHT};
        HiHealthAuth.getDataAuthStatusEx(mContext, userAllowTypesToWrite, userAllowTypesToRead, new IDataAuthStatusListener() {
            @Override
            public void onResult(int resultCode, String resultMsg, int[] ints, int[] ints1) {
                Log.i(TAG, "getDataAuthStatusEx resultCode:" + resultCode);
                Log.i(TAG, "getDataAuthStatusEx resultMsg:" + resultMsg);
                if (resultCode == HiHealthError.SUCCESS) {
                    Log.i(TAG, "getDataAuthStatusEx writeList length:" + userAllowTypesToWrite.length);
                    // Return the status value corresponding to each applied write permission.
                    for (int i = 0; i < userAllowTypesToWrite.length; i++) {
                        Log.i(TAG, "getDataAuthStatusEx writeTypes : " + userAllowTypesToWrite[i] + " writePermissionResult: " + userAllowTypesToWrite[i]);
                    }
                    Log.i(TAG, "getDataAuthStatusEx readList length:" + userAllowTypesToRead.length);
                    // Return the status value corresponding to each applied read permission.
                    for (int i = 0; i < userAllowTypesToRead.length; i++) {
                        Log.i(TAG, "getDataAuthStatusEx readTypes : " + userAllowTypesToRead[i] + " readPermissionResult: " + userAllowTypesToRead[i]);
                    }
                } else{
                    int[] userAllowTypesToRead =
                            new int[]{HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_INFORMATION,
                                    HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_FEATURE,
                                    HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_STEP_SUM,
                                    HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_RUN_METADATA,
                                    HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_WEIGHT,
                                    HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_REALTIME_HEARTRATE,
                                    HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_REAL_TIME_SPORT};
                    int[] userAllowTypesToWrite =
                            new int[]{HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_WRITE_DATA_SET_WEIGHT};
                    HiHealthAuth.requestAuthorization(mContext, userAllowTypesToWrite, userAllowTypesToRead,
                            (resultCod, object) -> {
                                Log.i(TAG, "requestAuthorization onResult:" + resultCod);
                                if (resultCod == HiHealthError.SUCCESS) {
                                    Log.i(TAG, "requestAuthorization success resultContent:" + object);
                                }
                                combineResult(resultCod, object);
                            });
                }
            }
        });
    }

// SET INIT DATA TO EXERCISE LIST
    private void setExerciseList(int type, int day) {
        this.i = day -1;
        this.t = type;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.getCurrentUser();
        String uid = firebaseAuth.getUid();
        morningExerciseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("workout").child(String.valueOf(i)).child("morning");
        mainExerciseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("workout").child(String.valueOf(i)).child("main");

        if(type == 0){
            //***
            //MORNING EXERCISE
            //***
            morningExerciseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            exerciseModel = ds.getValue(ExerciseModel.class);
                            exerciseList.add(exerciseModel);
                            tv_current_exercise_count_of_panelInfo.setText(String.valueOf(exerciseList.size()));
                            tv_exercise_name_current.setText(String.valueOf(exerciseList.get(0).getName()));
                            tv_exercise_name_next.setText("Отдых");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if(type == 1) {
            //***
            //CARDIO EXERCISE
            //***
            mainExerciseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            exerciseModel = ds.getValue(ExerciseModel.class);
                            exerciseList.add(exerciseModel);
                            tv_current_exercise_count_of_panelInfo.setText(String.valueOf(exerciseList.size()));
                            tv_exercise_name_current.setText(String.valueOf(exerciseList.get(0).getName()));
                            tv_exercise_name_next.setText("Отдых");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if(type == 2) {
            //***
            //POWER EXERCISE
            //***
            mainExerciseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            exerciseModel = ds.getValue(ExerciseModel.class);
                            exerciseList.add(exerciseModel);
                            tv_current_exercise_count_of_panelInfo.setText(String.valueOf(exerciseList.size()));
                            tv_exercise_name_current.setText(String.valueOf(exerciseList.get(0).getName()));
                            tv_exercise_name_next.setText("Отдых");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


//DISPLAY LIST DATA
    private void runExercise(int id){
        String relax = "Отдых";

        tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
        tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
        if(id == 1){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(0).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(0).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(0).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(0).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(0).getId()));
                fn_countdown();
            }
        }
        if(id == 2){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(1).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(1).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(1).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(1).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(1).getId()));
                fn_countdown();
            }
        }
        if(id == 3){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(2).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(2).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(2).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(2).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(2).getId()));
                fn_countdown();
            }
        }
        if(id == 4){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(3).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(3).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(3).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(3).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(3).getId()));
                fn_countdown();
            }
        }
        if(id == 5){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(4).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(4).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(4).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(4).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(4).getId()));
                fn_countdown();
            }
        }
        if(id == 6){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(5).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(5).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(5).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(5).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(5).getId()));
                fn_countdown();
            }
        }
        if(id == 7){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(6).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(6).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(6).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(6).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(6).getId()));
                fn_countdown();
            }
        }
        if(id == 8){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(7).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(7).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(7).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(7).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(7).getId()));
                fn_countdown();
            }
        }
        if(id == 9){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(8).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(8).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(8).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(8).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(8).getId()));
                fn_countdown();
            }
        }
        if(id == 10){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(9).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(9).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(9).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(9).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(9).getId()));
                fn_countdown();
            }
        }
        if(id == 11){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(10).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(10).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(10).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(10).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(10).getId()));
                fn_countdown();
            }
        }
        if(id == 12){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(11).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(11).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(11).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(11).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(11).getId()));
                fn_countdown();
            }
        }
        if(id == 13){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(12).getName()));
                tv_exercise_name_next.setText(relax);
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(12).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(12).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(12).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(12).getId()));
                fn_countdown();
            }
        }
        if(id == 14){
            if(exerciseList != null) {
                position = id;
                tv_exercise_name_current.setText(String.valueOf(exerciseList.get(13).getName()));
                tv_exercise_name_next.setText("Завершение");
                tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
                imageView_CenterCircut.setImageResource(Math.toIntExact(exerciseList.get(13).getImage()));
                tv_repeats.setText(String.valueOf(exerciseList.get(13).getRepeats()));
                tv_durations = Math.toIntExact(exerciseList.get(13).getDuration());
                tv_current_exercise_count_panelInfo.setText(String.valueOf(exerciseList.get(13).getId()));
                fn_countdown();
            }
        }
    }

    private void fn_countdown() {
        if (tv_durations >0) {
            myProgress = 0;

            try {

                countDownTimer.cancel();

            } catch (Exception ignored) {

            }

            String timeInterval = String.valueOf(tv_durations);
            endTime = Integer.parseInt(timeInterval); // up to finish time

            countDownTimer = new CountDownTimer(endTime * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
//                    setProgress(progress, endTime);
//                    progress = progress + 1;
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                    String newtime = hours + ":" + minutes + ":" + seconds;

                    if (newtime.equals("0:0:0")) {
                        total_time_countdown.setText("00:00");
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        total_time_countdown.setText("0" + minutes + ":0" + seconds);
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1)) {
                        total_time_countdown.setText("0" + minutes + ":" + seconds);
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        total_time_countdown.setText(minutes + ":0" + seconds);
                    } else if ((String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        total_time_countdown.setText(minutes + ":0" + seconds);
                    } else if (String.valueOf(hours).length() == 1) {
                        total_time_countdown.setText(minutes + ":" + seconds);
                    } else if (String.valueOf(minutes).length() == 1) {
                        total_time_countdown.setText(minutes + ":" + seconds);
                    } else if (String.valueOf(seconds).length() == 1) {
                        total_time_countdown.setText(minutes + ":0" + seconds);
                    } else {
                        total_time_countdown.setText(minutes + ":" + seconds);
                    }
                }
                public void onFinish() {
                    if(position < exerciseList.size()){
                        setRelaxTimer();
                    }else {
                        stopSport();
                    }
                }
            };
            countDownTimer.start();
        }
    }

    private void setRelaxTimer() {
        final int relax = 10;
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        String timeInterval = String.valueOf(relax);
        endTime = Integer.parseInt(timeInterval); // up to finish time
        tv_repeats.setText("-");
        tv_exercise_name_current.setText("Отдых");
        for (int i = 0; i < exerciseList.size(); i++) {
            tv_exercise_name_next.setText(exerciseList.get(i+=1).getName());
        }
        position++;
        tv_exercise_name_current.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
        tv_exercise_name_next.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up));
        relaxTimer = new CountDownTimer(relax * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);

                String newtime = hours + ":" + minutes + ":" + seconds;

                if (newtime.equals("0:0:0")) {
                    total_time_countdown.setText("00:00");
                } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                    total_time_countdown.setText("0" + minutes + ":0" + seconds);
                } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1)) {
                    total_time_countdown.setText("0" + minutes + ":" + seconds);
                } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                    total_time_countdown.setText(minutes + ":0" + seconds);
                } else if ((String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                    total_time_countdown.setText(minutes + ":0" + seconds);
                } else if (String.valueOf(hours).length() == 1) {
                    total_time_countdown.setText(minutes + ":" + seconds);
                } else if (String.valueOf(minutes).length() == 1) {
                    total_time_countdown.setText(minutes + ":" + seconds);
                } else if (String.valueOf(seconds).length() == 1) {
                    total_time_countdown.setText(minutes + ":0" + seconds);
                } else {
                    total_time_countdown.setText(minutes + ":" + seconds);
                }
                if(seconds == 03){
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                } else if(seconds == 02){
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                } else if(seconds == 01){
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                }
            }
            @Override
            public void onFinish() {
                runExercise(position);
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 50);
            }
        };
        relaxTimer.start();
    }

    // TIMER BEFORE GONE TO NEXT EXERCISE

    private void onChangeTimer() {
        final int change_exercise = 3;
        String timeInterval = String.valueOf(change_exercise);
        progress = 1;
        endTime = Integer.parseInt(timeInterval); // up to finish time
//        repeatsCountDown();
//        Toast.makeText(getApplicationContext(), "START NEW EXERCISE : " + change_exercise + " SECONDS ", Toast.LENGTH_LONG).show();
        onChangeTimer = new CountDownTimer(change_exercise * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progress = progress + 1;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                String newtime = hours + ":" + minutes + ":" + seconds;
//                tv_workout_state_message.setText("СЛЕДУЮЩЕЕ УПРАЖНЕНИЕ");
                if (newtime.equals("0:0:0")) {
                    total_time_countdown.setText("00:00");
                } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                    total_time_countdown.setText("0" + minutes + ":0" + seconds);
                } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1)) {
                    total_time_countdown.setText("0" + minutes + ":" + seconds);
                } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                    total_time_countdown.setText(minutes + ":0" + seconds);
                } else if ((String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                    total_time_countdown.setText(minutes + ":0" + seconds);
                } else if (String.valueOf(hours).length() == 1) {
                    total_time_countdown.setText(minutes + ":" + seconds);
                } else if (String.valueOf(minutes).length() == 1) {
                    total_time_countdown.setText(minutes + ":" + seconds);
                } else if (String.valueOf(seconds).length() == 1) {
                    total_time_countdown.setText(minutes + ":0" + seconds);
                } else {
                    total_time_countdown.setText(minutes + ":" + seconds);
                }

            }
            @Override
            public void onFinish() {
                runExercise(position +=1);

            }
        };
        onChangeTimer.start();
    }


//GENERATE NEW DATA FOR NEW RECORDS IN PERIOD OF 5 seconds
    void newDate() {
        new Calendar() {
            @Override
            protected void computeTime() {

            }

            @Override
            protected void computeFields() {

            }

            @Override
            public void add(int i, int i1) {

            }

            @Override
            public void roll(int i, boolean b) {

            }

            @Override
            public int getMinimum(int i) {
                return 0;
            }

            @Override
            public int getMaximum(int i) {
                return 0;
            }

            @Override
            public int getGreatestMinimum(int i) {
                return 0;
            }

            @Override
            public int getLeastMaximum(int i) {
                return 0;
            }
        };
        date = Calendar.getInstance().getTime();
    }

    //GET AND SET BPM AND STEPS EVERY 5 SECONDS TO BUNDLE;
    private void setHealthData() {
        heartModelsList = new ArrayList<>();

        if( timer != null) {
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newDate();
                            if (bpm != 0){
                                heartModelsList.add(new HeartModel(date, bpm));
                            } else {
                                heartModelsList.add(new HeartModel(date, 120));
                            }
                            Log.i(TAG, "gsuccess heartModelsList =============== :" + String.valueOf(heartModelsList));
                        }
                    });
                }
            }, 0, 15000);
        }
    }

    public void setOnClickListener() {
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        final Vibrator vibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
        start_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)  {
                if ((count%2) == 0)
                {
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                    isRun = true;
                    runningThread = true;
                    start_btn.setImageResource(R.drawable.ic_pause);

                    surroundCardView.setDuration(2000);
                    surroundCardView.setSurroundStrokeWidth(R.dimen.scv_stroke_width_default);
                    surroundCardView.setSurroundStrokeColor(R.color.colorAmber_A400);
                    surroundCardView.setStartPoint(StartPoint.TOP_START);
                    surroundCardView.surround();
                    surroundCardView.release();
                    tv_workout_state_message.setText("");
                    vibrator.vibrate(300);
                    startSport();
                    start_btn.setEnabled(false);
                    start_btn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            start_btn.setEnabled(true);
                        }
                    }, 3000);

                }
                else
                {
                    isRun = false;
                    start_btn.setImageResource(R.drawable.ic_play_green);
                    tv_workout_state_message.setText("Пауза");
                    tv_workout_state_message.setTextColor(getColor(R.color.colorAmber_A400));
                    vibrator.vibrate(300);
                    countDownTimer.cancel();
                    Toast.makeText(getApplicationContext(), "PRESS LONG TIME BUTTON TO STOP THE WORKOUT", Toast.LENGTH_LONG).show();
                    start_btn.setEnabled(false);
                    start_btn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            start_btn.setEnabled(true);
                        }
                    }, 3000);
                    start_btn.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            vibrator.vibrate(600);
                            start_btn.setEnabled(false);
                            stopSport();
                            return true;
                        }
                    });
                }
                count++;
            }
        });


//        pause.setOnClickListener(new View.OnClickListener() {
//                                     @Override
//                                     public void onClick(View view) {
//                                         sendNotification();
//
//                                         int timeout = 0;
//                                         long endTime = System.currentTimeMillis();
//                                         long startTime = endTime - 1000 * 60 * 60 * 24 * 30L;
//                                         HiHealthDataQuery stepHealthDataQuery = new HiHealthDataQuery(HiHealthPointType.DATA_POINT_STEP_SUM,
//                                                 startTime, endTime, new HiHealthDataQueryOption());
//                                         HiHealthDataStore.execQuery(mContext, stepHealthDataQuery, timeout, new ResultCallback() {
//                                             @Override
//                                             public void onResult(int resultCode, Object data) {
//                                                 Log.i(TAG, "query steps resultCode: " + resultCode);
//                                                 String result = "";
//                                                 if (resultCode == HiHealthError.SUCCESS) {
//                                                     List dataList = (ArrayList) data;
//                                                     if (dataList.size() >= 1) {
//                                                         HiHealthPointData pointData = (HiHealthPointData) dataList.get(dataList.size() - 1);
//                                                         result = result + pointData.getValue();
//                                                         Log.i(TAG, "query steps resultCode: QUERY STEPS " + result);
////                                                         tvR.setText("DIRECT SET TEXT " + result);
//                                                     }
//                                                 }
//                                                 combineResult(resultCode, result);
//                                             }
//                                         });
//                                     }
//                                 });
//        start.setOnClickListener(new View.OnClickListener() {
//            final String s = sportType;
//
//            final Vibrator vibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
//            @Override
//            public void onClick(View v) {
//                runningThread = true;
//                setHealthData();
//                int st = 258;
//                if (s == null){
//                    start.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.nav_default_pop_enter_anim));
//                    HiHealthDataStore.startSport(mContext, st, (resultCode, message) -> {
//                        combineResult(resultCode, message);
//                        if (resultCode == HiHealthError.SUCCESS) {
//                            HiHealthDataStore.registerSportData(mContext, sportDataCallback);
//                            vibrator.vibrate(300);
//                        }
//                    });
//                }else {
//                    start.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.nav_default_pop_enter_anim));
//                    HiHealthDataStore.startSport(mContext, Integer.parseInt(sportType), (resultCode, message) -> {
//                        combineResult(resultCode, message);
//                        if (resultCode == HiHealthError.SUCCESS) {
//                            HiHealthDataStore.registerSportData(mContext, sportDataCallback);
//                            vibrator.vibrate(300);
//                        }
//                    });
//                }
//            }
//        });
//        stop.setOnClickListener(new View.OnClickListener() {
//            final Vibrator vibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
//            @Override
//            public void onClick(View v) {
//                if (timer != null) {
//                    timer.cancel();
//                    timer = new Timer();
//                }
//                runningThread = false;
//                stop.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.nav_default_pop_enter_anim));
//                vibrator.vibrate(100);
//                HiHealthDataStore.stopSport(mContext, (resultCode, message) -> {
//                    combineResult(resultCode, message);
//                    if (resultCode == HiHealthError.SUCCESS) {
//                        HiHealthDataStore.unregisterSportData(mContext, sportDataCallback);
//
//                    }
//                });
//            }
//        });
    }

    private void startSport() {
        runExercise(1);
        setHealthData();
        startActivive();
//        HiHealthDataStore.startSport(mContext, 258, (resultCode, message) -> {
//            combineResult(resultCode, message);
//            if (resultCode == HiHealthError.SUCCESS) {
//
//                HiHealthDataStore.registerSportData(mContext, sportDataCallback);
//            }
//        });
    }

    private void startActivive() {
        final ActivityRecordsController activityRecordsController = HuaweiHiHealth.getActivityRecordsController(getApplicationContext());

        // Build the start time of the activity.
        long startTime = Calendar.getInstance().getTimeInMillis();
        // Build the ActivityRecord object and set the start time of the activity record.
        ActivityRecord activityRecord = new ActivityRecord.Builder()
                .setId("MyBeginActivityRecordId")
                .setName("BeginActivityRecord")
                .setDesc("This is ActivityRecord begin test!")
                .setActivityTypeId(HiHealthActivities.ELLIPTICAL)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .build();

        // Call beginActivityRecord.
        Task<Void> task1 = activityRecordsController.beginActivityRecord(activityRecord);
        // Add a listener for the ActivityRecord start success.
        task1.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "MyActivityRecord begin success", Toast.LENGTH_LONG).show();
                Log.i("ActivityRecords","MyActivityRecord begin success");
            }
            // Add a listener for the ActivityRecord start failure.
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                String errorCode = e.getMessage();
                String errorMsg = HiHealthStatusCodes.getStatusCodeMessage(Integer.parseInt(errorCode));
                Log.i("ActivityRecords",errorCode + ": " + errorMsg);
            }
        });
    }
    private void stopActivive(){
        final ActivityRecordsController activityRecordsController = HuaweiHiHealth.getActivityRecordsController(getApplicationContext());

        // Call endActivityRecord to stop the activity record. The input parameter is the ID string of ActivityRecord or null.
        // Stop an activity record of the current app by specifying the ID string as the input parameter.
        // Stop activity records of the current app by specifying null as the input parameter.
        Task<List<ActivityRecord>> endTask = activityRecordsController.endActivityRecord("MyBeginActivityRecordId");
        endTask.addOnSuccessListener(new OnSuccessListener<List<ActivityRecord>>() {
            @Override
            public void onSuccess(List<ActivityRecord> activityRecords) {
                Log.i("ActivityRecords","MyActivityRecord End success");
                // Return the list of activity records that have stopped.
                if (activityRecords.size() > 0) {
                    for (ActivityRecord activityRecord : activityRecords) {
                        DateFormat dateFormat = DateFormat.getDateInstance();
                        DateFormat timeFormat = DateFormat.getTimeInstance();
                        Log.i("ActivityRecords", "Returned for ActivityRecord: " + activityRecord.getName() + "\n\tActivityRecord Identifier is "
                                + activityRecord.getId() + "\n\tActivityRecord created by app is " + activityRecord.getPackageName()
                                + "\n\tDescription: " + activityRecord.getDesc() + "\n\tStart: "
                                + dateFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS)) + " "
                                + timeFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS)) + "\n\tEnd: "
                                + dateFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS)) + " "
                                + timeFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS)) + "\n\tActivity:"
                                + activityRecord.getActivityType());
                    }
                } else {
                    // null will be returned if the activity record hasn't stopped.
                    Log.i("ActivityRecords","MyActivityRecord End response is null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                String errorCode = e.getMessage();
                String errorMsg = HiHealthStatusCodes.getStatusCodeMessage(Integer.parseInt(errorCode));
                Log.i("ActivityRecords",errorCode + ": " + errorMsg);
            }
        });
    }

    private void stopSport(){

        tv_workout_state_message.setText("Завершена");
        tv_workout_state_message.setTextColor(getColor(R.color.colorRed_900));
        start_btn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
        if (timer != null) {
            timer.cancel();
            timer = new Timer();
        }
        runningThread = false;
        isRun = false;
        stopActivive();
        surroundCardView.setDuration(2000);
        surroundCardView.setSurroundStrokeWidth(R.dimen.scv_stroke_width_default);
        surroundCardView.setSurroundStrokeColor(R.color.colorRed_900);
        surroundCardView.setStartPoint(StartPoint.TOP_START);
        surroundCardView.surround();
        surroundCardView.release();
        saveWorkout();
        HiHealthDataStore.stopSport(mContext, (resultCode, message) -> {
            combineResult(resultCode, message);
            if (resultCode == HiHealthError.SUCCESS) {
                HiHealthDataStore.unregisterSportData(mContext, sportDataCallback);
                Toast.makeText(getApplicationContext(), "WORKOUT WAS STOPPED", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveWorkout(){
        HashMap list = new HashMap();

        list.put("bpm", heartModelsList);
        list.put("isCompleted", true);
        Log.i(TAG, "gsuccess heartModelsList =============== list :" + String.valueOf(list));
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("workout").child(String.valueOf(i)).child("stats");
        dbRef.updateChildren(list);
        TimerTask splash = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        };

        Timer delay = new Timer();
        delay.schedule(splash, 3000);
    }


    private void bindDataView() {
        mContext = this;
        pause = findViewById(R.id.cv_pause);
        start = findViewById(R.id.cv_start);
        stop = findViewById(R.id.cv_stop);
        tvHeart = findViewById(R.id.heart_bpm);
        start_btn = findViewById(R.id.iv_start);
        tv_exercise_name_next = findViewById(R.id.tv_exercise_name_next);
        tv_exercise_name_current = findViewById(R.id.tv_exercise_name_current);
        tv_workout_state_message = findViewById(R.id.tv_workout_state_message);
        tv_current_exercise_count_of_panelInfo = findViewById(R.id.tv_current_exercise_count_of_panelInfo);
        tv_current_exercise_count_panelInfo = findViewById(R.id.tv_current_exercise_count_panelInfo);
        tv_repeats = findViewById(R.id.tv_repeats);
        tv_add_weight = findViewById(R.id.tv_add_weight);
        imageView_CenterCircut = findViewById(R.id.imageView_CenterCircut);
        total_time_countdown = findViewById(R.id.total_time_countdown);
    }

    private final HiSportDataCallback sportDataCallback = new HiSportDataCallback() {

        @Override
        public void onResult(int resultCode) {
            Log.i(TAG, "resultCode:" + resultCode);
            combineResult(resultCode, "status changed");
        }

        @Override
        public void onDataChanged(int state, Bundle bundle) {
            if (state == 2) {
                for (String key : bundle.keySet()) {
                    tvHeart.setText((String.valueOf(bundle.get("heartRate"))));
//                    tvSteps.setText((String.valueOf(bundle.get("totalSteps"))));
//                    tvDuration.setText((String.valueOf(bundle.get("duration"))));
                    bpm = (int) bundle.get("heartRate");
//                    steps = bundle.getInt("totalSteps");
//                    tv_workout_state_message.setText(tvHeart.getText());
                    getState(Integer.parseInt(String.valueOf(tvHeart.getText())));
                    break;
                }
            }
        }
    };

    private static class FitnessData {
        int bpm;
        int steps;
        int duration;

        public FitnessData(int bpm, int steps, int duration) {
            this.bpm = bpm;
            this.steps = steps;
            this.duration = duration;
        }
    }


    @SuppressLint("HandlerLeak")
    private static class MessageHandler extends Handler {
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            handleMessageInfo(msg);
        }
        private void handleMessageInfo(Message msg) {
            String result = String.valueOf(msg.obj);
//            tvResults.setText(result);
        }
    }
    public void sendMessage(String result) {
        Message message = Message.obtain();
        message.obj = result;
        mHandler.sendMessage(message);
    }
    public void combineResult(int resultCode, Object object) {
        String sb = String.valueOf(resultCode) +
                object;
        sendMessage(sb);
    }

    //
    //** STATE BPM CIRCUT COLOR
    //
    public void getState(int bpm) {
        if(bpm <= 10) {
            stateBpm = this.findViewById(R.id.tv_workout_state_message);
//            stateBpm.setText("Нет данных");
        }
        if(bpm <= 100&& bpm >= 10) {
            setBlue();
        }
        if(bpm >= 101) {
            setGreen();
        }
        if(bpm >= 141) {
            setOrange();
        }
        if(bpm >= 150) {
            setRed();
        }
    }

    public void setGreen() {
        green = this.findViewById(R.id.ll_top_circut_green_dark);
        orange = this.findViewById(R.id.ll_top_circut_orange_dark);
        blue = this.findViewById(R.id.ll_bottom_circut_blue_dark);
        red = this.findViewById(R.id.ll_bottom_circut_red_dark);
        stateBpm = this.findViewById(R.id.tv_workout_state_message);
        titleBpm = this.findViewById(R.id.heart_bpm_title);
        numBpm = this.findViewById(R.id.heart_bpm);
        green.setImageResource(R.drawable.green_light);
        orange.setImageResource(R.drawable.orange_dark);
        blue.setImageResource(R.drawable.blue_dark);
        red.setImageResource(R.drawable.red_dark);
        if(type != null){
            if(type.equals("1")){
                stateBpm.setTextColor(Color.parseColor("#00FF55"));
                stateBpm.setText("ОТЛИЧНО");
                titleBpm.setTextColor(Color.parseColor("#00FF55"));
                numBpm.setTextColor(Color.parseColor("#00FF55"));
            }
        }

    }
    public void setOrange() {
        green = this.findViewById(R.id.ll_top_circut_green_dark);
        orange = this.findViewById(R.id.ll_top_circut_orange_dark);
        blue = this.findViewById(R.id.ll_bottom_circut_blue_dark);
        red = this.findViewById(R.id.ll_bottom_circut_red_dark);
        stateBpm = this.findViewById(R.id.tv_workout_state_message);
        titleBpm = this.findViewById(R.id.heart_bpm_title);
        numBpm = this.findViewById(R.id.heart_bpm);
        orange.setImageResource(R.drawable.orangle_light);
        green.setImageResource(R.drawable.green_dark);
        blue.setImageResource(R.drawable.blue_dark);
        red.setImageResource(R.drawable.red_dark);
        if(type != null) {
            if (type.equals("1")) {
                stateBpm.setTextColor(Color.parseColor("#FFC400"));
                stateBpm.setText("МЕДЛЕННЕЕ");
                titleBpm.setTextColor(Color.parseColor("#FFC400"));
                numBpm.setTextColor(Color.parseColor("#FFC400"));
            }
        }
    }
    public void setBlue() {
        green = this.findViewById(R.id.ll_top_circut_green_dark);
        orange = this.findViewById(R.id.ll_top_circut_orange_dark);
        blue = this.findViewById(R.id.ll_bottom_circut_blue_dark);
        red = this.findViewById(R.id.ll_bottom_circut_red_dark);
        stateBpm = this.findViewById(R.id.tv_workout_state_message);
        titleBpm = this.findViewById(R.id.heart_bpm_title);
        numBpm = this.findViewById(R.id.heart_bpm);
        blue.setImageResource(R.drawable.blue_light);
        green.setImageResource(R.drawable.green_dark);
        orange.setImageResource(R.drawable.orange_dark);
        red.setImageResource(R.drawable.red_dark);
        if(type != null) {
            if (type.equals("1")) {
                stateBpm.setTextColor(Color.parseColor("#00EFFF"));
                stateBpm.setText("БЫСТРЕЕ");
                titleBpm.setTextColor(Color.parseColor("#00EFFF"));
                numBpm.setTextColor(Color.parseColor("#00EFFF"));
            }
        }
    }
    public void setRed() {
        green = this.findViewById(R.id.ll_top_circut_green_dark);
        orange = this.findViewById(R.id.ll_top_circut_orange_dark);
        blue = this.findViewById(R.id.ll_bottom_circut_blue_dark);
        red = this.findViewById(R.id.ll_bottom_circut_red_dark);
        stateBpm = this.findViewById(R.id.tv_workout_state_message);
        titleBpm = this.findViewById(R.id.heart_bpm_title);
        numBpm = this.findViewById(R.id.heart_bpm);
        red.setImageResource(R.drawable.red_light);
        green.setImageResource(R.drawable.green_dark);
        orange.setImageResource(R.drawable.orange_dark);
        blue.setImageResource(R.drawable.blue_dark);
        if(type != null) {
            if (type.equals("1")) {
                stateBpm.setTextColor(Color.parseColor("#FF3300"));
                stateBpm.setText("МЕДЛЕННЕЕ");
                titleBpm.setTextColor(Color.parseColor("#FF3300"));
                numBpm.setTextColor(Color.parseColor("#FF3300"));
            }
        }
    }
}