package com.example.taskus;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MonthlyReviewFragment extends Fragment {
    final int selectedColor = R.color.cyan_500;
    final int unselectedColor = R.color.black;
    private CustomEditText editText;
    private ImageButton boldButton ;
    private ImageButton italicButton;
    private ImageButton underlineButton;
    private ImageButton listButton;
    private String curDate;
    TextView monthText;
    private Handler autoSaveHandler;
    private Runnable autoSaveRunnable;
    private static final int AUTO_SAVE_INTERVAL = 5000;

    public MonthlyReviewFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_review,container,false);
        curDate = getCurrentMonth();
        monthText = view.findViewById(R.id.date_text);
        monthText.setText(curDate);

        ImageButton previousButton = view.findViewById(R.id.previous_month_button);
        ImageButton nextButton = view.findViewById(R.id.next_month_button);
        previousButton.setOnClickListener(v->{
            changeMonth(-1);
        });

        nextButton.setOnClickListener(v->{
            changeMonth(1);
        });
        monthText.setOnClickListener(v->{
            showMonthPicker(inflater);
        });




       //----以下是文本编辑器的相关设定--------
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
                ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, false);
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
        return ReviewUtils.loadReview(requireContext(), curDate, false);
    }

    private void showMonthPicker(LayoutInflater inflater) {
        ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, false);
        Calendar calendar = Calendar.getInstance();
        int year = Integer.parseInt(curDate.split("-")[0]);
        int month = Integer.parseInt(curDate.split("-")[1]);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View monthPickerDialog = inflater.inflate(R.layout.month_picker_dialog, null);


        builder.setView(monthPickerDialog);

        NumberPicker monthPicker = monthPickerDialog.findViewById(R.id.month_picker);
        NumberPicker yearPicker = monthPickerDialog.findViewById(R.id.year_picker);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(month);

        int minYear = year - 10;
        int maxYear = year + 10;
        yearPicker.setMinValue(minYear);
        yearPicker.setMaxValue(maxYear);
        yearPicker.setValue(year);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d", yearPicker.getValue(), monthPicker.getValue());
            curDate = date;
            monthText.setText(date);
            editText.setText(getReviewText());
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // 调整 AlertDialog 窗口宽度
        Window window = alertDialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int dialogWidth = (int) (displayMetrics.widthPixels * 0.6);
            window.setLayout(dialogWidth,  WindowManager.LayoutParams.WRAP_CONTENT);
        }
        // 获取确认和取消按钮
        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        //设置按钮的颜色
        positiveButton.setTextColor(ContextCompat.getColor(requireContext(),R.color.black));
        negativeButton.setTextColor(ContextCompat.getColor(requireContext(),R.color.black));
        positiveButton.setText("确认");
        negativeButton.setText("取消");
    }

    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        return String.format(Locale.getDefault(), "%04d-%02d", year, month + 1);
    }

    private void changeMonth(int i ){
        ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, false);
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthText.getText().toString());
            if (date != null) {
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, i);
                String newMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.getTime());
                monthText.setText(newMonth);
                curDate = newMonth;
                editText.setText(getReviewText());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        ReviewUtils.saveReview(requireContext(), editText.getText(), curDate, false);
    }






}
