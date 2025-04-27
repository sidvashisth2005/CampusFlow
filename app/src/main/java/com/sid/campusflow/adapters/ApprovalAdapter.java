package com.sid.campusflow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sid.campusflow.R;
import com.sid.campusflow.models.Approval;
import com.sid.campusflow.utils.DateUtils;

import java.util.List;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.ApprovalViewHolder> {
    
    private List<Approval> approvals;
    private Context context;
    private OnApprovalActionListener listener;
    
    public interface OnApprovalActionListener {
        void onApprove(Approval approval, int position);
        void onReject(Approval approval, int position);
        void onItemClick(Approval approval, int position);
    }
    
    public ApprovalAdapter(Context context, List<Approval> approvals, OnApprovalActionListener listener) {
        this.context = context;
        this.approvals = approvals;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ApprovalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_approval, parent, false);
        return new ApprovalViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ApprovalViewHolder holder, int position) {
        Approval approval = approvals.get(position);
        
        // Set user info
        holder.txtUserName.setText(approval.getUserName());
        holder.txtRequestType.setText(approval.getRequestType());
        holder.txtRequestDetails.setText(approval.getRequestDetails());
        
        // Format and set date
        String timeAgo = DateUtils.getTimeAgo(approval.getRequestDate());
        holder.txtRequestTime.setText(timeAgo);
        
        // Load user image
        if (approval.getUserPhotoUrl() != null && !approval.getUserPhotoUrl().isEmpty()) {
            Glide.with(context)
                .load(approval.getUserPhotoUrl())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(holder.imgUser);
        } else {
            holder.imgUser.setImageResource(R.drawable.default_profile);
        }
        
        // Show/hide action buttons based on status
        if (approval.isPending()) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.txtStatus.setVisibility(View.GONE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.txtStatus.setVisibility(View.VISIBLE);
            
            if (approval.isApproved()) {
                holder.txtStatus.setText(R.string.status_approved);
                holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.status_approved));
            } else {
                holder.txtStatus.setText(R.string.status_rejected);
                holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
            }
        }
        
        // Set click listeners
        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApprove(approval, holder.getAdapterPosition());
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(approval, holder.getAdapterPosition());
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(approval, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return approvals != null ? approvals.size() : 0;
    }
    
    public void updateApprovals(List<Approval> newApprovals) {
        this.approvals = newApprovals;
        notifyDataSetChanged();
    }
    
    public void updateApproval(Approval approval, int position) {
        if (position >= 0 && position < approvals.size()) {
            approvals.set(position, approval);
            notifyItemChanged(position);
        }
    }
    
    static class ApprovalViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUser;
        TextView txtUserName, txtRequestType, txtRequestDetails, txtRequestTime, txtStatus;
        Button btnApprove, btnReject;
        
        public ApprovalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.iv_request_icon);
            txtUserName = itemView.findViewById(R.id.tv_request_title);
            txtRequestType = itemView.findViewById(R.id.tv_request_title);
            txtRequestDetails = itemView.findViewById(R.id.tv_request_desc);
            txtRequestTime = itemView.findViewById(R.id.tv_request_date);
            txtStatus = itemView.findViewById(R.id.tv_request_status);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
} 
