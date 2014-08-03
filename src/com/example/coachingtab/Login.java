package com.example.coachingtab;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
		
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uname = settings.getString("id", "");
        String pw = settings.getString("pw", "");
		
        // If the username is set, i.e. they were logged in before
        if (!uname.equals("")) {
        	// If they are not authenticated in the app
        	if (HttpSession.getInstance().isCurrentlyLoggedIn() == false){
            	// Log the user in again
            	HttpCommunication httpcomm = new HttpCommunication();
				String authenticated = "";
				try {
					authenticated = httpcomm.execute("LOGIN",uname,pw).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				//if (authenticated.equals("Success")) {
					HttpSession.getInstance().setCurrentlyLoggedIn(true);
				//}
        	}
            startGameView();
        } else {
        	// Otherwise, they were not logged in before... 
        	// but maybe they are authenticated on FireBase?
        	// For this part I'm not sure what's happening, or why we added this FireBase
        	// Maybe we should do this in Django instead?
            authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
                public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    String uname = settings.getString("id", "");
                    String pw = settings.getString("pw", "");
                    if (error != null) {
                    } else if (user == null) {
                    } else {
                    	if (HttpSession.getInstance().isCurrentlyLoggedIn() == false){
                        	// Log the user in again
                        	HttpCommunication httpcomm = new HttpCommunication();
            				String authenticated = "";
            				try {
            					authenticated = httpcomm.execute("LOGIN",uname,pw).get();
            				} catch (InterruptedException e) {
            					e.printStackTrace();
            				} catch (ExecutionException e) {
            					e.printStackTrace();
            				}
            				if (authenticated.equals("Success")) {
            					HttpSession.getInstance().setCurrentlyLoggedIn(true);
            				}
                    	}
                        saveUsername(user.getEmail(), pw);
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

    public void saveUsername(String uname, String pw) {
        // Save username
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("id", uname);
        editor.putString("pw", pw);
        editor.commit();
    }

    public String getUsername(){
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String id = settings.getString("id", "");
        return id;
    }

    public void startGameView() {
        // Go to game view
        setResult(RESULT_OK); // Activity exiting OK
        Intent intent = new Intent(this, CoachingTab.class);
        intent.putExtra("id", this.getUsername());
        startActivity(intent);
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    
    @Override
    public void onClick(View v) {

        final String uname = et_username.getText().toString().trim();
        final String pw = et_password.getText().toString();
        tv_error.setText("");

        // PW must be 6 or more chars and uname > 0
        if (uname.length() < 1) {
            tv_error.setText("Enter a username!");
            return;
        }
        if (!isEmailValid(uname)) {
            tv_error.setText("Invalid email");
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
            	
				// Login w/ Django
            	HttpCommunication httpcomm = new HttpCommunication();
				String authenticated = "";
				try {
					authenticated = httpcomm.execute("LOGIN",uname,pw).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				if (authenticated.equals("Success")) {
					// Login on FireBase too. If BOTH succeed... which they should...
					// then overall the login is a success
	                authClient.loginWithEmail(uname, pw, new SimpleLoginAuthenticatedHandler() {
	                    public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
	                        if (error != null) {
	                            tv_error.setText("Invalid email or password.");
	                        	b_login.setClickable(true);
	                        } else {
	        					// Log this user in
	        					HttpSession.getInstance().setCurrentlyLoggedIn(true);
	        					// Start game view
	                            saveUsername(uname, pw);
	                            startGameView();
	                        }
	                    }
	                });
				} else {
					tv_error.setText("Invalid email or password.");
	            	b_login.setClickable(false);
				}

                break;

            case R.id.id_signup_button:
            	
            	b_signup.setClickable(false);
            	
            	HttpCommunication httpcomm2 = new HttpCommunication();
				String uname_check_httpresponse = "";
				try {
					uname_check_httpresponse = httpcomm2.execute("CHECKUSERNAMEAVAIL",uname).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
				// If the username is valid
				if (uname_check_httpresponse.equals("OKAY")){
					
					AlertDialog.Builder alert = new AlertDialog.Builder(this);

					alert.setTitle("Confirm Password");
					alert.setMessage("Please enter your name, and re-type your password");

					// Set an EditText view to get user input
					LinearLayout layout = new LinearLayout(this);
					layout.setOrientation(LinearLayout.VERTICAL);

					final EditText et_first_name = new EditText(this);
					et_first_name.setHint("First Name");
					final EditText et_last_name  = new EditText(this);
					et_last_name.setHint("Last Name");
					final EditText et_password_retype = new EditText(this);
					et_password_retype.setHint("Password");
					et_password_retype.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

					layout.addView(et_first_name);
					layout.addView(et_last_name);
					layout.addView(et_password_retype);

					alert.setView(layout);

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							
							// Make sure passwords match
							String pw_retyped = et_password_retype.getText().toString();
							if (!pw_retyped.equals(pw)) {
								tv_error.setText("Your retyped password did not match.");
				            	b_signup.setClickable(true);
								return;
							}
							
							String firstn = et_first_name.getText().toString();
							String lastn  = et_last_name.getText().toString();
							HttpCommunication httpcomm = new HttpCommunication();
							String signup_httpresponse = "";
							try {
								signup_httpresponse = httpcomm.execute("SIGNUP",uname,pw,firstn,lastn).get();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
							if (signup_httpresponse.equals("Success")){
								// Now also login with firebase
								// If both succeed, start the gameview
				                authClient.createUser(uname, pw, new SimpleLoginAuthenticatedHandler() {
				                    public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
				                        if (error != null) {
				                            tv_error.setText(error.toString());
				                        	b_signup.setClickable(true);
				                        } else {
											HttpSession.getInstance().setCurrentlyLoggedIn(true);
				                            saveUsername(uname, pw);
				                            startGameView();
				                        }
				                    }
				                });
							} else {
								tv_error.setText("Problem signing up -- try again soon.");
				            	b_signup.setClickable(true);
							}
						}
					});

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
			            	b_signup.setClickable(true);
						}
					});
					final AlertDialog alert_dialog = alert.create();
					et_password_retype.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					    @Override
					    public void onFocusChange(View v, boolean hasFocus) {
					        if (hasFocus) {
					        	alert_dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					        }
					    }
					});
					alert_dialog.show();
				} else if (uname_check_httpresponse.equals("USERNAME_EXISTS")) {
					tv_error.setText("Username \"" + uname + "\" is already in use.");
	            	b_signup.setClickable(true);
				} else {
					tv_error.setText("Problem signing up -- try again soon.");
	            	b_signup.setClickable(true);
				}
        }
    }
}
