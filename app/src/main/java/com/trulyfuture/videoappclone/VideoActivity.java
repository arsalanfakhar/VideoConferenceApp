package com.trulyfuture.videoappclone;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.trulyfuture.videoappclone.databinding.ActivityVideoCallBinding;

import org.json.JSONException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoActivity extends AppCompatActivity implements Session.SessionListener{
    private static String API_KEY = "";
    private static String SESSION_ID = "";
    private static String TOKEN = "";
    private static final int RC_VIDEO_APP_PERM = 124;

    //Maximum clients
    private long maxClients=0;
    //Volley
    private RequestQueue mRequestQue;

    //Firebase
    private DatabaseReference reff;

    private Session mSession;

    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private static final String TAG = "VideoActivity";
    private ActivityVideoCallBinding binding;

    private VideoSession mVideoSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupViews();
    }

    private void setupViews(){
        mRequestQue = Volley.newRequestQueue(this);

        reff= FirebaseDatabase.getInstance().getReference().child("Users");
        requestPermissions();

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    maxClients = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mVideoSession=new VideoSession();

        //Cancel call
        binding.cancelBtn.setOnClickListener(v -> {
            mSession.disconnect();
            reff.removeValue();
            Intent intent = new Intent(VideoActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }


    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initialize and connect to the session
            fetchSessionConnectionData();

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void fetchSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://opentokvideoapp.herokuapp.com" + "/session",
                null, response -> {
                    try {

                        //Get session from heroku and start it

                        API_KEY = response.getString("apiKey");
                        SESSION_ID = response.getString("sessionId");
                        TOKEN = response.getString("token");

                        mVideoSession.setAPI_KEY(API_KEY);
                        mVideoSession.setSESSION_ID(SESSION_ID);
                        mVideoSession.setTOKEN(TOKEN);

                        // Session Created
                        mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
                        mSession.setSessionListener(this);
                        mSession.connect(TOKEN);

    //                    else
    //                    {
    //
    //                        Toast.makeText(getApplicationContext(), "User is busy", Toast.LENGTH_SHORT).show();
    //                        Intent intent = new Intent(Main2Activity.this, MainActivity.class);
    //                        startActivity(intent);
    //                    }
                    } catch(JSONException error){
                        Log.e(TAG, "Web Service error: " + error.getMessage());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Web Service error: " + error.getMessage());
            }
        }));

    }


    //Open tok session listener methods
    @Override
    public void onConnected(Session session) {

//        mPublisher = new Publisher.Builder(this).build();
//        mPublisher.setPublisherListener(this);
//        binding.publisherContainer.addView(mPublisher.getView());
//
//
//        if (mPublisher.getView() instanceof GLSurfaceView){
//            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
//        }
//        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TAG, "Stream Received");

        if(mSubscriber==null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            binding.subscriberContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            binding.subscriberContainer.removeAllViews();

        }
        Intent intent = new Intent(VideoActivity.this, MainActivity.class);
        startActivity(intent);

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TAG, "Session error: " + opentokError.getMessage());
    }

    //Publisher listener methods
//    @Override
//    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
//        Log.i(TAG, "Publisher onStreamCreated");
//        mVideoSession.setStream(stream.getCreationTime());
//        reff.child(String.valueOf(maxClients+1)).setValue(mVideoSession);
//
//    }
//
//    @Override
//    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
//        Log.i(TAG, "Publisher onStreamDestroyed");
//        mSession.unpublish(mPublisher);
//
//        Intent intent = new Intent(VideoActivity.this, MainActivity.class);
//        startActivity(intent);
//    }
//
//    @Override
//    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
//        Log.e(TAG, "Publisher error: " + opentokError.getMessage());
//    }
}
