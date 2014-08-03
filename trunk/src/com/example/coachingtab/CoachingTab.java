package com.example.coachingtab;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.firebase.client.Firebase;

public class CoachingTab extends Activity {

	public static final String PREFS_NAME = "myPref";

	// All our layouts
	private LinearLayout game_layout;
	private LinearLayout game_layout_court_button_horiz;
	private LinearLayout game_layout_half;
	private LinearLayout game_layout_only;

	private LayoutParams[] go_full_params;
	private LayoutParams[] go_half_params;
	private LayoutParams[] go_only_params;
	static final int INDEX_COURT = 0;
	static final int INDEX_EDIT = 1;
	static final int INDEX_BUTTON = 2;
	static final int NUM_LAYOUTS = 3;


	private FrameLayout actual_layout;

	private GameView court_view;
	private ButtonView button_view;
	private EditView edit_view;
	private SettingView setting_view;
	public boolean dummy_first;
	private SavedSettings saved_data;
	private List<Bitmap> playbookpictures;
	private ListView listView;
	GameLoopThread thread;

	private Typeface font;

	private DrawerLayout drawerLayout;
	private ListView drawerListView;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	SharedPreferences settings;

	public Object pauseLock = new Object();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		settings = getSharedPreferences(PREFS_NAME, 0);
		playbookpictures = new ArrayList<Bitmap>();

		boolean start_full_screen = true;

		saved_data = new SavedSettings();
		saved_data.savedBefore = settings.getBoolean("savedBefore", false);
		if (saved_data.savedBefore) {
			saved_data.numPlayers = settings.getInt("numPlayers", 0);
			saved_data.playerNames = new String[saved_data.numPlayers];
			saved_data.playerColors = new int[saved_data.numPlayers];
			saved_data.playerRPercent = new float[saved_data.numPlayers];
			saved_data.playerX = new float[saved_data.numPlayers][GameView.BACKGROUND_NUM];
			saved_data.playerY = new float[saved_data.numPlayers][GameView.BACKGROUND_NUM];

			for (int i = 0; i < saved_data.numPlayers; i++) {
				saved_data.playerNames[i] = settings.getString("playerName" + i, "");
				saved_data.playerColors[i] = settings.getInt("playerColor" + i, 0);
				saved_data.playerRPercent[i] = settings.getFloat("playerR" + i, 0);
				saved_data.playerX[i][GameView.HALFCOURT] = settings.getFloat("playerXHalf" + i, 0);
				saved_data.playerX[i][GameView.FULLCOURT] = settings.getFloat("playerXFull" + i, 0);
				saved_data.playerY[i][GameView.HALFCOURT] = settings.getFloat("playerYHalf" + i, 0);
				saved_data.playerY[i][GameView.FULLCOURT] = settings.getFloat("playerYFull" + i, 0);
			}
			saved_data.backgroundIndex = settings.getInt("backgroundIndex", 0);
			if (saved_data.backgroundIndex == GameView.HALFCOURT) {
				start_full_screen = false;

				// TODO: Remove the line below. Now it always starts
				// at full court
				saved_data.backgroundIndex = GameView.FULLCOURT;  // <-- REMOVE
			}
		}

		// Create a thread
		thread = new GameLoopThread(null, this, this);
		dummy_first = true;

		// Initialize each layout
		
		/* The code below initializes the full court layout, then also 
		 * creates the half court layout. Then, it sets layout back to
		 * the full court layout
		 * 
		 * TODO: if start_full_screen above is false, then we need to 
		 * switch the order of the code below to set the half court last
		 */

		// ----------------------------------------------------------
		// Court only -- now this is the first one
		// ----------------------------------------------------------
		// Need this line to findViewById. It also calls constuctors for 3 views
		setContentView(R.layout.gameview_layout_only);
		game_layout_only = (LinearLayout) findViewById(R.id.id_gameview_layout_only);
		court_view  = (GameView) findViewById(R.id.id_court_view);
		button_view = (ButtonView) findViewById(R.id.id_buttons_view);
		button_view.setZOrderOnTop(true);
		button_view.getHolder().setFormat(PixelFormat.TRANSPARENT);

		// Store params locally
		go_only_params = new LayoutParams[NUM_LAYOUTS];
		go_only_params[INDEX_COURT]  = court_view.getLayoutParams();
		go_only_params[INDEX_BUTTON] = button_view.getLayoutParams();

		button_view.setGameLoopThread(court_view.thread);

		// Now initialize the other layouts
		
		// ----------------------------------------------------------
		// Full Court
		// ----------------------------------------------------------
		setContentView(R.layout.gameview_layout);
		game_layout = (LinearLayout) findViewById(R.id.id_gameview_layout);//keep a local reference to the layout
		game_layout_court_button_horiz = (LinearLayout) findViewById(R.id.id_gameview_layout_court_button_horiz);
		edit_view = (EditView) findViewById(R.id.id_editbar_view);
		edit_view.setGameLoopThread(court_view.thread);

		// Store params locally
		go_full_params = new LayoutParams[NUM_LAYOUTS];
		go_full_params[INDEX_COURT] = court_view.getLayoutParams();
		go_full_params[INDEX_BUTTON] = button_view.getLayoutParams();
		go_full_params[INDEX_EDIT] = edit_view.getLayoutParams();

		court_view.set_other_views(button_view, edit_view);
		
		// ----------------------------------------------------------
		// Half court
		// ----------------------------------------------------------
		setContentView(R.layout.gameview_layout_half);
		game_layout_half = (LinearLayout) findViewById(R.id.id_gameview_layout_half);
		go_half_params = new LayoutParams[NUM_LAYOUTS];
		go_half_params[INDEX_COURT] = findViewById(R.id.id_half_layout_top).getLayoutParams();
		go_half_params[INDEX_BUTTON] = findViewById(R.id.id_half_layout_mid).getLayoutParams();
		go_half_params[INDEX_EDIT] = findViewById(R.id.id_half_layout_bot).getLayoutParams();

		//Set the actual view:
		setContentView(R.layout.actual_game);

		actual_layout = (FrameLayout) findViewById(R.id.gameview_layout);
		actual_layout.addView(game_layout_only);
		//Add navigation bar
		font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
		// get list items from strings.xml
		String[] drawerListViewItems = getResources().getStringArray(R.array.items);

		// get ListView defined in activity_main.xml
		drawerListView = (ListView) findViewById(R.id.left_drawer);
		// Set the adapter for the list view
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_view, drawerListViewItems) {
			@Override
			public View getView(int position, View view, ViewGroup viewGroup) {
				View v = super.getView(position, view, viewGroup);
				if (v != null) {
					ListView tempDrawerListView = (ListView) findViewById(R.id.left_drawer);
					((TextView) v).setTypeface(font);
					ViewGroup.LayoutParams params = v.getLayoutParams();
					if (params == null) {
						params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (tempDrawerListView.getHeight()) / 5);
					} else {
						params.height = (int) (tempDrawerListView.getHeight()) / 5;
					}

					v.setLayoutParams(params);
				}

				return v;
			}
		};
		drawerListView.setAdapter(adapter);
		drawerListView.setOnItemClickListener(new DrawerItemClickListener());

		// App Icon
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		actionBarDrawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
				drawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */) {

			@SuppressLint("NewApi")
			@Override
			public void onDrawerOpened(View drawer) {
				super.onDrawerOpened(drawer);
				drawer.bringToFront();
				drawer.buildLayer();
				drawer.clearFocus();
				drawer.requestLayout();
				drawer.forceLayout();
			}
		};

		// Set actionBarDrawerToggle as the DrawerListener
		drawerLayout.setDrawerListener(actionBarDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//setContentView(view);
	}
	public LinearLayout getGameOnlyLayout(){
		return game_layout_only;
	}
	public LinearLayout getGameLayout(){
		return this.game_layout;
	}
	public LayoutParams getFullLayoutParams(){
		return go_full_params[INDEX_COURT];
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns true
		// then it has handled the app icon touch event
		return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			if (position == 0) {
				startPlayBook();
			} else if (position == 1) {

				Play curr_play = getGameView().getRecorder().getCurrPlay();

				// Check that a play is open
				if (curr_play == null) {
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(CoachingTab.this, "Open or record a play to share", duration);
					toast.show();
				}
				// Check that the play has been saved and named
				else if (curr_play.getID() == null || "".equals(curr_play.getID())) {

					// If not, let them save it
					AlertDialog.Builder alert = new AlertDialog.Builder(CoachingTab.this);

					alert.setTitle("Name");
					alert.setMessage("Name your play in order to share it");

					// Set an EditText view to get user input
					final EditText input = new EditText(CoachingTab.this);
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
								for (i = 0; i < getGameView().getRecorder().getCatalog().size(); i++) {
									if (getGameView().getRecorder().getCatalog().get(i).compareTo(name) == 0) {
										break;
									}
								}
								if (i == getGameView().getRecorder().getCatalog().size()) {
									/*no duplicate, set name here*/
									final Play play = getGameView().getRecorder().getCurrPlay();
									play.setID(name);
									getGameView().getRecorder().getCatalog().add(name);
									getGameView().getRecorder().saveCurrPlay();

									break;
								} else {
									if (j > 1) {
										name = name.substring(0, name.length() - 1) + j;
									} else {
										name += j;
									}
								}
							}
							startSharing();
						}
					});

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// Canceled.
						}
					});

					alert.show();
				} else {
					startSharing();
				}

			} else if (position == 2) {
				startSetting();
			} else if (position == 3) {
				getGameView().toggleHideShow();
			} else if (position == 4) {
				logout();
			}
			drawerLayout.closeDrawers();
		}
	}

	private void logout() {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("id", "");
		editor.commit();
		setResult(RESULT_OK); // Activity exiting OK
		Intent intent = new Intent(this, Login.class);
		startActivity(intent);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		actionBarDrawerToggle.syncState();
	}


	private void createSettingView() {
		//setContentView(R.layout.activity_coaching_tab);
		actual_layout.removeAllViews();
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		actual_layout.addView(layoutInflater.inflate(R.layout.activity_coaching_tab, null));

		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
		Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
		Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
		EditText edtName = (EditText) findViewById(R.id.editText1);
		SeekBar sbSize = (SeekBar) findViewById(R.id.seekBar1);
		Button add = (Button) findViewById(R.id.button1);
		Button del = (Button) findViewById(R.id.button2);
		SeekBar sbReplaySpeed = (SeekBar) findViewById(R.id.seekBar2);
		SeekBar sbOverlap = (SeekBar) findViewById(R.id.seekBar3);
		setting_view = new SettingView(this, this, spinner1, spinner2, spinner3, btnSubmit, edtName, sbSize, add, del, sbReplaySpeed, sbOverlap);
		//actual_layout.removeAllViews();
		//actual_layout.addView(setting_view);
	}

	public SavedSettings getSavedSettings() {
		return saved_data;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.activity_coaching_tab, menu);
		return true;
	}


	public GameView getGameView() {
		return court_view;
	}

	public ButtonView getButtonView() {
		return button_view;
	}

	public EditView getEditView() {
		return edit_view;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//view.thread.setRunning(false);
		//		while(true){
		//		synchronized(this){
		court_view.thread.setRunning(false);
		//		}
		//		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean("savedBefore", true);
		editor.putInt("numPlayers", court_view.thread.getNumPlayers());

		court_view.toggleHalfFull(); /* to store the newly changed position to buffer that stores position by orientation */
		court_view.toggleHalfFull(); /* to change it back to the orientation the user left off */

		for (int i = 0; i < court_view.thread.getNumPlayers(); i++) {
			editor.putString("playerName" + i, court_view.thread.sprites.get(i).getName());
			editor.putInt("playerColor" + i, court_view.thread.sprites.get(i).getTeamColor());
			editor.putFloat("playerR" + i, court_view.thread.sprites.get(i).getR() / court_view.thread.sprites.get(i).getBaseR() * 100);
			editor.putFloat("playerXHalf" + i, court_view.thread.sprites.get(i).getXByCourt(GameView.HALFCOURT));
			editor.putFloat("playerXFull" + i, court_view.thread.sprites.get(i).getXByCourt(GameView.FULLCOURT));
			editor.putFloat("playerYHalf" + i, court_view.thread.sprites.get(i).getYByCourt(GameView.HALFCOURT));
			editor.putFloat("playerYFull" + i, court_view.thread.sprites.get(i).getYByCourt(GameView.FULLCOURT));
		}
		editor.putFloat("buttonX", button_view.buttons.get(0).getX());
		editor.putFloat("buttonY", button_view.buttons.get(0).getY());
		editor.putInt("backgroundIndex", court_view.background_index);
		editor.commit();


		String filename = "catalog";
		FileOutputStream outputStream;
		/*view.getRecorder().storeMatrix();*/

		if ((court_view.getRecorder().getCatalog() != null) && (court_view.getRecorder().getCatalog().size() > 0)) {
			try {
				outputStream = this.openFileOutput(filename, Context.MODE_PRIVATE);

				ObjectOutputStream out = new ObjectOutputStream(outputStream);
				out.writeObject(court_view.getRecorder().getCatalog());
				out.close();
				outputStream.close();
			} catch (IOException i) {
				i.printStackTrace();
			}
		}
	}


	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//if (view.isSurfaceCreated()){

		synchronized (this.pauseLock) {
			court_view.thread.setRunning(true);
			this.pauseLock.notifyAll();
		}

		//Thread.yield();
		//}


		/*if (this.dummy_first == false){
  		view.thread.setRunning(true);
  		view.thread.run();
  	}*/
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	public void startSetting() {
		court_view.thread.setRunning(false);
		/*if (setting_view == null){
			createSettingView();
		}*/

		Handler handler = new Handler();
		Runnable r = new SettingRunnable(this);
		//handler.postDelayed(r, 1000);
		handler.post(r);
	}

	private class SettingRunnable implements Runnable {
		CoachingTab activity;

		public SettingRunnable(CoachingTab act) {
			this.activity = act;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			/*Intent intent = new Intent(activity, SettingActivity.class);
			intent.putExtra("com.coachingtab2.activity_ref", (CharSequence) this);
    		startActivity(intent);*/
			//LayoutParams lp =new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			//activity.addContentView(setting_view, lp);
			activity.createSettingView();
			activity.setting_view.initializeSeekBars();
			//RelativeLayout rl = (RelativeLayout) findViewById(R.id.setting_menu);
			//rl.addView(setting_view);
		}
	}

	public void startPlayBook() {
		court_view.thread.setRunning(false);
		Handler handler = new Handler();
		Runnable r = new PlayBookRunnable(this);
		//handler.postDelayed(r, 1000);
		handler.post(r);

		/*change button states if there was no play before */
		if (court_view.getRecorder().getCurrPlay() == null) {
			button_view.buttons.get(ButtonView.BTN_REPLAY_ALL).ButtonEnable();
			button_view.buttons.get(ButtonView.BTN_REPLAY_STEP).ButtonEnable();
		}
	}

	private class PlayBookRunnable implements Runnable {
		CoachingTab activity;

		public PlayBookRunnable(CoachingTab act) {
			this.activity = act;
		}

		@Override
		public void run() {

			//setContentView(R.layout.playbook_layout);
			actual_layout.removeAllViews();
			LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			actual_layout.addView(layoutInflater.inflate(R.layout.playbook_layout, null));


			listView = (ListView) findViewById(R.id.playbook_layout);
			Button playbook_go_back = (Button) findViewById(R.id.playbook_go_back);

			registerForContextMenu(listView);
			//renderPlayBook();
			// Instance of ImageAdapter Class
			listView.setAdapter(new PlayBookAdapter(activity, activity));


			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

					court_view.getRecorder().selectPlay(position);
					startGameView();
				}
			});
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
					AlertDialog.Builder alert = new AlertDialog.Builder(activity);
					final int pos = position;
					alert.setTitle("Delete");
					alert.setMessage("You sure?");


					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							court_view.getRecorder().deletePlay(pos);
							activity.resetCatalogAdapter();
						}
					});

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// Canceled.
						}
					});

					alert.show();
					return true;
				}
			});

			playbook_go_back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					activity.startGameView();
				}

			});

		}
	}

	public void startSharing() {
		court_view.thread.setRunning(false);
		Handler handler = new Handler();
		Runnable r = new SharingRunnable(this);
		handler.post(r);
	}

	private class SharingRunnable implements Runnable {
		CoachingTab activity;

		// Widgets
		Button sharing_go_back;
		Button sharing_send;
		Button add_button;
		ListView lv_recipients;
		AutoCompleteTextView add_recipient;

		// Data structures used to hold names + suggestions
		String[] suggestions;
		ArrayAdapter<String> suggestions_adapter;
		ArrayList<String> recipients;
		ArrayAdapter<String> recipient_adapter;

		public SharingRunnable(CoachingTab act) {
			this.activity = act;
		}

		@Override
		public void run() {

			// Set the xml 
			//setContentView(R.layout.sharing_layout);
			actual_layout.removeAllViews();
			LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			actual_layout.addView(layoutInflater.inflate(R.layout.sharing_layout, null));

			// Get widgets
			sharing_go_back = (Button) findViewById(R.id.sharing_go_back);
			sharing_send = (Button) findViewById(R.id.sharing_send);
			add_button = (Button) findViewById(R.id.add_button);
			lv_recipients = (ListView) findViewById(R.id.recipients);
			add_recipient = (AutoCompleteTextView) findViewById(R.id.enter_name);

			// -- Adapter for AutoComplete ---
			// For the AutoComplete, get all of the auto-complete names
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			suggestions = settings.getString("contacts", "").split("\u001F");
			suggestions_adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, suggestions);
			add_recipient.setAdapter(suggestions_adapter);

			// -- Adapter for recipient ListView --
			recipients = new ArrayList<String>();
			recipient_adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, recipients);
			lv_recipients.setAdapter(recipient_adapter);

			// Callback Functions
			sharing_go_back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.startGameView();
				}
			});
			sharing_send.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					// Re-write the Shared Data
					String new_list = "";

					//Transform Receptions:

					// Combine the suggestions string array with the
					// new recipients string array (remove duplicates)
					Set<String> name_set = new HashSet<String>(Arrays.asList(suggestions));
					List<String> actualRecepients = new ArrayList<String>();
					for (String recipient : recipients) {
						name_set.add(recipient);
						actualRecepients.add(recipient.replace("@", "").replace(".", ""));
					}
					//String[] unique_names = name_set.toArray(new String[name_set.size()]);

					// Now concatenate unique names
					for (String s : name_set) {
						new_list += (s + "\u001F");
					}

					SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("contacts", new_list);
					editor.commit();

					Play curr_play = activity.getGameView().getRecorder().getCurrPlay();
					if (curr_play.getID() != null && !"".equals(curr_play.getID())) {
						for (String name : actualRecepients) {
							try {
								Firebase userref = new Firebase("https://esc472sportstab.firebaseio.com/users/" + name + "/plays/" + curr_play.getID());
								userref.setValue(curr_play.getID());
							} catch (Exception ignored) {

							}
						}
					}

					activity.startGameView();
				}
			});
			add_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					String new_name = add_recipient.getText().toString();

					if (new_name.length() == 0) {
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(activity, "Enter a name", duration);
						toast.show();
						return;
					} else if (recipients.contains(new_name)) {
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(activity, "Already entered", duration);
						toast.show();
					} else {
						recipients.add(new_name);
						recipient_adapter.notifyDataSetChanged();
					}
					add_recipient.setText("");
				}
			});
		}
	}

	public void resetCatalogAdapter() {
		listView.setAdapter(new PlayBookAdapter(this, this));
	}

	/*	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);  
		menu.setHeaderTitle("Context Menu");  
		menu.add(0, v.getId(), 0, "Delete");  
	}

	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		if(item.getTitle()=="Delete"){
			Log.d("THISHSIHSISHI", ""+item.getItemId());
		}
		return true;  
	} */
	public void startGameView() {
		//setContentView(court_relativeLayout);
		//setContentView(game_layout); //this..b/c we're not directly calling with the rsc argument..we avoid calling the constructors
		
		actual_layout.removeAllViews();
		if (court_view.hide_or_show == GameView.HIDE) {
			actual_layout.addView(game_layout_only);
		} else if (court_view.background_index == GameView.FULLCOURT) {
			actual_layout.addView(game_layout);
		} else if (court_view.background_index == GameView.HALFCOURT) {
			actual_layout.addView(game_layout_half);
		}
		
		/*court_view = (GameView) findViewById(R.id.id_court_view);
		button_view = (ButtonView) findViewById(R.id.id_buttons_view);
		edit_view = (EditView) findViewById(R.id.id_editbar_view);
		
		court_view.set_other_views(button_view, edit_view);
		button_view.setGameLoopThread(court_view.thread);
		edit_view.setGameLoopThread(court_view.thread);
		*/
		synchronized (this.pauseLock) {
			court_view.thread.setRunning(true);
			this.pauseLock.notifyAll();
		}
	}

	public void renderPlayBook() {
	}

	public int getPlayBookCount() {
		//return playbookpictures.size();
		return court_view.getRecorder().getCatalog().size();
	}

	public String getPlayBookObject(int position) {
		/*if (playbookpictures.size() > position){
			return playbookpictures.get(position);
		}
		return null;*/
		if (court_view.getRecorder().getCatalog().size() > position) {
			return court_view.getRecorder().getCatalog().get(position);
		}
		return null;
	}

	// Detatches 3 views from their current parent
	public void remove_from_parent() {
		ViewGroup parent;
		parent = (ViewGroup) edit_view.getParent();
		if (parent != null)
			parent.removeView(edit_view);

		parent = (ViewGroup) court_view.getParent();
		if (parent != null)
			parent.removeView(court_view);

		parent = (ViewGroup) button_view.getParent();
		if (parent != null)
			parent.removeView(button_view);
	}

	public void go_half() {

		court_view.hide_or_show = GameView.SHOW;

		this.remove_from_parent();

		court_view.setLayoutParams(go_half_params[INDEX_COURT]);
		button_view.setLayoutParams(go_half_params[INDEX_BUTTON]);
		edit_view.setLayoutParams(go_half_params[INDEX_EDIT]);

		game_layout_half.removeAllViews();
		game_layout_half.addView(court_view);
		game_layout_half.addView(button_view);
		game_layout_half.addView(edit_view);

		actual_layout.removeAllViews();
		//button_view.buttons.get(ButtonView.BTN_HALF_FULL).setName("\uf10c", true);
		actual_layout.addView(game_layout_half);
	}

	public void go_only() {

		court_view.hide_or_show = GameView.HIDE;
		
		this.remove_from_parent();

		court_view.setLayoutParams(go_only_params[INDEX_COURT]);
		button_view.setLayoutParams(go_only_params[INDEX_BUTTON]);
		game_layout_only.removeAllViews();
		game_layout_only.addView(court_view);
		game_layout_only.addView(button_view);

		actual_layout.removeAllViews();
		actual_layout.addView(game_layout_only);

	}

	public void go_full() {

		court_view.hide_or_show = GameView.SHOW;
		
		this.remove_from_parent();

		court_view.setLayoutParams(go_full_params[INDEX_COURT]);
		button_view.setLayoutParams(go_full_params[INDEX_BUTTON]);
		edit_view.setLayoutParams(go_full_params[INDEX_EDIT]);

		game_layout.addView(edit_view);
		game_layout_court_button_horiz.addView(court_view);
		game_layout_court_button_horiz.addView(button_view);

		actual_layout.removeAllViews();
		//button_view.buttons.get(ButtonView.BTN_HALF_FULL).setName("\uf042", true);
		actual_layout.addView(game_layout);
	}
}
