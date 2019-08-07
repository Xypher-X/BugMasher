/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will implement the Bug object that will show up
 * in the Bug Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.util.Log;

public class Bug {

    // States of a Bug
    enum BugState {
        Dead,
        Respawning,
        Alive,         // in the game
        DrawDead,      // draw dead bug on screen
    };

    BugState state;    // current state of the bug
    int x,y;           // location of bug on the screen
    double speed;      // speed of the bug (pixels per second)
    // All times are in seconds
    float timeToSpawn;     // number of seconds till spawn
    float startSpawnTimer; // starting timestamp when deciding to spawn
    float deathTime;
    float animateTimer;    // used to move and animate the bug
    float pauseTimer;     // starting timestamp when the game was paused
    boolean isSuper;       // determines if the spawned bug is a super bug
    float spawnSuperTimer; // starting timestamp when deciding to spawn a super bug
    float timeToSpawnSuper;// number of seconds till a super bug is able to spawn again
    int superTouchCount ;  // count for the number of touches made for a super bug
    boolean moveZigZag;    // determines if the super bug will move zig zag
    int xFlag;             // flag that keeps track of the first x-coordinate that the bug had spawned from
    int xLimit;            // a distance limit from xFlag, (+) and (-), that is used when moving the super bug in a zig zag pattern
    boolean moveLeft;      // determines which direction (left or right) should the super bug move it it is moving zig zag

    // Bug starts not alive
    public Bug() {
        state = BugState.Dead;
        timeToSpawnSuper = 20; // every 20 seconds, a super bug may spawn
        spawnSuperTimer = 0; // timer is restarted if this timer is 0
        superTouchCount = 0;
        xLimit = 150;
        moveLeft = true;
    }

    // Bug spawn processing
    public void respawn (Canvas canvas) {
        // Spawn a bug?
        if (state == BugState.Dead) {
            // Reset super bug spawn timer and touch count to 0 and isSuper to false if a super bug was just spawned
            if (isSuper) {
                spawnSuperTimer = 0;
                superTouchCount = 0;
                isSuper = false;
            }
            // Set it to respawning
            state = BugState.Respawning;
            // Set a random number of seconds before it comes to life
            timeToSpawn = (float) Math.random() * 5;
            // Note the current time
            startSpawnTimer = System.nanoTime() / 1000000000f;
            if (spawnSuperTimer == 0) {
                spawnSuperTimer = System.nanoTime() / 1000000000f;
            }
        }
        // Check if bug has spawned
        else if (state == BugState.Respawning) {
            float curTime = System.nanoTime() / 1000000000f;
            // Has the super bug spawn time expired?
            // Only try to spawn a super bug if spawn super timer is not -1, "disabled", and if the bug is not currently a super bug
            if ((spawnSuperTimer != -1) && (curTime - spawnSuperTimer >= timeToSpawnSuper) && isSuper == false) {
                // 5% chance that the next spawn will be a super bug
                if ((float)Math.random() <= 0.05f) {
                    isSuper = true;
                    //Log.i("ProjectLogging", "Bug: Super Bug Spawned >:D >>" +(curTime - spawnSuperTimer) );
                    if (Math.random() <= 0.5) // 50% chance the super bug will move zig zag
                        moveZigZag = true;
                    else
                        moveZigZag = false;
                }
            }
            // Has the regular spawn time expired?
            if (curTime - startSpawnTimer >= timeToSpawn) {
                // Spawn the bug
                state = BugState.Alive;
                // Set bug starting location at top of screen
                x = (int)(Math.random() * canvas.getWidth());
                xFlag = x;
                // Keep entire bug on screen
                if (x < Assets.bugL.getWidth() / 2) {
                    x = Assets.bugL.getWidth() / 2;
                    xFlag = x + xLimit;
                }
                else if (x > canvas.getWidth() - Assets.bugL.getWidth() / 2) {
                    x = canvas.getWidth() - Assets.bugL.getWidth() / 2;
                    xFlag = x - xLimit;
                }
                if ((x - xLimit) < Assets.bugL.getWidth() / 2) {
                    xFlag = (Assets.bugL.getWidth() / 2) + xLimit;
                }
                else if ((x + xLimit) >= canvas.getWidth()) {
                    xFlag = (canvas.getWidth() - Assets.bugL.getWidth() / 2) - xLimit;
                }

                y = 0;
                // Set speed of this bug
                speed = canvas.getHeight() / 4; // no faster than 1/4 a screen per second
                // subtracts a random amount off of this so some bugs are slower
                double rng = Math.random();
                while (rng >= 0.7) // so the bug will always move at a reasonable speed (not too slow)
                    rng = Math.random();
                speed -= rng * speed; // subtracts a percent of the current speed
                // Record timestamp of this bug being spawned
                animateTimer = curTime;
            }
        }
    }

    // Bug movement processing
    public void move (Canvas canvas) {
        // Make sure this bug is still alive
        if (state == BugState.Alive) {
            // Get elapsed time since last call here
            float curTime = System.nanoTime() / 1000000000f;
            float elapsedTime = curTime - animateTimer;
            animateTimer = curTime;
            // A super bug can move in a straight line or zig-zag
            if (isSuper && moveZigZag) {
                if ((x+speed*elapsedTime) > (xFlag + xLimit)) {
                    x -= (speed * elapsedTime);
                    moveLeft = true;
                }
                else if ((x-speed*elapsedTime) < (xFlag - xLimit)) {
                    x += (speed * elapsedTime);
                    moveLeft = false;
                }
                else if (moveLeft)
                    x -= (speed * elapsedTime);
                else
                    x += (speed * elapsedTime);
            }
            // Compute the amount of pixels to move (vertically down the screen)
            y += (speed * elapsedTime);
            // Draw the bug on screen
            if ((System.currentTimeMillis() / 100 % 10) % 2 == 0)
                if (isSuper) {
                    canvas.drawBitmap(Assets.suprBugL, x - Assets.bugL.getWidth() / 2, y - Assets.bugL.getHeight() / 2, null);
                }
                else {
                    canvas.drawBitmap(Assets.bugL, x - Assets.bugL.getWidth() / 2, y - Assets.bugL.getHeight() / 2, null);
                }
            else
                if (isSuper) {
                    canvas.drawBitmap(Assets.suprBugR, x - Assets.bugL.getWidth() / 2, y - Assets.bugL.getHeight() / 2, null);
                }
                else {
                    canvas.drawBitmap(Assets.bugR, x - Assets.bugR.getWidth() / 2, y - Assets.bugR.getHeight() / 2, null);
                }

            // Has it reached the bottom of the screen?
            if (y >= canvas.getHeight()) {
                // Play a sound effect
                Assets.sp.play(Assets.s_munch, 1, 1, 1, 0, 1);
                // Kill the bug
                state = BugState.Dead;
                // Subtract 1 life
                Assets.numLives--;
            }
        }
    }

    // Process touch to se if it kills a bug - return true if a bug was killed
    public int touched (Canvas canvas, int touchX, int touchY) {
        int isKilled = 0; // -1 = touched; 0 = alive; 1 = dead
        // Make sure this bug is alive
        if (state == BugState.Alive) {
            // Compute distance between touch and center of bug
            float dist = (float)(Math.sqrt ((touchX - x) * (touchX - x) + (touchY - y) * (touchY - y)));
            // Is this close enough for a kill?
            Bitmap currentBmp; // current bug bmp that is currently in the screen
            if (isSuper) {
                currentBmp = Assets.suprBugL;
            }
            else {
                currentBmp = Assets.bugL;
            }
            if (dist <= currentBmp.getWidth()*0.5f) {
                if (isSuper) {
                    superTouchCount++;
                    if (superTouchCount >= 4) {
                        state = BugState.DrawDead; // need to draw dead bug on screen for a while
                        isKilled = 1;
                        superTouchCount = 0; // resets superTouchCount
                    }
                    else {
                        isKilled = -1;
                    }
                }
                else {
                    state = BugState.DrawDead; // need to draw dead bug on screen for a while
                    isKilled = 1;
                }
                // Record time of death
                deathTime = System.nanoTime() / 1000000000f;
            }
        }
        return isKilled;
    }

    // Draw dead bug on the screen if needed
    public void drawDead (Canvas canvas) {
        if (state == BugState.DrawDead) {
            Bitmap bugBmp; // current bug bmp that is going to be drawn in the screen
            int divisor; // divisor used for the positioning the dead bug
            // Draw the super bug version of the dead bug if it is a super bug
            if (isSuper) {
                bugBmp = Assets.suprBugD;
                divisor = 3;
            }
            // Draw the regular bug version of the dead bug if it is a regular bug
            else {
                bugBmp = Assets.bugD;
                divisor = 2;
            }
            canvas.drawBitmap(bugBmp, x - bugBmp.getWidth() / divisor,  y - bugBmp.getHeight() / divisor, null);
            // Get time since death
            float curTime = System.nanoTime() / 1000000000f;
            float timeSinceDeath = curTime - deathTime;
            // Drawn dead bug long enough?
            if (timeSinceDeath > 3)
                state = BugState.Dead;
        }
    }

    // Pauses the bug animation by keeping track of the time it was paused
    public void pause() {
        // Record timestamp when the game was paused
        float curTime = System.nanoTime() / 1000000000f;
        pauseTimer = curTime;
    }

    // Resumes the bug animation
    public void resume() {
        // Record timestamp when the game was resumed
        float curTime = System.nanoTime() / 1000000000f;
        // Elapsed time from when the game was paused and resumed
        float elapsedTime = curTime - pauseTimer;
        // Add the paused time elapsed to the timers
        startSpawnTimer += elapsedTime;
        spawnSuperTimer += elapsedTime;
        animateTimer += elapsedTime;
    }
}
