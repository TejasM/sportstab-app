package com.example.coachingtab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;

public class Login extends Activity implements View.OnClickListener {

    // Widgets
    Button b_login;
    Button b_signup;
    EditText et_username;
    EditText et_password;
    TextView tv_error;
    Firebase ref = new Firebase("https://esc472sportstab.firebaseio.com/users");
    SimpleLogin authClient = new SimpleLogin(ref);

    // Write to prefs
    public static final String PREFS_NAME = "myPref";


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (getUsername()) {
            startGameView();
        } else {
            authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
                public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
                    if (error != null) {
                    } else if (user == null) {
                    } else {
                        saveUsername(user.getEmail());
                        startGameView();
                    }
                }
            });
            //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_login);

            // Form widgets
            b_login = (Button) findViewById(R.id.id_login_button);
            b_login.setOnClickListener(this);
            b_signup = (Button) findViewById(R.id.id_signup_button);
            b_signup.setOnClickListener(this);
            et_username = (EditText) findViewById(R.id.id_username);
            et_password = (EditText) findViewById(R.id.id_password);
            tv_error = (TextView) findViewById(R.id.id_error);
            if (!isNetworkAvailable()) {
                tv_error.setText("Need network connection");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_coaching_tab, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void saveUsername(String uname) {
        // Save username
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("id", uname);
        editor.commit();
    }

    public boolean getUsername() {
        // Save username
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String id = settings.getString("id", "");
        return !id.equals("");
    }

    public void startGameView() {
        // Go to game view
        setResult(RESULT_OK); // Activity exiting OK
        Intent intent = new Intent(this, CoachingTab.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {

        final String uname = et_username.getText().toString().trim();
        String pw = et_password.getText().toString();
        tv_error.setText("");

        // PW must be 6 or more chars and uname > 0
        if (uname.length() < 1) {
            tv_error.setText("Enter a username!");
            return;
        }
        if (pw.length() < 6) {
            tv_error.setText("Password must be at least 6 characters");
            return;
        }
        // Get Firebase user reference

        switch (v.getId()) {

            case R.id.id_login_button:
            	
            	b_login.setClickable(false);
            	
                authClient.loginWithEmail(uname, pw, new SimpleLoginAuthenticatedHandler() {
                    public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
                        if (error != null) {
                            tv_error.setText("Invalid email or password.");
                        } else {
                            saveUsername(uname);
                            startGameView();
                        }
                    }
                });

                break;

            case R.id.id_signup_button:
            	
            	b_signup.setClickable(false);
            	
                authClient.createUser(uname, pw, new SimpleLoginAuthenticatedHandler() {
                    public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
                        if (error != null) {
                            tv_error.setText(error.toString());
                        } else {
                            saveUsername(uname);
                            startGameView();
                        }
                    }
                });
        }
    }
}
