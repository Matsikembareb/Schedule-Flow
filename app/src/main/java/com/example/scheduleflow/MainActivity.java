package com.example.scheduleflow;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final int TASK_REQUEST_CODE = 101;

    private ArrayList<String> taskList;
    private TaskAdapter adapter;
    private RecyclerView recyclerView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request notification permission if on API level 33+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Initialize SharedPreferences
        prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);

        // Load saved tasks into taskList
        taskList = new ArrayList<>(prefs.getStringSet("tasks", new HashSet<>()));

        adapter = new TaskAdapter(taskList, updatedTasks -> {
            SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("tasks", new HashSet<>(updatedTasks));
            editor.apply();
        });
        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                taskList.remove(position);
                adapter.notifyItemRemoved(position);
                saveTasks(); // Ensure tasks are saved after deletion
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, TASK_REQUEST_CODE);
        });
    }

    // Receive task title from AddTaskActivity and update RecyclerView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            String newTask = data.getStringExtra("task_title");
            if (newTask != null) {
                taskList.add(newTask);
                adapter.notifyItemInserted(taskList.size() - 1);
                saveTasks(); // Save task immediately
            }
        }
    }

    // Save task list to SharedPreferences
    private void saveTasks() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> taskSet = new HashSet<>(taskList);
        editor.putStringSet("tasks", taskSet);
        editor.apply(); // Save tasks persistently
    }
}