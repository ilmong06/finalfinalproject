package com.example.wav2vecapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<ReportItem> reportList;

    public ReportAdapter(List<ReportItem> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportItem item = reportList.get(position);
        holder.tvIndex.setText(String.valueOf(item.getId()));       // 🔵 index
        holder.tvDate.setText(item.getDate());                      // 📅 날짜
        holder.tvKeyword.setText(item.getKeyword());                // 🔑 키워드
        holder.tvLocation.setText(item.getLocation());              // 📍 주소
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvDate, tvKeyword, tvLocation;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvKeyword = itemView.findViewById(R.id.tvKeyword);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
