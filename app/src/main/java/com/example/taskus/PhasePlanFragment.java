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

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhasePlanFragment extends Fragment implements TodoListAdapter.TodoListListener {
    private List<TodoList> todoLists;
    private TodoListAdapter todoListAdapter;
    ImageButton deleteButton;
    LinearLayout deleteLayout;
    private Handler autoSaveHandler;
    private Runnable autoSaveRunnable;
    private boolean deleteMode = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 为这个fragment填充布局
        View view = inflater.inflate(R.layout.fragment_phase_plan, container, false);
        // 在布局中找到RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.myView);
        // 为RecyclerView设置布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        todoLists = TodoListUtils.loadPhaseList(requireContext());
        // 创建一个TodoListAdapter实例，并将它设置为RecyclerView的适配器
        todoListAdapter = new TodoListAdapter(todoLists,getContext(),this);
        recyclerView.setAdapter(todoListAdapter);


        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deleteButton = view.findViewById(R.id.delete_button);
        deleteLayout = view.findViewById(R.id.delete_layout);
        //设置自动保存事件
        autoSaveHandler = new Handler(Looper.getMainLooper());
        autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                TodoListUtils.saveListsNoDate(requireContext(), todoLists);
                autoSaveHandler.postDelayed(this, 1000); // 1秒
            }
        };
        autoSaveHandler.post(autoSaveRunnable);
        //悬浮添加按钮被点击
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showTodoListDialog(null));

        //返回键监听
        OnBackPressedDispatcher onBackPressedDispatcher = requireActivity().getOnBackPressedDispatcher();
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (deleteMode) {
                    onDeleteModeChanged(false);

                } else {
                    setEnabled(false);
                    onBackPressedDispatcher.onBackPressed();
                }
            }
        };

        onBackPressedDispatcher.addCallback(getViewLifecycleOwner(), onBackPressedCallback);
    }

    private void showTodoListDialog(TodoList todoListToEdit) {
        // 判断是否处于编辑模式
        boolean isEditMode = todoListToEdit != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_todo, null);
        builder.setView(dialogView);

        LinearLayout layoutTodoList = dialogView.findViewById(R.id.layout_todo_list);
        TextView tvConfirm = dialogView.findViewById(R.id.tv_confirm);
        tvConfirm.setEnabled(isEditMode);



        // 如果处于编辑模式，用待办事项填充对话框
        if (isEditMode) {
            EditText dailyTitleEditText = dialogView.findViewById(R.id.daily_title);
            dailyTitleEditText.setText(todoListToEdit.getTitle());
            for (TodoItem item : todoListToEdit.getItems()) {
                EditText etTodo = addTodoItem(layoutTodoList, tvConfirm);
                etTodo.setText(item.getTitle());
            }
        }else {
            // 动态添加编辑框
            addTodoItem(layoutTodoList, tvConfirm);
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // 为确认按钮添加点击监听器
        tvConfirm.setOnClickListener(confirmView -> {
            EditText dailyTitleEditText = dialogView.findViewById(R.id.daily_title);
            String title = dailyTitleEditText.getText().toString();
            List<TodoItem> todoItems = getTodoItemsFromLayout(layoutTodoList);

            // 根据模式执行不同操作
            if (isEditMode) {
                // 更新传入的 TodoList 对象
                todoListToEdit.setTitle(title);
                todoListToEdit.setItems(todoItems);
            } else {
                // 创建一个新的 TodoList 对象，并将其添加到数据集中
                TodoList newTodoList = new TodoList(title, todoItems, false);
                todoLists.add(newTodoList);
            }

            // 更新 RecyclerView 的数据集
            todoListAdapter.updateData(todoLists);

            // 关闭对话框
            alertDialog.dismiss();
        });

    }

    private List<TodoItem> getTodoItemsFromLayout(LinearLayout layoutTodoList) {
        List<TodoItem> todoItems = new ArrayList<>();
        for (int i = 0; i < layoutTodoList.getChildCount(); i++) {
            View item = layoutTodoList.getChildAt(i);
            EditText editText = item.findViewById(R.id.et_todo);
            String itemText = editText.getText().toString().trim();

            // 只有当文本不为空时才将其添加到待办事项列表中
            if (!itemText.isEmpty()) {
                todoItems.add(new TodoItem(itemText, false));
            }
        }
        return todoItems;
    }

    private EditText addTodoItem(LinearLayout layoutTodoList, TextView tvConfirm) {
        View todoItemView = getLayoutInflater().inflate(R.layout.layout_todo_item, null);
        EditText etTodo = todoItemView.findViewById(R.id.et_todo);
        ImageView ivCheck = todoItemView.findViewById(R.id.iv_check);


        // 设置文本变化监听
        etTodo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasNonEmptyEditText = false;
                for (int i = 0; i < layoutTodoList.getChildCount(); i++) {
                    View item = layoutTodoList.getChildAt(i);
                    EditText editText = item.findViewById(R.id.et_todo);
                    if (!editText.getText().toString().trim().isEmpty()) {
                        hasNonEmptyEditText = true;
                        break;
                    }
                }

                if (hasNonEmptyEditText) {
                    // 设置文本按钮颜色为主色
                    tvConfirm.setTextColor(ContextCompat.getColor(requireContext(), R.color.cyan_500));
                    tvConfirm.setEnabled(true);
                } else {
                    // 设置文本按钮颜色为灰色
                    tvConfirm.setTextColor(ContextCompat.getColor(requireContext(), R.color.medium_gray));
                    tvConfirm.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 设置回车键监听
        etTodo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                // 如果当前编辑框输入不为空，则添加新的编辑框并将焦点设置到新编辑框
                if (!etTodo.getText().toString().isEmpty()) {
                    EditText newEtTodo = addTodoItem(layoutTodoList, tvConfirm);
                    newEtTodo.requestFocus();
                }
                return true;
            }
            return false;
        });


        // 设置退格键监听
        etTodo.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && etTodo.getText().toString().isEmpty()) {
                // 判断 LinearLayout 中的子视图数量，如果大于1，则删除当前编辑框并将焦点设置到最后一个编辑框
                if (layoutTodoList.getChildCount() > 1) {
                    layoutTodoList.removeView(todoItemView);
                    View lastItemView = layoutTodoList.getChildAt(layoutTodoList.getChildCount() - 1);
                    EditText lastEtTodo = lastItemView.findViewById(R.id.et_todo);
                    lastEtTodo.requestFocus();
                    lastEtTodo.setSelection(lastEtTodo.getText().length());
                    return true;
                }
            }
            return false;
        });



        layoutTodoList.addView(todoItemView);
        return etTodo;
    }

    @Override
    public void onDeleteModeChanged(boolean isInDeleteMode) {
        deleteMode = isInDeleteMode;
        if(deleteMode){
            deleteLayout.setVisibility(View.VISIBLE);
            if(deleteButton.isEnabled()){
                deleteButton.setOnClickListener(v -> {
                    // 执行删除操作
                    todoListAdapter.removeSelectedTodoLists();

                    // 退出删除模式
                    onDeleteModeChanged(false);
                    // 清空已选中的列表项
                    todoListAdapter.clearSelectedTodoLists();

                });
            }
        } else {
            deleteLayout.setVisibility(View.GONE);
            deleteButton.setOnClickListener(null);
            todoListAdapter.exitDeleteMode();
            deleteMode = false;

        }

    }

    @Override
    public void onTodoListDeleted(List<TodoList> selectedLists) {
        todoLists.removeAll(selectedLists);
        todoListAdapter.updateData(todoLists);
    }

    @Override
    public void onSelectedTodoListsChanged(int selectedCount) {
        if (selectedCount == 0) {
            deleteButton.setEnabled(false);
            deleteButton.setAlpha(0.5f);
        } else {
            deleteButton.setEnabled(true);
            deleteButton.setAlpha(1.0f);
        }

    }

    @Override
    public void onListEditing(TodoList todoList) {
        showTodoListDialog(todoList);
    }
    public void onDestroyView() {
        super.onDestroyView();
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
    }
}
