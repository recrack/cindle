package net.jong10.cindle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ActivityStart extends Activity {
    final private Context myContext = this; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        
        EditText editIp = (EditText)findViewById(R.id.EditTextIp);
        EditText editPrj = (EditText)findViewById(R.id.EditTextPrj);
        
        SharedPreferences prefs = getSharedPreferences("Cindle", Context.MODE_PRIVATE);
        editIp.setText( prefs.getString("ip", "") );
        editPrj.setText( prefs.getString("prj", "") );
        
//        // for test
//        Button testButtonScource = (Button) findViewById(R.id.loadfileButton);
//        testButtonScource.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(myContext, ActivityCodeview.class);
//                i.putExtra("prj", "android-platform");
//                // very long source code
//                // i.putExtra("filename", "frameworks/base/core/java/android/app/Fragment.java");
//                // short source code
//                i.putExtra("filename", "frameworks/base/core/java/android/util/Config.java");
//                i.putExtra("linnum", "0");
//                myContext.startActivity(i);
//            }
//        });
//        
//        // http://localhost/codeview/index.py/cscope?prj=android-platform&method=-1&query=shutdown
//        Button testButtonCscope = (Button) findViewById(R.id.cscopeButton);
//        testButtonCscope.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(myContext, ActivityCscope.class);
//                i.putExtra("prj", "android-platform");
//                i.putExtra("method", "-1");
//                i.putExtra("query", "shutdown");
//                myContext.startActivity(i);
//            }
//        });
//        
//        // http://localhost/codeview/index.py/cscope?prj=android-platform&method=-1&query=shutdown
//        Button testButtonListing = (Button) findViewById(R.id.buttonDirListing);
//        testButtonListing.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(myContext, ActivityDirListing.class);
//                i.putExtra("prj", "android-platform");
//                i.putExtra("path", "/frameworks");
//                myContext.startActivity(i);
//            }
//        });
    }
    
    public void onCindleStart(View view){
        EditText editIp = (EditText)findViewById(R.id.EditTextIp);
        EditText editPrj = (EditText)findViewById(R.id.EditTextPrj);
        if( editIp.getText() == null || editIp.getText().length() < 7 || editPrj.getText() == null || editPrj.getText().length() <= 0 ){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle( "not enough settings" );
            alert.setMessage( "check server and project setting" );
            alert.setPositiveButton(
                     "close", new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alert.show();
            return;
        }
        SharedPreferences.Editor prefsEdit = getSharedPreferences("Cindle", Context.MODE_PRIVATE).edit();
        prefsEdit.putString("ip", editIp.getText().toString() );
        prefsEdit.putString("prj", editPrj.getText().toString() );
        prefsEdit.commit();
        
        Intent i = new Intent(myContext, ActivityDirListing.class);
        i.putExtra("ip", editIp.getText().toString() );
        i.putExtra("prj", editPrj.getText().toString() );
        i.putExtra("path", "/"); // default path
        startActivity(i);
        
    }
    
    public void onCindleDemo(View view){
        SharedPreferences.Editor prefsEdit = getSharedPreferences("Cindle", Context.MODE_PRIVATE).edit();
        prefsEdit.putBoolean("demo", true);
        prefsEdit.commit();
        
        Intent i = new Intent(myContext, ActivityDirListing.class);
        i.putExtra("ip", "59.18.159.96");
        i.putExtra("prj", "android-platform");
        i.putExtra("path", "/");
        startActivity(i);
        return;
    }
}
