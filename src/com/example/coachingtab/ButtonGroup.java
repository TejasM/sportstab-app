package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

public class ButtonGroup extends SurfaceView {
	
	public CoachingTab activity;
	private SurfaceHolder holder;
	private boolean surfaceCreated = false;
	public GameLoopThread thread;
	
	public ButtonGroup(Context context) {
		super(context);
		init(context);
	}

	public void init(Context context) {
		activity = (CoachingTab) context;

		holder = getHolder();

		holder.addCallback(new Callback() {

			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				surfaceCreated = false;
			}

			public void surfaceCreated(SurfaceHolder holder) {
				
				surfaceCreated = true;
				//System.out.println("SURFACE CREATED");
			}

			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				// TODO Auto-generated method stub
				//System.out.println("SURFACE CHANGED");
				activity.getGameView().init_buttons_position();
			}
		});

	}
}
