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
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements TodoItemAdapter.OnTodoItemStatusChangeListener{
    private boolean isUncompletedExpanded;
    private boolean isCompletedExpanded;

    private boolean isInDeleteMode = false;

    private List<TodoList> selectedTodoLists = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView titleTextView;
        private CheckBox checkBox;
        private ImageButton expandButton;
        private boolean isDividerAdded = false;
        //todoItem
        private RecyclerView todoItemRecyclerView;

        private TodoItemAdapter todoItemAdapter;
        private boolean isSelected = false;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.title_text_view);
            checkBox = itemView.findViewById(R.id.checkbox);
            expandButton = itemView.findViewById(R.id.expand_button);
            todoItemRecyclerView = itemView.findViewById(R.id.todo_item_recycler_view);
        }


        public void bindData(TodoList todoList){
            titleTextView.setText(todoList.getTitle());
            checkBox.setOnCheckedChangeListener(null); // 防止无限递归调用
            checkBox.setChecked(todoList.getCompleted());

            // 创建一个新的 TodoItemAdapter，传入 todoList 中的 TodoItem 列表
            TodoItemAdapter todoItemAdapter = new TodoItemAdapter(todoList.getItems(), itemView.getContext(), TodoListAdapter.this);
            todoItemRecyclerView.setAdapter(todoItemAdapter);
            todoItemRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            // 点击展开按钮时，展开嵌套的RecyclerView
            expandButton.setOnClickListener(v -> {
                if (todoItemRecyclerView.getVisibility() == View.GONE) {
                    todoItemRecyclerView.setVisibility(View.VISIBLE);
                    expandButton.setImageResource(R.drawable.ic_angle_up);
                } else {
                    todoItemRecyclerView.setVisibility(View.GONE);
                    expandButton.setImageResource(R.drawable.ic_angle_down);
                }
            });

            //为已完成项目添加删除线
            if(todoList.getCompleted()){
                titleTextView.setTextColor(context.getResources().getColor(R.color.medium_gray));
                titleTextView.getPaint().setStrikeThruText(true);
            }else {
                titleTextView.setTextColor(context.getResources().getColor(R.color.light_gray));
                titleTextView.getPaint().setStrikeThruText(false);
            }
            titleTextView.invalidate();

            //勾选选择框后，改变todoList的完成状态
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    todoList.setCompleted();
                } else {
                    todoList.setNoCompleted();
                }
                updateTodoListCompletion(todoList); // 更新数据源并刷新列表
            });

            //为itemView设置长按监听器
            itemView.setOnLongClickListener(view -> {
                if (!isInDeleteMode) {
                    isInDeleteMode = true;
                    if (todoListListener != null) {
                        selectTodoList(todoList);
                        todoListListener.onDeleteModeChanged(true);
                        return true;
                    }
                }
                return false;
            });
            itemView.setOnClickListener(view -> {
                if(isInDeleteMode){
                    selectTodoList(todoList);
                }else {
                    todoListListener.onListEditing(todoList);
                }
            });
        }

        //编辑逻辑




        //将当前Todolist加入待删除列表中。
        private void selectTodoList(TodoList todoList) {
            if (selectedTodoLists.contains(todoList)) {
                selectedTodoLists.remove(todoList);
            } else {
                selectedTodoLists.add(todoList);
            }
            // 通知 Fragment 选中的 TodoLists 发生了变化
            todoListListener.onSelectedTodoListsChanged(selectedTodoLists.size());
            notifyDataSetChanged();
        }


    }

    public void setDeleteMode(boolean isInDeleteMode) {
        this.isInDeleteMode = isInDeleteMode;
        notifyDataSetChanged(); // 当切换模式时更新视图
    }

    public void exitDeleteMode() {
        isInDeleteMode = false;
        notifyDataSetChanged();
        Log.d("exec Exit","yes");
    }

    // 移除选定的 TodoList
    public void removeSelectedTodoLists() {
        // 从数据源中移除选定的 TodoList
        uncompletedTodoLists.removeAll(selectedTodoLists);
        completedTodoLists.removeAll(selectedTodoLists);

        // 将更新后的数据源传递给适配器
        todoListListener.onTodoListDeleted(selectedTodoLists);

        // 更新 RecyclerView
        notifyDataSetChanged();
    }


    // 清空 selectedTodoLists
    public void clearSelectedTodoLists() {
        selectedTodoLists.clear();
    }


    private TodoListListener todoListListener;

    public void setTodoListListener(TodoListListener todoListListener) {
        this.todoListListener = todoListListener;
    }
    public interface TodoListListener {
        void onDeleteModeChanged(boolean isInDeleteMode);
        void onTodoListDeleted(List<TodoList> selectedLists);
        void onSelectedTodoListsChanged(int selectedCount);

        void onListEditing(TodoList todoList);



    }
    public void onStatusChanged() {
        List<TodoList> todoListsToUpdate = new ArrayList<>();

        todoListsToUpdate.addAll(uncompletedTodoLists);
        todoListsToUpdate.addAll(completedTodoLists);

        for (TodoList todoList : todoListsToUpdate) {
            updateTodoListCompletion(todoList);
        }
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private ImageButton expandButton;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.group_title_text_view);
            expandButton = itemView.findViewById(R.id.group_expand_button);
        }

        public void bindData(String title) {
            titleTextView.setText(title);

            expandButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == 0) {
                    // 未完成待办事项组被点击
                    isUncompletedExpanded = !isUncompletedExpanded;
                    if (isUncompletedExpanded) {
                        expandButton.setImageResource(R.drawable.ic_sort_amount_up);
                    } else {
                        expandButton.setImageResource(R.drawable.ic_sort_amount_down);
                    }
                } else {
                    // 已完成待办事项组被点击
                    isCompletedExpanded = !isCompletedExpanded;
                    if (isCompletedExpanded) {
                        expandButton.setImageResource(R.drawable.ic_sort_amount_up);
                    } else {
                        expandButton.setImageResource(R.drawable.ic_sort_amount_down);
                    }
                }
                notifyDataSetChanged();
            });
        }
    }


    private List<TodoList> completedTodoLists;
    private List<TodoList> uncompletedTodoLists;

    private Context context;

    //设置已完成列表与未完成列表

    public TodoListAdapter(List<TodoList> todoLists, Context context, TodoListListener todoListListener) {
        this.completedTodoLists = new ArrayList<>();
        this.uncompletedTodoLists = new ArrayList<>();
        this.context = context;




        for (TodoList todoList : todoLists) {
            if (todoList.getCompleted()) {
                completedTodoLists.add(todoList);
            } else {
                uncompletedTodoLists.add(todoList);
            }
        }
        isUncompletedExpanded = true;
        isCompletedExpanded = true;
        setTodoListListener(todoListListener);
    }


    @NonNull
    @Override
    //加载列表项布局 使用LayoutInflater
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case TYPE_GROUP:
                itemView = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false);
                return new GroupViewHolder(itemView);
            case TYPE_TODO_LIST:
                itemView = LayoutInflater.from(context).inflate(R.layout.todo_list_item, parent, false);
                return new ViewHolder(itemView);
            default:
                return null;
        }
    }

    //将数据绑定到列表项视图上
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_GROUP) {
            if (position == 0) {
                ((GroupViewHolder) holder).bindData("未完成");
            } else {
                ((GroupViewHolder) holder).bindData("已完成");
            }

        } else {
            int completedGroupPosition = isUncompletedExpanded ? uncompletedTodoLists.size() + 1 : 1;
            int completedStartPosition = completedGroupPosition + 1;
            TodoList todoList;
            if (isUncompletedExpanded && position <= uncompletedTodoLists.size()) {
                todoList = uncompletedTodoLists.get(position - 1);
            } else if (isCompletedExpanded && position >= completedStartPosition) {
                todoList = completedTodoLists.get(position - completedStartPosition);
            } else {
                return;
            }
            // 添加分隔线
            if (!((ViewHolder) holder).isDividerAdded) {
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(holder.itemView.getContext(),
                        LinearLayoutManager.VERTICAL);
                ((ViewHolder) holder).todoItemRecyclerView.addItemDecoration(dividerItemDecoration);
                ((ViewHolder) holder).isDividerAdded = true;
            }
            ((ViewHolder)holder).bindData(todoList);

            // 更新背景颜色
            if (isInDeleteMode && selectedTodoLists.contains(todoList)) {
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private static final int TYPE_GROUP = 0;
    private static final int TYPE_TODO_LIST = 1;

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == (isUncompletedExpanded ? uncompletedTodoLists.size() + 1 : 1)) {
            return TYPE_GROUP;
        } else {
            return TYPE_TODO_LIST;
        }
    }
    @Override
    public int getItemCount() {
        int count = 2; // 为两个组标题添加计数
        if (isUncompletedExpanded) {
            count += uncompletedTodoLists.size();
        }
        if (isCompletedExpanded) {
            count += completedTodoLists.size();
        }
        return count;
    }

    //在更新待办事项的完成状态时，检查待办清单是否已完成，并更新数据源
    private void updateTodoListCompletion(TodoList todoList) {
        if (todoList.getCompleted()) {
            if (uncompletedTodoLists.contains(todoList)) {
                uncompletedTodoLists.remove(todoList);
                completedTodoLists.add(todoList);
            }
        } else {
            if (completedTodoLists.contains(todoList)) {
                completedTodoLists.remove(todoList);
                uncompletedTodoLists.add(todoList);
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<TodoList> todoLists) {
        completedTodoLists.clear();
        uncompletedTodoLists.clear();

        for (TodoList todoList : todoLists) {
            if (todoList.getCompleted()) {
                completedTodoLists.add(todoList);
            } else {
                uncompletedTodoLists.add(todoList);
            }
        }

        notifyDataSetChanged();
    }



}