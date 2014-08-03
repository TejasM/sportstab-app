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
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.view.GestureDetector;

public class GameLoopThread extends Thread {
	static final int MAX_NUM_PLAYERS = 20; /* this is 10 players with one setting button*/
	static final double PERCENT_COURT = 1;
	static final long FPS = 10;
	static final int[] team_colors = {Color.GREEN, Color.RED};
	private CoachingTab activity;
	private GameView view;
	private boolean running = false;
	public GestureDetector gestureDetector;
	public LearnGestureListener listener;

	public List<Sprite> sprites = new ArrayList<Sprite>();
	public Ball ball;
	public int whoHasBall;
	public int passingTo, passingFrom;
	static final int NoOne = -1;
	private boolean court_initialized;
	private boolean edit_initialized;
	private boolean button_initialized;
	private String[] playernames= {"SG", "PF", "PG", "C", "SF"};

	private int default_number_players = 6;

	public GameLoopThread (GameView view, Context context, CoachingTab activity){
		this.view = view;
		this.activity = activity;
		listener = new LearnGestureListener(activity);
		gestureDetector = new GestureDetector(context, listener);
		listener.setAccessThread(this);
		passingTo = NoOne;
		court_initialized = false;
		edit_initialized = false;
		button_initialized = false;

	}
	public boolean isGameInitialized(){
		return court_initialized;
	}
	public boolean isButtonInitialized(){
		return button_initialized;
	}
	public boolean isEditInitialized(){
		if (view.hide_or_show == GameView.HIDE) {
			return true;
		}
		return edit_initialized;
	}

	public int getNumPlayers(){
		return sprites.size();
	}

	public void setRunning(boolean run){
		running = run;
	}

	// Initialize the thread for all 3 views
	public void init_game(){
		Canvas c = null;
		try{
			c = view.getHolder().lockCanvas();
			synchronized(view.getHolder()){
				createBackground(c.getWidth(), c.getHeight());
				createBall();
				createSprites();
				ball.updateByPlayer(sprites.get(0).getX(),sprites.get(0).getY());
				sprites.get(0).setBall(true);
			}
			court_initialized = true;
		} finally {
			if (c != null){
				view.getHolder().unlockCanvasAndPost(c);
			}
		}
	}
	public void init_button(){
		Canvas c = null;
		try{
			c = view.button_view.getHolder().lockCanvas();
			synchronized(view.button_view.getHolder()){
				if ((view.button_view.buttons==null)||(view.button_view.buttons.size()==0))
				view.button_view.createButtons();
			}
			button_initialized = true;
		} finally {
			if (c != null){
				view.button_view.getHolder().unlockCanvasAndPost(c);
			}
		}
	}
	public void init_edit(){
		Canvas c = null;
		try{
			c = view.edit_view.getHolder().lockCanvas();
			synchronized(view.edit_view.getHolder()){
				view.edit_view.createEditBars(sprites.size());
				view.edit_view.createPointer();
			}
			edit_initialized = true;
		} finally {
			if (c != null){
				view.edit_view.getHolder().unlockCanvasAndPost(c);
			}
		}
	}

	@SuppressLint("WrongCall")
	@Override
	public void run(){
		long ticksPS = 1000/FPS;
		long startTime;
		long sleepTime;

		Canvas game_canvas = null;
		Canvas edit_canvas = null;
		Canvas button_canvas = null;
		
		synchronized(this.activity.pauseLock){
			while (true){
				if (running){
					
					game_canvas = null;
					edit_canvas = null;
					button_canvas = null;
					
					startTime = System.currentTimeMillis();
					try{
						game_canvas = view.getHolder().lockCanvas();
						// For edit canvas, first make sure it's on the screen
						if (view.hide_or_show != GameView.HIDE) {
							edit_canvas = view.edit_view.getHolder().lockCanvas();
						}
						button_canvas = view.button_view.getHolder().lockCanvas();
						
						synchronized(view.getHolder()){
							/*if no one has ball, then thread updates it*/
							if (whoHasBall == NoOne){
								boolean donePass = ball.updateByPass(sprites.get(passingTo));
								/*if (view.getRecorder().isOn()){
									view.getRecorder().onUpdatePass(passCompleteness());
								}*/
								if (donePass)
								{
									passComplete();
								}
							}
							if (game_canvas != null) {
								view.onDraw(game_canvas);
							} else {
								Thread.yield();
							}

							if (edit_canvas != null) {
								view.edit_view.onDraw(edit_canvas);
							} else {
								// Might not have an edit view on screen if go_only
								//Thread.yield();
							}

							if (button_canvas != null) {
								view.button_view.onDraw(button_canvas);
							} else {
								Thread.yield();
							}
						}
					} finally {
						if (game_canvas != null){
							view.getHolder().unlockCanvasAndPost(game_canvas);
						}
						if (edit_canvas != null){
							view.edit_view.getHolder().unlockCanvasAndPost(edit_canvas);
						}
						if (button_canvas != null){
							view.button_view.getHolder().unlockCanvasAndPost(button_canvas);
						}
					}
					/*sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
					try{
						if (sleepTime>0)
							sleep(sleepTime);
						else
							sleep(10);
					}catch (Exception e){}*/
				}/*else{
					activity.dummy_first = false;
					Thread.yield();
					//break;
				}*/
				activity.dummy_first = false;
				while(running ==false){
					try{
						activity.pauseLock.wait();
					}catch(InterruptedException e){
						
					}
				}
			}
		}
		/*if (!running){
				//activity.startSetting();
				activity.dummy_first = false;

				return;
			}*/

	}
	public float passCompleteness(){
		if (passingTo != NoOne){
			float retval = ball.passCompleteness(sprites.get(passingFrom), sprites.get(passingTo)); 
			return retval;
		}
		else{/* the pass is done already, so the completeness of the pass can be symbolized as greater than 1 */
			return (float) 1.1;
		}
	}
	
	public void set_game_vew(GameView v) {
		this.view = v;
	}

	private void createBackground(int width, int height){
		SavedSettings data = activity.getSavedSettings();
		if (data.savedBefore){
			view.background_index = data.backgroundIndex;
		}else{
			view.background_index = GameView.FULLCOURT;
		}
		view.c_height = height;
		view.c_width = width;
		view.createBackground(width, height);
	}

	public 	void passComplete(){
		if (passingTo != NoOne){
			sprites.get(passingTo).setBall(true);
			whoHasBall = passingTo;
			passingTo = NoOne;
			sprites.get(whoHasBall).setState(Sprite.FREE);
			/* for the case where ball hasn't reached the target, and target is removed */
			ball.setBeingPassed(false);
		}
	}
	public 	void setBall(int i){
		if (whoHasBall != NoOne)
		{
			if (whoHasBall < sprites.size()){ /* the other case is the case of selecting a play with fewer players */
				sprites.get(whoHasBall).setBall(false);
				passingFrom = whoHasBall;
			}
			whoHasBall = NoOne;
			passingTo = i;
			sprites.get(i).setState(Sprite.LOCK);
			if (view.getRecorder().isOn()){
				view.getRecorder().onPassEvent(passingTo, passingFrom);
			}
		}
	}
	public void setBallInMiddleOfPass(int i){
		passComplete();
		setBall(i);
	}
	private void createBall() {
		Bitmap bmp = BitmapFactory.decodeResource(view.getResources(), R.drawable.bball);
		Bitmap ret = Bitmap.createScaledBitmap(bmp, view.background[view.background_index].getWidth()/12, view.background[view.background_index].getWidth()/12, true);
		ball = new Ball(this.view, ret);
	}

	public void createSprites(){
		SavedSettings data = activity.getSavedSettings();
		if ((data.savedBefore) && (data.numPlayers > 0)){
			for (int i = 0; i < data.numPlayers; i++){
				sprites.add(createSprite(/*R.drawable.android_boi*/));
				sprites.get(i).initByCourt(data.playerX[i], data.playerY[i]);
				sprites.get(i).setPosByCourt(view.background_index);
				sprites.get(i).setName(data.playerNames[i], true);
				sprites.get(i).setTeamColor(data.playerColors[i]);
				sprites.get(i).resizePlayer(data.playerRPercent[i]);
			}
		}else{
			default_number_players = 10;
			for (int i = 0; i < default_number_players; i++){
				sprites.add(createSprite(/*R.drawable.android_boi*/));
				float x[] = new float[GameView.BACKGROUND_NUM];
				float y[] = new float[GameView.BACKGROUND_NUM];
				for (int j = 0; j < GameView.BACKGROUND_NUM; j++){
					if (i > 4) {
						switch(9 - i){
						case 0:
							x[j] = (float) (view.background[j].getWidth()*0.25);
							y[j] = (float) (view.background[j].getHeight()*(1.0/3.0));
							break;
						case 1:
							x[j] = (float) (view.background[j].getWidth()*0.4);
							y[j] = (float) (view.background[j].getHeight()*(0.5/3.0));
							break;
						case 2:
							x[j] = (float) (view.background[j].getWidth()*0.5);
							y[j] = (float) (view.background[j].getHeight()*(1.4/3.0));
							break;
						case 3:
							x[j] = (float) (view.background[j].getWidth()*0.6);
							y[j] = (float) (view.background[j].getHeight()*(0.7/3.0));
							break;
						case 4:
							x[j] = (float) (view.background[j].getWidth()*0.75);
							y[j] = (float) (view.background[j].getHeight()*(1.0/3.0));
							break;
						default:
							x[j] = (float) ((i%5)+1)*view.background[j].getWidth()/((default_number_players/2)+1);;
							y[j] = (float) (view.background[j].getHeight()*(1.0/3.0));
							break;
						}
					} else {
						switch(i){
						case 0:
							x[j] = (float) (view.background[j].getWidth()*0.25);
							y[j] = (float) (view.background[j].getHeight()*(2.0/3.0));
							break;
						case 1:
							x[j] = (float) (view.background[j].getWidth()*0.4);
							y[j] = (float) (view.background[j].getHeight()*(2.3/3.0));
							break;
						case 2:
							x[j] = (float) (view.background[j].getWidth()*0.5);
							y[j] = (float) (view.background[j].getHeight()*(1.6/3.0));
							break;
						case 3:
							x[j] = (float) (view.background[j].getWidth()*0.6);
							y[j] = (float) (view.background[j].getHeight()*(2.5/3.0));
							break;
						case 4:
							x[j] = (float) (view.background[j].getWidth()*0.75);
							y[j] = (float) (view.background[j].getHeight()*(2.0/3.0));
							break;
						default:
							x[j] = (float) ((i%5)+1)*view.background[j].getWidth()/((default_number_players/2)+1);;
							y[j] = (float) (view.background[j].getHeight()*(2.0/3.0));
							break;
						}
					}
				}
				sprites.get(i).initByCourt(x,y);
				sprites.get(i).setPosByCourt(view.background_index);
				
				if (i < (default_number_players/2)){
					sprites.get(i).setTeamColor(GameLoopThread.team_colors[0]);
				} else{
					sprites.get(i).setTeamColor(GameLoopThread.team_colors[1]);
				}
				sprites.get(i).setName(playernames[i%5], false);
			}
		}				
		view.initPlayerOrder();
	}

	public Sprite createSprite(/*int resource*/) {
		int bitmap_width = view.background[view.background_index].getWidth()/10;
		Bitmap bmp = BitmapFactory.decodeResource(view.getResources(), R.drawable.bball);
		Bitmap ret = Bitmap.createScaledBitmap(bmp, bitmap_width, bitmap_width, true);
		//Bitmap ret = Bitmap.createBitmap(bitmap_width, bitmap_width, Bitmap.Config.RGB_565);

		/* painting over the android boi...lol */
		Canvas canvas = new Canvas(ret);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawCircle((float)ret.getWidth()/2, (float)ret.getWidth()/2, (float)(ret.getWidth()/2 * 0.8), paint);		
		/*
		for (int x = 0; x < ret.getWidth(); x++){
			for (int y = 0; y < ret.getHeight(); y++){
				double dXSquared = Math.pow(x - ret.getWidth()/2, 2);
				double dYSquared = Math.pow(y - ret.getWidth()/2, 2);
				double dR = Math.sqrt(dYSquared + dXSquared);
				if ((ret.getWidth()/2 * 0.8 < dR) && (dR < ret.getWidth()/2)){
					ret.setPixel(x, y, Color.GREEN) ;
				}
			}
		}*/
		Sprite sprite = new Sprite(this.view, ret, activity);
		sprite.setTeamColor(GameLoopThread.team_colors[0]);
		sprite.initBall(ball);
		return sprite;
	}
	public void setSetting(boolean b) {
		// TODO Auto-generated method stub

	}

}
