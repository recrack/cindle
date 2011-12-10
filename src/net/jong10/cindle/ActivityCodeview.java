package net.jong10.cindle;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ActivityCodeview extends Activity {
    final private String TAG = "cindle";
    final private Context myApp = this;
    
    private String mProject = "test";
    private String mFilename = "FindResult.java";
    private String mLinnum = "0";
    private String mIp = "";
    
    private String mSourceCode = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "on create");
        setContentView(R.layout.codeview);
        
        mProject = getIntent().getStringExtra( "prj" );
        mFilename = getIntent().getStringExtra( "filename" );
        mLinnum = getIntent().getStringExtra( "linnum" );
        mIp = getIntent().getStringExtra( "ip" );

        
        // make mSourceCode content
        loadCode();

        // setup project, filename
        WebView wv = (WebView)this.findViewById(R.id.codeView);
        wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/www/test.html");
        wv.addJavascriptInterface(  new CodeviewJavaScriptInterface(), "Cindle" );
        StringBuilder sb = new StringBuilder("javascript:callJS('").append(mSourceCode).append("')");
        wv.loadUrl( sb.toString() );
    }

    // setup mSourceCode content 
    private void loadCode() {
        // get file from web
        HttpClient client = new DefaultHttpClient();
        StringBuilder sb = new StringBuilder("http://").append(mIp)
                .append("/codeview/index.py/get_file?prj=").append(mProject)
                .append("&filename=").append(mFilename)
                .append("&linnum=").append(mLinnum);
        Log.i(TAG, sb.toString());
        
        HttpGet request = new HttpGet( sb.toString() );
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            mSourceCode = client.execute(request, responseHandler);
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        if( mSourceCode == null ){
            Log.e(TAG, "response str is null : " + sb.toString() );
        }
        // Log.i(TAG, mSourceCode);
    }

    final class CodeviewJavaScriptInterface {
        private String currentHtmlText = null;

        public void log( String string ){
            Log.i(TAG, string);
        }

        public void clickhook(String innerHTML) {
            String[] findTypeString = myApp.getResources().getStringArray(R.array.findBy);
            AlertDialog.Builder dialog = new AlertDialog.Builder(myApp);
            currentHtmlText = innerHTML;
            dialog.setTitle( "cscope find : " + innerHTML );
            dialog.setItems( findTypeString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent( myApp, ActivityCscope.class );
                    intent.putExtra("prj", mProject);
                    intent.putExtra("query", currentHtmlText);
                    intent.putExtra("method", -(which+1));
                    myApp.startActivity(intent);
                }
            } );
            dialog.setCancelable(true);
            dialog.create();
            dialog.show();
        }

        public String loadfile() {
            // already loaded onCreate
            return mSourceCode;
        }
        
        public String getFilename(){
            return mFilename;
        }
    }
}
