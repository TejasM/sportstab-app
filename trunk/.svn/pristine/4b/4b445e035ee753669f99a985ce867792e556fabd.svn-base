package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SettingView extends View{
	private CoachingTab activity;
	private Context context;
	private SeekBar sbSize;
	private Spinner spinner1, spinner2, spinner3;
	private Button btnSubmit;
	private EditText edtName;
	private Button btnAddPlayer;
	private Button btnDelPlayer;
	private SeekBar sbReplaySpeed;
	private SeekBar sbOverlap;
	//private String[] listPlayer;
	private List<String> listPlayer;
	private List<String> listCourt;
	ArrayAdapter<String> adapterEdtPlayer;
	ArrayAdapter<String> adapterCourt;
	private boolean stupid_default;/* when update spinner1, it always select one as default...stupid */ 
	private int what_it_should_be;/* above */
	private float tempSizeFactor;
	private int tempReplaySpeed;
	private float tempOverlap;

	public SettingView(Context context, CoachingTab activity, Spinner sp1, Spinner sp2, Spinner sp3, Button btn, EditText name, SeekBar sizebar, Button add, Button del, SeekBar replay_speed, SeekBar overlap) {
		super(context);
		this.context = context;
		this.activity = activity;
		spinner1 = sp1;
		spinner2 = sp2;
		spinner3 = sp3;
		btnSubmit = btn;
		edtName = name;
		sbSize = sizebar;
		btnAddPlayer = add;
		btnDelPlayer = del;
		sbReplaySpeed = replay_speed;
		sbOverlap = overlap;
		//listPlayer = new String[GameLoopThread.MAX_NUM_PLAYERS];
		listPlayer = new ArrayList<String>();
		addItemsOnSpinner1();
		addItemsOnSpinner2();
		addItemsOnSpinner3();
		addListenerOnButton();		
		addEdtNameListener();
		addPlayerSizeSeekbar();
		addReplaySpeedSeekbar();
		addOverlapSeekbar();
		addListenerOnSpinnerItemSelection();
	}

	//public SettingView(Context context, SettingActivity activity) {
	//super(context);
	/*sb_test = (SeekBar)findViewById(R.id.seekBar1);
		sb_test.setMax(360);
		sb_test.setOnSeekBarChangeListener(this);*/
	//}
	private void addPlayerSizeSeekbar(){
		sbSize.setMax(200);
		sbSize.setOnSeekBarChangeListener(new playerSizeOnSeekBarChangeListener());

	}
	private void addReplaySpeedSeekbar(){
		sbReplaySpeed.setMax(10);
		sbReplaySpeed.setOnSeekBarChangeListener(new replaySpeedOnSeekBarChangeListener());

	}
	private void addOverlapSeekbar(){
		sbOverlap.setMax(100);
		sbOverlap.setOnSeekBarChangeListener(new overlapOnSeekBarChangeListener());

	}
	
	

	private void addEdtNameListener() {
		edtName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {

					activity.getGameView().thread.sprites.get(spinner1.getSelectedItemPosition()).setName(edtName.getText().toString(), true);
					listPlayer.remove(spinner1.getSelectedItemPosition());
					listPlayer.add(spinner1.getSelectedItemPosition(), edtName.getText().toString());
					//adapterEdtPlayer.notifyDataSetChanged();
					stupid_default = true;
					what_it_should_be = spinner1.getSelectedItemPosition();
					adapterEdtPlayer = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listPlayer);
					adapterEdtPlayer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner1.setAdapter(adapterEdtPlayer);

					handled = true;
				}
				return handled;
			}
		});

	}

	public void run(){

	}


	public void addItemsOnSpinner1() {

		//spinner2 = (Spinner) findViewById(R.id.spinner2);
		//listPlayer = new ArrayList<String>();

		for(int i = 0; i < activity.getGameView().thread.sprites.size(); i++){
			listPlayer.add(activity.getGameView().thread.sprites.get(i).getName());
		}

		adapterEdtPlayer = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listPlayer);
		adapterEdtPlayer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapterEdtPlayer);
	}
	// add items into spinner dynamically
	public void addItemsOnSpinner2() {

		//spinner2 = (Spinner) findViewById(R.id.spinner2);
		List<String> list = new ArrayList<String>();

		list.add("Green");
		list.add("Red");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
	}
	
	public void addItemsOnSpinner3() {

		List<String> list = new ArrayList<String>();

		list.add("Regular");
		list.add("Wood Tile");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner3.setAdapter(adapter);
	}

	public void addListenerOnSpinnerItemSelection() {
		//spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener1());
		spinner2.setOnItemSelectedListener(new CustomOnItemSelectedListener2());
		spinner3.setOnItemSelectedListener(new CustomOnItemSelectedListener3());
	}

	// get the selected dropdown list value
	public void addListenerOnButton() {



		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.startGameView();
			}

		});
		btnAddPlayer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (activity.getGameView().thread.sprites.size() == GameLoopThread.MAX_NUM_PLAYERS){
					Toast.makeText(context, 
							"You got way too many players you know what i'm saying",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (somebodyThereAlready()){
					Toast.makeText(context, 
							"Somebody already there, go move him away",
							Toast.LENGTH_SHORT).show();
					return;
				}

				Sprite s = activity.getGameView().thread.createSprite(/*R.drawable.android_boi*/);
				s.setName("Default", false);
				activity.getGameView().thread.sprites.add(s);
				//activity.getGameView().playerOrder.add(activity.getGameView().thread.sprites.size() - 1);
				activity.getGameView().initPlayerOrder();
				/* deal with drop down list */
				listPlayer.add(s.getName());
				//adapterEdtPlayer.notifyDataSetChanged();
				stupid_default = true;
				what_it_should_be = listPlayer.size() - 1;
				adapterEdtPlayer = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listPlayer);
				adapterEdtPlayer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner1.setAdapter(adapterEdtPlayer);
			}

		});
		btnDelPlayer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (activity.getGameView().thread.sprites.size() == 1){
					Toast.makeText(context, 
							"Are you stupid or something? How are gonna play ball with no player?",
							Toast.LENGTH_SHORT).show();
					return;
				}
				/* check if the ball is being passed */
				if (activity.getGameView().thread.passingTo != GameLoopThread.NoOne){
					activity.getGameView().thread.passComplete();
				}
				/* pass the ball first if this fucker has it */
				if (activity.getGameView().thread.sprites.get(spinner1.getSelectedItemPosition()).getHasBall()){
					for (int i = 0; i < activity.getGameView().thread.sprites.size(); i++){
						if (i != spinner1.getSelectedItemPosition()){
							activity.getGameView().thread.setBall(i);
							activity.getGameView().thread.passComplete();
							if (activity.getGameView().thread.whoHasBall > spinner1.getSelectedItemPosition()){
								activity.getGameView().thread.whoHasBall--;
							}
							break;
						}
					}		
				}
				activity.getGameView().thread.sprites.remove(spinner1.getSelectedItemPosition());
				activity.getGameView().initPlayerOrder();
				/*activity.getGameView().playerOrder.remove((Object) spinner1.getSelectedItemPosition());
				// since the size of the list decreased, some player index need to decrement by one 
				for (int i = 0; i < activity.getGameView().playerOrder.size(); i++){
					if (activity.getGameView().playerOrder.get(i) > spinner1.getSelectedItemPosition()){
						int tmp = activity.getGameView().playerOrder.get(i); 
						tmp--;
						activity.getGameView().playerOrder.add(i, tmp);
						activity.getGameView().playerOrder.remove(i + 1);
					}
				}*/

				/* deal with drop down list */
				listPlayer.remove(spinner1.getSelectedItemPosition());
				adapterEdtPlayer = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listPlayer);
				adapterEdtPlayer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner1.setAdapter(adapterEdtPlayer);
			}

		});
	}
	private boolean somebodyThereAlready(){
		Sprite sprite = activity.getGameView().thread.createSprite(/*R.drawable.android_boi*/);
		Sprite sprite1 = activity.getGameView().thread.sprites.get(0);//this line is just here so it compiles
		for (int j = activity.getGameView().thread.sprites.size() -1 ; j>=0; j--){
			sprite1 = activity.getGameView().thread.sprites.get(j);
			if (sprite!=sprite1){
				if (activity.getGameView().doSpritesOverlap(sprite,sprite1)){
					return true;
				}
			}
		}
		return false;
	}
	public class CustomOnItemSelectedListener1 implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			if (!stupid_default){
				if (activity.getGameView().thread.sprites.get(pos).getTeamColor() == GameLoopThread.team_colors[0]){
					spinner2.setSelection(0);
				}else if (activity.getGameView().thread.sprites.get(pos).getTeamColor() == GameLoopThread.team_colors[1]){
					spinner2.setSelection(1);
				}
				sbSize.setProgress((int)((activity.getGameView().thread.sprites.get(pos).getR() * 100)/ activity.getGameView().thread.sprites.get(pos).getBaseR()));
			}else{
				spinner1.setSelection(what_it_should_be);
				stupid_default = false;
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
	public class CustomOnItemSelectedListener2 implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			if (pos == 0){
				activity.getGameView().thread.sprites.get(spinner1.getSelectedItemPosition()).setTeamColor(GameLoopThread.team_colors[0]);
			}else if(pos == 1){
				activity.getGameView().thread.sprites.get(spinner1.getSelectedItemPosition()).setTeamColor(GameLoopThread.team_colors[1]);
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
	public class CustomOnItemSelectedListener3 implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			activity.getGameView().setBackgroundType(pos);
			int width = activity.getGameView().c_width;
			int height = activity.getGameView().c_height;
			activity.getGameView().createBackground(width, height);
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
	public class playerSizeOnSeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (seekBar == sbSize){
				if (progress == 0){
					tempSizeFactor = progress + 1;
				}else{
					tempSizeFactor = progress;
				}

			}		
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			activity.getGameView().thread.sprites.get(spinner1.getSelectedItemPosition()).resizePlayer(tempSizeFactor);
			Toast.makeText( context , 
					"SizeFactor: " + tempSizeFactor + "%",
					Toast.LENGTH_SHORT).show();
		}
	}
	public class replaySpeedOnSeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (seekBar == sbReplaySpeed){
				if (progress == 0){
					tempReplaySpeed = progress + 1;
				}else{
					tempReplaySpeed = progress;
				}

			}		
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			activity.getGameView().getRecorder().setReplaySpeed(tempReplaySpeed);
			Toast.makeText( context , 
					"Replay Speed is now " + tempReplaySpeed + " times of the default",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public class overlapOnSeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (seekBar == sbOverlap){
					tempOverlap = progress;
			}		
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			activity.getGameView().setOverlap(tempOverlap);
			Toast.makeText( context , 
					"You're allowing players to overlap by" + tempOverlap + "%",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void initializeSeekBars() {
		sbReplaySpeed.setProgress((int)(activity.getGameView().getRecorder().getReplaySpeed()));
		sbOverlap.setProgress((int)(activity.getGameView().getplayerSizeModifier()));
		spinner3.setSelection(activity.getGameView().getCourtType());
	}
}
