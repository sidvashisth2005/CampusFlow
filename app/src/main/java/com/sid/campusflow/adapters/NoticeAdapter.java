package com.sid.campusflow.adapters;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.sid.campusflow.R;
import com.sid.campusflow.models.Notice;
import com.sid.campusflow.utils.DateUtils;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> noticeList;

    public NoticeAdapter(List<Notice> noticeList) {
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        
        holder.tvTitle.setText(notice.getTitle());
        holder.tvContent.setText(notice.getContent());
        
        if (notice.getPublishDate() != null) {
            holder.tvDate.setText(DateUtils.formatDate(notice.getPublishDate()));
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }
        
        if (!TextUtils.isEmpty(notice.getAuthorName())) {
            holder.tvAuthor.setText(notice.getAuthorName());
            holder.tvAuthor.setVisibility(View.VISIBLE);
        } else {
            holder.tvAuthor.setVisibility(View.GONE);
        }
        
        // Set important flag visual indicator
        if (notice.isImportant()) {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_light));
        } else {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }
        
        // Handle attachment if available
        if (!TextUtils.isEmpty(notice.getAttachmentUrl())) {
            holder.btnAttachment.setVisibility(View.VISIBLE);
            holder.btnAttachment.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(notice.getAttachmentUrl()));
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnAttachment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvContent, tvDate, tvAuthor;
        Button btnAttachment;

        NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            btnAttachment = itemView.findViewById(R.id.btn_attachment);
        }
    }
} 