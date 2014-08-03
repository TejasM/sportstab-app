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
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

public class EditView extends SurfaceView {
	static final int TOGGLE_BOTH = 0;
	static final int TOGGLE_ONE = 1;
	static final int TOGGLE_TWO = 2;
	static final int TOGGLE_NUM = 3;
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
	private int toggle_state = 0;
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
		//canvas.drawColor(Color.GREEN);
		//draw editbars
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		for (EditBar edtb: editbars.getList()){
			if (edtb != null){
				edtb.onDraw(canvas);
			}
		}

		if (this.activity.getGameView().getRecorder().getCurrPlay() != null) {
			this.pointer.onDraw(canvas);
		}
	}
	public void setEditbarBitmaps(EditBar e){
		//set 2 sets (different size) of bitmaps for editbar e 
		List<Bitmap> bmps;
		this.editbarHeightFactor = 11;
		bmps = this.scaleEditBarBitmaps(e);
		e.setBitmapsSmall(bmps);
		List<Bitmap> big;
		this.editbarHeightFactor = 6;
		big = this.scaleEditBarBitmaps(e);
		e.setBitmapsBig(big);
	}
	public void createEditBars(int num_players){
		if (editbars==null){
			editbars = new EditBars(this, activity);
			this.editbarHeightFactor=11;
			//WARNING: if the order of the sprites are mixed b/w teams, we're displaying the first 5 & last 5, which could make no sense, To fix this: check for teams and organize by team
			for (int i = 0; i < num_players; i++){
				editbars.add(createEditBar());
				this.setEditbarBitmaps(editbars.get(i));
				editbars.get(i).setBitmaps(EditBar.BMP_SMALL);
				editbars.get(i).setXY(0, getHeight() - (i+1)*editbars.get(i).getHeight());
			}
			this.createPlayerIcons();
		}
		GestureRecorder g =this.activity.getGameView().getRecorder(); 
		editbars.render(g.getCurrPlay());
		//g.loadPlay(g.getCurrPlay().getID());

	}
	public void toggleEditBars(){
		synchronized(this.activity.getGameView().getHolder()){
			this.toggle_state = (this.toggle_state+1)%TOGGLE_NUM;
			switch (this.toggle_state){
			case TOGGLE_ONE:
				this.editbarHeightFactor = 6;
				this.toggleEditBarBitmaps(EditBar.BMP_BIG);
				for (int i = 0; i < 5; i++){
					this.editbars.get(i).setXY(0, getHeight() - (i+1)*editbars.get(i).getHeight());
				}
				for (int i = 5; i < this.editbars.size(); i++){
					this.editbars.get(i).setXY(0, getHeight() + 100);
				}
				break;
			case TOGGLE_TWO:
				this.editbarHeightFactor = 6;
				this.toggleEditBarBitmaps(EditBar.BMP_BIG);
				for (int j = 5; j < this.editbars.size(); j++){
					int i = j - 5;
					this.editbars.get(j).setXY(0, getHeight() - (i+1)*editbars.get(j).getHeight());
				}
				for (int i = 0; i < 5; i++){
					this.editbars.get(i).setXY(0, getHeight() + 100);
				}
				break;
			case TOGGLE_BOTH:
				this.editbarHeightFactor = 11;
				this.toggleEditBarBitmaps(EditBar.BMP_SMALL);
				for (int i = 0; i < this.editbars.size(); i++){
					this.editbars.get(i).setXY(0, getHeight() - (i+1)*editbars.get(i).getHeight());
				}
				break;
			}
		}
	}
	public void toggleEditBarBitmaps(int index){
		//WARNING: if the order of the sprites are mixed b/w teams, we're displaying the first 5 & last 5, which could make no sense, To fix this: check for teams and organize by team
		for (int i = 0; i < this.activity.thread.getNumPlayers(); i++){
			EditBar e =editbars.get(i);
			e.setBitmaps(index);
		}
		this.createPlayerIcons();
	}
	private List<Bitmap> scaleEditBarBitmaps(EditBar e){
		List<Bitmap> bmps= new ArrayList<Bitmap>();
		float ratio;
		int[] res_ls = {R.drawable.mid,R.drawable.start,R.drawable.end, R.drawable.line,
				R.drawable.sym_ball,R.drawable.sym_block,R.drawable.sym_move,
				R.drawable.paren_f,R.drawable.paren_b,R.drawable.run_ball,
				R.drawable.start_or,R.drawable.mid_or,R.drawable.end_or
		};
		for (int i = 0; i < res_ls.length;i++){
			Bitmap bmp= BitmapFactory.decodeResource(getResources(),res_ls[i]);
			ratio = (float)getHeight()/(float)this.editbarHeightFactor/(float)bmp.getHeight();
			Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int) (ratio*(float)bmp.getWidth()), (int) (ratio*(float)bmp.getHeight()), true);
			bmps.add(tmp);
		} 
		return bmps;
	}
	public void createPlayerIcons(){
		for (int i = 0; i < this.activity.thread.sprites.size(); i++){
			Sprite s = this.activity.thread.sprites.get(i);
			Bitmap b = s.getBitmap();
			int size = this.getHeight()/this.editbarHeightFactor;
			Bitmap icon = Bitmap.createScaledBitmap(b, size, size, false);
			this.editbars.get(i).setPlayerIcon(icon);
		}
	}

	private EditBar createEditBar() {
		return new EditBar(this, activity);
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
