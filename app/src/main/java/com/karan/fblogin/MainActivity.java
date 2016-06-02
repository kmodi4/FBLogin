package com.karan.fblogin;



import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    LoginButton loginButton;
    private CallbackManager callbackManager;
    TextView tv;
    AccessTokenTracker accessTokenTracker;
    ProfileTracker profileTracker;
    ProfilePictureView profilePictureView;
    ImageView iv;
    Profile newProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);

        tv= (TextView) findViewById(R.id.textview);
        iv = (ImageView) findViewById(R.id.imageView);
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilepic);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        //loginButton.setReadPermissions("user_friends");

        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldprofile, Profile newprofile) {
                newProfile = newprofile;
               getProfileData(newprofile);

            }
        };
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                Profile profile = Profile.getCurrentProfile();
                if (profile!=null)
                 tv.setText("Welcome "+profile.getName());

                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("JsonResp:", object.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday"); // 01/31/1980 format
                                    Toast.makeText(getApplicationContext(),email+"\n"+birthday,Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,birthday");
                request.setParameters(parameters);
                request.executeAsync();


               // final FBUser fbUser = new FBUser();
              /*  GraphRequestAsyncTask request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                        fbUser.setEmail(user.optString("email"));
                        fbUser.setName(user.optString("name"));
                        fbUser.setId(user.optString("id"));
                    }
                }).executeAsync();*/
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e("Err:",exception.getCause().toString());
            }
        });
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

    }

    public void getProfileData(Profile newprofile){
        if(newprofile!=null) {
            tv.setText("Welcome " + newprofile.getName());
            //Toast.makeText(getApplicationContext(), newprofile.getLinkUri().toString(), Toast.LENGTH_LONG).show();
            //String url = "https://graph.facebook.com/" + newprofile.getId()+ "/picture?type=small";
            String url = newprofile.getProfilePictureUri(100,100).toString();
            Log.e("Url:",url);
            Glide.with(getApplicationContext()).load(url).crossFade().into(iv);
            profilePictureView.setProfileId(newprofile.getId());
        }
        else {
            iv.setImageDrawable(null);
            profilePictureView.setProfileId(null);
            tv.setText("Welcome to FB");
        }

    }

    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // getProfileData(newProfile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
        disconnectFromFacebook();
    }
}
