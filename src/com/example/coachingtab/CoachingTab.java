package com.example.coachingtab;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class CoachingTab extends Activity {

	public static final String PREFS_NAME = "myPref";

	// All our layouts
	private RelativeLayout game_layout;
	private LinearLayout game_layout_court_button_horiz;
	private LinearLayout game_layout_half;
	private LinearLayout game_layout_main;
	private LinearLayout button_category;
	private RelativeLayout game_layout_only;
	private RelativeLayout game_layout_court_button;

	private LayoutParams[] go_full_params;
	private LayoutParams[] go_half_params;
	private LayoutParams[] go_only_params;
	static final int INDEX_COURT = 0;
	static final int INDEX_EDIT = 1;
	static final int INDEX_BUTTON = 2;
	static final int NUM_LAYOUTS = 3;

	private float lastFactor;


	private FrameLayout actual_layout;

	private GameView court_view;
	private ButtonView button_view;
	private EditView edit_view;
	private SettingView setting_view;
	public boolean dummy_first;
	public boolean set_w = false;
	private boolean sw = false;
	static boolean change_view = false;
	private SavedSettings saved_data;
	private List<Bitmap> playbookpictures;
	private ListView listView;
	GameLoopThread thread;
    private String user_id;
	private String id;
	private Typeface font;

	private DrawerLayout drawerLayout;
	private ListView drawerListView;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	SharedPreferences settings;
	
	static int BTB_MODE = 0;
	static final int BTB_DEFAULT = 0;
	static final int BTB_SHOWN = 1;
	static final int BTB_PSHOWN = 2;
	static final int BTB_HIDDEN = 3;
	public TranslateAnimation hide = new TranslateAnimation(0, 0, 0, 0);
	public TranslateAnimation show = new TranslateAnimation(0, 0, 0, 0);
	public float offset = 0;
	public float offset_tweak = 0;
	public TranslateAnimation hide_g;
	public TranslateAnimation show_g;
	public float offset_g = 0;
	public TranslateAnimation hide_pg;
	public TranslateAnimation show_pg;
	public float offset_pg = 0;
	public TranslateAnimation show_ppg;
	public float offset_ppg = 0;
	
	public List<Button> button_categories = new ArrayList<Button>();
	static final int NUM_CAT = 5;
	
	public static boolean BTN_vis = true;
	
	static int DIS_MODE = 0;
	static final int DIS_DEFAULT = 0;
	static final int DIS_FULL = 1;
	static final int DIS_EDIT = 2;
	static final int DIS_HAFL = 3;
	
	static int curr_cat = 0;
	static final int CAT_DEFAULT = 0; 
	static final int CAT_STD = 1;
	static final int CAT_PLAYBACK = 2;
	static final int CAT_EDIT = 3;
	static final int CAT_TOOLS = 4;
	
	private float x, y;
	
	public Object pauseLock = new Object();
	public Object switchLock = new Object();
	public String getId(){
		return this.id;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		settings = getSharedPreferences(PREFS_NAME, 0);
		playbookpictures = new ArrayList<Bitmap>();
		button_category = new LinearLayout(this);
		game_layout_court_button = new RelativeLayout(this);
		id = settings.getString("id", "").replace("@", "").replace(".", "");
		//get the username from login
		Intent intent = getIntent();
		user_id = intent.getStringExtra("id");
		boolean start_full_screen = true;
		
		saved_data = new SavedSettings();
		saved_data.savedBefore = settings.getBoolean("savedBefore", false);
		saved_data.savedBefore = false;
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
		game_layout_only = (RelativeLayout) findViewById(R.id.id_gameview_layout_only);
		court_view  = (GameView) findViewById(R.id.id_court_view);
		button_view = (ButtonView) findViewById(R.id.id_buttons_view);
		//button_view.setZOrderOnTop(true);
		//button_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
		
		//court_view.setZOrderOnTop(true);
		court_view.getHolder().setFormat(PixelFormat.TRANSPARENT);

		// Store params locally
		go_only_params = new LayoutParams[NUM_LAYOUTS];
		go_only_params[INDEX_COURT]  = court_view.getLayoutParams();
		go_only_params[INDEX_BUTTON] = button_view.getLayoutParams();

		button_view.setGameLoopThread(court_view.thread);
		button_view.setGestureDetector();

		// Now initialize the other layouts

		// ----------------------------------------------------------
		// Full Court
		// ----------------------------------------------------------
		setContentView(R.layout.gameview_layout);
		game_layout = (RelativeLayout) findViewById(R.id.id_gameview_layout);//keep a local reference to the layout
		game_layout_main = (LinearLayout) findViewById(R.id.id_gameview_layout_main);
		game_layout_court_button_horiz = (LinearLayout) findViewById(R.id.id_gameview_layout_court_button_horiz);
		edit_view = (EditView) findViewById(R.id.id_editbar_view);
		edit_view.setGameLoopThread(court_view.thread);

		edit_view.setZOrderOnTop(true);
		edit_view.getHolder().setFormat(PixelFormat.TRANSPARENT);

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
		font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
		
		actual_layout = (FrameLayout) findViewById(R.id.gameview_layout);
		actual_layout.addView(game_layout_only);
		/*//Add navigation bar
		
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
		actionBarDrawerToggle = new ActionBarDrawerToggle(this,                   //host Activity 
				drawerLayout,         // DrawerLayout object 
				R.drawable.ic_drawer, //  nav drawer icon to replace 'Up' caret 
				R.string.drawer_open,  // "open drawer" description 
				R.string.drawer_close  // "close drawer" description 
				) {

			@SuppressLint("NewApi")
			@Override
			public void onDrawerOpened(View drawer) {
				super.onDrawerOpened(drawer);
				sw = true;
				getButtonView().startAnimation(hide);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				drawerListView.bringToFront();
				drawerLayout.setTop(court_view.getTop() + 1);
				drawerLayout.bringChildToFront(drawer);
				drawerLayout.requestLayout();
				
			}


			@SuppressLint("NewApi")
			public void onDrawerSlide(View drawerView, float slideOffset)
			{
				if (getGameView().getRecorder().isOn()){
					drawerLayout.closeDrawers();
				}
				float moveFactor = (drawerListView.getWidth() * slideOffset);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					actual_layout.setTranslationX(moveFactor);
				}
				else
				{
					TranslateAnimation anim = new TranslateAnimation(lastFactor, moveFactor, 0.0f, 0.0f);
					anim.setDuration(0);
					anim.setFillAfter(true);
					actual_layout.startAnimation(anim);

					lastFactor = moveFactor;
				}
			}
		};

		// Set actionBarDrawerToggle as the DrawerListener
		drawerLayout.setDrawerListener(actionBarDrawerToggle);
		//getActionBar().setDisplayHomeAsUpEnabled(true);*/
		court_view.hide_or_show = GameView.HIDE;

		animation_init();
		init_button_categories();
		button_view_creator();
	}
	public String getUserId() {
		return user_id;
	}
	public void setUserId(String user_id) {
		this.user_id = user_id;
	}
	public RelativeLayout getGameOnlyLayout(){
		return game_layout_only;
	}
	public RelativeLayout getGameLayout(){
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

	public void lockDrawer() {
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	public void unlockDrawer() {
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
				synchronized (court_view.getHolder()) {
					if (actual_layout.findViewById(court_view.getId()) == null){
						startGameView();
					}
					getGameView().toggleHideShow();
				}
			} else if (position == 4) {
				logout();
			}
			drawerLayout.closeDrawers();
		}
	}

	public void change_view() {
		synchronized (court_view.getHolder()) {
			if (actual_layout.findViewById(court_view.getId()) == null){
				startGameView();
			}
			getGameView().toggleHideShow();
		}
	}
	
	public void logout() {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("id", "");
		editor.commit();
		setResult(RESULT_OK); // Activity exiting OK
		Intent intent = new Intent(this, Login.class);
		startActivity(intent);
	}

/*	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		actionBarDrawerToggle.syncState();
	}*/


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
	
	public LinearLayout getButtonBar() {
		return button_category;
	}
	
	public int getCat(){
		return curr_cat; 
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

		//court_view.toggleHalfFull(); /* to store the newly changed position to buffer that stores position by orientation */
		//court_view.toggleHalfFull(); /* to change it back to the orientation the user left off */

		for (int i = 0; i < court_view.thread.getNumPlayers(); i++) {
			editor.putString("playerName" + i, court_view.thread.sprites.get(i).getName());
			editor.putInt("playerColor" + i, court_view.thread.sprites.get(i).getTeamColor());
			editor.putFloat("playerR" + i, court_view.thread.sprites.get(i).getR() / court_view.thread.sprites.get(i).getBaseR() * 100);
			editor.putFloat("playerXHalf" + i, court_view.thread.sprites.get(i).getXByCourt(GameView.HALFCOURT));
			editor.putFloat("playerXFull" + i, court_view.thread.sprites.get(i).getXByCourt(GameView.FULLCOURT));
			editor.putFloat("playerYHalf" + i, court_view.thread.sprites.get(i).getYByCourt(GameView.HALFCOURT));
			editor.putFloat("playerYFull" + i, court_view.thread.sprites.get(i).getYByCourt(GameView.FULLCOURT));
		}
		editor.putFloat("buttonX", button_view.all_buttons.get(0).getX());
		editor.putFloat("buttonY", button_view.all_buttons.get(0).getY());
		editor.putInt("backgroundIndex", court_view.background_index);
		editor.commit();


		String filename = "catalog" + id;
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

		this.startGameView();
		/*synchronized (this.pauseLock) {
			court_view.thread.setRunning(true);
			this.pauseLock.notifyAll();
		}*/

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
			button_view.all_buttons.get(ButtonView.BTN_REPLAY_ALL).ButtonEnable();
			//			button_view.buttons.get(ButtonView.BTN_REPLAY_STEP).ButtonEnable();
		}
	}

	private class PlayBookRunnable implements Runnable {
		CoachingTab activity;

		public PlayBookRunnable(CoachingTab act) {
			this.activity = act;
		}

		@Override
		public void run() {
			court_view.deleteRecoder();
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
		ListView lv_contacts;		
		AutoCompleteTextView add_recipient;

		// Data structures used to hold names + suggestions
		String[] past_recipients;
		String[] suggestions;	// = Past recipients + email contacts
		ArrayAdapter<String> suggestions_adapter;
		ArrayAdapter<String> suggestions_adapter2;
		ArrayList<String> recipients;
		ArrayAdapter<String> recipient_adapter;

		public SharingRunnable(CoachingTab act) {
			this.activity = act;
		}

		// The following function gets email addresses from the contact list
		// Copied from: http://stackoverflow.com/questions/10117049
		public ArrayList<String> getNameEmailDetails() {
		    ArrayList<String> emlRecs = new ArrayList<String>();
		    HashSet<String> emlRecsHS = new HashSet<String>();
		    ContentResolver cr = activity.getContentResolver();
		    String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID, 
		            ContactsContract.Contacts.DISPLAY_NAME,
		            ContactsContract.Contacts.PHOTO_ID,
		            ContactsContract.CommonDataKinds.Email.DATA, 
		            ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
		    String order = "CASE WHEN " 
		            + ContactsContract.Contacts.DISPLAY_NAME 
		            + " NOT LIKE '%@%' THEN 1 ELSE 2 END, " 
		            + ContactsContract.Contacts.DISPLAY_NAME 
		            + ", " 
		            + ContactsContract.CommonDataKinds.Email.DATA
		            + " COLLATE NOCASE";
		    String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
		    Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
		    if (cur.moveToFirst()) {
		        do {
		            // names comes in hand sometimes
		            String name = cur.getString(1);
		            String emlAddr = cur.getString(3);

		            // keep unique only
		            if (emlRecsHS.add(emlAddr.toLowerCase())) {
		                emlRecs.add(emlAddr);
		            }
		        } while (cur.moveToNext());
		    }

		    cur.close();
		    return emlRecs;
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
			lv_contacts = (ListView) findViewById(R.id.contacts);
			add_recipient = (AutoCompleteTextView) findViewById(R.id.enter_name);

			// -- Adapter for AutoComplete ---
			// For the AutoComplete, get all of the auto-complete names
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			String saved_past_recipient_string = settings.getString("contacts", "");
			if (saved_past_recipient_string.length() > 0) {
				past_recipients = saved_past_recipient_string.split("\u001F");
			} else {
				past_recipients = new String[0];
			}
			
			// Merge the past recipients with the email contacts
			Set<String> all_emails = new TreeSet<String>(Arrays.asList(past_recipients));
			for (String contact : getNameEmailDetails()) {
				all_emails.add(contact);
			}
			suggestions = (String[]) all_emails.toArray(new String[0]);
			suggestions_adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, suggestions);
			add_recipient.setAdapter(suggestions_adapter);
			suggestions_adapter2 = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, suggestions);
			lv_contacts.setAdapter(suggestions_adapter2);
			
			// -- Adapter for Past contacts ---

			// -- Adapter for recipient ListView --
			recipients = new ArrayList<String>();
			recipient_adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, recipients);
			lv_recipients.setAdapter(recipient_adapter);

			// Callback Functions
			lv_contacts.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					final String email = (String) parent.getItemAtPosition(position);
					if (recipients.contains(email)) {
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(activity, "Already entered", duration);
						toast.show();
					} else {
						recipients.add(email);
						recipient_adapter.notifyDataSetChanged();
					}
				}
			});

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
					Set<String> name_set = new HashSet<String>(Arrays.asList(past_recipients));
					List<String> actualRecepients = new ArrayList<String>();
					for (String recipient : recipients) {
						name_set.add(recipient);
						actualRecepients.add(recipient.replace("@", "").replace(".", ""));
					}
					//String[] unique_names = name_set.toArray(new String[name_set.size()]);

					// Now concatenate unique names
					for (String s : name_set) {
						if (new_list.length() != 0) {
							new_list += "\u001F";
						}
						new_list += s;
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
		System.out.println("startGameView()");
		System.out.println(court_view.hide_or_show);
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
		
		parent = (ViewGroup) button_category.getParent();
		if (parent != null)
			parent.removeView(button_category);
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
		for (int i = 0; i<button_view.all_buttons.size();i++){
			button_view.all_buttons.get(i).ButtonEnable();
		}
	}

	public void go_only() {

		CoachingTab.DIS_MODE = DIS_FULL;
		
		court_view.hide_or_show = GameView.HIDE;

		this.remove_from_parent();

		court_view.setLayoutParams(go_only_params[INDEX_COURT]);
		//button_view.setLayoutParams(go_only_params[INDEX_BUTTON]);
		
		button_view_creator();
		
		actual_layout.removeAllViews();
		actual_layout.addView(game_layout_only);

	}
	
	public void animation_init(){
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int d = metrics.heightPixels;
		final int w = metrics.widthPixels;
		offset_tweak = w / 8f;
		offset = -(/*court_view.getHeight()*/d * 1.5f);
		hide = new TranslateAnimation(0, 0, 0, offset);
		show = new TranslateAnimation(0,0, offset, 0);
		
		
		hide.setDuration(100);
		hide.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				button_view.setVisibility(View.GONE);
				
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
//				button_view.setTranslationY(offset);
				
				button_view.setTranslationX(offset_tweak);
				button_view.disableAllButtons();
				button_view.setVisibility(View.VISIBLE);
							
				if (sw){
					sw = false;
				} else{
					button_category.startAnimation(show_ppg);
				}
				
				if (change_view){
					change_view = false;
					change_view();
				}
			}
		});
		hide.setFillBefore(true);
		
		show.setDuration(100);
		show.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				button_view.setTranslationY(0);
				button_view.setTranslationX(0);
				button_view.setVisibility(View.GONE);
				button_view.enableButtonsByCat(CoachingTab.curr_cat);
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				button_view.setVisibility(View.VISIBLE);
				BTN_vis =  true;
			}
		});
		show.setFillAfter(true);
		
		offset_g = /*court_view.getWidth()*/w * 0.68f;
		hide_g = new TranslateAnimation(0, offset_g, 0, 0);
		show_g = new TranslateAnimation(offset_g, 0, 0, 0);
		offset_pg = /*court_view.getWidth()*/w * 0.533f;
		hide_pg = new TranslateAnimation(0, offset_pg, 0, 0);
		show_pg = new TranslateAnimation(offset_pg, 0, 0, 0);
		offset_ppg = /*court_view.getWidth()*/w / 6;
		show_ppg = new TranslateAnimation(offset_ppg, 0, 0, 0);
		
		hide_g.setDuration(100);
		hide_g.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				CoachingTab.BTB_MODE = CoachingTab.BTB_HIDDEN;
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				
				
			}
		});
		hide_g.setFillBefore(true);
		
		show_g.setDuration(100);
		show_g.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				button_category.setTranslationX(0);
				CoachingTab.BTB_MODE = CoachingTab.BTB_SHOWN;
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub	
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}
		});
		show_g.setFillAfter(true);
		
		hide_pg.setDuration(100);
		hide_pg.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				CoachingTab.BTB_MODE = CoachingTab.BTB_PSHOWN;
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				button_category.setTranslationX(offset_pg);
			}
		});
		hide_pg.setFillBefore(true);
		
		show_pg.setDuration(100);
		show_pg.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				button_category.setTranslationX(0);
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				//button_category.setVisibility(LinearLayout.VISIBLE);
			}
		});
		show_ppg.setFillAfter(true);
		
		show_ppg.setDuration(100);
		show_ppg.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				button_category.setTranslationX(offset_pg);
			}
			
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}
		});
		show_ppg.setFillBefore(true);
		
	}
	
	public void init_button_categories(){
		
		LinearLayout.LayoutParams bparam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		bparam.weight = 0.2f;
		button_category.setWeightSum(1f);
		button_category.removeAllViews();
		
		Button b;
		
		while(button_categories.size() < NUM_CAT){
			button_categories.add(new Button(this));
			b = button_categories.get(button_categories.size() - 1);
			b.setTypeface(font);
			b.setTextColor(Color.rgb(215, 108, 34));
			b.setBackgroundColor(Color.TRANSPARENT);
//			std = new Button(this);
//			std.setTypeface(font);
//			std.setText("\uf009");
//			std.setTextColor(Color.rgb(215, 108, 34));
//			std.setBackgroundColor(Color.TRANSPARENT);
//			std.setOnClickListener(new OnClickListener(){
//	    		@Override
//	    		public void onClick(View v){
//	    			display.setText(std.getText());
//	    			button_category.startAnimation(hide_pg);
//	    			CoachingTab.curr_cat = CoachingTab.CAT_STD;
//	    		}
//			});
			
			switch (button_categories.size()){
				case 1:	b.setText("\uf0a8");
						b.setTextColor(Color.WHITE);
						b.setOnTouchListener(new OnTouchListener() {
							
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								
								float deltaX, deltaY;
								
								switch (event.getAction()){
								case MotionEvent.ACTION_DOWN:
									x = event.getX();
									y = event.getY();
									break;
								
								case MotionEvent.ACTION_UP:
									deltaX = event.getX() - x;
									deltaY = event.getY() - y;
									
									if (Math.abs(deltaY) > Math.abs(deltaX) && deltaY > 0){
										button_category.setTranslationX(offset_g);
										getButtonView().startAnimation(show);
										
									}else if (Math.abs(deltaY) < Math.abs(deltaX) && deltaX < 0){
										if (CoachingTab.BTB_MODE != CoachingTab.BTB_SHOWN){
											button_category.startAnimation(show_pg);
										}
									}
									break;
								}
								return false;
							}
						});
						break;
				
				case 2:	b.setText("\uf096");
						b.setOnClickListener(new OnClickListener(){
				    		@Override
				    		public void onClick(View v){
				    			button_categories.get(0).setText(button_categories.get(1).getText());
				    			button_category.startAnimation(hide_pg);
				    			CoachingTab.curr_cat = CoachingTab.CAT_STD;
				    		}
						});
						break;
				case 3: b.setText("\uf03d");
						b.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								button_categories.get(0).setText(button_categories.get(2).getText());
								button_category.startAnimation(hide_pg);
								CoachingTab.curr_cat = CoachingTab.CAT_PLAYBACK;
							}
						});
						break;
				
				case 4: b.setText("\uf044");
						b.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								button_categories.get(0).setText(button_categories.get(3).getText());
								button_category.startAnimation(hide_pg);
								CoachingTab.curr_cat = CoachingTab.CAT_EDIT;
							}
						});
						break;
						
				case 5: b.setText("\uf0c9");
						b.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								button_categories.get(0).setText(button_categories.get(4).getText());
								button_category.startAnimation(hide_pg);
								CoachingTab.curr_cat = CoachingTab.CAT_TOOLS;
							}
						});
						break;
			}
			button_category.addView(b, bparam);
		}
		
		button_category.setBackgroundResource(R.drawable.buttons_category);
//		
//		
//		
//		std = new Button(this);
//		std.setTypeface(font);
//		std.setText("\uf009");
//		std.setTextColor(Color.rgb(215, 108, 34));
//		std.setBackgroundColor(Color.TRANSPARENT);
//		std.setOnClickListener(new OnClickListener(){
//    		@Override
//    		public void onClick(View v){
//    			display.setText(std.getText());
//    			button_category.startAnimation(hide_pg);
//    			CoachingTab.curr_cat = CoachingTab.CAT_STD;
//    		}
//		});
//		
//		playback = new Button(this);
//		
//		
//		edit = new Button(this);
//		edit.setTypeface(font);
//		edit.setText("\uf040");
//		edit.setTextColor(Color.rgb(215, 108, 34));
//		edit.setBackgroundColor(Color.TRANSPARENT);
//		edit.setOnClickListener(new OnClickListener(){
//    		@Override
//    		public void onClick(View v){
//    			display.setText(edit.getText());
//    			button_category.startAnimation(hide_pg);
//    			CoachingTab.curr_cat = CoachingTab.CAT_EDIT;
//    		}
//		});
//		
//		display = new Button(this);
//		display.setTypeface(font);
//		display.setText("\uf0a8");
//		display.setTextColor(Color.WHITE);
//		display.setBackgroundColor(Color.TRANSPARENT);
//		display.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				
//				float deltaX, deltaY;
//				
//				switch (event.getAction()){
//				case MotionEvent.ACTION_DOWN:
//					x = event.getX();
//					y = event.getY();
//					break;
//				
//				case MotionEvent.ACTION_UP:
//					deltaX = event.getX() - x;
//					deltaY = event.getY() - y;
//					
//					if (Math.abs(deltaY) > Math.abs(deltaX) && deltaY > 0){
//						
//						
//						button_category.setTranslationX(offset_g);
//						getButtonView().startAnimation(show);
//						
//					}else if (Math.abs(deltaY) < Math.abs(deltaX) && deltaX < 0){
//						if (CoachingTab.BTB_MODE != CoachingTab.BTB_SHOWN){
//							button_category.startAnimation(show_pg);
//						}
//					}
//					
//					break;
//				}
//				return false;
//			}
//		});
	}
	
	public void button_view_creator(){
		/**********************
		 * WARNING: in this function we use the "screen"'s dimensions, as opposed to the xml's width and height
		 * we're doing this b/c the dimension of the views are dynamically created at weird times by Android
		 * however, this might cause trouble in the future when we add other layouts to the activity
		 * look for 'metrics' 
		 *  
		 * */
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int d = metrics.heightPixels;
		final int w = metrics.widthPixels;
		offset_tweak = w / 8f;
		offset = -(/*court_view.getHeight()*/d * 1.5f);
		
		offset_g = /*court_view.getWidth()*/w * 0.68f;

		offset_pg = /*court_view.getWidth()*/w * 0.533f;

		offset_ppg = /*court_view.getWidth()*/w / 6;

		//button_category.addView(edit);
		
		RelativeLayout.LayoutParams rparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rparam.width = (int) offset_tweak;
		rparam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); 
		RelativeLayout.LayoutParams lparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lparam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lparam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lparam.width = /*court_view.getWidth()*/w - (w / 3);
		
		game_layout_only.removeAllViews();
		game_layout.removeAllViews();

		if (CoachingTab.DIS_MODE == DIS_DEFAULT || CoachingTab.DIS_MODE == DIS_FULL){
			
			game_layout_only.addView(court_view);
			game_layout_only.addView(button_view, rparam);
			game_layout_only.addView(button_category, lparam);
			CoachingTab.curr_cat = CAT_STD;
		
		} else if (CoachingTab.DIS_MODE == DIS_EDIT){
			game_layout.addView(game_layout_main);
			game_layout.addView(button_view, rparam);
			game_layout.addView(button_category, lparam);
			CoachingTab.curr_cat = CAT_EDIT;
		}
		
		BTN_vis = false;
		button_category.setTranslationX(offset_pg);
		//button_view.setTranslationY(offset);
		button_view.setTranslationX(offset_tweak);
		//button_view.setVisibility(View.GONE);
		button_view.disableAllButtons();
	}

	public void go_full() {
		
		CoachingTab.DIS_MODE = DIS_EDIT;

		court_view.hide_or_show = GameView.SHOW;

		this.remove_from_parent();

		court_view.setLayoutParams(go_full_params[INDEX_COURT]);
		//button_view.setLayoutParams(go_full_params[INDEX_BUTTON]);
		edit_view.setLayoutParams(go_full_params[INDEX_EDIT]);
		
		game_layout_main.removeAllViews();
		
		game_layout_court_button_horiz.removeAllViews();
		game_layout_court_button_horiz.addView(court_view);
		game_layout_main.addView(game_layout_court_button_horiz);
		game_layout_main.addView(edit_view);
		
		button_view_creator();
		
		actual_layout.removeAllViews();
		//button_view.buttons.get(ButtonView.BTN_HALF_FULL).setName("\uf042", true);
		actual_layout.addView(game_layout);
	}
}
