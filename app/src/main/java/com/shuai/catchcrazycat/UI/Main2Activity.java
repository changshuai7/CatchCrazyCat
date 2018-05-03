package com.shuai.catchcrazycat.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shuai.catchcrazycat.View.Playground;


/**
 * 游戏页面
 */

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Playground playground = new Playground(this);//new一个自定义控件
        setContentView(playground);//填充游戏View
        getSupportActionBar().hide();//隐藏TitleBar

    }
}
