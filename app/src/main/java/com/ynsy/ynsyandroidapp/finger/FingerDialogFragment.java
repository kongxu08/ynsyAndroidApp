package com.ynsy.ynsyandroidapp.finger;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ynsy.ynsyandroidapp.R;

public class FingerDialogFragment extends DialogFragment {

    private static final long DELAY_MILLIS = 1000;

    private TextView mCancelButton;
    private ImageView imageView;
    private TextView textView;
    public void setTextView(String msg){
        this.textView.setText(msg);
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("指纹识别");
            }
        }, DELAY_MILLIS);
    }
    public void setImageView(int id){
        this.imageView.setImageResource(id);
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(R.drawable.ic_fp_40px);
            }
        }, DELAY_MILLIS);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(android.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("验 证");
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);

        imageView=v.findViewById(R.id.fingerprint_icon);
        textView=v.findViewById(R.id.fingerprint_status);

        mCancelButton = v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return v;
    }

}
