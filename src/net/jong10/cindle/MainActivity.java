package net.jong10.cindle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

public class MainActivity extends Activity {
final Context myApp = this;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MY_TAG", "on create");
        setContentView(R.layout.main);
        WebView wv = (WebView)this.findViewById(R.id.codeView);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/www/test.html");
        wv.addJavascriptInterface( new CodeviewJavaScriptInterface(), "Codeview" );
    }
    
    
    final class CodeviewJavaScriptInterface {
        private String currentHtmlText = null;
        public void log( String string ){
            Log.i("MY_TAG", string);
        }
        public void Clickhook(String innerHTML) {
            String[] findTypeString = myApp.getResources().getStringArray(R.array.findBy);
            AlertDialog.Builder dialog = new AlertDialog.Builder(myApp);
            currentHtmlText = innerHTML;
            dialog.setTitle("cscope find");
            dialog.setItems( findTypeString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent( myApp, FindResult.class );
                    intent.putExtra("findBy", which);
                    intent.putExtra("text", currentHtmlText);
                    myApp.startActivity(intent);
                }
            } );
            dialog.setCancelable(true);
            dialog.create();
            dialog.show();
        }
        
        public String loadfile( String filename ) {
            Log.i("MY_TAG", "called loadfile function" + filename);
            File sdcard = Environment.getExternalStorageDirectory();
            Log.i("MY_TAG", sdcard.toString());
            try {
                BufferedReader in = new BufferedReader(new FileReader( sdcard + "/cindle/code.cpp" ));
                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println(s);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return filename;
        }
    }
}