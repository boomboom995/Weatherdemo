package com.example.weatherforecast2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast2.Activity.DiaryEditActivity;
import com.example.weatherforecast2.DatabaseHelper.DiaryDatabaseHelper;
import com.example.weatherforecast2.Entity.DiaryEntry;
import com.example.weatherforecast2.R;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private Context context;
    private List<DiaryEntry> diaryEntries;
    private final DiaryDatabaseHelper dbHelper;

    public DiaryAdapter(Context context, List<DiaryEntry> diaryEntries) {
        this.context = context;
        this.diaryEntries = diaryEntries;
        this.dbHelper = new DiaryDatabaseHelper(context);
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.diary_list_item, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = diaryEntries.get(position);

        holder.tvDate.setText(entry.getDate());
        holder.tvContent.setText(entry.getContent());

        // 设置心情图标
        int moodResId = getMoodDrawableId(entry.getMood());
        if (moodResId != 0) {
            holder.ivMood.setImageResource(moodResId);
        }

        // 设置菜单点击事件
        holder.ivMenu.setOnClickListener(v -> showPopupMenu(v, entry, position));
    }

    private void showPopupMenu(View view, DiaryEntry entry, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.diary_item_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_item_edit) {
                // 跳转到编辑页面
                Intent intent = new Intent(context, DiaryEditActivity.class);
                intent.putExtra("diary_id", entry.getId());
                context.startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_item_delete) {
                // 弹出确认删除对话框
                new AlertDialog.Builder(context)
                        .setTitle("删除日记")
                        .setMessage("确定要删除这篇日记吗？此操作无法撤销。")
                        .setPositiveButton("删除", (dialog, which) -> {
                            deleteDiary(entry, position);
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void deleteDiary(DiaryEntry entry, int position) {
        int result = dbHelper.deleteDiary(entry.getId());
        if (result > 0) {
            diaryEntries.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, diaryEntries.size());
            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }
    
    public void updateData(List<DiaryEntry> newEntries) {
        this.diaryEntries.clear();
        this.diaryEntries.addAll(newEntries);
        notifyDataSetChanged();
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

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMood, ivMenu;
        TextView tvDate, tvContent;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMood = itemView.findViewById(R.id.iv_list_item_mood);
            ivMenu = itemView.findViewById(R.id.iv_list_item_menu);
            tvDate = itemView.findViewById(R.id.tv_list_item_date);
            tvContent = itemView.findViewById(R.id.tv_list_item_content);
        }
    }
}