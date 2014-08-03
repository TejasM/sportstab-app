package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class EditBar {
	private Bitmap idle;
	private Bitmap move1;
	private Bitmap ball_idle;
	private Bitmap black;
	private Bitmap ball_move;
	private List<Bitmap> bmps = new ArrayList<Bitmap>();
	private CoachingTab activity;
	private EditView view;
	private float x;
	private float y;
	private int height;
	private float down_x = -1;
	private float curr_x = -1;
	private int selectedEventIndex = -1;
	public List<EditFragment> fragments = new ArrayList<EditFragment>();

	public EditBar(EditView view, CoachingTab activity, Bitmap idle,  Bitmap move1, Bitmap ball_idle, Bitmap black, Bitmap ball_move) {

		this.activity = activity;
		this.view = view;
		this.idle = idle;
		this.move1 = move1;
		this.ball_idle = ball_idle;
		this.black = black;
		this.ball_move = ball_move;
		
		this.bmps.add(idle);
		this.bmps.add(move1);
		this.bmps.add(ball_idle);
		this.bmps.add(ball_move);
		this.bmps.add(black);
	
		this.height = idle.getHeight();

	}
	public void setXY(float x, float y){
		this.x = x;
		this.y = y;
	}
	public int getHeight(){
		return height;
	}
	public boolean isTouched(float x,float y){
		boolean ret = (this.y - y) * (this.y+this.height-y) < 0;
		if (ret){//if bar is touched, figure out which event 
			for (int i = 0; i<fragments.size();i++){
				float start = (float)(fragments.get(i).getStart()*view.getWidth());
				float end = (float)(fragments.get(i).getEnd()*view.getWidth());
				if ((end - x)*(start-x)<0){
					this.selectedEventIndex = i;
					break;
				}
			}
		}
		if (ret&&(this.selectedEventIndex==-1)){
			System.out.println("wtf");
		}
		return ret;
	}
	public void updateCurrX(float x){
		if (this.down_x == -1){//first update
			this.down_x = x;
		}else{
			float prev_x = this.curr_x;
			this.curr_x = x;
			//now check if this guy is gonna overlap the meaningful event before or after him
			float dx = this.percentDX();
			EditFragment e = this.fragments.get(selectedEventIndex);
			
			if (this.selectedEventIndex>1){
				EditFragment last2 = this.fragments.get(this.selectedEventIndex-2);
				if ((e.getStart()+dx<=last2.getEnd())&&(last2.getType()!=EditBars.IDLE)){
					this.curr_x=prev_x;
				}
			}
			if (this.selectedEventIndex<this.fragments.size()-2){
				EditFragment next2 = this.fragments.get(this.selectedEventIndex+2);
				if ((e.getEnd()+dx>=next2.getStart())&&(next2.getType()!=EditBars.IDLE)){
					this.curr_x=prev_x;
				} 
			}
			EditFragment next = this.fragments.get(this.selectedEventIndex+1);
			EditFragment last = this.fragments.get(this.selectedEventIndex-1);
			if ((e.getType()==EditBars.BALL_IDLE)&&(last.getType()==EditBars.IDLE)){
				if ((e.getStart()+dx>=next.getStart())){
					this.curr_x=prev_x;
				}
			}

		}
	}
	private float percentDX(){
		float dx = this.curr_x - this.down_x;
		float ref = (float)view.getWidth();
		return dx = dx/ref;
	}
	@SuppressWarnings("unused")
	public void finishMoving(int index){


		Step step = activity.getGameView().getRecorder().getCurrPlay().steps.get(0);
		// render the fragments
		float dx = this.percentDX();
		EditFragment e = this.fragments.get(selectedEventIndex);
		OneMove move = step.playerMove.get(index);
		int start_index = (int)(e.getStart() * (float)move.points.size());
		int end_index = (int)(e.getEnd() * (float)move.points.size());
		int dx_index = (int) (dx*move.points.size());
		int new_start_index =start_index+dx_index;
		int new_end_index = end_index + dx_index;
		int size = move.points.size();
		
		float epsilon = (float)0.05;
		if ((new_start_index<0)||(new_end_index>=size)){
			int dumb = 1; //user moved the event to outside of the screen
		}
		else if ((this.selectedEventIndex==0)||(this.selectedEventIndex==this.fragments.size()-1)){
			int dumb = 1; //if the selected event is the first or last one...don't do anything.
			//TODO: what does it mean if i move the first or last fragment..?
		}else{
			
			EditFragment last = this.fragments.get(this.selectedEventIndex-1);
			EditFragment next = this.fragments.get(this.selectedEventIndex+1);
			
			if ((e.getType()==EditBars.BALL_MOVE)||(e.getType()==EditBars.MOVE)||(e.getType()==EditBars.BLACK)){
				if ((e.getType()==EditBars.BALL_MOVE)||(e.getType()==EditBars.MOVE)){
					List<List<MyPointF>> motions = new ArrayList<List<MyPointF>>();
					//motions.add(new ArrayList<MyPointF>(move.points.subList(start_index, end_index)));
					List<MyPointF> m = new ArrayList<MyPointF>();
					for (int j=start_index;j<end_index;j++){
						m.add(new MyPointF(move.points.get(j)));
					}
					motions.add(m);
					for (int j = new_start_index;j<new_end_index;j++){
						int i = j-new_start_index;
						move.points.get(j).x = motions.get(0).get(i).x;
						move.points.get(j).y = motions.get(0).get(i).y;
					}
				}
				if (e.getType()==EditBars.BLACK){
					for (int i = 0; i < step.events.size();i++){
						Event event = step.events.get(i);
						float percent_time =((float)event.timepoint/(float)size);
						//need to find out which event we're editing in the original events array
						if (event.screenPlayerIndex==index){//event happens on the same person & same type SCREEN
							if ((e.getStart()>=percent_time-epsilon)&&(e.getStart()<=percent_time+epsilon)){//this event happens around the same time as the one we're editting
								event.timepoint=(int)((percent_time+dx)*(float)size);
							}
						}
						//done screen event
						if ((event.doneScreenPlayerIndex==index)){
							if ((e.getEnd()>=percent_time-epsilon)&&(e.getEnd()<=percent_time+epsilon)){
								event.timepoint=(int)((percent_time+dx)*(float)size);
							}
						}
					}
				}
				//pad the next or prev event with where this guy started/finished
				MyPointF startPos = move.points.get(start_index);
				MyPointF endPos = move.points.get(end_index);
				for (int j = start_index; j < new_start_index;j++){
					move.points.get(j).x = startPos.x;
					move.points.get(j).y = startPos.y;
				}
				for (int j = new_end_index; j < end_index;j++){
					move.points.get(j).x = endPos.x;
					move.points.get(j).y = endPos.y;
				}
				
				/*
				e.setStart(dx+e.getStart());
				e.setEnd(dx+e.getEnd());


				last.setEnd(dx+last.getEnd());
				next.setStart(dx+next.getStart());
				*/
				this.view.editbars.render(activity.getGameView().getRecorder().getCurrPlay());
			}
			if ((e.getType()==EditBars.BALL_IDLE)&&(last.getType()==EditBars.IDLE)){//move the pass
				int passer_id = -1; 
				int old_timepoint = -1;
				int new_timepoint = -1;
				for (int i = 0; i < step.events.size();i++){
					Event event = step.events.get(i);
					float percent_time =((float)event.timepoint/(float)size);
					
					//need to find out which event we're editing in the original events array
					if (event.passTo==index){//matches the receiver of the pass
						if ((e.getStart()>=percent_time-epsilon)&&(e.getStart()<=percent_time+epsilon)){//this event happens around the same time as the one we're editting
							old_timepoint = event.timepoint;
							event.timepoint=(int)((percent_time+dx)*(float)size);
							new_timepoint = event.timepoint;
							passer_id = event.passFrom;
							
						}
					}
				}
				//now, update the hasBall data struct 
				//first find out how many 'ball-in-the-air' we have WARNING: this might actually change if the position of the players are also changed, but i'm just not gonna worry about it now
				int count = 1;
				for (int i = old_timepoint + 1;step.whoHasBall.get(i)==-1;i++){
					count++;
				}
				if (new_timepoint < old_timepoint){
					int i;
					for (i = new_timepoint; i < new_timepoint+count;i++){
						step.whoHasBall.remove(i);
						step.whoHasBall.add(i,-1);
					}
					for (;i<=old_timepoint+count;i++){
						step.whoHasBall.remove(i);
						step.whoHasBall.add(i,index);
					}
				}else{
					int i;
					for (i = old_timepoint; i<new_timepoint;i++){
						step.whoHasBall.remove(i);
						step.whoHasBall.add(i,passer_id);
					}
					for (;i<=new_timepoint+count;i++){
						step.whoHasBall.remove(i);
						step.whoHasBall.add(i,-1);
					}
				}
				//render the editbars..not just the receiver, but also the passer
				/*
				List<EditFragment> passer_fragments = this.view.editbars.get(passer_id).fragments;
				int passer_event_id=-1;
				for (int i=0; i<passer_fragments.size();i++){
					if ((passer_fragments.get(i).getEnd()>=e.getStart()-epsilon)&&(passer_fragments.get(i).getEnd()<=e.getStart()+epsilon)){
						passer_fragments.get(i).setEnd(passer_fragments.get(i).getEnd()+dx);
						passer_fragments.get(i+1).setStart(passer_fragments.get(i+1).getStart()+dx);
						break;
					}
				}
				e.setStart(dx+e.getStart());
				last.setEnd(dx+last.getEnd());
				*/
				this.view.editbars.render(activity.getGameView().getRecorder().getCurrPlay());
			}

		}
		//cleanup
		this.down_x = -1;
		this.selectedEventIndex = -1;
	}

	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas){	
		if (fragments.size()==0){
			canvas.drawBitmap(idle, x,y, null);
		}else{
			for (int i = 0; i<fragments.size();i++){
				int start = (int)(fragments.get(i).getStart()*view.getWidth());
				int end = (int)(fragments.get(i).getEnd()*view.getWidth());
				Rect src = new Rect(start, 0, end, height);
				Rect dst = new Rect((int)x+start,(int) y,(int) x + end,(int) y + height);
				canvas.drawBitmap(bmps.get(fragments.get(i).getType()), src, dst, null);
			}
			if ((this.selectedEventIndex!=-1)&&(this.selectedEventIndex<this.fragments.size())){//WARNING: the second check is one of those bad checks that hide crashes :(
				int i = this.selectedEventIndex;
				int start = (int)(fragments.get(i).getStart()*view.getWidth());
				int end = (int)(fragments.get(i).getEnd()*view.getWidth());
				Rect src = new Rect(start, 0, end, height);
				Rect dst = new Rect((int)x+start,(int) y,(int) x + end,(int) y + height);
				float dx = this.curr_x - this.down_x;
				Rect dst1 = new Rect((int)(x+start+dx),(int)( y-height*0.1),(int) (x + end+dx),(int)( y + height-height*0.1));
				canvas.drawBitmap(bmps.get(fragments.get(i).getType()), src, dst1, null);
			}
		}
	}

}
