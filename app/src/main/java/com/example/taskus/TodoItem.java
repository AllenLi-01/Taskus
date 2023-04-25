package com.example.taskus;

public class TodoItem {
    private String title;
    private boolean isCompleted;

    public TodoItem(String title, boolean isCompleted) {
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "title='" + title + '\'' +
                '}';
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
