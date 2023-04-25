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
import android.text.Spanned;
import android.text.TextUtils;
import androidx.core.text.HtmlCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReviewUtils {

    private static final String REVIEW_FILE_EXTENSION = ".review";
    private static final String REVIEW_WEEKLY_PREFIX = "weekly";
    private static final String REVIEW_MONTHLY_PREFIX = "monthly";

    public static void saveReview(Context context, Spanned content, String curDate, boolean isWeekly) {
        if (TextUtils.isEmpty(content)) {
            return;
        }

        String htmlContent = HtmlCompat.toHtml(content, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        String fileName = generateFileName(curDate, isWeekly);

        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(htmlContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Spanned loadReview(Context context, String curDate, boolean isWeekly) {
        String fileName = generateFileName(curDate, isWeekly);
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream fis = context.openFileInput(fileName)) {
            int byteData;
            while ((byteData = fis.read()) != -1) {
                stringBuilder.append((char) byteData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String htmlContent = stringBuilder.toString();
        return HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    private static String generateFileName(String curDate, boolean isWeekly) {
        String prefix = isWeekly ? REVIEW_WEEKLY_PREFIX : REVIEW_MONTHLY_PREFIX;
        return prefix + "_" + curDate + REVIEW_FILE_EXTENSION;
    }
}
