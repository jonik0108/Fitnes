package com.zadira.fitnes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView head, count_repeat, count_task, now, next, mTimer;
    ImageView image,playpause;
    Boolean o=false;
    int count=0;int m=0; int p=0; int i = 0; int j = 1; int l = 0;int s=0;
    SharedPreferences preferences ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //инициализация

        preferences = getApplicationContext().getSharedPreferences("fromCache", Context.MODE_PRIVATE);

        init1();
    }
    @Override
    protected void onResume(){
        sharedprefadd();
        super.onResume();
    }
    private void init1(){
        head = findViewById(R.id.header_title);
        count_repeat = findViewById(R.id.count_repeat);
        count_task = findViewById(R.id.count_tasks);
        now = findViewById(R.id.now_text);
        next = findViewById(R.id.next_task);
        image = findViewById(R.id.image);
        mTimer = findViewById(R.id.time_text);
        playpause=findViewById(R.id.playpauze);
    }
    @SuppressLint("SetTextI18n")
    private void sharedprefadd(){
        if (preferences.getBoolean("isFinish",false)){
            toast("Заданий больше нет");
        }else {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("fromCache", Context.MODE_PRIVATE);
            sport();

            int fromCacheI = preferences.getInt("KeyI",0);
            int fromCacheJ = preferences.getInt("KeyJ",1);
            int fromCacheP = preferences.getInt("KeyP", 0);

            ArrayList<User> day1;
            day1 = new Gson().fromJson(preferences.getString("day1", "[]"), new TypeToken<ArrayList<User>>() {
            }.getType());
            //get(0) это означает что когда первый день
            head.setText(day1.get(0).name);
            String nowtext = day1.get(0).tasks.get(fromCacheI);
            String nexttext = day1.get(0).tasks.get(fromCacheJ);
            now.setText("" + nowtext);
            next.setText("" + nexttext);
            image.setImageResource(R.drawable.ic_launcher_background);
            count_task.setText((fromCacheP - 1)+" из " + day1.get(0).count.size());
            count_repeat.setText("" + day1.get(0).count.get(fromCacheI));
        }
    }
    private void sport(){

        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getString("day1", "[]").equals("[]")) {
            ArrayList<User> day1 = new ArrayList<>();
            //задания
            ArrayList<String> task1 = new ArrayList<>();
            task1.add("1 Задание");
            task1.add("2 Задание");
            task1.add("3 Задание");
            task1.add("4 Задание");
            task1.add("5 Задание");
            task1.add("6 Задание");
            task1.add("Заданий больше нет");
            //изображения
            ArrayList<Integer> rasm = new ArrayList<>();
            rasm.add(R.drawable.one);
            rasm.add(R.drawable.two);
            rasm.add(R.drawable.three);
            rasm.add(R.drawable.one);
            rasm.add(R.drawable.two);
            rasm.add(R.drawable.one);
            //количество повторений
            ArrayList<Integer> count_repeat = new ArrayList<>();
            count_repeat.add(1);
            count_repeat.add(2);
            count_repeat.add(3);
            count_repeat.add(2);
            count_repeat.add(3);
            count_repeat.add(1);
            //время прохождения
            ArrayList<Integer> time_list = new ArrayList<>();
            time_list.add(20000);
            time_list.add(22000);
            time_list.add(23000);
            time_list.add(25000);
            time_list.add(23000);
            time_list.add(23000);
            //все что выше добавляем в массив под именем day1
            day1.add(new User("Зарядка", task1, rasm, count_repeat, time_list));
            //сохраняем
            editor.putString("day1", new Gson().toJson(day1)).apply();
        }
    }
    private void run(){
        new CountDownTimer(10000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 1000 < 10) {
                    toast("До начала старта\n  00:0" + millisUntilFinished / 1000);
                    if (millisUntilFinished/1000==9){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.start);
                        mp.start();
                    }
                } else {
                    mTimer.setText("00:" + millisUntilFinished / 1000);
                    if (millisUntilFinished/1000==9){
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.start);
                    mp.start();
                    }
                }
            }

            public void onFinish() {
                run2();
            }
        }.start();

    }
    private void run2(){

        ArrayList<User> day1;
        day1 = new Gson().fromJson(preferences.getString("day1", "[]"), new TypeToken<ArrayList<User>>() {}.getType());
        int a;

        l = preferences.getInt("Time",0);
        if(l==day1.get(0).time.size() || l>day1.get(0).time.size()){
            a=0;
        }else {
            a=day1.get(0).time.get(l);
            l++;
        }
        preferences.edit().putInt("Time",l).apply();
        new CountDownTimer(a, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                if (l / 1000 < 10) {
                    mTimer.setText("00:0" + l / 1000);
                    if (l/1000==3){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.music2);
                        mp.start();
                    }
                } else {
                    mTimer.setText("00:"+ l / 1000);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                mTimer.setText("00:00");
                set2();

            }
        }.start();

    }
    @SuppressLint("SetTextI18n")
    private void set2(){

        ArrayList<User> day1 =  new Gson().fromJson(preferences.getString("day1", "[]"), new TypeToken<ArrayList<User>>() {}.getType());

        int i = preferences.getInt("KeyI",0);
        int j = preferences.getInt("KeyJ",1);

        i++;

        if (j < day1.get(0).tasks.size()){
            j++;
        }

        preferences.edit().putInt("KeyI",i).apply();
        preferences.edit().putInt("KeyJ",j).apply();

        if (i == day1.get(0).tasks.size() || i>day1.get(0).tasks.size()) {
            now.setText("Заданий больше нет");
        } else {
            now.setText(day1.get(0).tasks.get(i));
        }
        if (j == day1.get(0).tasks.size() || j>day1.get(0).tasks.size()) {
            next.setText("Заданий больше нет");
        } else {
            next.setText(day1.get(0).tasks.get(j));
        }

        if (i==day1.get(0).rasm.size() || i>day1.get(0).rasm.size()){
            image.setImageResource(R.drawable.ic_launcher_background);
        }else {
            image.setImageResource(day1.get(0).rasm.get(i));
        }
        if (i==day1.get(0).count.size() || i>day1.get(0).count.size()){
            count_repeat.setText("-");
        }else {
            count_repeat.setText(""+day1.get(0).count.get(i));
        }

        if (p==day1.get(0).count.size()||p>day1.get(0).count.size()){
            count_task.setText(""+day1.get(0).count.size()+" из "+day1.get(0).count.size());
            o=true;
        }else {
            p = preferences.getInt("KeyP",1);
            count_task.setText(p+" из "+ day1.get(0).count.size());

            p++;
            preferences.edit().putInt("KeyP",p).apply();
        }
        if (o){
            count=1;
            preferences.edit().putInt("Boolea",count).apply();
            m=0;
            playpause.setImageResource(R.drawable.ic_playwithcircularbuttonwithrightarrowofboldroundedfilledtriangle_80162);
            Toast.makeText(this,"SuccesFull",Toast.LENGTH_SHORT).show();
            preferences.edit().putBoolean("isFinish",true).apply();



        }else {
            run();
            s=0;
        }


    }
    private void toast(String text){
        final Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(toast::cancel, 500);
    }
    public void onsstart(View view){

        ArrayList<User> day1;

        int fromCacheI = preferences.getInt("KeyI",0);


        day1 = new Gson().fromJson(preferences.getString("day1", "[]"), new TypeToken<ArrayList<User>>() {
        }.getType());
        if (fromCacheI==day1.get(0).rasm.size() || fromCacheI>day1.get(0).rasm.size()){
            image.setImageResource(R.drawable.ic_launcher_background);
        }else {
            image.setImageResource(day1.get(0).rasm.get(fromCacheI));
        }
        playpause.setImageResource(R.drawable.pauze);
        int coount = preferences.getInt("Boolea",0);
        if (coount==1){

            toast("Tugadi");
            playpause.setImageResource(R.drawable.ic_playwithcircularbuttonwithrightarrowofboldroundedfilledtriangle_80162);

        }else {
            if (m==0){
                run();
                m++;
            }
        }

    }

}