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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class GameView extends SurfaceView {
	static final int FULLCOURT = 0;
	static final int HALFCOURT = 1;
	static final int BACKGROUND_NUM = 2;
	static final int HIDE = 0;
	static final int SHOW = 1;
	static final int EDIT = 0;
	static final int FREEPLAY=1;
	public int background_index;
	public int hide_or_show;
	public int background_type = R.drawable.court_raptors;
	public int h_background_type = R.drawable.basketball_court_half;
	private static final String TAG = "GameView";
	public Bitmap background[] = new Bitmap[BACKGROUND_NUM];
	private SurfaceHolder holder;
	public GameLoopThread thread;
	private int pointerID[] = new int[GameLoopThread.MAX_NUM_PLAYERS];
	private int b_pointerID[] = new int[GameLoopThread.MAX_NUM_PLAYERS];
	private List<Integer> playerOrder = new ArrayList<Integer>();
	private List<ScreenPoint> points = new ArrayList<ScreenPoint>();
	//Path   straight = new Path();
	Paint  paint = new Paint();//now set in createbackground
	public CoachingTab activity;
	public float playerSizeModifier = (float) 1;
	private boolean surfaceCreated = false;
	private GestureRecorder recorder;
	public boolean aligned_on_height[] = new boolean[BACKGROUND_NUM];
	private int cnt = 0;
	private int mode = 0;
	public int c_width = 0;
	public int c_height = 0;
	EditView edit_view;
	ButtonView button_view;
	
	public GameView(Context context) {
		super(context);
		init(context);
	}
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
		
	}
	
	public GameView(Context context, final CoachingTab activity) {
		super(context);
		init(context);
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void set_other_views (ButtonView b, EditView e) {
		edit_view = e;
		button_view = b;
	}
	
	public void init(Context context) {
		activity = (CoachingTab) context;
		hide_or_show = HIDE;
		mode = FREEPLAY;
		thread = activity.thread;
		if (this.activity.dummy_first==true){
			thread.set_game_vew(this);
		}
		holder = getHolder();
		recorder = new GestureRecorder(this);
		/* initialize pointerID to -1, supposedly invalid pointerID */
		for (int i = 0; i < GameLoopThread.MAX_NUM_PLAYERS; i++){
			pointerID[i] = -1;
			b_pointerID[i] = -1;
		}

		holder.addCallback(new Callback() {

			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				surfaceCreated = false;
			}

			public void surfaceCreated(SurfaceHolder holder) {
				if (activity.dummy_first == true){
					if (!thread.isGameInitialized()){
						thread.init_game();
					}
					if (thread.isGameInitialized() && thread.isEditInitialized() && thread.isButtonInitialized()) {
						thread.setRunning(true);
						thread.start();
					}
				}else{
					if (activity.getButtonView().isSurfaceCreated() &&
						activity.getEditView().isSurfaceCreated())
					{
						thread.init_game();
						thread.setRunning(true);
						Thread.yield();
					}
					//thread.start();
				}
				surfaceCreated = true;
			}

			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				// TODO Auto-generated method stub
				GestureRecorder g = activity.getGameView().recorder;
				if ((g.getCurrPlay()!=null)&&(g.getCurrPlay().getID()!=null)){
					g.loadPlay(g.getCurrPlay().getID());
				}
				System.out.println("SURFACE CHANGED");
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
		return surfaceCreated;
	}

	public void createBackground(int width, int height){
		double scale = GameLoopThread.PERCENT_COURT;
		Bitmap unscaledBackground = BitmapFactory.decodeResource(getResources(), background_type);
		float r;
		// ERROR: This is always -1/-1
		if ((float) unscaledBackground.getHeight()/(float) unscaledBackground.getWidth() > (float) (height*scale)/(float) width){
			r = (float) (height*scale)/(float) unscaledBackground.getHeight();
			aligned_on_height[FULLCOURT] = true;

		}else{
			r = (float) width/(float) unscaledBackground.getWidth();
			aligned_on_height[FULLCOURT] = false;
		}
		background[FULLCOURT] = Bitmap.createScaledBitmap(unscaledBackground,(int) (r * unscaledBackground.getWidth()), (int) (r * unscaledBackground.getHeight()), true);
		/* for half court */
		unscaledBackground = BitmapFactory.decodeResource(getResources(), h_background_type);
		if ((float) unscaledBackground.getHeight()/(float) unscaledBackground.getWidth() > (float) (height*scale)/(float) width){
			r = (float) (height*scale)/(float) unscaledBackground.getHeight();
			aligned_on_height[HALFCOURT] = true;

		}else{
			r = (float) width/(float) unscaledBackground.getWidth();
			aligned_on_height[HALFCOURT] = false;
		}
		background[HALFCOURT] = Bitmap.createScaledBitmap(unscaledBackground,(int) (r * unscaledBackground.getWidth()), (int) (r * unscaledBackground.getHeight()), true);
	}

	public void initPlayerOrder(){
		while(playerOrder.size() > 0){
			playerOrder.remove(0);
		}
		for (int i = 0; i < thread.getNumPlayers(); i++){
			playerOrder.add(i);
		}

	}


	@SuppressLint("WrongCall")
	@Override
	protected void onDraw (Canvas canvas){
		//canvas.drawColor(Color.rgb(239, 239, 235));
		//canvas.drawColor(Color.rgb(239, 239, 235));
		//draw background
		if (background[background_index]!=null)
		canvas.drawBitmap(background[background_index], (float)(0.0),(float) (0.0), null);

		if (recorder.replay){
			recorder.replay();
		}

		if (recorder.replay) {
			if (!activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getName().equals("\uf04c")) {
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getBitmap().eraseColor(android.graphics.Color.TRANSPARENT);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).setName("\uf04c", true);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).paintName();
			}
		} else {
			if (!activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getName().equals("\uf04b")) {
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).getBitmap().eraseColor(android.graphics.Color.TRANSPARENT);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).setName("\uf04b", true);
				activity.getButtonView().buttons.get(ButtonView.BTN_PAUSE).paintName();
			}
		}
		/*
		//draw paths associated with players
		thread.ball.onDrawPath(canvas);
		//draw paths associated with players
		if (mode == EDIT){
			for (Sprite sprite : thread.sprites){
				sprite.onDrawPath(canvas, false);
			}
		}else{
			for (Sprite sprite : thread.sprites){
				sprite.onDrawPath(canvas, true);
			}
		}
		*/
		recorder.onDraw(canvas);
		boolean hasBallBeenDrawn = false;
		//draw players
		for (int j = playerOrder.size() - 1; j >= 0; j-- ){
			int i = playerOrder.get(j);
			thread.sprites.get(i).onDraw(canvas);
			if (thread.sprites.get(i).getHasBall()){
				thread.ball.onDraw(canvas);
				hasBallBeenDrawn = true;
			}
		}
		//draw ball
		if (!hasBallBeenDrawn){
			thread.ball.onDraw(canvas);
		}
		if (recorder.isOn()){
			recorder.recordOneSnapshot(this);
		}
	}

	boolean TouchEventHandlerByID(MotionEvent event, int player, int id, float x, float y) {		
		/* look for players that were already moving and update them with their respective pointers */
		if (player < GameLoopThread.MAX_NUM_PLAYERS){
			Sprite sprite = thread.sprites.get(player);
			if(sprite.isMoving()){	
				if ((event.getActionIndex()== event.findPointerIndex(id))
						&& ((event.getActionMasked() == MotionEvent.ACTION_UP)||(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)))
				{
					thread.sprites.get(player).TouchUpCback();
					pointerID[player] = -1;
				}
				else
				{
					positionSprite(sprite, x, y);
				}
			}
			else
				Log.d(TAG, "FUCK SOMETHING IS WRONG");
			return true;
		}
		/* use the remaining pointers to see if any non-moving players were touched */
		for (int i = thread.sprites.size() - 1; i>=0;i--){
			Sprite sprite = thread.sprites.get(i);
			if(sprite.isTouched(x, y)){
				if ((event.getActionIndex()== event.findPointerIndex(id))
						&& ((event.getActionMasked() == MotionEvent.ACTION_DOWN)||(event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN))
						&& (sprite.getState() == Sprite.FREE))
				{
					positionSprite(sprite, x, y);
					pointerID[i] = id;
					playerOrder.remove((Object) i);
					playerOrder.add(0, i);
					return true;
				}
				else{/* the player is touched, but not a well-handled event.  eg. when a player in block state but it's touched */
					if (sprite.getState() == Sprite.BLOCK){
						return true;	
					}
				}
			}
		}


		/* the empty spaces */
		int j;
		for (j = 0; j < points.size(); j++)
		{
			if (points.get(j).getID() == id)
			{/* this is already in the book */
				Log.d(TAG, "HI  "+id);
				int counter = 0;
				/*while((event.getActionIndex() != event.findPointerIndex(id)) && (counter < 100)){
					Log.d(TAG, "asdfasdf FUCKING SHIT asdfasdfsadf");
					event.getActionMasked();
					counter++;
				}*/
				if (/* (event.findPointerIndex(id) == event.getActionIndex())
						&& */(event.getActionMasked() == MotionEvent.ACTION_MOVE)) /* see if it passes through any player */
				{
					for (int i = thread.sprites.size() - 1; i>=0;i--)
					{
						Sprite sprite = thread.sprites.get(i);
						if(sprite.isTouched(x, y)){
							Log.d(TAG, "Touched"+id);
							points.get(j).addIndex(i);
							break;
						}
					}
				} 
				else if ((event.getActionIndex()== event.findPointerIndex(id))
						&& ((event.getActionMasked() == MotionEvent.ACTION_UP)||(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)))
				{
					for (int i = 0; points.get(j).spriteIndex[i] != -1; i++)
					{
						points.get(j).setUp(new MyPointF(x,y));
						thread.sprites.get(points.get(j).spriteIndex[i]).onScreen(points.get(j).getDown(), points.get(j).getUp());
						if (recorder.isOn()){
							recorder.onScreenEvent(points.get(j).spriteIndex[i], points.get(j).getDown(), points.get(j).getUp());
						}
					}
					points.remove(j);
					return true;
				}else{
					Log.d(TAG, "Unhandled Bitch" + counter);
				}
			}else{
				Log.d(TAG,"ALT HI" + id);
			}
		}
		if (j == points.size())
		{
			if ((event.getActionIndex()== event.findPointerIndex(id)) /* a new empty down */
					&& ((event.getActionMasked() == MotionEvent.ACTION_DOWN)||(event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)))
			{
				points.add(new ScreenPoint());
				points.get(j).setDown(new MyPointF(x,y));
				points.get(j).setID(id);
				Log.d(TAG, "pointer DOWN"+id);
				return true;
			}
		}

		/* END of the empty spaces */
		return true;
	}
	/***********
	 * @param ID
	 * @return the player index corresponding to the ID, or GameLoopThread.MAX_NUM_PLAYERS if no player is
	 * 		   associated with the ID.
	 */
	private int IsIDUsed(int ID){
		for (int i = 0; i < GameLoopThread.MAX_NUM_PLAYERS; i++){
			if (pointerID[i] == ID){
				return i;
			}
		}
		/*
		for (int i = 0; i < GameLoopThread.MAX_NUM_PLAYERS; i++){
			if (b_pointerID[i] == ID){
				return i + GameLoopThread.MAX_NUM_PLAYERS;
			}
		}*/
		return GameLoopThread.MAX_NUM_PLAYERS * 2;
	}
	@SuppressLint("WrongCall")
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		//if (!recorder.replay){
		synchronized (getHolder()){
			thread.gestureDetector.onTouchEvent(ev);
			final int historySize = ev.getHistorySize();
			final int pointerCount = ev.getPointerCount();
			int playerNum;
			for (int h = 0; h < historySize; h++) {
				for (int p = 0; p < pointerCount; p++) {
					playerNum = IsIDUsed(ev.getPointerId(p));
					TouchEventHandlerByID(ev, playerNum, ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
				}
				//record da players
				if (recorder.isOn()){
					recorder.recordOneSnapshot(this);
				}
				//Canvas c = this.getHolder().lockCanvas();
				//this.draw(c);
				//this.getHolder().unlockCanvasAndPost(c);
			}
			for (int p = 0; p < pointerCount; p++) {
				playerNum = IsIDUsed(ev.getPointerId(p));
				TouchEventHandlerByID(ev, playerNum, ev.getPointerId(p), ev.getX(p), ev.getY(p));
			}
			if (recorder.isOn()){
				recorder.recordOneSnapshot(this);
			}
		}
		return true;
	}
	public void updatePointer(float x, float y){
		edit_view.pointer.update(x,y);
		float percent = x/(float)this.getWidth();
		recorder.scrollPlay(percent);
		/*check if we need to replay or undo any event */
		recorder.checkEvent();
	}
	/***************************************************
	 * 
	 * @param sprite
	 * @param dX
	 * @param dY
	 * 1. update the position
	 * 2. see if there's collision, and then replace it so sprites don't overlap
	 */
	public void positionSprite(Sprite sprite, float dX, float dY){
		if (sprite.getState() == Sprite.FREE){
			sprite.update(dX,dY);//first update, then check if this new position overlaps with other sprite
			if (!recorder.replay){
				int count = 0;
				boolean second_time = false;
				Sprite sprite1, sprite2;
				int j, k;
				sprite1 = thread.sprites.get(0);//this line is just here so it compiles
				while(count<thread.sprites.size()){
					if (second_time==false){
						for (j = thread.sprites.size() -1 ; j>=0; j--){
							sprite1 = thread.sprites.get(j);
							if (sprite!=sprite1){
								if (this.doSpritesOverlap(sprite,sprite1)){
									sprite.positionBasedOnArg(sprite1);
									break;
								}
							}
							count++;
						}
						second_time = true;
					}
					else{
						for (k = thread.sprites.size() -1 ; k>=0; k--){
							sprite2 = thread.sprites.get(k);
							if ((sprite!=sprite2)&&(sprite1!=sprite2)){
								if (this.doSpritesOverlap(sprite,sprite2)){
									sprite.setStuck(true);
									sprite.positionBasedOnArg(sprite1, sprite2);
									break;
								}
							}
							count++;
						}
						break;
					}
				}
			}
			sprite.updatePath();
			if (sprite.getHasBall()){
				sprite.updateBall();
			}
		}
		else if (sprite.getState() == Sprite.BLOCK){

		}
	}

	public boolean doSpritesOverlap (Sprite s1, Sprite s2){
		/* first we assume s1 can never be in BLOCK state...cause it shouldn't be moved */
		float dX = Math.abs(s1.getX() - s2.getX());
		float dY = Math.abs(s1.getY() - s2.getY());

		if (dX*dX + dY*dY >= (float)(Math.pow((s1.getR() + s2.getR()) * playerSizeModifier , 2)))
			return false;
		else
			return true;


	}
	public GestureRecorder getRecorder() {
		return recorder;
	}
	public void toggleHalfFull() {
		if (thread.passingTo == GameLoopThread.NoOne){
			if(background_index == FULLCOURT){
				background_index = HALFCOURT;
				activity.go_half();
			}else{
				background_index = FULLCOURT;
				activity.go_full();
			}
			initByOrientation();
		}
	}
	// Hide/Show editbars
	public void toggleHideShow() {
		float scaleX, scaleY;
		if (hide_or_show == SHOW) {
			hide_or_show = HIDE;
			scaleX = (float)this.activity.getGameLayout().getWidth()/(float)this.getWidth();
			scaleY = (float)this.activity.getGameLayout().getHeight()/(float)this.getHeight();
			activity.go_only();
			//activity.getButtonView().buttons.get(ButtonView.BTN_HALF_FULL).ButtonDisable();
		} else {
			hide_or_show = SHOW;
			if(background_index == HALFCOURT){
				activity.go_half();
			}else{
				activity.go_full();
			}
			scaleX = (float)0.75;//WARNING: magic number hack...
			scaleY = (float)0.75;
			//activity.getButtonView().buttons.get(ButtonView.BTN_HALF_FULL).ButtonEnable();
		}
		scale(scaleX, scaleY);
		//initByOrientation(); //we'll just not go b/w full and half now..
	}
	public void scale(float scaleX, float scaleY){
		Bitmap bmp = this.background[this.background_index];
		this.background[this.background_index] = Bitmap.createScaledBitmap(bmp,(int) (bmp.getWidth()*scaleX), (int) (bmp.getHeight()*scaleY), true);
		for (Sprite s: this.activity.thread.sprites){
			s.scale(scaleX, scaleY, scaleX);
		}
		this.activity.thread.ball.scale(scaleX, scaleY, scaleX);
	}
	public void setHalfFull(int index){
		if (background_index != index){
			toggleHalfFull();
		}
	}
	// Hide/Show editbars
	public void setHideShow(int index){
		if (hide_or_show != index){
			toggleHideShow();
		}
	}
	public void initByOrientation(){
		//thread.initButtonsByBackground();
		thread.ball.resizeByCourt();
		for (Sprite s: thread.sprites){
			s.updateByCourtOrientation(background_index);
		}
		this.init_buttons_position();
		/*for (Sprite s: button_view.buttons){
			s.updateByCourtOrientation(background_index);
		}*/
	}
	public void init_buttons_position(){
		System.out.println("HI I'M HERE");
		int h = button_view.getHeight();
		int w = button_view.getWidth();
		int n = button_view.buttons.size();
		float epsilon;
		for (int i = 0; i<n;i++){
			Sprite s = button_view.buttons.get(i);
			float offset = s.getR();
			epsilon = offset; //WARNING: tweaking positions for cosmetic reason
			float x,y;
			if (h>w){
				x = 0;
				y = (float)i/(float)(n+1)*button_view.getHeight();
			}else{
				x = (float)i/(float)(n+1)*button_view.getWidth();
				y = 0;
			}
			s.init(x+offset, y+offset +(float) 1.5*epsilon);
			s.initButtonMatrix();
			//s.setPosBackup(x+offset,y+offset,this.background_index);
			//s.initButtonMatrixByCourt();
			//s.setMatrixByCourt(this.background_index);
			//s.updateByCourtOrientation(this.background_index);
		}
	}
	public void setOverlap(float tempOverlap) {
		playerSizeModifier = 1 - tempOverlap/100;

	}
	public int getplayerSizeModifier(){
		return (int)((1 - playerSizeModifier) * 100);
	}
	public int getCourtType(){
		if (background_type == R.drawable.court_tiled){
			return 1;
		} else {
			return 0;
		}
	}
	public void setBackgroundType(int pos){
		if (pos == 0) {
			background_type = R.drawable.basketball_court_big;
			h_background_type = R.drawable.basketball_court_half;
		} else{
			background_type = R.drawable.court_tiled;
			h_background_type = R.drawable.h_court_tiled;
		}
	}
}
