package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.graphics.PointF;


public class Step implements java.io.Serializable{
	/**
	 * 
	 */
	public static final int STILL = 0;
	public static final int MOVE = 1;
	public static final int PASS = 2;
	public static final int DONESCREEN = 3;
	public static final int SCREEN = 4;
	
	private static final long serialVersionUID = -8470140382638603968L;
	private int initBall;


	public List<OneMove> playerMove = new ArrayList<OneMove>();
	public List<MyPointF>  initialPositions = new ArrayList<MyPointF>();
	public List<Integer> whoHasBall = new ArrayList<Integer>();
	public List<Event> events = new ArrayList<Event>();
	public int replay_progress;
	public int currEventNum = 0;

	public List<Integer> initState = new ArrayList<Integer>();
	public transient List<Matrix> initMatrices = new ArrayList<Matrix>();
	public List<MatrixData> initMatrices_data = new ArrayList<MatrixData>();


	public Step(List<Sprite> sprites, GameView view) {
		initBall = GameLoopThread.NoOne;
		for (int i = 0; i < sprites.size(); i++){
			if (sprites.get(i).getHasBall()){
				initBall = i;
			}
		}
		
		if (initBall == GameLoopThread.NoOne){/* in case some fucktard wants to start recording while the ball is in mid-air */
			//view.thread.setBall(0);
			initBall = view.thread.passingTo;
			view.thread.passComplete();

		}

		for (int i = 0; i < sprites.size(); i++){
			playerMove.add(new OneMove());
			initialPositions.add(new MyPointF(sprites.get(i).getX(), sprites.get(i).getY()));
		}
	}
	public int getInitBall(){
		return this.initBall;
	}
	public void setReplayProgress(int p){
		replay_progress = p;
	}
	/*********************replaying part********************************/
	/***********
	 * 
	 * @param sprites
	 * @param view
	 * @return whether or not this step is done replaying
	 */
	public boolean replay(List<Sprite> sprites, GameView view, int replay_speed){
		if (replay_helper(sprites, view, replay_speed) == true) /* run it, and see if this step is done */
		{/* done */
			return true;
		}
		return false;
	}
	public boolean replay_helper(List<Sprite> sprites, GameView view, int replay_speed){

		for (int j = 0 ; j < replay_speed; j++){/* just to test... */
			if(replay_progress<playerMove.get(playerMove.size()-1).points.size()){
				for (int i = 0; i < playerMove.size(); i++){
					float x1 = playerMove.get(i).points.get(replay_progress).x;
					float y1 = playerMove.get(i).points.get(replay_progress).y;
					view.positionSprite(sprites.get(i), x1, y1);	
				}
				
				if ((currEventNum < events.size())&&(replay_progress == events.get(currEventNum).timepoint)){
					if (events.get(currEventNum).event_type == PASS){
						view.thread.setBall(events.get(currEventNum).passTo);
					}
					else if (events.get(currEventNum).event_type == DONESCREEN){
						sprites.get(events.get(currEventNum).doneScreenPlayerIndex).doneScreen();
					}else if (events.get(currEventNum).event_type == SCREEN){
						sprites.get(events.get(currEventNum).screenPlayerIndex).onScreen(events.get(currEventNum).screenDown,events.get(currEventNum).screenUp);
					}
					currEventNum++;
				}
				replay_progress++;
			}else{
				return true;
			}
		}
		return false;
	}



	public void startStep(List<Sprite> sprites, GameView view){
		/* clear all the paths, set state to free */
		int matrix_count = 0;
		for (int i = 0; i < sprites.size(); i++){
			Sprite s = sprites.get(i);
			s.resetPaths();
			s.doneScreen();
			if (initState.get(i) == Sprite.BLOCK){
				s.setState(Sprite.BLOCK);
				s.setMatrix(initMatrices.get(matrix_count));
				matrix_count++;
			}
		}
		/* first set the ball to its initial owner */
		view.thread.setBallInMiddleOfPass(initBall);
		view.thread.passComplete();
		view.thread.ball.resetPaths();
		
		for (int i = 0; i < sprites.size(); i++){
			float x1 = initialPositions.get(i).x;
			float y1 = initialPositions.get(i).y;
			sprites.get(i).simpleUpdate(x1, y1);
			if (sprites.get(i).getHasBall()){
				sprites.get(i).updateBall();
			}
		}
		currEventNum=0;
		replay_progress=0;
	}

	/*********************recording part*********************************/

	/********
	 * Record who are moving in this step and their moves
	 * 
	 * @param sprites
	 */
	private boolean noOneIsMoving(List<Sprite> sprites){
		for (int i = 0; i < sprites.size(); i++){
			if (sprites.get(i).getMoving()) return false;
		}
		return true;
	}


	public void onPassEvent(int passingTo, int passFrom) {
		events.add(new Event(this.playerMove.get(0).points.size()-1,PASS));
		events.get(events.size()-1).setPass(passingTo, passFrom);
	}
	public void onScreenEvent(int playerIndex, MyPointF down, MyPointF up){
		events.add(new Event(this.playerMove.get(0).points.size()-1,SCREEN));
		events.get(events.size()-1).setScreen(playerIndex, down, up);
	}
	public void onDoneScreenEvent(int playerIndex, List<MyPointF> pts){
		events.add(new Event(this.playerMove.get(0).points.size()-1,DONESCREEN));
		events.get(events.size()-1).setDoneScreen(playerIndex, pts);
	}
	public void recordOneStep(List<Sprite> sprites, GameView view){
		int ball_index=-1;
		for (int i = 0; i < sprites.size(); i++){
			playerMove.get(i).addPointF(new MyPointF(sprites.get(i).getX(), sprites.get(i).getY()));
			if (sprites.get(i).getHasBall()){
				ball_index=i;
			}
		}
		whoHasBall.add(ball_index);
	}
	public void recordInitStates(List<Sprite> sprites, GameView view){
		for (Sprite s: sprites){
			initState.add(s.getState());
			if (s.getState() == Sprite.BLOCK){
				initMatrices.add(new Matrix(s.getMatrix()));
			}
		}
	}
	/*********** saving/loading ************/
	public void storeMatrixData(){
		float [] tmp = new float[9];
		for (int i = 0; i < initMatrices.size(); i++){
			initMatrices_data.add(new MatrixData());
			initMatrices.get(i).getValues(tmp);
			initMatrices_data.get(i).setMatrixData(tmp);
		}
	}

	public void restoreMatrixData(){
		float [] tmp = new float[9];	
		initMatrices = new ArrayList<Matrix>();

		for (int i = 0; i < initMatrices_data.size(); i++){
			initMatrices.add(new Matrix());
			initMatrices_data.get(i).getMatrixData(tmp);
			initMatrices.get(i).setValues(tmp);
		}
	}
	public void sortEvents() {
		List<Event> list = new ArrayList<Event>();
		int lastMin =-1;
		int min, minIndex;
		for (int i = 0; i<this.events.size();i++){
			min = 10000;
			minIndex = -1;
			for (int j=0; j<this.events.size();j++){
				if ((this.events.get(j).timepoint<min)&&(list.contains(this.events.get(j))==false)){
					minIndex = j;
					min = this.events.get(j).timepoint;
				}
			}
			list.add(this.events.get(minIndex));
			lastMin = min;
			minIndex = -1;
		}
		this.events = list;
	}
}