/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will implement the title activity of the Bug
 * Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Class data field
    private Button playButton, highScoreButton;

    @Override
    // onCreate - sets up the button functions seen on the screen
    protected void onCreate(Bundle inBundle) {
        super.onCreate(inBundle);
        setContentView(R.layout.activity_main);

        // Sets the buttons to the buttons seen on the screen
        playButton = (Button) findViewById(R.id.button_play);
        highScoreButton = (Button) findViewById(R.id.button_highScore);

        // Enables the buttons to be clickable by the user
        playButton.setOnClickListener(this);
        highScoreButton.setOnClickListener(this);
    }

    @Override
    // onResume - retrieves high score and plays the background music if enabled
    protected void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Retrieves the high score from the shared preferences
        Assets.highScore = prefs.getInt("key_highScore", 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        // Switch statement that determines if the play, or high score button is pressed
        switch (v.getId()) {

            // Starts a new activity that will display the game screen
            case R.id.button_play:
                startActivity(new Intent(this, GameActivity.class));
                break;

            // Starts a new activity that will display a preference screen displaying the high score
            case R.id.button_highScore:
                startActivity(new Intent(this, HighScoreActivity.class));
                break;
        }
    }
}
