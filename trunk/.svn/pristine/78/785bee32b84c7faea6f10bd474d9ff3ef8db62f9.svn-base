package com.example.coachingtab;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.firebase.client.*;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

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

	private static final Handler handler = new Handler();

	private final Runnable action = new Runnable() {
		@Override
		public void run() {
			Log.d("Play Bind", "Runnable");
			if (dataSnapshot != null) {
				try {
					Gson gson = new Gson();
					Play JsonPlay = gson.fromJson(dataSnapshot.getValue().toString(), Play.class);
					FileOutputStream outputStream = openFileOutput(dataSnapshot.getName(), Context.MODE_PRIVATE);
					ObjectOutputStream out_play = new ObjectOutputStream(outputStream);
					out_play.writeObject(JsonPlay);
					out_play.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				FileOutputStream fileOut = null;
				try {
					fileOut = openFileOutput("catalog", CoachingTab.MODE_PRIVATE);
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
				}
			}
		}
	};

	private Firebase listRef;
	private ArrayList<String> catalog;
	private DataSnapshot dataSnapshot = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Play Bind", "Bind");
		if (intent.hasExtra("catalog")) {
			catalog = intent.getStringArrayListExtra("catalog");
		}
		Log.d("Play Bind", "Create");
		if (catalog != null) {
			super.onCreate();

			SharedPreferences settings = getSharedPreferences(CoachingTab.PREFS_NAME, CoachingTab.MODE_PRIVATE);
			String id = settings.getString("id", "").replace("@", "").replace(".", "");
			listRef = new Firebase("https://esc472sportstab.firebaseio.com/users/" + id + "/plays");
			listRef.addChildEventListener(new ChildEventListener() {
				@Override
				public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
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

							Context context = getApplicationContext();
							NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle("New Play: " + snapshot.getName()).setContentText("Downloading New Play!");
							Intent notificationIntent = new Intent(context, CoachingTab.class);
							PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
							mBuilder.setContentIntent(contentIntent);
							NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							mNotificationManager.notify(1, mBuilder.build());
							dataSnapshot = snapshot;
							handler.post(action);
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
