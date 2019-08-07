/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will implement the view of the screen while
 * playing the Bug Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    Context context;
    private GameThread t = null;

    // Constructor
    @SuppressWarnings("deprecation")
    public GameView (Context context) {
        super(context);
        // Save context
        this.context = context;
        // Retrieve the SurfaceHolder instance associated with this SurfaceView.
        holder = getHolder();
        // Initialize variables
        Assets.state = Assets.GameState.GettingReady;
        Assets.numLives = 3;
        Assets.score = 0;
        // Load the sound effects if enabled
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Assets.sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        } else {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            Assets.sp = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
        }
        Assets.s_getReady = Assets.sp.load(context, R.raw.get_ready, 1);
        Assets.s_gameOver = Assets.sp.load(context, R.raw.game_over, 1);
        Assets.s_pause = Assets.sp.load(context, R.raw.pause, 1);
        Assets.s_squish1 = Assets.sp.load(context, R.raw.squish_1, 1);
        Assets.s_squish2 = Assets.sp.load(context, R.raw.squish_2, 1);
        Assets.s_squish3 = Assets.sp.load(context, R.raw.squish_3, 1);
        Assets.s_thud = Assets.sp.load(context, R.raw.thud, 1);
        Assets.s_munch = Assets.sp.load(context, R.raw.munch, 1);
        Assets.s_superHit = Assets.sp.load(context, R.raw.super_bug_grunt,1);
        Assets.s_superKilled = Assets.sp.load(context, R.raw.super_bug_killed,1);
        Assets.s_newHighScore = Assets.sp.load(context, R.raw.new_high_score, 1);

        // Specify this class as the class that implements the three callback methods required by SurfaceHolder.Callback
        holder.addCallback(this);
    }

    // waits on the thread to stop executing before continuing
    public void pause() {
        if (t != null) {
            t.setRunning(false);
            while (true) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            t = null;
        }
    }

    public void resume() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x, y;
        int action = event.getAction();
        x = event.getX();
        y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            if (t != null) {
                t.setXY((int) x, (int) y);
            }
        }
        return true; // indicates that we have handled this event
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.i("ProjectLogging", "GameView: Surface Created: Assets.state = "+Assets.state);
        // Create and start a drawing thread whose Runnable object is defined by this class
        if (t == null) {
            t = new GameThread(holder, context);
            t.setRunning(true);
            t.start();
            setFocusable(true); // make sure we get events
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Log.i("ProjectLogging", "GameView: Surface Changed");
        if (Assets.bug != null) {
            for (int i = 0; i < Assets.bug.length; i++)
                Assets.bug[i].resume();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Log.i("ProjectLogging", "GameView: Surface Destroyed: Assets.state = "+Assets.state);
        if (Assets.state != null) {
            holder.lockCanvas();
            // Clear some assets only if the back button was pressed
            if (Assets.backPressed) {
                Assets.sp = null;
                if (Assets.bug != null)
                    Assets.bug = null;
            }
            else {
                Assets.gamePaused = false;
                if (Assets.bug != null)
                    for (int i = 0; i < Assets.bug.length; i++)
                        Assets.bug[i].pause();
            }
        }
    }
}
