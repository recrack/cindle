package net.jong10.cindle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityStart extends Activity {
    final private Context myContext = this; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        
        // for test
        Button testButton = (Button) findViewById(R.id.loadfileButton);
        testButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(myContext, ActivityCodeview.class);
                i.putExtra("prj", "android-platform");
                // very long source code
                // i.putExtra("filename", "frameworks/base/core/java/android/app/Fragment.java");
                // short source code
                i.putExtra("filename", "frameworks/base/core/java/android/util/Config.java");
                i.putExtra("linnum", "0");
                myContext.startActivity(i);
            }
        });
        
        
        // http://localhost/codeview/index.py/cscope?prj=android-platform&method=-1&query=shutdown
        Button testCscopeButton = (Button) findViewById(R.id.cscopeButton);
        testCscopeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(myContext, ActivityCscope.class);
                i.putExtra("prj", "android-platform");
                // very long source code
                // i.putExtra("filename", "frameworks/base/core/java/android/app/Fragment.java");
                // short source code
                i.putExtra("method", "-1");
                i.putExtra("query", "shutdown");
                myContext.startActivity(i);
            }
        });
    }
}
