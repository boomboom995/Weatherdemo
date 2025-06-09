// 文件路径: app/src/main/java/com/example/weatherforecast2/Adapter/CalendarAdapter.java
package com.example.weatherforecast2.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherforecast2.DatabaseHelper.DiaryDatabaseHelper;
import com.example.weatherforecast2.R;
import com.example.weatherforecast2.util.LunarUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final Context context;
    private final List<Date> days;
    private final Calendar currentMonthCalendar;
    private final Calendar todayCalendar;
    private final CalendarClickListener listener;
    private final DiaryDatabaseHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface CalendarClickListener { void onDayClick(Date date); }

    public CalendarAdapter(Context context, List<Date> days, Calendar currentMonthCalendar, CalendarClickListener listener) {
        this.context = context;
        this.days = days;
        this.currentMonthCalendar = currentMonthCalendar;
        this.todayCalendar = Calendar.getInstance();
        this.listener = listener;
        this.dbHelper = new DiaryDatabaseHelper(context);
    }

    @NonNull @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.date_grid_item, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Date day = days.get(position);
        Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTime(day);

        boolean isCurrentMonth = dayCalendar.get(Calendar.MONTH) == currentMonthCalendar.get(Calendar.MONTH);

        if (isCurrentMonth) {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setText(String.valueOf(dayCalendar.get(Calendar.DAY_OF_MONTH)));
            holder.tvLunarDate.setText(LunarUtil.getLunarDateString(dayCalendar.get(Calendar.YEAR), dayCalendar.get(Calendar.MONTH) + 1, dayCalendar.get(Calendar.DAY_OF_MONTH)));

            boolean isToday = dayCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && dayCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR);
            holder.tvDayNumber.setTextColor(isToday ? ContextCompat.getColor(context, R.color.today_highlight_color) : ContextCompat.getColor(context, R.color.white));

            String dateString = dateFormat.format(day);
            boolean hasPlan = dbHelper.hasPlanForDate(dateString);
            boolean isRestDay = (dayCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || dayCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);

            int baseCircleColor = isRestDay ? R.color.weekday_blue : R.color.white;
            holder.tvRestIndicator.setVisibility(isRestDay ? View.VISIBLE : View.GONE);

            Drawable backgroundDrawable;
            if (hasPlan) {
                LayerDrawable borderedDrawable = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.circle_with_red_border).mutate();
                GradientDrawable baseCircle = (GradientDrawable) borderedDrawable.findDrawableByLayerId(R.id.base_circle);
                baseCircle.setColor(ContextCompat.getColor(context, baseCircleColor));
                backgroundDrawable = borderedDrawable;
            } else {
                GradientDrawable circleDrawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.circle_background_template).mutate();
                circleDrawable.setColor(ContextCompat.getColor(context, baseCircleColor));
                backgroundDrawable = circleDrawable;
            }
            holder.moodPlanCircle.setBackground(backgroundDrawable);


            String mood = dbHelper.getMoodForDate(dateString);
            if (mood != null && !mood.isEmpty()) {
                holder.ivMoodEmoji.setVisibility(View.VISIBLE);
                holder.tvRestIndicator.setVisibility(View.GONE);
                int moodResId = getMoodDrawableId(mood);
                if (moodResId != 0) holder.ivMoodEmoji.setImageResource(moodResId);
            } else {
                holder.ivMoodEmoji.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onDayClick(day); });
        } else {
            holder.itemView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private int getMoodDrawableId(String moodTag) {
        if (moodTag == null) return 0;
        switch (moodTag) {
            case "开心": return R.drawable.ic_mood_happy;
            case "平静": return R.drawable.ic_mood_quiet;
            case "兴奋": return R.drawable.ic_mood_excited;
            case "心动": return R.drawable.ic_mood_enjoy;
            case "伤心": return R.drawable.ic_mood_sad;
            case "生气": return R.drawable.ic_mood_awful;
            case "烦躁": return R.drawable.ic_mood_speechless;
            case "心累": return R.drawable.ic_mood_tired;
            default: return 0;
        }
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber, tvLunarDate, tvRestIndicator;
        FrameLayout moodPlanCircle;
        ImageView ivMoodEmoji;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            tvLunarDate = itemView.findViewById(R.id.tv_lunar_date);
            tvRestIndicator = itemView.findViewById(R.id.tv_rest_indicator);
            moodPlanCircle = itemView.findViewById(R.id.mood_plan_circle);
            ivMoodEmoji = itemView.findViewById(R.id.iv_mood_emoji);
        }
    }
}