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

import java.util.List;

public class TodoList {
    private String title;
    private List<TodoItem> items;
    private boolean isExpanded;//是否展开

    private boolean isCompleted;

    public boolean getCompleted(){
        for (TodoItem item:items){
            if(!item.isCompleted()) return false;
        }
        return true;
    }

    public void setCompleted(){
        for(TodoItem item:this.items){
            item.setCompleted(true);
        }
        this.isCompleted = true;
    }

    public void setNoCompleted(){
        for(TodoItem item:this.items){
            item.setCompleted(false);
        }
        this.isCompleted = false;
    }
    public TodoList(String title, List<TodoItem> items, boolean isExpanded) {
        this.title = title;
        this.items = items;
        this.isExpanded = isExpanded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TodoItem> getItems() {
        return items;
    }

    public void setItems(List<TodoItem> items) {
        this.items = items;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
