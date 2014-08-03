package com.example.coachingtab;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class SettingActivity extends Activity {

    private SettingView settingView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coaching_tab);

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.setting_menu);
        
        //settingView = new SettingView(this, this);
        rl.addView(settingView);
    }

}
