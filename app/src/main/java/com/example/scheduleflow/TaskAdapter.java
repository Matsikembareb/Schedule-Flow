package com.example.scheduleflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<String> taskList;
    private OnTaskRemovedListener taskRemovedListener; // Callback for task deletion

    public TaskAdapter(List<String> taskList, OnTaskRemovedListener listener) {
        this.taskList = taskList;
        this.taskRemovedListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.taskTitle.setText(taskList.get(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Method to remove a task safely
    public void removeTask(int position) {
        if (position >= 0 && position < taskList.size()) {
            String removedTask = taskList.remove(position);
            notifyItemRemoved(position);

            // Ensure callback is not null before triggering task removal
            if (taskRemovedListener != null) {
                taskRemovedListener.onTaskRemoved(taskList);
            }
        }
    }

    // Method to add a task dynamically
    public void addTask(String task) {
        taskList.add(task);
        notifyItemInserted(taskList.size() - 1);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
        }
    }

    // Interface for communicating task removal to MainActivity
    public interface OnTaskRemovedListener {
        void onTaskRemoved(List<String> updatedTasks);
    }
}