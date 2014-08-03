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

	public EditBars(EditView view, CoachingTab activity) {
		this.activity = activity;
		this.view = view;
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
		/*just do 1 step for now*/
		if (play==null)return;
		Step step = play.steps.get(0);
		int len = step.playerMove.get(0).points.size();
		
		float prevX = -1;
		float prevY = -1;
		/*loop through each player*/
		for (int i = 0; i < step.playerMove.size(); i++){
			editbars.get(i).fragments =new ArrayList<EditFragment>();//initialize them to emtpy list on start..this refreshes it when we render a new play
			OneMove move = step.playerMove.get(i);
			int prev_state = -1;
			int curr_state = -1;
			int curr_event_num = 0;
			/*check initial condition*/
			if ((move.points.get(0).x == step.initialPositions.get(i).x) && (move.points.get(0).y == step.initialPositions.get(i).y)){
				prev_state = IDLE;
			}else{
				prev_state = MOVE;
			}
			if (step.getInitBall()==i){
				if (prev_state == IDLE){
					editbars.get(i).fragments.add(new EditFragment((float)0,(float)1,this.BALL_IDLE));
					prev_state = BALL_IDLE;
				}else{
					editbars.get(i).fragments.add(new EditFragment((float)0,(float)1,this.BALL_MOVE));
					prev_state = BALL_MOVE;
				}
			}else if (step.initState.get(i) == Sprite.BLOCK){
				editbars.get(i).fragments.add(new EditFragment((float)0,(float)1,this.BLACK));
				prev_state = BLACK;
			}else{
				if (prev_state == IDLE){
					editbars.get(i).fragments.add(new EditFragment((float)0,(float)1,this.IDLE));
				}else{
					editbars.get(i).fragments.add(new EditFragment((float)0,(float)1,this.MOVE));
				}
			}
			/*I am not checking event for initial condition..it shouldn't happen either*/
			int patience = 30;
			prevX = step.playerMove.get(i).points.get(0).x;
			prevY = step.playerMove.get(i).points.get(0).y;
			for (int j = 1; j < move.points.size(); j++){
				if ((prev_state==MOVE)||(prev_state==BALL_MOVE)){
					
					int tmp = j;
					for (int p = 0;p<patience;p++){//data is choppy, we might have redundant points
						//so one continuous movement can be chopped up to 100 discrete movements..i'm looping..as a hack..to fix this
						if ((prevX != move.points.get(j).x) && (prevY != move.points.get(j).y)){
							curr_state = MOVE;
						}else{
							curr_state = IDLE;
						}
						if ((j<step.whoHasBall.size())&&(step.whoHasBall.get(j) == i)){/*this is weird..j shouldn't exceed the size of whoHasBall*/
							if (curr_state==MOVE){
								curr_state = BALL_MOVE;
							}else{
								curr_state = BALL_IDLE;
							}
						}
						if ((curr_state==MOVE)||(curr_state==BALL_MOVE)) break;
						if (j < move.points.size()-1) j++;
						else break;
					}
					j=tmp;
				}
				if (!((curr_state==MOVE)||(curr_state==BALL_MOVE))) {
					if ((prevX != move.points.get(j).x) && (prevY != move.points.get(j).y)){
						curr_state = MOVE;
					}else{
						curr_state = IDLE;
					}
					if ((curr_state == IDLE)&&(prev_state == BLACK)) curr_state=BLACK;
					if ((j<step.whoHasBall.size())&&(step.whoHasBall.get(j) == i)){/*this is weird..j shouldn't exceed the size of whoHasBall*/
						if (curr_state==MOVE){
							curr_state = BALL_MOVE;
						}else{
							curr_state = BALL_IDLE;
						}
						System.out.println("index: "+j);
					}/*if this guy has ball, he won't be in BLACK 
					else 
					okay..i still didn't figure out why when i add the 'else'..it didn't work	{*/
					if ((curr_event_num < step.events.size())&&(step.events.get(curr_event_num).timepoint == j)){
						System.out.println("AM I EVER CALLED? :( "+j);
						if ((step.events.get(curr_event_num).event_type == Step.SCREEN)&&(step.events.get(curr_event_num).screenPlayerIndex==i)){
							curr_state = BLACK;
							System.out.println("RIGHT WAY "+j);
						} else if ((prev_state == BLACK)&&(step.events.get(curr_event_num).event_type == Step.DONESCREEN)&&(step.events.get(curr_event_num).doneScreenPlayerIndex==i)){
							curr_state = IDLE;
							System.out.println("WRONG WAY "+j);
						}
						curr_event_num++;
					}
						//}

				}

				if (prev_state != curr_state){
					prev_state = curr_state;
					editbars.get(i).fragments.get(editbars.get(i).fragments.size()-1).setEnd((float)j/(float)len);
					editbars.get(i).fragments.add(new EditFragment((float)j/(float)len,(float)1,curr_state));
				}
				prevX = step.playerMove.get(i).points.get(j).x;
				prevY = step.playerMove.get(i).points.get(j).y;
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
}
