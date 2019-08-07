/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will contain all the global variables that will
 * be used in the Bug Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class Assets {
    // Global Variables
    static Bitmap background;
    static Bitmap bugL;      // bug left
    static Bitmap bugR;      // bug right
    static Bitmap bugD;      // bug dead
    static Bitmap suprBugL;  // super bug left
    static Bitmap suprBugR;  // super bug right
    static Bitmap suprBugD;  // super bug dead
    static Bitmap scoreBar;  // score bar
    static Bitmap lifeIcon;  // life icon
    static Bitmap pauseIcon; // pause button icon
    static Bitmap playIcon; // play button icon

    // States of the game screen
    enum GameState {
        GettingReady, // plays "get ready" sound and start timer then go to next state
        Starting,     // after 3 seconds, go to next state
        Running,      // play the game until numLives == 0, go to next state
        GameEnding,   // show game over message
        GameOver,     // game is over, wait for player to touch the screen then go back to title activity
    };
    static GameState state; // current state of the game
    static float gameTimer; // seconds
    static int numLives;    // 0-3
    static int score;       // current score in the game

    static SoundPool sp;
    static int s_pause;
    static int s_getReady;
    static int s_gameOver;
    static int s_newHighScore;
    static int s_squish1;
    static int s_squish2;
    static int s_squish3;
    static int s_superHit;
    static int s_superKilled;
    static int s_thud;
    static int s_munch;

    static Bug bug[];
    static int superBugIndex; // only one bug in the array has a chance to become a super bug to make sure that only one super bug respawns at a time

    public static MediaPlayer mp;


    public static int highScore; // high Score

    public static boolean gamePaused;     // Indicates if the game is paused
    public static boolean returnedPaused; // Indicates if the game is paused when user pauses the application (onPause was called while game is paused)

    public static boolean backPressed; // Indicates if back button was pressed
}
