package be.kuleuven.softdev.blendfangnan.timers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        String action = intent.getStringExtra("action");
        if(action.equals("action1")){
            performAction1();
        }
        else if(action.equals("action2")){
            performAction2();

        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void performAction1(){
        MainActivity.mainActivity.pause();
        if (MainActivity.mainActivity.pause)
            Toast.makeText(MainActivity.mainActivity,"Timer paused.",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MainActivity.mainActivity,"Timer resumed.",Toast.LENGTH_SHORT).show();
    }

    public void performAction2(){
        Toast.makeText(MainActivity.mainActivity,"App closed.",Toast.LENGTH_SHORT).show();
        MainActivity.mainActivity.pause = true;
        MainActivity.mainActivity.mBuilder.setTimeoutAfter(1);
        MainActivity.mainActivity.mNotificationManager.notify(0, MainActivity.mainActivity.mBuilder.build());
        MainActivity.mainActivity.finish();

    }

}