package com.sid.campusflow.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.sid.campusflow.R;

public class RejectionDialogFragment extends DialogFragment {
    private OnRejectionReasonListener listener;

    public interface OnRejectionReasonListener {
        void onReasonEntered(String reason);
    }

    public RejectionDialogFragment(OnRejectionReasonListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rejection_reason, null);

        final EditText reasonEditText = view.findViewById(R.id.rejectionReason);

        builder.setView(view)
                .setTitle("Enter Rejection Reason")
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = reasonEditText.getText().toString().trim();
                    if (!reason.isEmpty()) {
                        listener.onReasonEntered(reason);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
} 