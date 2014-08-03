package com.example.coachingtab;

import java.util.ArrayList;
import java.util.Hashtable;
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

public class ButtonView extends SurfaceView {
	static final int FULLCOURT = 0;
	static final int HALFCOURT = 1;
	static final int BACKGROUND_NUM = 2;
	static final int EDIT = 0;
	static final int FREEPLAY = 1;
	static final int BTN_RECORD = 0;
	//static final int BTN_HALF_FULL = 1;
	//	static final int BTN_NEXT_STEP = 1;
	static final int BTN_REPLAY_ALL = 1;
	//	static final int BTN_REPLAY_STEP = 3;
	static final int BTN_PAUSE = 2;
	static final int BTN_EDIT_MOVE = 3;
	static final int BTN_EDIT_BALL = 4;
	static final int BTN_EDIT_SCREEN = 5;
	static final int BTN_RESET_PLAYERS = 6;
	static final int BTN_UPDATE = 7;
	static final int BTN_TOG_TEAM = 8;
	static final int BTN_TOD_EDIT = 10;
	static final int BTN_PLAYBOOK = 11;
	static final int BTN_SHARE = 12;
	static final int BTN_SETTING = 13;
	static final int BTN_CHANGE_VIEW = 14;
	static final int BTN_LOGOUT = 15;
	static final int BTN_TOGGLE = 16;
	static final int BTN_NUM = 17;
	public int background_index;
	public int background_type = R.drawable.basketball_court_big;
	public int h_background_type = R.drawable.basketball_court_half;
	private static final String TAG = "ButtonView";
	public Bitmap background[] = new Bitmap[BACKGROUND_NUM];
	private SurfaceHolder holder;
	//Path   straight = new Path();
	Paint paint = new Paint();//now set in createbackground
	public CoachingTab activity;
	public float playerSizeModifier = (float) 1;
	private boolean surfaceCreated = false;
	public boolean aligned_on_height[] = new boolean[BACKGROUND_NUM];
	private int cnt = 0;
	private int mode = 0;
	public int c_width = 0;
	public int c_height = 0;
	public List<Sprite> buttons;
	public List<Sprite> all_buttons = new ArrayList<Sprite>();
	public Hashtable<Integer, Integer> buttons_ind = new Hashtable<Integer, Integer>();
	public GameLoopThread thread;
	public GestureDetector gestureDetector;
	public LearnGestureListener listener;

	public ButtonView(Context context) {
		super(context);
		init(context);
	}

	public ButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ButtonView(Context context, final CoachingTab activity) {
		super(context);
		init(context);
	}

	public ButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}


	public void init(Context context) {
		activity = (CoachingTab) context;
		mode = FREEPLAY;
		holder = getHolder();

		holder.addCallback(new Callback() {

			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				surfaceCreated = false;
			}

			public void surfaceCreated(SurfaceHolder holder) {
				if (activity.dummy_first == true) {
					if (!thread.isButtonInitialized()) {
						thread.init_button();
					}
					if (thread.isGameInitialized() && thread.isEditInitialized() && thread.isButtonInitialized()) {
						thread.setRunning(true);
						thread.start();
					}
				} else {
					if (activity.getEditView().isSurfaceCreated() && activity.getGameView().isSurfaceCreated()) {
						thread.init_button();
						thread.setRunning(true);
						Thread.yield();
					}
					//thread.start();
				}
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

	public void setMode(int m) {
		mode = m;
	}

	public void setGameLoopThread(GameLoopThread t) {
		thread = t;
	}
	public void setGestureDetector(){
		listener = new LearnGestureListener(activity, LearnGestureListener.FROM_BUTTON);
		gestureDetector = new GestureDetector(this.activity, listener);
		listener.setAccessThread(this.thread);
	}
	public int getMode() {
		return mode;
	}

	public boolean isSurfaceCreated() {
		return surfaceCreated;
	}

	@SuppressLint("WrongCall")
	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawColor(Color.rgb(239, 239, 235));
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		canvas.drawColor(Color.TRANSPARENT);
		for (Sprite btn : buttons) {
			btn.onDraw(canvas);
		}
	}

	public void createButtons() {
		int total_width = getWidth();
		int total_height = getHeight();
		float epsilon = (float)BTN_NUM/2; //WARNING: tweaking size for cosmetic reason
		Typeface font = Typeface.createFromAsset(activity.getBaseContext().getAssets(), "fonts/fontawesome.ttf");
		for (int i = 0; i < BTN_NUM; i++) {
			//if (activity.getGameView().aligned_on_height[activity.getGameView().background_index]) {
			if (i == BTN_EDIT_MOVE || i == BTN_EDIT_BALL || i == BTN_EDIT_SCREEN){
				all_buttons.add(createButtonAlt((int) (total_height / (BTN_NUM + epsilon)), i, font));
				all_buttons.get(i).init((float) (total_width / 2), (float) ((float) (i + 1) / (float) (BTN_NUM + 1) * total_height));
			} else if (i == BTN_TOGGLE){
				all_buttons.add(createButton((int) (total_height / (BTN_NUM + epsilon) ), font));
				all_buttons.get(i).init((float) (total_width / 4), (float) ((float) (1) / (float) (BTN_NUM + 1) * total_height));
			} else {
				all_buttons.add(createButton((int) (total_height / (BTN_NUM + epsilon) ), font));
				all_buttons.get(i).init((float) (total_width / 2), (float) ((float) (i + 1) / (float) (BTN_NUM + 1) * total_height));
			}
			/*} else {
				buttons.add(createButton((int) (total_width / (BTN_NUM + epsilon)), font));
				buttons.get(i).init((float) ((float) (i + 1) / (float) (BTN_NUM + 1) * total_width), (float) (total_height / 2));
			}*/

		}

		for (int i = 0; i < BTN_NUM; i++) {/*
			float x[] = new float[GameView.BACKGROUND_NUM];
			float y[] = new float[GameView.BACKGROUND_NUM];
			for (int j = 0; j < GameView.BACKGROUND_NUM;j++){
				if (activity.getGameView().aligned_on_height[j]){
					x[j] = (float) (total_width/2);
					y[j] = (float) ((float)(i+1)/(float)(BTN_NUM+1)*total_height);
				}else{
					x[j] = (float) ((float)(i+1)/(float)(BTN_NUM+1)*total_width);
					y[j] = (float) (total_height/2);
				}
			}
			buttons.get(i).initByCourt(x, y);
			buttons.get(i).setPosByCourt(background_index);
			buttons.get(i).initButtonMatrixByCourt();
			buttons.get(i).setMatrixByCourt(background_index);*/
			if (i == BTN_EDIT_MOVE || i == BTN_EDIT_BALL || i == BTN_EDIT_SCREEN){
			 	continue;
			}
			all_buttons.get(i).setTeamColor(Color.WHITE);
		}
		buttons = all_buttons;
		activity.getGameView().init_buttons_position();
		all_buttons.get(BTN_RECORD).setName("\uf111", true);
		//		buttons.get(BTN_NEXT_STEP).setName("\uf051", true);
		//		buttons.get(BTN_NEXT_STEP).ButtonDisable();
		all_buttons.get(BTN_REPLAY_ALL).setName("\uf04a", true);
		//		buttons.get(BTN_REPLAY_STEP).setName("\uf048", true);
		//		buttons.get(BTN_REPLAY_STEP).ButtonDisable();
		all_buttons.get(BTN_PAUSE).setName("\uf04c", true);
//		buttons.get(BTN_EDIT_MOVE).setName("Move", true);
//		buttons.get(BTN_EDIT_BALL).setName("Ball", true);
//		buttons.get(BTN_EDIT_SCREEN).setName("Screen", true);
		all_buttons.get(BTN_RESET_PLAYERS).setName("\uf021", true);
		all_buttons.get(BTN_UPDATE).setName("\uf0c7", true);
		all_buttons.get(BTN_TOG_TEAM).setName("TEAM", true);
		all_buttons.get(BTN_TOD_EDIT).setName("TOG", true);
		all_buttons.get(BTN_PLAYBOOK).setName("\uf02d", true);
		all_buttons.get(BTN_SETTING).setName("\uf013", true);
		all_buttons.get(BTN_SHARE).setName("\uf09e", true);
		all_buttons.get(BTN_CHANGE_VIEW).setName("\uf0db", true);
		all_buttons.get(BTN_LOGOUT).setName("\uf08b", true);
		all_buttons.get(BTN_TOGGLE).setName("\uf102", true);
		//enableButtonsByMode();
		disableAllButtons();
	}

	private Sprite createButtonAlt(int width, int number, Typeface font) {
		Bitmap ret = null;
		if (number==BTN_EDIT_MOVE){
			ret = BitmapFactory.decodeResource(activity.getResources(), R.drawable.sym_move_orage);
		} else if (number==BTN_EDIT_BALL) {
			ret = BitmapFactory.decodeResource(activity.getResources(), R.drawable.sym_ball);
		} else if (number==BTN_EDIT_SCREEN){
			ret = BitmapFactory.decodeResource(activity.getResources(), R.drawable.sym_block);
		} else{
			  return null;
		}
		ret = Bitmap.createScaledBitmap(ret, width, width, false);
		return new Sprite(this, ret, activity, font);
	}

	public void enableButtonsByMode(){
		if (this.activity.getGameView().getRecorder().isOn()){
			for (int i = 0; i<all_buttons.size();i++){
				if (i!=ButtonView.BTN_RECORD){
					all_buttons.get(i).ButtonDisable();
				}else{
					all_buttons.get(i).ButtonEnable();
				}
			}
		}else{
			if (this.activity.getGameView().getRecorder().getCurrPlay()==null){
				for (int i = 0; i<all_buttons.size();i++){
					if (i!=ButtonView.BTN_RECORD && i != ButtonView.BTN_RESET_PLAYERS && i != ButtonView.BTN_TOGGLE){
						all_buttons.get(i).ButtonDisable();
					}else{
						all_buttons.get(i).ButtonEnable();
					}
				}
			}else{
				for (int i = 0; i<all_buttons.size();i++){
					all_buttons.get(i).ButtonEnable();
				}
			}
		}
	}
	
	public void enableButtonsByCat(int cat){
		buttons = new ArrayList<Sprite>();
		
		if (cat == CoachingTab.CAT_STD || cat == CoachingTab.CAT_DEFAULT){
			buttons.add(all_buttons.get(BTN_TOG_TEAM));
			buttons.add(all_buttons.get(BTN_RESET_PLAYERS));
			buttons.add(all_buttons.get(BTN_TOGGLE));
			
		} else if (cat == CoachingTab.CAT_EDIT){
			buttons.add(all_buttons.get(BTN_EDIT_BALL));
			buttons.add(all_buttons.get(BTN_EDIT_MOVE));
			buttons.add(all_buttons.get(BTN_EDIT_SCREEN));
			buttons.add(all_buttons.get(BTN_UPDATE));
			buttons.add(all_buttons.get(BTN_TOD_EDIT));
			buttons.add(all_buttons.get(BTN_TOGGLE));
			
		} else if (cat == CoachingTab.CAT_PLAYBACK){
			buttons.add(all_buttons.get(BTN_RECORD));
			buttons.add(all_buttons.get(BTN_PAUSE));
			buttons.add(all_buttons.get(BTN_REPLAY_ALL));
			buttons.add(all_buttons.get(BTN_TOGGLE));
			
		} else if (cat == CoachingTab.CAT_TOOLS){
			buttons.add(all_buttons.get(BTN_PLAYBOOK));
			buttons.add(all_buttons.get(BTN_SETTING));
			buttons.add(all_buttons.get(BTN_SHARE));
			buttons.add(all_buttons.get(BTN_CHANGE_VIEW));
			buttons.add(all_buttons.get(BTN_LOGOUT));
			buttons.add(all_buttons.get(BTN_TOGGLE));
			
		}
		
		for (Sprite s: buttons){
			s.ButtonEnable();
		}
		
		activity.getGameView().init_buttons_position();
	}
	
	public void disableAllButtons(){
		for (int i = 0; i<all_buttons.size();i++){
			all_buttons.get(i).ButtonDisable();
		}
	}
	
	private Sprite createButton(int width/*int resource*/, Typeface font) {
		//Bitmap bmp = BitmapFactory.decodeResource(view.getResources(), R.drawable.android_boi);
		//Bitmap ret = Bitmap.createScaledBitmap(bmp, view.background[view.background_index].getWidth()/10, view.background[view.background_index].getWidth()/10, true);
		//int bitmap_width = view.background[view.background_index].getWidth()/10;
		int bitmap_width = width;
		Bitmap ret = Bitmap.createBitmap(bitmap_width, bitmap_width, Bitmap.Config.ARGB_8888);
		//ret.eraseColor(Color.argb(30, 30, 30, 30));
		/*for (int x = 0; x < ret.getWidth(); x++) {
			for (int y = 0; y < ret.getHeight(); y++) {
				int color = ret.getPixel(x, y);
				int red = Color.red(color);
				int blue = Color.blue(color);
				int green = Color.green(color);
				int alpha = Color.alpha(color);
				//color = Color.argb(alpha/2, red, green, blue);
				ret.setPixel(x, y, Color.rgb(239, 239, 235));
			}
		}*/
		return new Sprite(this, ret, activity, font);

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		synchronized (getHolder()) {
			if (ev != null) {
				this.gestureDetector.onTouchEvent(ev);
			}
		}
		return true;
	}
}

