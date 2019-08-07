/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will implement the game activity of the Bug
 * Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
//import android.util.Log;
import android.view.WindowManager;

public class GameActivity extends Activity {
    GameView v;

    @Override
    protected void onCreate(Bundle inBundle) {
        super.onCreate(inBundle);
        if (Assets.state != Assets.GameState.GameOver) {
            // Make full screen
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Start the view
            v = new GameView(this);
            setContentView(v);
            //Retrieve high score from the shared preference
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Assets.highScore = prefs.getInt("key_highScore", 0);
            Assets.backPressed = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.i("ProjectLogging", "GameActivity: onPause() called");
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean m_enabled = prefs.getBoolean("key_music_enabled", true);
        if (m_enabled) {
            if (Assets.mp != null) {
                Assets.mp.pause();
                Assets.mp.release();
                Assets.mp = null;
            }
        }
        // Game is finishing when pausing activity or game state is at game over
        if (isFinishing() || (Assets.state == Assets.GameState.GameOver)) {
            if (v != null)
                v.pause();
            // Resets variables from Assets.java
            Assets.gamePaused = false;
            Assets.returnedPaused = false;
            if (Assets.state == Assets.GameState.GameOver)
                Assets.state = null;
        }
        // Game is paused when pausing activity
        else if (Assets.gamePaused){
            Assets.returnedPaused = true;
            if (!Assets.backPressed) {
                Assets.gamePaused = false;
            }
        }
        // Game is not paused when activity is pausing
        else {
            Assets.returnedPaused = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("ProjectLogging", "GameActivity: onResume: State = "+Assets.state);
        if (Assets.state == Assets.GameState.GameOver || Assets.state == null)
            finish();
        else {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean m_enabled = prefs.getBoolean("key_music_enabled", true);
            if (m_enabled) {
                Assets.mp = MediaPlayer.create(this, R.raw.farty_mcsty);
                Assets.mp.setLooping(true);
                Assets.mp.start();
            }
            if ((Assets.bug != null) && !Assets.returnedPaused && Assets.backPressed) {
                //Log.i("ProjectLogging", "GameActivity: Setting gamePaused to true");
                Assets.gamePaused = true;
            }
            v.resume();
        }
    }

    @Override
    public void onBackPressed() {
        if (Assets.state == Assets.GameState.GameOver) {
            finish();
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Quitting Game")
                    .setMessage("Return to title screen?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Assets.gamePaused) {
                                Assets.gamePaused = false;
                            }
                            Assets.backPressed = true;
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
