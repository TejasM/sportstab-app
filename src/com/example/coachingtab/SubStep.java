package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
/***********
 * Each Step (from user) is broken down into many substeps
 * These are used so we know when a player is moved relative to the other players, or just the relative timing of events in one user step
 * One substep is going to consist of
 * a optional beginning where no one is moving...denoted by initialPositions
 * then a series of move...where the number of players moving stays unchanged
 * @author jacksowa
 *
 */

public class SubStep implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -702872087981108549L;

	

	private MyPointF screenDown;
	private MyPointF screenUp;

	private float passCompleteness;


	/*junk
	 * 
	 * if (numPlayerMovingChanged(sprites)){// start a new substep
	subSteps.add(new SubStep(sprites, GameLoopThread.NoOne, SubStep.MOVE));
}else if(onPassEvent(view)){
	subSteps.add(new SubStep(sprites, view.thread.passingTo, SubStep.PASS));
}else if (screenEvent){
	subSteps.add(new SubStep(sprites, screenPlayerIndex, screenDown, screenUp));
	screenEvent = false;
}else if (doneScreenEvent){
	subSteps.add(new SubStep(sprites, doneScreenPlayerIndex, SubStep.DONESCREEN));
	doneScreenEvent = false;
}else{// in the middle of a substep 
	if (subSteps.size() != 0){ // the zero size case happened when u just press record...no one was moving, and no one is moving now either 
		subSteps.get(subSteps.size() - 1).recordSubStepSnapshot(sprites);
	}
}*/






}