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
import java.util.*;
import android.content.Context;
import android.util.Log;

import java.io.*;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TodoListUtils {
    // 文件名格式为 "todoLists_xx.json"
    private static final String FILE_NAME_FORMAT = "todoLists_%s.json";
    private static final String FILE_NAME_FORMAT_WEEK = "todoLists_week_%s.json";
    /**
     * 将todoLists保存到内部存储中的文件，每天都有独立的文件名
     *
     * @param context 上下文，用于获取文件目录。
     * @param todoLists 要保存的todoLists。
     * @param date 日期字符串
     */
    public static void saveTodoLists(Context context, List<TodoList> todoLists, String date) {
        String fileName = String.format(FILE_NAME_FORMAT, date);
        File file = new File(context.getFilesDir(), fileName);

        // 将 TodoLists 集合转换为 JSON 字符串
        Gson gson = new Gson();
        String jsonString = gson.toJson(todoLists);

        // 将 JSON 字符串写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveWeekLists(Context context, List<TodoList> todoLists, String date) {
        String fileName = String.format(FILE_NAME_FORMAT_WEEK, date);
        File file = new File(context.getFilesDir(), fileName);

        // 将 TodoLists 集合转换为 JSON 字符串
        Gson gson = new Gson();
        String jsonString = gson.toJson(todoLists);

        // 将 JSON 字符串写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将todoLists保存到内部存储中的文件，每天都有独立的文件名
     *
     * @param context 上下文，用于获取文件目录。
     * @param todoLists 要保存的todoLists。
     */
    public static void saveListsNoDate(Context context, List<TodoList> todoLists) {
        String fileName = String.format(FILE_NAME_FORMAT, "Phase");
        File file = new File(context.getFilesDir(), fileName);

        // 将 TodoLists 集合转换为 JSON 字符串
        Gson gson = new Gson();
        String jsonString = gson.toJson(todoLists);

        // 将 JSON 字符串写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从内部存储中读取 TodoLists
    /**
     *从内部存储中读取 TodoLists.
     * @param context 上下文
     * @param date 日期字符串 yyyy-mm-dd
     * */
    public static List<TodoList> loadTodoLists(Context context, String date) {
        String fileName = String.format(FILE_NAME_FORMAT, date);
        File file = new File(context.getFilesDir(), fileName);

        // 如果文件不存在，返回一个空的 TodoLists 集合
        if (!file.exists()) {
            Log.d("no exist","new list");
            return new ArrayList<>();
        }

        // 从文件中读取 JSON 字符串
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            //如果不存在，则返回一个空的List
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        // 将 JSON 字符串转换为 TodoLists 集合
        Gson gson = new Gson();
        Type type = new TypeToken<List<TodoList>>() {}.getType();
        return gson.fromJson(jsonString.toString(), type);
    }

    public static List<TodoList> loadWeekLists(Context context, String date) {
        String fileName = String.format(FILE_NAME_FORMAT_WEEK, date);
        File file = new File(context.getFilesDir(), fileName);

        // 如果文件不存在，返回一个空的 TodoLists 集合
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // 从文件中读取 JSON 字符串
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            //如果不存在，则返回一个空的List
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        // 将 JSON 字符串转换为 TodoLists 集合
        Gson gson = new Gson();
        Type type = new TypeToken<List<TodoList>>() {}.getType();
        return gson.fromJson(jsonString.toString(), type);
    }

    public static List<TodoList> loadPhaseList(Context context) {
        String fileName = String.format(FILE_NAME_FORMAT, "Phase");
        File file = new File(context.getFilesDir(), fileName);

        // 如果文件不存在，返回一个空的 TodoLists 集合
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // 从文件中读取 JSON 字符串
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            //如果不存在，则返回一个空的List
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        // 将 JSON 字符串转换为 TodoLists 集合
        Gson gson = new Gson();
        Type type = new TypeToken<List<TodoList>>() {}.getType();
        return gson.fromJson(jsonString.toString(), type);
    }

}
