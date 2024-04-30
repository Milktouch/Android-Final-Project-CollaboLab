package com.collabolab.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.collabolab.R;

public class LoadDialog extends Dialog {
    public LoadDialog(Context context){
        super(context);
    }
    public void setMessage(String message){
        this.setCancelable(false);
        try{
            TextView messageTV =findViewById(R.id.message);
            messageTV.setText(message);
        }catch (Exception e){
            View view = getLayoutInflater().inflate(R.layout.load_dialog_layout,null);
            super.setContentView(view);
            TextView messageTV =view.findViewById(R.id.message);
            messageTV.setText(message);
        }
    }


}
