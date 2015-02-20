package com.fireminder.podcastcatcher.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.services.SubscriptionService;

public class SubscribeDialogFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnClickListener {
    private static final String EXTRA_SUBSCRIPTION_URI = "extra_subscription_uri";
    EditText mUrlEditText;

    public static SubscribeDialogFragment newInstance(String emptyOrUri) {
        SubscribeDialogFragment fragment = new SubscribeDialogFragment();
        if (!emptyOrUri.isEmpty()) {
            Bundle args = new Bundle();
            args.putString(EXTRA_SUBSCRIPTION_URI, emptyOrUri);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View promptsView = inflater.inflate(R.layout.dialog_subscribe, null, false);


        mUrlEditText = (EditText) promptsView.findViewById(R.id.rss_feed);

        Bundle args = getArguments();
        if (args != null) {
            String uri = args.getString(EXTRA_SUBSCRIPTION_URI);
            mUrlEditText.setText(uri);
        }
        (promptsView.findViewById(R.id.paste_btn)).setOnClickListener(this);

        return new AlertDialog.Builder(getActivity()).setView(promptsView)
                .setPositiveButton(getString(R.string.ok), this)
                .setNegativeButton(getString(R.string.cancel), this)
                .create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.paste_btn) {
            //TODO update Clipboard
            ClipboardManager clipboard = (ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            String pasteData = "";
            if (clipboard.hasText()) {
                pasteData = clipboard.getText().toString();
            }
            if (pasteData != null) {
                mUrlEditText.setText(pasteData);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                String targetUrl = mUrlEditText.getText().toString();
                SubscriptionService.launchSubscriptionService(getActivity(), targetUrl);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
        }

    }
}
