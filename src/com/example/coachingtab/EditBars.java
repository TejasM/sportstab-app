package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.provider.CalendarContract.Events;

public class EditBars {
	static final int IDLE = 0;
	static final int MOVE = 1;
	static final int BALL_IDLE = 2;
	static final int BALL_MOVE = 3;
	static final int BLACK = 4;


	private CoachingTab activity;
	private EditView view;
	private List<EditBar> editbars = new ArrayList<EditBar>();
	private int selectedBarIndex = -1;
	private int editEventMode;
	public List<EditFragment> possessionByPlayer = new ArrayList<EditFragment>();// a single array of fragments that goes from 0 to 1 changing by player, used to update possessions
	public int getEditEventMode() {
		return editEventMode;
	}
	public EditBars(EditView view, CoachingTab activity) {
		this.activity = activity;
		this.view = view;
	}
	public int size(){
		return editbars.size();
	}
	public EditBar get(int index){
		return editbars.get(index);
	}
	public void setSelectedBarIndex(int i){
		this.selectedBarIndex = i;
	}

	public void add(EditBar edt){
		editbars.add(edt);
	}

	public List<EditBar> getList(){
		return editbars;
	}

	public void render(Play play){
		synchronized(this.view.getHolder()){
			/*just do 1 step for now*/
			if (play==null)return;
			Step step = play.steps.get(0);
			for (int i = 0; i < step.playerMove.size(); i++){
				editbars.get(i).fragments =new ArrayList<EditFragment>();
			}
			//sort the events by their timepoint, cause users can mess up the order of the events
			play.steps.get(0).sortEvents();
			renderComponents(play);
			//		System.out.println("render");
			/*now use the components to make the actual fragments*/
			renderFragments();
		}


	}
	public void renderFragments(){
		for (int i = 0; i<this.editbars.size();i++){
			int m=0;
			int p=0;
			int s=0;
			EditBar edtb = this.editbars.get(i);
			float curr_time=0;
			//			System.out.println(edtb.possessions.size());
			while ((m<edtb.movements.size())&&(p<edtb.possessions.size())&&(s<edtb.screens.size())){
				int type = -1;
				float end;
				end = min(edtb.movements.get(m).getEnd(),edtb.possessions.get(p).getEnd(),edtb.screens.get(s).getEnd());
				int t = argmin(edtb.movements.get(m).getEnd(),edtb.possessions.get(p).getEnd(),edtb.screens.get(s).getEnd());
				if (t==1){
					type = parseType(edtb.movements.get(m).getType(),edtb.possessions.get(p).getType());
					editbars.get(i).fragments.add(new EditFragment((float)curr_time,end,type));
					m++;
				}if(t==2){
					type = parseType(edtb.movements.get(m).getType(),edtb.possessions.get(p).getType());
					editbars.get(i).fragments.add(new EditFragment((float)curr_time,end,type));
					p++;
				}if(t==3){
					if (edtb.screens.get(s).getType()==BLACK){
						type = BLACK;
					}else{
						type = edtb.possessions.get(p).getType();
					}
					editbars.get(i).fragments.add(new EditFragment((float)curr_time,end,type));
					s++;
				}
				curr_time = end;
			}
		}
	}
	private float min(float f1, float f2, float f3){
		if (f1>f2){
			if (f2>f3){
				return f3;
			}else{
				return f2;
			}
		}else{
			if (f1>f3){
				return f3;
			}else{
				return f1;
			}
		}
	}
	private int argmin(float f1, float f2, float f3){
		if (f1>f2){
			if (f2>f3){
				return 3;
			}else{
				return 2;
			}
		}else{
			if (f1>f3){
				return 3;
			}else{
				return 1;
			}
		}
	}
	public int parseType(int m, int p){
		int ret = -1;
		if ((m==IDLE)&&(p==IDLE)){
			ret = IDLE;
		}
		if ((m==IDLE)&&(p==BALL_IDLE)){
			ret = BALL_IDLE;
		}
		if ((m==MOVE)&&(p==IDLE)){
			ret = MOVE;
		}
		if ((m==MOVE)&&(p==BALL_IDLE)){
			ret = BALL_MOVE;
		}
		if (ret==-1){
			//			System.out.println(m);
			//			System.out.println(p);
		}
		//		System.out.println("called");
		return ret;
	}
	public void renderComponents(Play play){
		renderMovements(play);
		renderPossessions(play);
		renderScreens(play);
	}
	public void renderMovements(Play play){

		/*just do 1 step for now*/
		if (play==null)return;
		Step step = play.steps.get(0);
		int len = step.playerMove.get(0).points.size();

		float prevX = -1;
		float prevY = -1;
		/*loop through each player*/
		for (int i = 0; i < step.playerMove.size(); i++){
			editbars.get(i).movements =new ArrayList<EditFragment>();//initialize them to emtpy list on start..this refreshes it when we render a new play
			OneMove move = step.playerMove.get(i);
			int prev_state = -1;
			int curr_state = -1;
			/*check initial condition*/
			if ((move.points.get(0).x == step.initialPositions.get(i).x) && (move.points.get(0).y == step.initialPositions.get(i).y)){
				prev_state = IDLE;
			}else{
				prev_state = MOVE;
			}

			if (prev_state == IDLE){
				editbars.get(i).movements.add(new EditFragment((float)0,(float)1,this.IDLE));
			}else{
				editbars.get(i).movements.add(new EditFragment((float)0,(float)1,this.MOVE));
			}
			/*I am not checking event for initial condition..it shouldn't happen either*/
			int patience = 30;
			prevX = step.playerMove.get(i).points.get(0).x;
			prevY = step.playerMove.get(i).points.get(0).y;
			for (int j = 1; j < move.points.size(); j++){
				int tmp = j;
				for (int p = 0;p<patience;p++){//data is choppy, we might have redundant points
					//so one continuous movement can be chopped up to 100 discrete movements..i'm looping..as a hack..to fix this
					if ((prevX != move.points.get(j).x) && (prevY != move.points.get(j).y)){
						curr_state = MOVE;
					}else{
						curr_state = IDLE;
					}
					if (curr_state==MOVE) break;
					if (j < move.points.size()-1) j++;
					else break;
				}
				j=tmp;
				if (prev_state != curr_state){
					prev_state = curr_state;
					editbars.get(i).movements.get(editbars.get(i).movements.size()-1).setEnd((float)j/(float)len);
					editbars.get(i).movements.add(new EditFragment((float)j/(float)len,(float)1,curr_state));
				}
				prevX = step.playerMove.get(i).points.get(j).x;
				prevY = step.playerMove.get(i).points.get(j).y;
			}
		}
	}
	public void renderPossessions(Play play){
		/*just do 1 step for now*/
		if (play==null)return;
		Step step = play.steps.get(0);
		int len = step.playerMove.get(0).points.size();

		/*loop through each player*/
		for (int i = 0; i < step.playerMove.size(); i++){
			editbars.get(i).possessions =new ArrayList<EditFragment>();
			int prev_state = -1;
			int curr_state = -1;
			if (step.whoHasBall.get(0)==i){
				prev_state = BALL_IDLE;
				editbars.get(i).possessions.add(new EditFragment((float)0,(float)1,this.BALL_IDLE));
			}
			else {
				prev_state = IDLE;
				editbars.get(i).possessions.add(new EditFragment((float)0,(float)1,this.IDLE));
			}
			for (int j = 1; j < len; j++){
				if ((j<step.whoHasBall.size())&&(step.whoHasBall.get(j) == i)){
					curr_state = BALL_IDLE;
				}else{
					curr_state = IDLE;
				}

				if (prev_state != curr_state){
					prev_state = curr_state;
					editbars.get(i).possessions.get(editbars.get(i).possessions.size()-1).setEnd((float)j/(float)len);
					editbars.get(i).possessions.add(new EditFragment((float)j/(float)len,(float)1,curr_state));
				}
			}
		}
		/* make the possessionByIndex */
		possessionByPlayer = new ArrayList<EditFragment>();
		int curr_owner = -10;
		for (int j = 0; j < len; j++){
			if (curr_owner!=step.whoHasBall.get(j)){
				curr_owner = step.whoHasBall.get(j);
				if (this.possessionByPlayer.size()>0){
					this.possessionByPlayer.get(this.possessionByPlayer.size()-1).setEnd((float)j/(float)len);
				}
				this.possessionByPlayer.add(new EditFragment((float)j/(float)len,(float)1,-1));
				this.possessionByPlayer.get(this.possessionByPlayer.size()-1).setPossessionIndex(curr_owner);
			}
		}
	}
	public void renderScreens(Play play){
		/*just do 1 step for now*/
		if (play==null)return;
		Step step = play.steps.get(0);
		int len = step.playerMove.get(0).points.size();
		/*loop through each player*/
		for (int i = 0; i < step.playerMove.size(); i++){
			editbars.get(i).screens =new ArrayList<EditFragment>();//initialize them to emtpy list on start..this refreshes it when we render a new play
			int prev_state = -1;
			int curr_state = -1;
			/*check initial condition*/
			if (step.initState.get(i) == Sprite.BLOCK){
				editbars.get(i).screens.add(new EditFragment((float)0,(float)1,this.BLACK));
				prev_state = BLACK;
				curr_state = BLACK;
			}else{
				editbars.get(i).screens.add(new EditFragment((float)0,(float)1,this.IDLE));
				prev_state = IDLE;
				curr_state = IDLE;
			}
			/*I am not checking event for initial condition..it shouldn't happen either*/
			for (int k = 0; k < step.events.size();k++){
				if ((step.events.get(k).event_type == Step.SCREEN)&&(step.events.get(k).screenPlayerIndex==i)){
					curr_state = BLACK;
				}else if ((prev_state == BLACK)&&(step.events.get(k).event_type == Step.DONESCREEN)&&(step.events.get(k).doneScreenPlayerIndex==i)){
					curr_state = IDLE;
				}
				if (prev_state != curr_state){
					prev_state = curr_state;
					float j = step.events.get(k).timepoint;
					editbars.get(i).screens.get(editbars.get(i).screens.size()-1).setEnd((float)j/(float)len);
					editbars.get(i).screens.add(new EditFragment((float)j/(float)len,(float)1,curr_state));
				}
			}
		}
	}
	public int isTouched(float x, float y){
		for (int i = 0; i < this.editbars.size(); i++){
			EditBar e = editbars.get(i);
			if(e.isTouched(x, y)){
				this.selectedBarIndex = i;
				return i;
			}
		}
		return -1;
	}
	public void update(float x){
		try{
			this.editbars.get(this.selectedBarIndex).updateCurrX(x);
		}
		catch(Exception e){
			int a = 1;
		}
	}
	public void finishMoving(){
		this.editbars.get(this.selectedBarIndex).finishMoving(this.selectedBarIndex);
		this.selectedBarIndex =-1;
	}

	public void setEditEventMode(int btnEditBall) {
		this.editEventMode = btnEditBall;
	}
	public int prevMeaningfulPossessionIndex(int i){
		while (i>0){
			i--;
			if (this.possessionByPlayer.get(i).getPossessionIndex()!=-1){
				return i;
			}
		}
		return -1;
	}
}
