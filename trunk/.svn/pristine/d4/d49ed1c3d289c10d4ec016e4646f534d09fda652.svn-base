package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
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
	static final int BTN_NEXT_STEP = 1;
	static final int BTN_REPLAY_ALL = 2;
	static final int BTN_REPLAY_STEP = 3;
	static final int BTN_PAUSE = 4;
	static final int BTN_NUM = 5;
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
	public List<Sprite> buttons = new ArrayList<Sprite>();
	public GameLoopThread thread;

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
				buttons.add(createButton((int) (total_height / (BTN_NUM + epsilon) ), font));
				buttons.get(i).init((float) (total_width / 2), (float) ((float) (i + 1) / (float) (BTN_NUM + 1) * total_height));
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
			buttons.get(i).setTeamColor(Color.WHITE);
		}
		activity.getGameView().init_buttons_position();
		buttons.get(BTN_RECORD).setName("\uf111", true);
		buttons.get(BTN_NEXT_STEP).setName("\uf051", true);
		buttons.get(BTN_NEXT_STEP).ButtonDisable();
		buttons.get(BTN_REPLAY_ALL).setName("\uf04a", true);
		buttons.get(BTN_REPLAY_ALL).ButtonDisable();
		buttons.get(BTN_REPLAY_STEP).setName("\uf048", true);
		buttons.get(BTN_REPLAY_STEP).ButtonDisable();
		buttons.get(BTN_PAUSE).setName("\uf04c", true);
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
				thread.gestureDetector.onTouchEvent(ev);
			}
		}
		return true;
	}
}


