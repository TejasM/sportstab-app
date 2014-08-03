package com.example.coachingtab;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

/**
 * Created by tmehta on 04/03/14.
 */
public class UpdatePlayBookService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Play Bind", "Bind");
		catalog = intent.getStringArrayListExtra("catalog");
		return null;
	}

	private String id;

	private static final Handler handler = new Handler();

	private final Runnable action = new Runnable() {
		@Override
		public void run() {
			Log.d("Play Bind", "Runnable");
			if (dataSnapshot != null) {/*
				try {
					Gson gson = new Gson();
					
					 Replaced firebase with django. dataSnapshot.getValue() is no
					 * longer needed, but we can keep getName() since the update from
					 * firebase will still have a name.
					Play JsonPlay = gson.fromJson(dataSnapshot.getValue().toString(), Play.class);
  					 
					Play JsonPlay = gson.fromJson(json_play, Play.class);
					FileOutputStream outputStream = openFileOutput(dataSnapshot.getName(), Context.MODE_PRIVATE);
					ObjectOutputStream out_play = new ObjectOutputStream(outputStream);
					out_play.writeObject(JsonPlay);
					out_play.close();
					Context context = getApplicationContext();
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle("New Play: " + dataSnapshot.getName()).setContentText("Finished Downloading!");
					Intent notificationIntent = new Intent(context, CoachingTab.class);
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
					mBuilder.setContentIntent(contentIntent);
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(1, mBuilder.build());
				} catch (IOException e) {
					e.printStackTrace();
				} catch(NullPointerException e){
					e.printStackTrace();
				}
				FileOutputStream fileOut = null;
				try {
					fileOut = openFileOutput("catalog" + id, CoachingTab.MODE_PRIVATE);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					if (!catalog.contains(dataSnapshot.getName())) {
						catalog.add(dataSnapshot.getName());
						out.writeObject(catalog);
					}
					out.close();
					fileOut.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
		}
	};

	private Firebase listRef;
	private ArrayList<String> catalog;
	private DataSnapshot dataSnapshot = null;
	private String json_play = "";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Play Bind", "Bind");
		if (intent.hasExtra("catalog")) {
			catalog = intent.getStringArrayListExtra("catalog");
		}
		if (intent.hasExtra("id")){
			id = intent.getStringExtra("id");
		}

		if (catalog != null) {
			Log.d("Play Bind", "Create");
			super.onCreate();

			SharedPreferences settings = getSharedPreferences(CoachingTab.PREFS_NAME, CoachingTab.MODE_PRIVATE);
			String id = settings.getString("id", "").replace("@", "").replace(".", "");
			listRef = new Firebase("https://esc472sportstab.firebaseio.com/users/" + id + "/plays");
			listRef.addChildEventListener(new ChildEventListener() {
				@Override
				public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
					Log.d("Play Bind", snapshot.getName());
					Firebase ref = new Firebase("https://esc472sportstab.firebaseio.com/plays/" + snapshot.getName());
					ref.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot snapshot) {
							String[] files = fileList();
							for (String file : files) {
								if (file.equals(snapshot.getName())) {
									return;
								}
							}

							Log.d("Play Bind Inner", snapshot.getName());
							Context context = getApplicationContext();
							NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle("New Play: " + snapshot.getName()).setContentText("Downloading New Play!");
							Intent notificationIntent = new Intent(context, CoachingTab.class);
							PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
							mBuilder.setContentIntent(contentIntent);
							NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							mNotificationManager.notify(1, mBuilder.build());
							dataSnapshot = snapshot;
							handler.post(action);
							
							// --------------------------------------------------------
							// Also download this play from Django
                        	HttpCommunication httpcomm = new HttpCommunication();
            				try {
            					// Rather than return plays/play123 or something (which could be
            					// the play json string), I am going to make a separate function which
            					// requires login, so in the future plays can be secret
            					json_play = httpcomm.execute("GETPLAY",snapshot.getName()).get();
            				} catch (InterruptedException e) {
            					e.printStackTrace();
            				} catch (ExecutionException e) {
            					e.printStackTrace();
            				}
							// --------------------------------------------------------
						}

						@Override
						public void onCancelled(FirebaseError firebaseError) {

						}

					});
				}

				@Override
				public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
				}

				@Override
				public void onChildRemoved(DataSnapshot snapshot) {
				}

				@Override
				public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
				}

				@Override
				public void onCancelled(FirebaseError firebaseError) {

				}

			});

		}
		return flags;
	}

}
