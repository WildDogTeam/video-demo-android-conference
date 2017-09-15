package com.wilddog.wilddogroom;

import android.app.Application;

import com.wilddog.wilddogroom.util.Constants;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by fly on 17-9-11.
 */

public class RoomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WilddogOptions options = new WilddogOptions.Builder().setSyncUrl("https://"+ Constants.WILDDOG_VIDEO_ID+".wilddogio.com").build();
        WilddogApp.initializeApp(this,options);
    }
}
