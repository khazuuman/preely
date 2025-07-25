package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.preely.util.Constraints.*;

import com.example.preely.R;

public class CustomToast extends Toast {
    private static final long SHORT = 2000;
    private static final long LONG = 7000;

    public CustomToast(Context context) {
        super(context);
    }

    @SuppressLint("MissingInflatedId")
    public static Toast makeText(Context context, String message, int duration, int type) {
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        View layout = LayoutInflater.from(context).inflate(R.layout.custom_toast, null, false);
        TextView l1 = layout.findViewById(R.id.notiText);
        ConstraintLayout constraintLayout = layout.findViewById(R.id.bg_layout);
        ImageView iconImg = layout.findViewById(R.id.iconImg);
        l1.setText(message);
        if (type == NotificationType.SUCCESS) {
            constraintLayout.setBackgroundResource(R.drawable.success_shape);
            iconImg.setImageResource(R.drawable.ic_success);
        } else if (type == NotificationType.ERROR) {
            constraintLayout.setBackgroundResource(R.drawable.error_shape);
            iconImg.setImageResource(R.drawable.ic_error);
        }
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        return toast;
    }

//    public static Toast makeText(Context context, String message, int duration, int type, int ImageResource) {
//        Toast toast = new Toast(context);
//        View layout = LayoutInflater.from(context).inflate(R.layout.fancytoast_layout, null, false);
//        TextView l1 = (TextView) layout.findViewById(R.id.toast_text);
//        LinearLayout linearLayout = (LinearLayout) layout.findViewById(R.id.toast_type);
//        ImageView img = (ImageView) layout.findViewById(R.id.toast_icon);
//        l1.setText(message);
//        if (type == 1) {
//            linearLayout.setBackgroundResource(R.drawable.success_shape);
//            img.setImageResource(ImageResource);
//        } else if (type == 2) {
//            linearLayout.setBackgroundResource(R.drawable.warning_shape);
//            img.setImageResource(ImageResource);
//        }
//        toast.setView(layout);
//        return toast;
//    }

}