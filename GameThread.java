/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will implement the functions of the game thread
 * that will be run during gameplay of the Bug Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
//import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class GameThread extends Thread {
    private SurfaceHolder holder;
    private Handler handler;                // for running code in the UI thread
    private boolean isRunning = false;
    Context context;
    Paint paint;
    int touchX, touchY;                     // x,y of touch event
    boolean touched;                        // true if touch happened
    boolean data_initialized;
    private int pauseCoords[] = new int[2]; // position of the pause button in the screen
    private final int MAX_NUM_BUGS = 10;    // max number of bug that can appear on the screen at a time
    private String message;                 // message that displays at the game over screen
    private boolean messageFinish;          // ensures that the high score message does not repeat over again
    private static final Object lock = new Object();

    public GameThread (SurfaceHolder surfaceHolder, Context context) {
        holder = surfaceHolder;
        this.context = context;
        handler = new Handler();
        data_initialized = false;
        touched = false;
        message = "Score:";
        messageFinish = false;
    }

    public Thread setRunning(boolean b) {
        isRunning = b;
        return null;
    }

    // Set the touch event x,y location and flag indicating a touch has happened
    public void setXY (int x, int y) {
        synchronized (lock) {
            touchX = x;
            touchY = y;
            this.touched = true;
        }
    }

    @Override
    public void run() {
        //Log.i("ProjectLogging", "GameThread: run() start: Assets.state = "+Assets.state);
        while (isRunning) {
            // Lock the canvas before drawing
            Canvas canvas = holder.lockCanvas();
            if (canvas != null && !Assets.gamePaused) {
                // Perform drawing operations on the canvas
                render(canvas);
                // After drawing, unlock the canvas and display it
                holder.unlockCanvasAndPost(canvas);
            }
            else if (Assets.gamePaused) {
                //Log.i("ProjectLogging", "GameThread: run(): Game is paused");
                // Pauses all bugs
                for (int i = 0; i < MAX_NUM_BUGS; i++)
                    Assets.bug[i].pause();
                // Only processTouch until game is resumed
                while (Assets.gamePaused)
                    processTouch(canvas);
                // Resumes all bugs
                for (int i = 0; i < MAX_NUM_BUGS; i++)
                    Assets.bug[i].resume();
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // Loads graphics used in the game
    private void loadData (Canvas canvas) {
        Bitmap bmp;
        int newWidth, newHeight;
        float scaleFactor;
        double sizeMultiplier = 1.5; // Multiplier used for the size of a super bug

        // Create new paint object for the score text that has a set text size
        paint = new Paint();
        paint.setColor(Color.rgb(255, 150, 0)); // orange
        paint.setTextSize(40);

        // Load bug1
        bmp = BitmapFactory.decodeResource (context.getResources(), R.drawable.bug_l);
        // Compute size bitmap needed
        newWidth = (int)(canvas.getWidth() * 0.2f);
        // What was the scaling factor to get this?
        scaleFactor = (float)newWidth / bmp.getWidth();
        // Compute the new height
        newHeight = (int)(bmp.getHeight() * scaleFactor);
        // Scale it to a new size
        Assets.bugL = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);
        // Scales a super size version of bugL
        Assets.suprBugL = Bitmap.createScaledBitmap(bmp, (int)(newWidth * sizeMultiplier), (int)(newHeight * sizeMultiplier), false);

        // Load bug2
        bmp = BitmapFactory.decodeResource (context.getResources(), R.drawable.bug_r);
        // Compute size bitmap needed
        newWidth = (int)(canvas.getWidth() * 0.2f);
        // What was the scaling factor to get this?
        scaleFactor = (float)newWidth / bmp.getWidth();
        // Compute the new height
        newHeight = (int)(bmp.getHeight() * scaleFactor);
        // Scale it to a new size
        Assets.bugR = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);
        // Scales a super size version of bugR
        Assets.suprBugR= Bitmap.createScaledBitmap(bmp, (int)(newWidth * sizeMultiplier), (int)(newHeight * sizeMultiplier), false);

        // Load dead bug
        bmp = BitmapFactory.decodeResource (context.getResources(), R.drawable.bug_d);
        // Compute size bitmap needed
        newWidth = (int)(canvas.getWidth() * 0.2f);
        // What was the scaling factor to get this?
        scaleFactor = (float)newWidth / bmp.getWidth();
        // Compute the new height
        newHeight = (int)(bmp.getHeight() * scaleFactor);
        // Scale it to a new size
        Assets.bugD = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);
        // Scales a super size version of bugL
        Assets.suprBugD = Bitmap.createScaledBitmap(bmp, (int)(newWidth * sizeMultiplier), (int)(newHeight * sizeMultiplier), false);

        // Load score bar
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.score_bar);
        // score bar should be as long as the screen
        newWidth = canvas.getWidth();
        // height of the score bar 1/12 of the canvas height
        newHeight = canvas.getHeight()/12;
        // Scale the bmp to a new size
        Assets.scoreBar = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);

        // Load life icon
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.life_icon);
        // Compute size bitmap needed
        // Let icons be 3% of width of screen
        newWidth = (int)(bmp.getWidth() * 0.3f);
        // What was the scaling factor to get this?
        scaleFactor = (float)newWidth / bmp.getWidth();
        // Compute the new height
        newHeight = (int)(bmp.getHeight() * scaleFactor);
        // Scale it to a new size
        Assets.lifeIcon = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);

        // Load pause icon
        bmp = BitmapFactory.decodeResource (context.getResources(), R.drawable.pause_icon);
        // Scales it to the same size as the life icon
        Assets.pauseIcon = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);

        // Load play icon
        bmp = BitmapFactory.decodeResource (context.getResources(), R.drawable.play_icon);
        // Scales it to the same size as the pause icon
        Assets.playIcon = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);

        // Create a bug
        Assets.superBugIndex = 0; // bug[0] will be the only bug that will have a chance to become a super bug
        Assets.bug = new Bug[MAX_NUM_BUGS]; // initializes the bug array with length indicated by MAX_NUM_BUGS
        for (int i = 0; i < MAX_NUM_BUGS; i++) {
            Assets.bug[i] = new Bug();
        }
    }

    // Load background screen
    private void loadBackground (Canvas canvas, int resId) {
        // Load Background
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resId);
        // Scale it to fit the entire canvas
        Assets.background = Bitmap.createScaledBitmap(bmp, canvas.getWidth(), canvas.getHeight(), false);
    }

    private void render (Canvas canvas) {
        int i, x, y;

        if (!data_initialized) {
            loadData(canvas);
            data_initialized = true;
        }

        switch (Assets.state) {
            case GettingReady:
                loadBackground(canvas, R.drawable.background_image_bugmasher);
                // Draw background screen
                canvas.drawBitmap(Assets.background, 0, 0, null);
                // Play sound effect
                Assets.sp.play(Assets.s_getReady, 1, 1, 1, 0, 1);
                // Start a timer
                Assets.gameTimer = System.nanoTime() / 1000000000f;
                // Goes to next state
                Assets.state = Assets.GameState.Starting;
                break;
            case Starting:
                // Draw background screen
                canvas.drawBitmap(Assets.background, 0, 0, null);
                // Has 3 seconds elapsed?
                float currentTime = System.nanoTime() / 1000000000f;
                if (currentTime - Assets.gameTimer >= 3) {
                    // Goes to next state
                    Assets.state = Assets.GameState.Running;
                }
                break;
            case Running:
                // Are there no lives left?
                if (Assets.numLives == 0) {
                    // Goes to next state
                    Assets.state = Assets.GameState.GameEnding;
                }

                // Draw background screen
                canvas.drawBitmap(Assets.background, 0, 0, null);

                // Runs the following bug methods for all bugs
                for (i = 0; i < MAX_NUM_BUGS; i++) {
                    // Sets the super spawn timer to -1, meaning "disable", if the current bug is not the
                    // specified bug who can become a super bug
                    if (i != Assets.superBugIndex) {
                        Assets.bug[i].spawnSuperTimer = -1;
                    }
                    // Draw dead bug
                    Assets.bug[i].drawDead(canvas);
                    // Move bugs on screen
                    Assets.bug[i].move(canvas);
                    // Respawn a new bug?
                    Assets.bug[i].respawn(canvas);
                }

                // Draw score bar (on top of the bugs that just respawned)
                canvas.drawBitmap(Assets.scoreBar, 0, 0, null);
                // Draw the pause button icon
                int spacing = 8; // spacing between the icons
                x = (canvas.getWidth() / 2) - (Assets.pauseIcon.getWidth() / 2);
                y = spacing;
                pauseCoords[0] = x;
                pauseCoords[1] = y;
                canvas.drawBitmap(Assets.pauseIcon, x, y, null);
                // Draw one life icon for each life at top right corner of screen
                x = canvas.getWidth() - Assets.lifeIcon.getWidth() - spacing - 20; // coordinates for right most icon to draw
                for (i = 0; i < Assets.numLives; i++) {
                    canvas.drawBitmap(Assets.lifeIcon, x, y, null);
                    // reposition to draw the next icon to the left
                    x -= (Assets.lifeIcon.getWidth() + spacing);
                }
                // Draw the score text that will display the current score in the game at the top-left of the screen
                x = spacing * 3;
                y = Assets.scoreBar.getHeight() / 2;
                canvas.drawText(Integer.toString(Assets.score), x, y, paint);

                // Process a touch
                processTouch(canvas);

                break;
            case GameEnding:
                // Load Game Over screen
                loadBackground(canvas, R.drawable.game_over_bugmasher);
                // Display Game Over screen
                canvas.drawBitmap(Assets.background, 0, 0, null);
                // Display the final score text
                paint.setTextSize(50);
                paint.setTextAlign(Paint.Align.CENTER);
                x = (canvas.getWidth() / 2);
                y = (canvas.getHeight() / 2);
                canvas.drawText(message, x, y, paint);
                paint.setTextSize(100);
                y += (canvas.getHeight() / 8);
                canvas.drawText(Integer.toString(Assets.score), x, y, paint);
                // Plays a sound effect
                Assets.sp.play(Assets.s_gameOver, 1, 1, 1, 0, 1);
                // Clears the bug object
                Assets.bug = null;
                // Goes to next state
                Assets.state = Assets.GameState.GameOver;
                break;
            case GameOver:
                // Display Game Over screen
                canvas.drawBitmap(Assets.background, 0, 0, null);
                // Display the final score text
                paint.setTextSize(50);
                paint.setTextAlign(Paint.Align.CENTER);
                x = (canvas.getWidth() / 2);
                y = (canvas.getHeight() / 2);
                canvas.drawText(message, x, y, paint);
                paint.setTextSize(100);
                y += (canvas.getHeight() / 8);
                canvas.drawText(Integer.toString(Assets.score), x, y, paint);
                // Checks if the current score is the new high score
                if (Assets.score > Assets.highScore && !messageFinish) {
                    messageFinish = true; // ensures that this section does not loop forever
                    message = "New High Score:";
                    // Sets a color for the final score text in the game over screen if it is a new high score
                    paint.setColor(Color.rgb(255,223,0)); // golden yellow
                    // Write out high score to shared preferences
                    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("key_highScore", Assets.score);
                    editor.commit();
                    // Wait 1.5 seconds and display high score dialog
                    Assets.gameTimer = System.nanoTime() / 1000000000f;
                    while (true) {
                        currentTime = System.nanoTime() / 1000000000f;
                        //Log.i("ProjectLogging", "GameThread: In loop: "+(currentTime - Assets.gameTimer));
                        if (currentTime - Assets.gameTimer >= 1.5) {
                            // Show a new high score message
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(context, "New High Score!!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            // Plays a sound effect saying "new high score"
                            Assets.sp.play(Assets.s_newHighScore, 1, 1, 1, 0, 1);
                            break;
                        }
                    }
                }
                break;
        }
    }

    // Method that processes a touch to see if it touched a bug or pressed the pause button
    private void processTouch(Canvas canvas) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean m_enabled = prefs.getBoolean("key_music_enabled", true);
        if (touched) {
            // Set touch flag to false since we are processing this touch now
            touched = false;
            // See if this touch pressed the pause button
            if (pauseTouched()) {
                //Log.i("ProjectLogging", "GameThread: Paused button pressed!");
                // Plays a sound effect for pressing the pause button
                Assets.sp.play(Assets.s_pause, 1, 1,1,0,1);
                // Display "PAUSED" message at the center of the screen
                int x = (canvas.getWidth() / 2);
                int y = (canvas.getHeight() / 2);
                Paint pauseTxt = new Paint();
                pauseTxt.setTextSize(75);
                pauseTxt.setColor(Color.rgb(75, 0, 130));
                pauseTxt.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("PAUSED", x, y, pauseTxt);
                // Draws the play icon over the pause icon
                canvas.drawBitmap(Assets.playIcon, pauseCoords[0], pauseCoords[1], null);
                if (!Assets.gamePaused) {
                    Assets.gamePaused = true;
                    // Pause the background music if enabled
                    if (m_enabled) {
                        if (Assets.mp != null) {
                            Assets.mp.pause();
                            Assets.mp.release();
                            Assets.mp = null;
                        }
                    }
                }
                else {
                    // Continue playing the background music if enabled
                    if (m_enabled) {
                        Assets.mp = MediaPlayer.create(context, R.raw.farty_mcsty);
                        Assets.mp.setLooping(true);
                        Assets.mp.start();
                    }
                    Assets.gamePaused = false;
                }
            }
            // See if this touch killed a bug only if the game is not paused
            else if (!Assets.gamePaused) {
                // only calculates touch if it is not in the area of the score bar
                if (!((touchX >= 0) && ((touchY >= 0) && (Assets.scoreBar.getHeight() >= touchY)))) {
                    boolean bugHit = false;
                    for (int i = 0; i < MAX_NUM_BUGS; i++) {
                        int bugKilled = Assets.bug[i].touched(canvas, touchX, touchY);
                        // Bug is killed
                        if (bugKilled == 1) {
                            bugHit = true;
                            // Adds 10 points to the score if a super bug is touched 4 times
                            if (Assets.bug[i].isSuper) {
                                Assets.score += 10;
                                // Plays a special sound effect for killing a super bug
                                Assets.sp.play(Assets.s_superKilled, 1, 1, 1, 0, 1);
                            }
                            // Adds 1 point to the score otherwise
                            else {
                                Assets.score++;
                                // Plays random squish sound
                                int s_squishGen = (int) ((Math.random() * 100) % 3);
                                switch (s_squishGen) {
                                    case 0:
                                        Assets.sp.play(Assets.s_squish1, 1, 1, 1, 0, 1);
                                        break;
                                    case 1:
                                        Assets.sp.play(Assets.s_squish2, 1, 1, 1, 0, 1);
                                        break;
                                    case 2:
                                        Assets.sp.play(Assets.s_squish3, 1, 1, 1, 0, 1);
                                        break;
                                }
                            }
                        }
                        // Bug is a super bug and is only touched (not killed)
                        else if (Assets.bug[i].isSuper && bugKilled == -1) {
                            bugHit = true;
                            // Play a different sound effect for touching a super bug but not killing it
                            Assets.sp.play(Assets.s_superHit, 1, 1, 1, 0, 1);
                        }
                    }
                    // Plays the thud sound if no bug, excluding the score bar, is touched
                    if (!bugHit) {
                        Assets.sp.play(Assets.s_thud, 1, 1, 1, 0, 1);
                    }
                }
            }
        }
    }

    private boolean pauseTouched() {
        if ((touchX >= pauseCoords[0] && (pauseCoords[0] + Assets.pauseIcon.getWidth()) >= touchX) && (touchY >= pauseCoords[1] && (pauseCoords[1] + Assets.pauseIcon.getHeight()) >= touchY)) {
            return true;
        }
        return false;
    }
}
