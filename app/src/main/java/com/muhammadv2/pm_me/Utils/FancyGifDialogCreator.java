package com.muhammadv2.pm_me.Utils;

import android.app.Activity;
import android.graphics.Color;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.muhammadv2.pm_me.R;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

public class FancyGifDialogCreator {
    public static FancyAlertDialog createFancyAlertDialog(final Activity activity, String userName) {
        return new FancyAlertDialog.Builder(activity)
                .setTitle(userName)
                .setNegativeBtnText("Cancel")
                .setPositiveBtnBackground(R.color.colorPrimaryDark)  //Don't pass R.color.colorvalue
                .setPositiveBtnText("Save")
                .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                .setAnimation(Animation.POP)
                .isCancellable(true)
                .setIcon(R.drawable.man, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(activity, "Rate", Toast.LENGTH_SHORT).show();
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(activity, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }
}
