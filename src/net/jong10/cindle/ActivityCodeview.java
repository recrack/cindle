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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ActivityCodeview extends Activity {
    final private String TAG = "cindle";
    final private Context thisActivity = this;
    
    private String mProject = "test";
    private String mFilename = "FindResult.java";
    private String mLinnum = "0";
    private String mIp = "";
    
    private String mSourceCode = null;
    
    private enum RESULT { SUCCESS, FAIL };
    private enum PROGRESS { SOURCE, HTML, PARSING };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codeview);
        
        // setup project, filename
        mProject = getIntent().getStringExtra( "prj" );
        mFilename = getIntent().getStringExtra( "filename" );
        mLinnum = getIntent().getStringExtra( "linnum" );
        mIp = getIntent().getStringExtra( "ip" );

        // alert listener
        WebView wv = (WebView)findViewById(R.id.codeView);
        wv.setWebChromeClient( mAlertListner );

        // make mSourceCode content
        pd = ProgressDialog.show(this, "now loading...", "");
        pd.setCancelable(true);
        new CodeviewLoader().execute(0);
    }
    
    WebChromeClient mAlertListner = new WebChromeClient() {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            Log.i(TAG, "onJsAlert : " + message);
            return true;
        };
    };
    
    final class CodeviewJavaScriptInterface {
        private String currentHtmlText = null;

        public void log( String string ){
            Log.i(TAG, string);
        }

        public void clickhook(String innerHTML) {
            String[] findTypeString = thisActivity.getResources().getStringArray(R.array.findBy);
            AlertDialog.Builder dialog = new AlertDialog.Builder(thisActivity);
            currentHtmlText = innerHTML;
            dialog.setTitle( "cscope find : " + innerHTML );
            dialog.setItems( findTypeString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent( thisActivity, ActivityCscope.class );
                    intent.putExtra("prj", mProject);
                    intent.putExtra("query", currentHtmlText);
                    intent.putExtra("method", -(which+1));
                    intent.putExtra("ip", mIp);
                    thisActivity.startActivity(intent);
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
    
    ProgressDialog pd = null;
    private class CodeviewLoader extends AsyncTask<Integer, PROGRESS, RESULT> {
        @Override
        protected void onProgressUpdate(PROGRESS... progress) {
            switch( progress[0] ){
            case SOURCE:
                pd.setMessage("load code from server");
                break;
            case HTML:
                pd.setMessage("load codeview page");
                break;
            case PARSING:
                pd.setMessage("parsing source code. plaea wait. maybe it takes long time...");
                break;
            }
        }

        protected void onPostExecute(RESULT result) {
            switch( result ){
            case SUCCESS:
                break;
            case FAIL:
                break;
            }
        }

        @Override
        protected RESULT doInBackground(Integer... params) {
            publishProgress(PROGRESS.SOURCE);
            try {
                loadSource();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
                return RESULT.FAIL;
            } catch (IOException e1) {
                e1.printStackTrace();
                return RESULT.FAIL;
            }
            
            publishProgress(PROGRESS.HTML);
            WebView wv = loadWebview();
            
            publishProgress(PROGRESS.PARSING);
            StringBuilder sb = new StringBuilder("javascript:callJS('").append(mSourceCode).append("')");
            wv.loadUrl( sb.toString() );
            return RESULT.SUCCESS;
        }

        private WebView loadWebview() {
            WebView wv = (WebView)findViewById(R.id.codeView);
            wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.loadUrl("file:///android_asset/www/codeview.html");
            wv.addJavascriptInterface(  new CodeviewJavaScriptInterface(), "Cindle" );
            wv.setWebViewClient(new WebViewClient() {
                
                public void onPageFinished(WebView view, String url) {
                    if( pd != null )
                        pd.dismiss();
                }
             });
            return wv;
        }

        private void loadSource() throws ClientProtocolException, IOException {
            HttpClient client = new DefaultHttpClient();
            StringBuilder sb = new StringBuilder("http://").append(mIp)
                    .append("/codeview/index.py/get_file?prj=").append(mProject)
                    .append("&filename=").append(mFilename)
                    .append("&linnum=").append(mLinnum);
            Log.i(TAG, sb.toString());
            HttpGet request = new HttpGet( sb.toString() );
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            mSourceCode = client.execute(request, responseHandler);
            if( mSourceCode == null ){
                Log.e(TAG, "response str is null : " + sb.toString() );
            }
        }
    }
}
