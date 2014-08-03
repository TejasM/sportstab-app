package com.example.coachingtab;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import com.firebase.client.*;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;
import com.google.gson.Gson;


@SuppressLint("WrongCall")
public class GestureRecorder {

	private List<Play> plays;
	private Play curr_play;
	private boolean on;
	public boolean replay;
	private Paint paint;
	private boolean replay_all;
	private GameView view;
	public List<String> catalog;
	private int replay_speed;
	private Firebase listRef;
	private float court_width;
	private float court_height;

	public GestureRecorder(GameView view) {
		on = false;
		replay = false;
		this.view = view;
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(100);
		paint.setTextAlign(Align.CENTER);
		paint.setStyle(Paint.Style.FILL);
		plays = new ArrayList<Play>();
		catalog = new ArrayList<String>();
		replay_speed = 1;
		Intent intent = new Intent(UpdatePlayBookService.class.getName());
		try {
			FileInputStream fileIn = view.activity.openFileInput("catalog");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			catalog = (List<String>) in.readObject();
			in.close();
			fileIn.close();

			intent.putStringArrayListExtra("catalog", (ArrayList<String>) catalog);
			view.activity.startService(intent);
			//updatePlaybook();
		} catch (FileNotFoundException e) {
			//updatePlaybook();
			intent.putStringArrayListExtra("catalog", (ArrayList<String>) catalog);
			view.activity.startService(intent);
			return;
		} catch (IOException i) {
			i.printStackTrace();
			//updatePlaybook();
			intent.putStringArrayListExtra("catalog", (ArrayList<String>) catalog);
			view.activity.startService(intent);
			return;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* load matrix from MatrixData for the plays
		restoreMatrix(); */
	}

	private void updatePlaybook() {
		SharedPreferences settings = view.activity.getSharedPreferences(CoachingTab.PREFS_NAME, CoachingTab.MODE_PRIVATE);
		String id = settings.getString("id", "").replace("@", "").replace(".", "");
		listRef = new Firebase("https://esc472sportstab.firebaseio.com/users/" + id + "/plays");
		listRef.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
				Firebase ref = new Firebase("https://esc472sportstab.firebaseio.com/plays/" + snapshot.getName());
				ref.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot snapshot) {
						try {
							String[] files = view.activity.fileList();
							for (String file : files) {
								if (file.equals(snapshot.getName())) {
									return;
								}
							}
							Gson gson = new Gson();

							Play JsonPlay = gson.fromJson(snapshot.getValue().toString(), Play.class);
							FileOutputStream outputStream = view.activity.openFileOutput(snapshot.getName(), Context.MODE_PRIVATE);
							ObjectOutputStream out_play = new ObjectOutputStream(outputStream);
							out_play.writeObject(JsonPlay);
							out_play.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}

					@Override
					public void onCancelled(FirebaseError firebaseError) {

					}

				});

				FileOutputStream fileOut = null;
				try {
					fileOut = view.activity.openFileOutput("catalog", CoachingTab.MODE_PRIVATE);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					if (!catalog.contains(snapshot.getName())) {
						catalog.add(snapshot.getName());
						out.writeObject(catalog);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			}

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
			}

			@Override
			public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
			}

			@Override
			public void onCancelled(FirebaseError firebaseError) {

			}

		});
	}

	public void onDraw(Canvas canvas) {
		if (replay) {
			int step = curr_play.getCurrStep() + 1;
			canvas.drawText("Step: " + step, canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
		}

	}

	//public List<Play> getPlays(){
	/*return plays;*/
	//}

	public List<String> getCatalog() {
		return catalog;
	}

	public boolean isOn() {
		return on;
	}

	public void selectPlay(int position) {
		/*curr_play = plays.get(position);*/
		loadPlay(catalog.get(position));
		Bitmap baseBmp = Bitmap.createBitmap(view.thread.sprites.get(0).getBaseBitmap());
		Bitmap baseBlock = Bitmap.createBitmap(view.thread.sprites.get(0).getBaseBlockBitmap());
		while (view.thread.sprites.size() > 0) {
			view.thread.sprites.remove(0);
		}
		//before setting up the new players, i need to do an extra step of setting the court orientation, b/c the constructor is dependent on this
		view.setHalfFull(curr_play.getOrientation());

		for (int i = 0; i < curr_play.players.size(); i++) {
			view.thread.sprites.add(new Sprite(curr_play.players.get(i), view, view.activity, baseBmp, baseBlock));
		}
		view.initPlayerOrder();
		curr_play.setCurrStep(0);
		startReplay();
		view.edit_view.editbars.render(curr_play);
		view.edit_view.createPlayerIcons();//re-render the player icons, as the player sets might have been changed

	}

	public void deletePlay(int position) {
		File dir = view.activity.getFilesDir();
		File file = new File(dir, catalog.get(position));
		file.delete();
		catalog.remove(position);
	}

	public void storeMatrix() {
		/*for (int i = 0; i < plays.size(); i++){
			plays.get(i).storeMatrix();
		}*/
		curr_play.storeMatrix();

	}

	public void restoreMatrix() {
		/*for (int i = 0; i < plays.size(); i++){
			plays.get(i).restoreMatrix();
		}*/
		curr_play.restoreMatrix();
	}

	public Play getCurrPlay() {
		return curr_play;
	}

	/**
	 * ******************replaying part*******************************
	 */
	public void replay() {
		if (replay_all) {
			if (curr_play.CurrStep().replay(view.thread.sprites, view, replay_speed) == true) {/* this step is done */
				for (Sprite sprite : view.thread.sprites) {
					sprite.TouchUpCback();
				}
				if (!curr_play.IncrementStep()) {/* not the last step, start next step */
					curr_play.CurrStep().startStep(view.thread.sprites, view);
				} else {
					replay = false;
					curr_play.setCurrStep(curr_play.steps.size() - 1);
					curr_play.CurrStep().startStep(view.thread.sprites, view);
				}
			}
		} else {
			if (curr_play.CurrStep().replay(view.thread.sprites, view, replay_speed) == true) {/* this step is done */
				replay = false;
				for (Sprite sprite : view.thread.sprites) {
					sprite.TouchUpCback();
				}
				curr_play.CurrStep().startStep(view.thread.sprites, view);
			}
		}
		updatePointer();
	}

	public void incrementStep() {
		if (!curr_play.IncrementStep()) {/* not the last step, start next step */
			curr_play.CurrStep().startStep(view.thread.sprites, view);
		}
	}

	public void decrementStep() {
		if (!curr_play.decrementStep()) {/* not the last step, start next step */
			curr_play.CurrStep().startStep(view.thread.sprites, view);
		}
	}

	public void startReplayAll() {
		if (curr_play != null) {
			replay = true;
			curr_play.setCurrStep(0);
			replay_all = true;
			startReplay();
		}
	}

	public void startReplayStep() {
		if (curr_play != null) {
			replay = true;
			replay_all = false;
			startReplay();
		}
	}

	public void startReplay() {
		view.setHalfFull(curr_play.getOrientation());
		curr_play.CurrStep().startStep(view.thread.sprites, view);

	}

	public void setReplaySpeed(int tempReplaySpeed) {
		replay_speed = tempReplaySpeed;
	}

	public void toggleReplay() {
		replay = !replay;
	}

	public void updatePointer() {
		int left = (int) ((float) curr_play.CurrStep().replay_progress / (float) curr_play.CurrStep().playerMove.get(0).points.size() * (float) view.edit_view.getWidth());
		if (view.edit_view.pointer != null) {
			view.edit_view.pointer.simpleUpdate(left, view.edit_view.pointer.getY());
		}
	}

	/**
	 * ******************recording part********************************
	 */
	public void onScreenEvent(int playerIndex, MyPointF down, MyPointF up) {
		curr_play.CurrStep().onScreenEvent(playerIndex, down, up);
	}

	public void onDoneScreenEvent(int playerIndex, List<MyPointF> pts) {
		curr_play.CurrStep().onDoneScreenEvent(playerIndex, pts);
	}

	public void onPassEvent(int passingTo, int passFrom) {
		curr_play.CurrStep().onPassEvent(passingTo, passFrom);
	}

	public void onUpdatePass(float percentage) {
		//curr_play.CurrStep().onUpdatePass(percentage);
	}

	public void turnOn() {
		curr_play = new Play(view.background_index, view.thread.sprites, view, view.activity);
		on = true;
		//let's just say...we only have one step for now
		//if (play.steps.size() != 0){
		//	play.steps.remove(0);	
		//}
		curr_play.steps.add(new Step(view.thread.sprites, view));
		initStep();
		updatePointer();
	}

	public void turnOff() {
		on = false;
		if (view.edit_view.editbars!=null)
		view.edit_view.editbars.render(curr_play);
		//plays.add(curr_play);
		//curr_play.setID(String.format("Play #%d", plays.size()));

	}

	public void saveCurrPlay() {

		final ProgressDialog progress = ProgressDialog.show(view.activity, "Saving", "Saving to PlayBook...", true);
		progress.setCancelable(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				FileOutputStream outputStream;
				view.getRecorder().storeMatrix();

				if ((view.getRecorder().getCatalog() != null) && (view.getRecorder().getCatalog().size() > 0)) {
					try {
						outputStream = view.activity.openFileOutput(curr_play.getID(), Context.MODE_PRIVATE);

						ObjectOutputStream out = new ObjectOutputStream(outputStream);
						curr_play.scaleXY((float) 1 / court_width, (float) 1 / court_height);
						out.writeObject(curr_play);
						//curr_play.scaleXY(view.getWidth(), view.getHeight());
						Firebase ref = new Firebase("https://esc472sportstab.firebaseio.com/users");
						SimpleLogin authClient = new SimpleLogin(ref);
						authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
							public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
								if (error != null) {
								} else {
									Gson gson = new Gson();
									String JsonPlay = gson.toJson(curr_play);

									SharedPreferences settings = view.activity.getSharedPreferences(CoachingTab.PREFS_NAME, CoachingTab.MODE_PRIVATE);
									String id = settings.getString("id", "").replace("@", "").replace(".", "");
									Firebase ref = new Firebase("https://esc472sportstab.firebaseio.com/plays/" + curr_play.getID());
									Firebase userref = new Firebase("https://esc472sportstab.firebaseio.com/users/" + id + "/plays/" + curr_play.getID());
									userref.setValue(curr_play.getID());
									ref.setValue(JsonPlay);
								}
							}
						});
						out.close();
						outputStream.close();
						//curr_play.scaleXY(view.background[view.background_index].getWidth(), view.background[view.background_index].getHeight());
						loadPlay(curr_play.getID());
					} catch (IOException i) {
						i.printStackTrace();
					}
				}
				progress.dismiss();
			}
		}).start();
	}

	public void loadPlay(String play_name) {
		try {
			FileInputStream fileIn = view.activity.openFileInput(play_name);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			curr_play = (Play) in.readObject();
			curr_play.scaleXY(view.background[view.background_index].getWidth(), view.background[view.background_index].getHeight());
			System.out.println("LOADPLAY");
			System.out.println(view.background[view.background_index].getWidth());
			System.out.println(view.background[view.background_index].getHeight());
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		restoreMatrix();
	}

	private void initStep() {
		this.court_height = this.view.background[this.view.background_index].getHeight();
		this.court_width = this.view.background[this.view.background_index].getWidth();
		curr_play.CurrStep().recordInitStates(view.thread.sprites, view);
	}

	/**
	 * **
	 * use to identify which step the user think this is, and call recordOneStep
	 * to actually record what happened
	 */
	public void recordOneSnapshot(GameView view) {
		curr_play.CurrStep().recordOneStep(view.thread.sprites, view);
	}

	public void recordNextStep() {
		curr_play.addStep(view.thread.sprites, view);
		initStep();
	}

	public int getReplaySpeed() {
		return replay_speed;
	}

	public void scrollPlay(float percent) {
		int progress = (int) (percent * (float) curr_play.CurrStep().playerMove.get(0).points.size());
		curr_play.CurrStep().setReplayProgress(progress);
		this.replay();
	}

	public void checkEvent() {
		List<Event> events = this.curr_play.CurrStep().events;
		Sprite p = view.edit_view.pointer;
		for (int i = 0; i < events.size(); i++) {
			float x = (float) view.getWidth() * (float) events.get(i).timepoint / (float) this.curr_play.CurrStep().playerMove.get(0).points.size();
			if (p.justCrossed(x)) {
				if (events.get(i).event_type == Step.PASS) {
					if (p.getX() > x) {//moving forward in time
						view.thread.setBall(events.get(i).passTo);
					} else {
						view.thread.setBall(events.get(i).passFrom);
						view.thread.passComplete();//don't animate..just get it back in a instant
					}
				} else if (events.get(i).event_type == Step.DONESCREEN) {
					if (p.getX() > x) {//moving forward in time
						view.thread.sprites.get(events.get(i).doneScreenPlayerIndex).doneScreen();
					} else {
						view.thread.sprites.get(events.get(i).doneScreenPlayerIndex).onScreen(events.get(i).doneScreenDown, events.get(i).doneScreenUp);
					}

				} else if (events.get(i).event_type == Step.SCREEN) {
					if (p.getX() > x) {//moving forward in time
						view.thread.sprites.get(events.get(i).screenPlayerIndex).onScreen(events.get(i).screenDown, events.get(i).screenUp);
					} else {
						view.thread.sprites.get(events.get(i).screenPlayerIndex).doneScreen();
					}
				}
			}
		}
	}

	public void updatePlayBook(){
		FileInputStream fileIn = null;
		try {
			fileIn = view.activity.openFileInput("catalog");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			catalog = (List<String>) in.readObject();
			in.close();
			fileIn.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
