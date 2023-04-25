/*
 * Copyright (c) 2023 Allen Li
 *
 * All rights reserved.
 *
 * This software is the property of Allen Li and is protected by copyright,
 * trademark, and other intellectual property laws. You may not reproduce, modify,
 * distribute, or create derivative works based on this software, in whole or in part,
 * without the express written permission of Allen Li.
 */

package com.example.taskus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.ViewHolder> {

    private List<TodoItem> todoItems;
    private Context context;
    //实现item列表与List列表通信，把状态更改实时传递给外层
    public interface OnTodoItemStatusChangeListener {
        void onStatusChanged();
    }
    private OnTodoItemStatusChangeListener listener;

    public TodoItemAdapter(List<TodoItem> todoItems, Context context, OnTodoItemStatusChangeListener listener) {
        this.todoItems = todoItems;
        this.context = context;
        this.listener = listener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoItem todoItem = todoItems.get(position);
        holder.bindData(todoItem);
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTextView;
        private CheckBox itemCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.item_text_view);
            itemCheckBox = itemView.findViewById(R.id.item_checkbox);
        }

        public void bindData(TodoItem todoItem) {
            itemTextView.setText(todoItem.getTitle());
            itemCheckBox.setChecked(todoItem.isCompleted());

            itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                todoItem.setCompleted(isChecked);
                if (listener != null) {
                    listener.onStatusChanged();
                }
            });
        }
    }
}
