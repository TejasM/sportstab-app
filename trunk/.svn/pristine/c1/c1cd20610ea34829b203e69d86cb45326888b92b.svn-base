package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

public class EditView extends SurfaceView {
	static final int FULLCOURT = 0;
	static final int HALFCOURT = 1;
	static final int BACKGROUND_NUM = 2;
	static final int EDITBAR_SIZE_FACTOR = 25;
	public EditBars editbars;
	public Sprite pointer;
	static final int EDIT = 0;
	static final int FREEPLAY=1;
	public int background_index;
	public int background_type = R.drawable.basketball_court_big;
	public int h_background_type = R.drawable.basketball_court_half;
	private static final String TAG = "EditView";
	public Bitmap background[] = new Bitmap[BACKGROUND_NUM];
	private SurfaceHolder holder;
	private List<Integer> playerOrder = new ArrayList<Integer>();
	private List<ScreenPoint> points = new ArrayList<ScreenPoint>();
	//Path   straight = new Path();
	Paint  paint = new Paint();//now set in createbackground
	public CoachingTab activity;
	public float playerSizeModifier = (float) 1;
	private boolean surfaceCreated = false;
	private int cnt = 0;
	private int mode = 0;
	public int c_width = 0;
	public int c_height = 0;
	public GameLoopThread thread;
	private List<Bitmap> player_icons;

	private int editbarselected = -1;
	private int editbarHeightFactor;

	public EditView(Context context) {
		super(context);
		init(context);
	}

	public EditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EditView(Context context, final CoachingTab activity) {
		super(context);
		init(context);
	}

	public EditView(Context context, AttributeSet attrs, int defStyle) {
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
				if (activity.dummy_first == true){
					if (!thread.isEditInitialized()){
						thread.init_edit();
					}
					if (thread.isGameInitialized() && thread.isEditInitialized() && thread.isButtonInitialized()) {
						thread.setRunning(true);
						thread.start();
					}
				}else{
					//if (activity.getButtonView().isSurfaceCreated() &&
					//		activity.getGameView().isSurfaceCreated())
					//{
					// Don't do these checks for edit view because the others
					// may have already been created and destroyed by the time
					// we want to go_full or go_half and display edit_view
						thread.init_edit();
						thread.setRunning(true);
						Thread.yield();
						//}
					//thread.start();
				}
				surfaceCreated = true;
				
				if (activity.getGameView().getRecorder().getCurrPlay() != null) {
					editbars.render(activity.getGameView().getRecorder().getCurrPlay());
				}
				
			}

			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				// TODO Auto-generated method stub

			}
		});

	}
	public void setMode(int m){
		mode = m;
	}
	public int getMode(){
		return mode;
	}
	public boolean isSurfaceCreated(){
		if (activity.getGameView().hide_or_show == GameView.HIDE) {
			return true;
		}
		return surfaceCreated;
	}
	public void setGameLoopThread(GameLoopThread t){
		thread = t;
	}

	@SuppressLint("WrongCall")
	@Override
	protected void onDraw (Canvas canvas){
		canvas.drawColor(Color.GREEN);
		//draw editbars
		for (EditBar edtb: editbars.getList()){
			edtb.onDraw(canvas);
		}
		for (int i = 0; i < this.player_icons.size(); i++){
			Bitmap b = this.player_icons.get(i);
			canvas.drawBitmap(b, 0, getHeight() - (i+1)*editbars.get(i).getHeight(), null);
		}

		if (this.activity.getGameView().getRecorder().getCurrPlay() != null) {
			this.pointer.onDraw(canvas);
		}
	}

	public void createEditBars(int num_players){
		if (editbars==null){
			editbars = new EditBars(this, activity);
			this.editbarHeightFactor=11;
			for (int i = 0; i < num_players; i++){
				editbars.add(createEditBar());
				editbars.get(i).setXY(0, getHeight() - (i+1)*editbars.get(i).getHeight());
			}
			this.createPlayerIcons();
		}
		GestureRecorder g =this.activity.getGameView().getRecorder(); 
		editbars.render(g.getCurrPlay());
		//g.loadPlay(g.getCurrPlay().getID());
		
	}

	public void createPlayerIcons(){
		this.player_icons = new ArrayList<Bitmap>();
		for (Sprite s: this.activity.thread.sprites){
			Bitmap b = s.getBitmap();
			int size = this.getHeight()/this.editbarHeightFactor;
			Bitmap icon = Bitmap.createScaledBitmap(b, size, size, false);
			this.player_icons.add(icon);
		}
	}
	private EditBar createEditBar() {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.idle);
		Bitmap idle = Bitmap.createScaledBitmap(bmp, getWidth(), getHeight()/this.editbarHeightFactor, true);
		Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.move1);
		Bitmap move1 = Bitmap.createScaledBitmap(bmp1, getWidth(), getHeight()/this.editbarHeightFactor, true);
		Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.ball_idle);
		Bitmap ball_idle = Bitmap.createScaledBitmap(bmp2, getWidth(), getHeight()/this.editbarHeightFactor, true);
		Bitmap bmp3 = BitmapFactory.decodeResource(getResources(), R.drawable.black);
		Bitmap black = Bitmap.createScaledBitmap(bmp3, getWidth(), getHeight()/this.editbarHeightFactor, true);
		Bitmap bmp4 = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		Bitmap ball_move = Bitmap.createScaledBitmap(bmp4, getWidth(), getHeight()/this.editbarHeightFactor, true);
		return new EditBar(this, activity, idle, move1, ball_idle, black, ball_move);

	}
	public void createPointer() {
		int bitmap_width = getHeight()/this.editbarHeightFactor;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
		int height = (int)((float)bmp.getHeight()/(float)bmp.getWidth()*(float)bitmap_width);
		Bitmap ret = Bitmap.createScaledBitmap(bmp, bitmap_width, height, true);
		pointer = new Sprite(this, ret, activity);
		pointer.simpleUpdate(0, bitmap_width/2);
	}
	@SuppressLint("WrongCall")
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		//if (!recorder.replay){
		synchronized (getHolder()){
			thread.gestureDetector.onTouchEvent(ev);
			final int historySize = ev.getHistorySize();
			int playerNum;
			for (int h = 0; h < historySize; h++) {
				//moving edit pointer
				if (pointer.isTouched(ev.getX(),ev.getY())){
					updatePointer(ev.getX(), pointer.getY());
				}
			}
			//moving edit pointer
			if (pointer.isTouched(ev.getX(),ev.getY())){
				updatePointer(ev.getX(), pointer.getY());
			}
			if (this.activity.getGameView().getRecorder().getCurrPlay()!=null){
				if (this.editbarselected!=-1){
					if ((ev.getActionMasked() == MotionEvent.ACTION_UP)||(ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP)){

						editbars.setSelectedBarIndex(this.editbarselected);
						editbars.finishMoving();
						this.editbarselected=-1;
					}
					else{
						editbars.setSelectedBarIndex(this.editbarselected);
						editbars.update(ev.getX());
					}

				}else{
					int tmp = this.editbars.isTouched(ev.getX(), ev.getY());
					if ((tmp!=-1)){

						if ((ev.getActionMasked() == MotionEvent.ACTION_DOWN)||(ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)){
							editbars.update(ev.getX());
							this.editbarselected = tmp;
						}
					}
				}
			}
		}
		return true;
	}
	public void updatePointer(float x, float y){
		pointer.update(x,y);
		float percent = x/(float)this.getWidth();
		activity.getGameView().getRecorder().scrollPlay(percent);
		/*check if we need to replay or undo any event */
		activity.getGameView().getRecorder().checkEvent();
	}
}
