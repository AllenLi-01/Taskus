package com.example.taskus;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.richeditor.RichEditor;

public class WeeklyReviewFragment extends Fragment {
    private String curDate;
    private TextView weekText;
    private TextView rangeText;
    final int selectedColor = R.color.cyan_500;
    final int unselectedColor = R.color.black;
    private CustomEditText editText;
    private ImageButton boldButton ;
    private ImageButton italicButton;
    private ImageButton underlineButton;
    private ImageButton listButton;
    private  Handler autoSaveHandler;
    private Runnable autoSaveRunnable;

    private static final int AUTO_SAVE_INTERVAL = 5000;
    private RichEditor richEditor;



    public WeeklyReviewFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly_review,container,false);

        weekText = view.findViewById(R.id.date_text);
        rangeText = view.findViewById(R.id.date_range_text);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        curDate = String.format("%d-%d", year, week);

        weekText.setText(formatWeek(curDate));
        rangeText.setText(getWeekRange(curDate));

        ImageButton previousButton = view.findViewById(R.id.previous_button);
        ImageButton nextButton = view.findViewById(R.id.next_button);

        previousButton.setOnClickListener(v->{
            changeWeek(-1);
        });

        nextButton.setOnClickListener(v->{
            changeWeek(1);
        });


        LinearLayout floatingToolbar = view.findViewById(R.id.floating_toolbar);
        boldButton = view.findViewById(R.id.bold_button);
        italicButton = view.findViewById(R.id.italic_button);
        underlineButton = view.findViewById(R.id.underline_button);
        editText = view.findViewById(R.id.edit_text);
        editText.setText(getReviewText());

        autoSaveHandler = new Handler(Looper.getMainLooper());
        autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, true);
                autoSaveHandler.postDelayed(this, AUTO_SAVE_INTERVAL);
            }
        };
        autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_INTERVAL);



        //设置呼出键盘时工具栏浮现
        View rootView = view.getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is an arbitrary number, adjust as needed
                floatingToolbar.setVisibility(View.VISIBLE);
            } else {
                floatingToolbar.setVisibility(View.GONE);
            }
        });

        boldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyStyleToSelectedText(editText, Typeface.BOLD);
            }
        });

        italicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyStyleToSelectedText(editText, Typeface.ITALIC);
            }
        });

        underlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyUnderlineToSelectedText(editText);
            }
        });

        listButton = view.findViewById(R.id.list_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursorPosition = editText.getSelectionStart();
                Editable text = editText.getText();
                int start = getParagraphStart(cursorPosition, text);
                int end = getParagraphEnd(cursorPosition, text);
                String listItemMark = "• "; // 圆点为列表项标记
                if (hasListItemMark(start, text)) { // 如果段首已经有标记符，就取消
                    text.delete(start, start + listItemMark.length());
                } else { // 否则为段首加上标记符
                    text.insert(start, listItemMark);
                }

            }
        });

        editText.setOnSelectionChangedListener(new CustomEditText.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                updateButtonColors(selStart, selEnd);
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    int cursorPosition = editText.getSelectionStart();
                    Editable text = editText.getText();
                    int start = getParagraphStart(cursorPosition, text);
                    int end = getParagraphEnd(cursorPosition, text);
                    String listItemMark = "• "; // 圆点为列表项标记
                    if (hasListItemMark(start, text)) { // 如果段首已经有标记符，就在新行首也加上标记符
                        text.insert(cursorPosition, "\n"+listItemMark);
                        return true; // 返回true表示已经处理了回车键事件，EditText不再处理
                    }
                }
                return false; // 返回false表示未处理回车键事件，EditText继续处理
            }
        });




        return view;
    }
    private Spanned getReviewText(){
        return ReviewUtils.loadReview(requireContext(), curDate, true);
    }

    private void changeWeek(int i) {
        ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, true);

        String[] parts = curDate.split("-");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.WEEK_OF_YEAR, week);

        calendar.add(Calendar.WEEK_OF_YEAR, i);
        year = calendar.get(Calendar.YEAR);
        week = calendar.get(Calendar.WEEK_OF_YEAR);

        curDate = String.format(Locale.getDefault(), "%04d-%02d", year, week);
        weekText.setText(formatWeek(curDate));
        rangeText.setText(getWeekRange(curDate));
        editText.setText(getReviewText());
    }

    private String getWeekRange(String date) {
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String endDate = dateFormat.format(calendar.getTime());

        return startDate + " - " + endDate;
    }

    @SuppressLint("DefaultLocale")
    private String formatWeek(String date) {
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        return String.format("%d年 第%d周 周度总结",year,week);
    }

    private int getParagraphStart(int cursorPosition, Editable text) {
        int start = cursorPosition;
        while (start > 0 && text.charAt(start - 1) != '\n') {
            start--;
        }
        return start;
    }

    private int getParagraphEnd(int cursorPosition, Editable text) {
        int end = cursorPosition;
        while (end < text.length() && text.charAt(end) != '\n') {
            end++;
        }
        return end;
    }

    private boolean hasListItemMark(int start, Editable text) {
        String listItemMark = "• "; // 圆点为列表项标记
        return text.length() >= start + listItemMark.length()
                && text.subSequence(start, start + listItemMark.length()).toString().equals(listItemMark);
    }

    private void updateButtonColors(int selStart, int selEnd) {

        Spannable spannable = editText.getText();
        boolean isBold = false, isItalic = false, isUnderlined = false;

        if (selStart >= 0 && selEnd > selStart) {
            StyleSpan[] styleSpans = spannable.getSpans(selStart, selEnd, StyleSpan.class);

            for (StyleSpan span : styleSpans) {
                if (span.getStyle() == Typeface.BOLD) {
                    isBold = true;
                } else if (span.getStyle() == Typeface.ITALIC) {
                    isItalic = true;
                }
            }

            UnderlineSpan[] underlineSpans = spannable.getSpans(selStart, selEnd, UnderlineSpan.class);
            if (underlineSpans.length > 0) {
                isUnderlined = true;
            }
        }


        ColorStateList selectedTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selectedColor));
        ColorStateList unselectedTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), unselectedColor));

        ImageViewCompat.setImageTintList(boldButton, isBold ? selectedTint : unselectedTint);
        ImageViewCompat.setImageTintList(italicButton, isItalic ? selectedTint : unselectedTint);
        ImageViewCompat.setImageTintList(underlineButton, isUnderlined ? selectedTint : unselectedTint);
    }



    private void applyStyleToSelectedText(EditText editText, int style) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start >= 0 && end > start) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editText.getText());
            StyleSpan[] spans = spannableStringBuilder.getSpans(start, end, StyleSpan.class);
            boolean found = false;

            for (StyleSpan span : spans) {
                if (span.getStyle() == style) {
                    spannableStringBuilder.removeSpan(span);
                    found = true;
                }
            }

            if (!found) {
                spannableStringBuilder.setSpan(new StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            editText.setText(spannableStringBuilder);
            editText.setSelection(start, end);
        }
    }

    private void applyUnderlineToSelectedText(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start >= 0 && end > start) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editText.getText());
            UnderlineSpan[] spans = spannableStringBuilder.getSpans(start, end, UnderlineSpan.class);

            if (spans.length > 0) {
                for (UnderlineSpan span : spans) {
                    spannableStringBuilder.removeSpan(span);
                }
            } else {
                spannableStringBuilder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            editText.setText(spannableStringBuilder);
            editText.setSelection(start, end);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start auto-saving
        autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop auto-saving and save immediately
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, true);
    }

}
