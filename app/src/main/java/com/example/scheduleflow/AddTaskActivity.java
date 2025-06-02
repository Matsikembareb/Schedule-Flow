package com.example.scheduleflow;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.os.Build;


public class AddTaskActivity extends AppCompatActivity {

    // Will hold the user-selected date and time
    private Calendar selectedDateTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        EditText editTextTitle = findViewById(R.id.editTextTitle);
        Button buttonSelectDateTime = findViewById(R.id.buttonSelectDateTime);
        TextView textViewSelectedDateTime = findViewById(R.id.textViewSelectedDateTime);
        Button buttonSaveTask = findViewById(R.id.buttonSaveTask);

        // Set better button styling
        buttonSaveTask.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
        buttonSaveTask.setTextColor(getResources().getColor(R.color.white));

        buttonSelectDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar currentDate = Calendar.getInstance();
                selectedDateTime = Calendar.getInstance();

                new DatePickerDialog(AddTaskActivity.this, (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(AddTaskActivity.this, (timeView, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        selectedDateTime.set(Calendar.SECOND, 0);
                        selectedDateTime.set(Calendar.MILLISECOND, 0);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        textViewSelectedDateTime.setText(sdf.format(selectedDateTime.getTime()));
                    }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
                }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Validate user input before saving
        buttonSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTitle.getText().toString().trim();

                if (title.isEmpty()) {
                    editTextTitle.setError("Task title cannot be empty");
                    return;
                }
                if (selectedDateTime == null) {
                    Toast.makeText(AddTaskActivity.this, "Please select a date and time", Toast.LENGTH_SHORT).show();
                    return;
                }

                long dueTimeMillis = selectedDateTime.getTimeInMillis();
                scheduleTaskNotification(title, dueTimeMillis);
                Toast.makeText(AddTaskActivity.this, "Task scheduled", Toast.LENGTH_SHORT).show();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("task_title", title);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void scheduleTaskNotification(String title, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskNotificationReceiver.class);
        intent.putExtra("task_title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) triggerAtMillis,  // using trigger time as a unique request code
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                // Check for API level >= 31 before calling canScheduleExactAlarms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(this, "Exact alarms are not permitted on this device.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // For devices below API 31, or if check passed, schedule the alarm
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            } catch (SecurityException e) {
                // Handle potential SecurityException
                Toast.makeText(this, "Error scheduling exact alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
