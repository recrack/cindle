package net.jong10.cindle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartActivity extends Activity {
    final private String TAG = "cindle";
    final private Context myContext = this; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "on create");
        setContentView(R.layout.start);
        
        // for test
        Button testButton = (Button) findViewById(R.id.startButton);
        testButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(myContext, MainActivity.class);
                i.putExtra("prj", "android-platform");
                // very long source code
                // i.putExtra("filename", "frameworks/base/core/java/android/app/Fragment.java");
                // short source code
                i.putExtra("filename", "frameworks/base/core/java/android/util/Config.java");
                i.putExtra("linnum", "0");
                myContext.startActivity(i);
            }
        });
    }
}
