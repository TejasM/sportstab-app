package com.example.coachingtab;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

public class LearnGestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener {
	public GameLoopThread game_thread;
	private CoachingTab activity;
	private float scrl_x = 0;
	private float scrl_y = 0;
	private long scrl_t = 0;
	SharedPreferences settings;

	public LearnGestureListener(CoachingTab activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		settings = activity.getSharedPreferences("myPref", Context.MODE_PRIVATE);
	}

	public void setAccessThread(GameLoopThread thread) {
		this.game_thread = thread;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent ev) {
		//Log.d("onSingleTapUp",ev.toString());
		for (int i = game_thread.sprites.size() - 1; i >= 0; i--) {
			Sprite sprite = game_thread.sprites.get(i);
			if (sprite.isTouched(ev.getX(), ev.getY())) {
				if (sprite.getState() == Sprite.BLOCK) {
					List<MyPointF> pts = sprite.doneScreenGetData();
					if (activity.getGameView().getRecorder().isOn()) {
						activity.getGameView().getRecorder().onDoneScreenEvent(i, pts);
					}
				} else if (sprite.getState() == Sprite.FREE) {
					game_thread.setBall(i);
				}
			}
		}

		if (activity.getButtonView().buttons.get(ButtonView.BTN_RECORD).isTouched(ev.getX(), ev.getY())) {
			if (activity.getGameView().getRecorder().isOn()) {
				activity.getGameView().setHideShow(GameView.SHOW);
				Toast.makeText(activity, "Done Recording! Press Re-all or Re-step to replay", Toast.LENGTH_LONG).show();

				AlertDialog.Builder alert = new AlertDialog.Builder(activity);

				alert.setTitle("Name");
				alert.setMessage("Just finished recording, name your play so you can find it in your playbook!");

				// Set an EditText view to get user input 
				final EditText input = new EditText(activity);
				alert.setView(input);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String name = input.getText().toString();
						if (name.length() == 0) {
							name = "Default";
						}
						/* check if there's duplicate string, if yes, append a number */
						for (int j = 1; ; j++) {
							int i;
							for (i = 0; i < activity.getGameView().getRecorder().getCatalog().size(); i++) {
								if (activity.getGameView().getRecorder().getCatalog().get(i).compareTo(name) == 0) {
									break;
								}
							}
							if (i == activity.getGameView().getRecorder().getCatalog().size()) {
								/*no duplicate, set name here*/
								final Play play = activity.getGameView().getRecorder().getCurrPlay();
								play.setID(name);
								activity.getGameView().getRecorder().getCatalog().add(name);
								activity.getGameView().getRecorder().saveCurrPlay();

								break;
							} else {
								if (j > 1) {
									name = name.substring(0, name.length() - 1) + j;
								} else {
									name += j;
								}
							}
						}
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

				alert.show();
				activity.getButtonView().buttons.get(ButtonView.BTN_NEXT_STEP).ButtonDisable();
				activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_ALL).ButtonEnable();
				activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_STEP).ButtonEnable();
				activity.getGameView().getRecorder().turnOff();
			} else if (!activity.getGameView().getRecorder().replay) {
				activity.getGameView().setHideShow(GameView.HIDE);
				activity.getGameView().getRecorder().turnOn();
				Toast.makeText(activity, "Starting to record :)", Toast.LENGTH_SHORT).show();
				activity.getButtonView().buttons.get(ButtonView.BTN_NEXT_STEP).ButtonEnable();
				activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_ALL).ButtonDisable();
				activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_STEP).ButtonDisable();
				activity.getGameView().setMode(GameView.FREEPLAY);
			}
		}
		if (activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).isTouched(ev.getX(), ev.getY())) {
			if (activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getName().equals("\uf04c")) {
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getBitmap().eraseColor(android.graphics.Color.TRANSPARENT);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).setName("\uf04b", true);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).paintName();
			} else {
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getBitmap().eraseColor(android.graphics.Color.TRANSPARENT);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).setName("\uf04c", true);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).paintName();
			}
			activity.getGameView().getRecorder().toggleReplay();
		}
		if (activity.getButtonView().buttons.get(ButtonView.BTN_NEXT_STEP).isTouched(ev.getX(), ev.getY())) {
			if (activity.getGameView().getRecorder().isOn()) {
				activity.getGameView().getRecorder().recordNextStep();
				Toast.makeText(activity, "Start Drawing Next Step", Toast.LENGTH_SHORT).show();
			}
		}
		if (!activity.getGameView().getRecorder().isOn()) {
			/*if (activity.getButtonView().buttons.get(ButtonView.BTN_HALF_FULL).isTouched(ev.getX(), ev.getY())) {
				activity.getGameView().toggleHalfFull();
			}*/
			if (activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_ALL).isTouched(ev.getX(), ev.getY())) {
				activity.getGameView().getRecorder().startReplayAll();
				activity.getGameView().setMode(GameView.EDIT);
			}
			if (activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_STEP).isTouched(ev.getX(), ev.getY())) {
				activity.getGameView().getRecorder().startReplayStep();
			}
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent ev) {
		//Log.d("onShowPress",ev.toString());
	}

	@Override
	public void onLongPress(MotionEvent ev) {
		//Log.d("onLongPress",ev.toString());
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
		Log.d("onScroll", e1.toString());
		Log.d("onScroll", e2.toString());
		/*for (int i = thread.sprites.size() - 1; i>=0;i--){
			Sprite sprite = thread.sprites.get(i);
			if(sprite.isTouched(e2.getX(),e2.getY())){
				sprite.onFling(e1, e2, velocityX, velocityY);
			}
		}*/
		if (activity.getGameView().getRecorder().getCurrPlay() != null) {
			if (activity.getGameView().background_index == activity.getGameView().getRecorder().getCurrPlay().getOrientation()) {
				if (!((scrl_x == e1.getX()) && (scrl_y == e1.getY()) && (scrl_t == e1.getDownTime()))) {
					if (e1.getX() - e2.getX() > activity.getGameView().getWidth() * 0.7) {
						activity.getGameView().getRecorder().incrementStep();
						scrl_x = e1.getX();
						scrl_y = e1.getY();
						scrl_t = e1.getDownTime();
					}
					if (e2.getX() - e1.getX() > activity.getGameView().getWidth() * 0.7) {
						activity.getGameView().getRecorder().decrementStep();
						scrl_x = e1.getX();
						scrl_y = e1.getY();
						scrl_t = e1.getDownTime();
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean onDown(MotionEvent ev) {
		//Log.d("onDownd",ev.toString());
		/*for (int i = thread.sprites.size() - 1; i>=0;i--){
			Sprite sprite = thread.sprites.get(i);
			if(sprite.isTouched(ev.getX(),ev.getY())){
				sprite.SetMoveRef(ev.getX(),ev.getY());
			}
		}*/
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		//Log.d("d",e1.toString());
		//Log.d("e2",e2.toString());
		/*for (int i = thread.sprites.size() - 1; i>=0;i--){
			Sprite sprite = thread.sprites.get(i);
			if(sprite.isTouched(e2.getX(),e2.getY())){
				sprite.onFling(e1, e2, velocityX, velocityY);
			}
		}*/
		return true;
	}

}
