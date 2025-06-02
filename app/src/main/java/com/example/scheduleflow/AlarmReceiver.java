package com.example.scheduleflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Create an intent to launch TargetActivity when the alarm triggers.
        Intent activityIntent = new Intent(context, TargetActivity.class);
        // This flag is required when starting an activity from a non-activity context.
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}
