package be.kuleuven.softdev.blendfangnan.timers;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MainActivity extends AppCompatActivity {

    private List<MyTimer> timersList = new ArrayList<>();
    private List<MyTimer> currentTimers = new ArrayList<>();
    private RecyclerView recyclerView;
    private TimersAdapter mAdapter;
    NotificationManager mNotificationManager;
    Notification.Builder mBuilder;
    static MainActivity mainActivity;
    public boolean pause = true;
    Button pauseButton;
    MyTimer myTimer1;
    Integer receivedTime;
    String receivedLabel;
    int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mAdapter = new TimersAdapter(timersList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mainActivity = MainActivity.this;

        /**
         * Notify MainActivity class that something is happening in the Adapter class.
         */
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        /**
         * get from DB
         */
        if (ConnectivityHelper.isConnectedToNetwork(MainActivity.this)) {
            getTimerListFromDB();
            Toast.makeText(MainActivity.this, "Added from DB successfully.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this,"No internet connection available to communicate with DB.", Toast.LENGTH_SHORT).show();
        }
        //prepareTimerData();

        /**
         * check if app is opened for the first time to display instructions
         */
        final String PREFS_NAME = "Preference";
        Boolean isFirstRun = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            customDialogFirstTimeOpened();
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean("isFirstRun", false).commit();
        }

        /** Update Everything **/
        Update update = new Update();
        new Thread(update).start();


        pauseButton = (Button) findViewById(R.id.button1);
        Button startOverButton = (Button) findViewById(R.id.button2);
        Button removeAllButton = (Button) findViewById(R.id.button11);
        Button addNewButton = (Button) findViewById(R.id.button4);
        Button getFromDbButton = (Button) findViewById(R.id.button10);
        Button syncToDbButton = (Button) findViewById(R.id.button9);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pause)
                    pause = false;
                else
                    pause = true;
                customNotification();
            }
        });

        /**
         * getDatabaseList() function can be called in the thread update
         */
        startOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOver();
                mAdapter.notifyDataSetChanged();
            }
        });

        removeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = true;
                timersList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogAddTimer();
            }
        });

        getFromDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityHelper.isConnectedToNetwork(MainActivity.this)) {
                    getTimerListFromDB();
                    Toast.makeText(MainActivity.this, "Added from DB successfully.", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this,"No internet connection available.", Toast.LENGTH_SHORT).show();
            }
        });

        syncToDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityHelper.isConnectedToNetwork(MainActivity.this)) {
                    getCurrentTimers();
                    customDialogSyncChoice();
                }
                else
                    Toast.makeText(MainActivity.this,"No internet connection available.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /**
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_about_app) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
         */

        if (id == R.id.action_about_app) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void startOver() {
        for (MyTimer timer: timersList) {
            timer.setSecondsLeft(timer.getSeconds());
            timer.setInitiated(false);
            timer.setActive(false);
        }
        pause = true;
    }

    public void pause() {
        if (pause)
            pause = false;
        else
            pause = true;
    }

    public void customDialogFirstTimeOpened() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.mainActivity);
        builderSingle.setTitle("Hi, welcome to Timers!");
        builderSingle.setMessage("Instructions on how to use the app can be found by clicking in the three dots in the top right corner of the screen.");
        builderSingle.setCancelable(false);

        builderSingle.setNegativeButton("No, thank you", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"Operation canceled.", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.setPositiveButton("Show instructions", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this,"Synced successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.show();
    }

    public void customDialogAddTimer() {
        LayoutInflater factory = LayoutInflater.from(this);
        View textEntryView = factory.inflate(R.layout.alert_dialog, null);

        LinearLayout layout1 = (LinearLayout) textEntryView.findViewById(R.id.layout1);
        LinearLayout layout2 = (LinearLayout) textEntryView.findViewById(R.id.layout2);

        final EditText editText1 = (EditText) textEntryView.findViewById(R.id.editText1);

        final NumberPicker numberPicker1 = (NumberPicker) textEntryView.findViewById(R.id.numberPicker1);
        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(59);

        final NumberPicker numberPicker2 = (NumberPicker) textEntryView.findViewById(R.id.numberPicker2);
        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(59);

        layout1.setMinimumWidth(layout2.getWidth());

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.mainActivity);
        builderSingle.setTitle("Add new timer");
        builderSingle.setMessage("Enter your label and time:");

        builderSingle.setView(textEntryView);

        builderSingle.setNegativeButton("Default", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timersList.add(new MyTimer(60, "No label"));
                mAdapter.notifyDataSetChanged();
            }
        });

        builderSingle.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!(numberPicker1.getValue() == 0 && numberPicker2.getValue() == 0))
                    receivedTime = ((numberPicker1.getValue() * 60) + (numberPicker2.getValue()));
                else
                    receivedTime = 60;

                if (!editText1.getText().toString().equals(""))
                    receivedLabel = editText1.getText().toString();
                else
                    receivedLabel = "No label";

                timersList.add(new MyTimer(receivedTime, receivedLabel));
                mAdapter.notifyDataSetChanged();
            }
        });

        builderSingle.setNeutralButton("Default list", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prepareTimerData();
            }
        });

        builderSingle.show();
    }

    public void customDialogSyncChoice () {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.mainActivity);
        builderSingle.setTitle("Choose sync method");
        builderSingle.setMessage("Option 1: Add missing timers to database and update current timer list.\n\nOption 2: Clear database and sync current timer list.");

        builderSingle.setNegativeButton("Option 1", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearTimers();
                clearTimerList();
                syncAllTimers();
                customDialogConfirmation1();
            }
        });

        builderSingle.setPositiveButton("Option 2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearTimers();
                clearTimerList();
                currentTimers.clear();
                customDialogConfirmation2();
            }
        });

        builderSingle.show();
    }

    public void customDialogConfirmation1() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.mainActivity);
        builderSingle.setTitle("Add missing timers to database and update current timer list");
        builderSingle.setMessage("Are you sure you want to continue?");
        builderSingle.setCancelable(false);

        builderSingle.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"Operation canceled.", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                syncTimerList();
                Toast.makeText(MainActivity.this,"Synced successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.show();
    }

    public void customDialogConfirmation2() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.mainActivity);
        builderSingle.setTitle("Clear database and sync current timer list");
        builderSingle.setMessage("Are you sure you want to continue?");
        builderSingle.setCancelable(false);

        builderSingle.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"Operation canceled.", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                syncAllTimers();
                syncTimerList();
                Toast.makeText(MainActivity.this,"Synced successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.show();
    }

    public void customNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("channel_1", "default", NotificationManager.IMPORTANCE_MIN);
            mChannel.setDescription("Default");
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit intent for an Activity in your app
        // Intent intent = new Intent(this, MainActivity.class);
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // intents for notification center buttons
        /*
        Intent intentAction = new Intent(this, ActionReceiver.class);
        intentAction.putExtra("action", "action1");
        intentAction.putExtra("action", "action2");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
        */

        Intent intentAction1 = new Intent(this, ActionReceiver.class);
        intentAction1.putExtra("action", "action1");
        Intent intentAction2 = new Intent(this, ActionReceiver.class);
        intentAction2.putExtra("action", "action2");
        PendingIntent pIntent1 = PendingIntent.getBroadcast(this, 0, intentAction1, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntent2 = PendingIntent.getBroadcast(this, 1, intentAction2, PendingIntent.FLAG_UPDATE_CURRENT);

        // building the actions
        Notification.Action actionPause = new Notification.Action.Builder(R.drawable.ic_stat_pause, "Resume/Pause", pIntent1).build();
        Notification.Action actionStop = new Notification.Action.Builder(R.drawable.ic_stat_stop, "Stop", pIntent2).build();

        mBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = new Notification.Builder(this, "channel_1");
        } else {
            mBuilder = new Notification.Builder(this);
        }
        mBuilder.setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setContentTitle("Timers is running");
        mBuilder.setContentText("Timer: " + timersList.get(currentIndex).getLabel() + " - " + timersList.get(currentIndex).showSecondsLeftProperly());
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.addAction(actionPause);
        mBuilder.addAction(actionStop);
        mBuilder.setOngoing(true);
        mBuilder.setAutoCancel(true);


        mNotificationManager.notify(0, mBuilder.build());
    }

    private void prepareTimerData() {
        myTimer1 = new MyTimer(90, "Squats");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(30, "Break");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(30, "Push-ups");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(30, "Break");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(45, "Mountain Climbing");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(30, "Break");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(60, "Lunges");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(30, "Break");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(45, "Jumping Jacks");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(30, "Break");
        timersList.add(myTimer1);
        myTimer1 = new MyTimer(90, "Plank");
        timersList.add(myTimer1);

        mAdapter.notifyDataSetChanged();
    }

    private void getTimerListFromDB() {
        // initiation
        RequestQueue queue = Volley.newRequestQueue(this);

        // JSON Array Request
        String url2 = "https://studev.groept.be/api/a18_sd502/select_timerlist";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url2, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject element = response.getJSONObject(i);
                        timersList.add(new MyTimer(element.getInt("time"), element.getString("label")));
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // add to queue
        queue.add(jsonArrayRequest);
    }

    private void getCurrentTimers() {
        // initiation
        RequestQueue queue = Volley.newRequestQueue(this);

        currentTimers.clear();

        // JSON Array Request
        String url2 = "https://studev.groept.be/api/a18_sd502/select_all_timers";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url2, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject element = response.getJSONObject(i);
                        currentTimers.add(new MyTimer(element.getInt("time"), element.getString("label")));
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // add to queue
        queue.add(jsonArrayRequest);
    }

    private void clearTimerList() {
        // initiation
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://studev.groept.be/api/a18_sd502/delete_timerlist";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // add to queue
        queue.add(jsonArrayRequest);
    }

    private void clearTimers() {
        // initiation
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://studev.groept.be/api/a18_sd502/delete_all_timers";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // add to queue
        queue.add(jsonArrayRequest);
    }

    private void syncAllTimers() {
        // initiation
        RequestQueue queue = Volley.newRequestQueue(this);

        Set<MyTimer> timersSet = new HashSet<>(currentTimers);
        timersSet.addAll(timersList);

        String url = "https://studev.groept.be/api/a18_sd502/insert_into_timers_withid/";
        int id = 1;
        String url2;
        for (MyTimer timer: timersSet) {
            // url2 = url + String.valueOf(timer.getSeconds()) + "/" + timer.getLabel();
            url2 = url + String.valueOf(id) + "/" + String.valueOf(timer.getSeconds()) + "/" + timer.getLabel();

            // JSON Array Request
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url2, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            // add to queue
            queue.add(jsonArrayRequest);
            url2 = null;
            id++;
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.N)

    private void syncTimerList() {
        // initiation
        RequestQueue queue = Volley.newRequestQueue(this);

        Set<MyTimer> timersSet = new HashSet<>(currentTimers);
        timersSet.addAll(timersList);
        List<MyTimer> timersSetList = new ArrayList<>(timersSet);

        String url = "https://studev.groept.be/api/a18_sd502/insert_into_timerlist/";
        int id = 1;
        String url2;
        for (final MyTimer timer: timersList) {
            if (timersSetList.stream()
                    .anyMatch(new Predicate<MyTimer>() {
                        @Override
                        public boolean test(MyTimer t) {
                            boolean checkLabel = t.getLabel().equals(timer.getLabel());
                            boolean checkTime = t.getSeconds() == timer.getSeconds();
                            if (checkLabel && checkTime)
                                return true;
                            return false;
                        }
            })) {
                url2 = url + String.valueOf(id) + "/" +
                        String.valueOf(timersSetList.indexOf(timersSetList.stream()
                                .filter(new Predicate<MyTimer>() {
                                    @Override
                                    public boolean test(MyTimer t) {
                                        boolean checkLabel = t.getLabel().equals(timer.getLabel());
                                        boolean checkTime = t.getSeconds() == timer.getSeconds();
                                        if (checkLabel && checkTime)
                                            return true;
                                        return false;
                                    }
                                })
                                .findAny()
                                .get()) + 1) + "/";
            }
            else
                continue;

            // JSON Array Request
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url2, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            // add to queue
            queue.add(jsonArrayRequest);
            url2 = null;
            id++;
        }
    }

    private List<MyTimer> getUniqueElements(List<MyTimer> list1, List<MyTimer> list2) {
        List<MyTimer> uniqueTimersList = new ArrayList<>();
        int count = 0;
        for (MyTimer myTimer1: list1) {
            for (MyTimer myTimer2: list2) {
                if ((myTimer1.getSeconds() == myTimer2.getSeconds()) && (myTimer1.getLabel().equals(myTimer2.getLabel())))
                    count++;
            }
            if (count == 0) {
                uniqueTimersList.add(myTimer1);
            }
            count = 0;
        }

        return uniqueTimersList;
    }

    /**
     * Updates timers and plays notification sound.
     */
    class Update implements Runnable {

        public Update() {
        }

        public void run() {
            try {
                while (!(new Thread(this).isInterrupted())) {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < timersList.size(); i++) {
                                if (!timersList.get(i).isFinished()) {
                                    if (!timersList.get(i).isActive()) {
                                        timersList.get(i).setInitiated(true);
                                        timersList.get(i).setActive(true);
                                        break;
                                    }
                                    else {
                                        break;
                                    }
                                }
                            }

                            if (pause) {
                                //pauseButton.setText("RESUME");
                                pauseButton.setPadding(0,20,0,0);
                                pauseButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play_arrow_white_48dp, 0, 0);
                            }

                            else {
                                //pauseButton.setText("PAUSE");
                                pauseButton.setPadding(0,24,0,0);
                                pauseButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pause_white_44dp, 0, 0);
                            }

                            int index = 0;
                            for (MyTimer timer: timersList) {
                                if (timer.isFinished()) {
                                    if (timer.isInitiated()) {
                                        try {
                                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                            r.play();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    timer.setInitiated(false);
                                    timer.setActive(false);
                                }
                                if (timer.isActive()) {
                                    if (!pause) {
                                        timer.decrement();
                                        currentIndex = index;
                                        mAdapter.notifyItemChanged(currentIndex);
                                        MainActivity.mainActivity.customNotification();
                                    }
                                }
                                index++;
                            }

                            int count = 0;
                            for (int i = 0; i < timersList.size(); i++){
                                if (timersList.get(i).isActive())
                                    count++;
                            }
                            if (count > 0)
                                for (MyTimer timer: timersList)
                                    timer.setActive(false);



                        }
                    });
                }
            } catch (InterruptedException e1) {
            }
        }
    }
}
