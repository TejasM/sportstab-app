package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.firebase.client.DataSnapshot;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LearnGestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener {
	final static int FROM_GAME = 0;
	final static int FROM_BUTTON = 1;
	public GameLoopThread game_thread;
	private CoachingTab activity;
	private float scrl_x = 0;
	private float scrl_y = 0;
	private long scrl_t = 0;
	SharedPreferences settings;
	private int from;
	private List<Boolean> tag_state = new ArrayList<Boolean>();
	private List<String> tag_name = new ArrayList<String>();
	private int NUM_TAGS = 10;
	private Tags tags_from_server;
	public LearnGestureListener(CoachingTab activity, int from) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		settings = activity.getSharedPreferences("myPref", Context.MODE_PRIVATE);
		this.from = from;
		/* ...test code before server
		this.tag_name.add("Transition");
		this.tag_name.add("Early Set");
		this.tag_name.add("Set Play");
		this.tag_name.add("Against Zone");
		this.tag_name.add("Against Man");*/
	}

	public void setAccessThread(GameLoopThread thread) {
		this.game_thread = thread;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent ev) {
		try{
			if (this.from == LearnGestureListener.FROM_GAME){
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
			}
			if (this.from == LearnGestureListener.FROM_BUTTON){
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_RECORD).isTouched(ev.getX(), ev.getY())) {
					if (activity.getGameView().getRecorder().isOn()) {
						activity.getGameView().setHideShow(GameView.SHOW);
						Toast.makeText(activity, "Done Recording! Press Re-all or Re-step to replay", Toast.LENGTH_LONG).show();

						AlertDialog.Builder alert = new AlertDialog.Builder(activity);

						alert.setTitle("Name");
						alert.setMessage("Just finished recording, name your play so you can find it in your playbook!");

						// Set an EditText view to get user input 
						//final EditText input = new EditText(activity);
						RelativeLayout ll = (RelativeLayout) activity.findViewById(R.id.tags);
						RelativeLayout content = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.tags, null);
						
						/*Button b1 = new Button(activity);
						b1.setText("OH yea~");
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						lp.addRule(RelativeLayout.BELOW, R.id.editText1);
						lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
						content.addView(b1, lp);
						System.out.println(b1.getWidth());
						*/List<Button> bs = new ArrayList<Button>();
						//b1.setId(1000);
						//bs.add(b1);
						
						//retrieve the tags for this user from django
						String json_tag = "";
						HttpCommunication httpcomm = new HttpCommunication();
        				try {
        					json_tag = httpcomm.execute("GETTAG").get();
        					System.out.println(json_tag);
        					//json_tag = "{\"tags\":[\"tag1\",\"tag2\",\"tag3\"]}";
        					Gson gson = new Gson();
        					tags_from_server = gson.fromJson(json_tag, Tags.class);
        					this.tag_name = tags_from_server.tags;
        				} catch (InterruptedException e) {
        					e.printStackTrace();
        				} catch (ExecutionException e) {
        					e.printStackTrace();
        				}
						NUM_TAGS = this.tag_name.size();
						tag_state = new ArrayList<Boolean>();
						for (int i = 0; i<NUM_TAGS; i++){
							tag_state.add(false);
						}
						for (int i = 0; i<NUM_TAGS; i++){
							final int j = i;
							final Button b= new Button(activity);
							b.setBackgroundColor(Color.GRAY);
							b.setOnClickListener(new View.OnClickListener() {
					             public void onClick(View v) {
					            	 if (tag_state.get(j)==false){
					            		 b.setBackgroundColor(Color.GREEN);
					            		 tag_state.remove(j);
					            		 tag_state.add(j, true);
					            	 }else{
					            		 b.setBackgroundColor(Color.GRAY);
					            		 tag_state.remove(j);
					            		 tag_state.add(j, false);
					            	 }
					             }
					         });
							b.setText(this.tag_name.get(i));
							RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT);
							
							/*lp1.addRule(RelativeLayout.ALIGN_BASELINE,  bs.get(bs.size()-1).getId());
							lp1.addRule(RelativeLayout.ALIGN_BOTTOM,  bs.get(bs.size()-1).getId());
							lp1.addRule(RelativeLayout.RIGHT_OF, bs.get(bs.size()-1).getId());
							*/
							if (i==0)lp1.addRule(RelativeLayout.BELOW, R.id.editText1);
							else lp1.addRule(RelativeLayout.BELOW, bs.get(i-1).getId());
							content.addView(b, lp1);
							b.setId(1000+i);
							bs.add(b);
							
						}
						alert.setView(content);
						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								//EditText edit = (EditText) activity.findViewById(R.id.editText1);
								Play cur_play = activity.getGameView().getRecorder().getCurrPlay();
								tags_from_server.states = tag_state;
								cur_play.setTags(tags_from_server);
								savePlayHelper(((EditText)((AlertDialog) dialog).getCurrentFocus()).getText().toString(), "default");
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								savePlayHelper("tmp", "canceled");//WARNING: users cannot cancel saving play now
							}
						});

						alert.show();
						activity.unlockDrawer();
						//					activity.getButtonView().buttons.get(ButtonView.BTN_NEXT_STEP).ButtonDisable();
						activity.getButtonView().all_buttons.get(ButtonView.BTN_REPLAY_ALL).ButtonEnable();
						//					activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_STEP).ButtonEnable();
						activity.getGameView().getRecorder().turnOff();
					} else if (!activity.getGameView().getRecorder().replay) {
						activity.getGameView().setHideShow(GameView.HIDE);
						activity.getGameView().getRecorder().turnOn();
						activity.lockDrawer();
						Toast.makeText(activity, "Starting to record :)", Toast.LENGTH_SHORT).show();
						//					activity.getButtonView().buttons.get(ButtonView.BTN_NEXT_STEP).ButtonEnable();
						activity.getButtonView().all_buttons.get(ButtonView.BTN_REPLAY_ALL).ButtonDisable();
						//					activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_STEP).ButtonDisable();
						activity.getGameView().setMode(GameView.FREEPLAY);
					}
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).isTouched(ev.getX(), ev.getY())) {
					if (activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).getName().equals("\uf04c")) {
						activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).getBitmap().eraseColor(android.graphics.Color.TRANSPARENT);
						activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).setName("\uf04b", true);
						activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).paintName();
					} else {
						activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).getBitmap().eraseColor(android.graphics.Color.TRANSPARENT);
						activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).setName("\uf04c", true);
						activity.getButtonView().all_buttons.get(ButtonView.BTN_PAUSE).paintName();
					}
					activity.getGameView().getRecorder().toggleReplay();
				}
				/*		if (activity.getButtonView().buttons.get(ButtonView.BTN_NEXT_STEP).isTouched(ev.getX(), ev.getY())) {
				if (activity.getGameView().getRecorder().isOn()) {
					activity.getGameView().getRecorder().recordNextStep();
					Toast.makeText(activity, "Start Drawing Next Step", Toast.LENGTH_SHORT).show();
				}
			}*/
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_UPDATE).isTouched(ev.getX(), ev.getY())) {
					String name = this.activity.getGameView().getRecorder().getCurrPlay().getID()+"by"+this.activity.getUserId().replace("@", "").replace(".", "");
					savePlayHelper(name, "canceled");
					Toast.makeText(activity, "Play Updated", Toast.LENGTH_SHORT).show();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_EDIT_BALL).isTouched(ev.getX(), ev.getY())) {
					activity.getEditView().editbars.setEditEventMode(ButtonView.BTN_EDIT_BALL);
					Toast.makeText(activity, "Drag to move pass", Toast.LENGTH_SHORT).show();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_EDIT_MOVE).isTouched(ev.getX(), ev.getY())) {
					activity.getEditView().editbars.setEditEventMode(ButtonView.BTN_EDIT_MOVE);
					Toast.makeText(activity, "Drag to move movement", Toast.LENGTH_SHORT).show();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_EDIT_SCREEN).isTouched(ev.getX(), ev.getY())) {
					activity.getEditView().editbars.setEditEventMode(ButtonView.BTN_EDIT_SCREEN);
					Toast.makeText(activity, "Drag to move screen", Toast.LENGTH_SHORT).show();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_RESET_PLAYERS).isTouched(ev.getX(), ev.getY())) {
					activity.thread.resetSpirites();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_TOG_TEAM).isTouched(ev.getX(), ev.getY())) {
					activity.thread.changeDisplayTeams();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_TOD_EDIT).isTouched(ev.getX(), ev.getY())) {
					activity.getGameView().edit_view.toggleEditBars();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_PLAYBOOK).isTouched(ev.getX(), ev.getY())) {
					activity.startPlayBook();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_SETTING).isTouched(ev.getX(), ev.getY())) {
					activity.startSetting();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_SHARE).isTouched(ev.getX(), ev.getY())) {
					Play curr_play = activity.getGameView().getRecorder().getCurrPlay();

					// Check that a play is open
					if (curr_play == null) {
						int duration = Toast.LENGTH_LONG;
						Toast toast = Toast.makeText(activity, "Open or record a play to share", duration);
						toast.show();
					}
					// Check that the play has been saved and named
					else if (curr_play.getID() == null || "".equals(curr_play.getID())) {

						// If not, let them save it
						AlertDialog.Builder alert = new AlertDialog.Builder(activity);

						alert.setTitle("Name");
						alert.setMessage("Name your play in order to share it");

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
								activity.startSharing();
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Canceled.
							}
						});

						alert.show();
					} else {
						activity.startSharing();
					}
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_CHANGE_VIEW).isTouched(ev.getX(), ev.getY())) {
					CoachingTab.change_view = true;
					activity.getButtonView().startAnimation(activity.hide);
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_LOGOUT).isTouched(ev.getX(), ev.getY())) {
					activity.logout();
				}
				if (activity.getButtonView().all_buttons.get(ButtonView.BTN_TOGGLE).isTouched(ev.getX(), ev.getY())){
					if (activity.BTN_vis){
						activity.getButtonView().startAnimation(activity.hide);

						//activity.getButtonBar().startAnimation(activity.show_g);

						activity.BTN_vis = false;
					}
				}
				if (!activity.getGameView().getRecorder().isOn()) {
					/*if (activity.getButtonView().buttons.get(ButtonView.BTN_HALF_FULL).isTouched(ev.getX(), ev.getY())) {
					activity.getGameView().toggleHalfFull();
				}*/
					if (activity.getButtonView().all_buttons.get(ButtonView.BTN_REPLAY_ALL).isTouched(ev.getX(), ev.getY())) {
						activity.getGameView().getRecorder().startReplayAll();
						activity.getGameView().setMode(GameView.EDIT);
					}
					/*if (activity.getButtonView().buttons.get(ButtonView.BTN_REPLAY_STEP).isTouched(ev.getX(), ev.getY())) {
						activity.getGameView().getRecorder().startReplayStep();
					}*/
				}
			}
		}catch(NullPointerException e){
			
		}finally{
		
			
		}
		return true;
	}
	private void savePlayHelper(String name, String defaultName){
		if (name.length() == 0) {
			name = defaultName;
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
					name = name.substring(0, name.length() - j/10-1) + j;
				} else {
					name += j;
				}
			}
		}
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
		//Log.d("onScroll", e1.toString());
		//Log.d("onScroll", e2.toString());
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
