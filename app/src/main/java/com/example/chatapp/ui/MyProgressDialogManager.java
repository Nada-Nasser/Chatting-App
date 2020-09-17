package com.example.chatapp.ui;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.VisibleForTesting;

public class MyProgressDialogManager
{
    // loading display
    @VisibleForTesting
    private static ProgressDialog mProgressDialog = null;

    public static void showProgressDialog(Context context)
    {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
