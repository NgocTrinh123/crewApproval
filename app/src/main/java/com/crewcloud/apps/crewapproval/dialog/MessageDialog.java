package com.crewcloud.apps.crewapproval.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crewcloud.apps.crewapproval.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jerry  on 30/03/2016.
 */
public class MessageDialog extends BaseDialog {

    @Bind(R.id.message_text)
    TextView tvMessage;

    @Bind(R.id.ok)
    Button btClose;


    private String message = "";

    private String messageButton;

    public MessageDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        ButterKnife.bind(this);
    }

    public void setMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            this.message = message;
        }
    }

    public void setMessageButton(String messageButton) {
        this.messageButton = messageButton;
    }

    @Override
    public void show() {
        super.show();
        tvMessage.setText(message);
        if (messageButton != null) {
            btClose.setText(messageButton);
        }
    }

    @OnClick(R.id.ok)
    public void clickOK() {
        dismiss();
    }

    @OnClick(R.id.cancel)
    public void cliclCancel() {
        Toast.makeText(getContext(), "cancel", Toast.LENGTH_LONG).show();
    }
}
