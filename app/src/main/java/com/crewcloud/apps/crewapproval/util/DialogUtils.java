package com.crewcloud.apps.crewapproval.util;

import android.content.Context;

import com.crewcloud.apps.crewapproval.dialog.MessageDialog;

/**
 * Created by tunglam on 12/7/16.
 */

public class DialogUtils {

    public static void showDialogWithMessage(Context context, String message) {
        MessageDialog messageDialog = new MessageDialog(context);
        messageDialog.setMessage(message);
        messageDialog.show();
    }

    public static void showDialogConfirm(Context context,String title, String content){
    }
}
