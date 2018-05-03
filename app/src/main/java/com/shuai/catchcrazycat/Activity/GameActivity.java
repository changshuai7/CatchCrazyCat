package com.shuai.catchcrazycat.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shuai.catchcrazycat.View.GameView;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameView gameView = new GameView(this);
        setContentView(gameView);
        getSupportActionBar().hide();
    }
}
