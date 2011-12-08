package net.jong10.cindle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityCscope extends ListActivity {
    final private String TAG = "cindle";
    
    private String mProject = "test";
    private String mMethod = "FindResult.java";
    private String mQuery = "0";
    
    private String queryReuslt = null;
    private ArrayList<CscopeResult> mCscopeResults;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cscope);
        CscopeQuery( savedInstanceState );
        
        String lines[] = queryReuslt.split("\\r?\\n");
        mCscopeResults = new ArrayList<CscopeResult>();
        for( String line : lines )
            mCscopeResults.add( new CscopeResult(line) );
        
        CscopeAdapter m_adapter = new CscopeAdapter(this, R.layout.cscope_row);
        setListAdapter(m_adapter);
        
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        CscopeResult item = mCscopeResults.get(position);
        Intent i = new Intent(this, ActivityCodeview.class);
        i.putExtra("prj", "android-platform");
        // very long source code
        // i.putExtra("filename", "frameworks/base/core/java/android/app/Fragment.java");
        // short source code
        i.putExtra("filename", item.getPath());
        i.putExtra("linnum", item.getLinnum());
        startActivity(i);
    }

    private void CscopeQuery( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cscope);
        
        mProject = getIntent().getStringExtra( "prj" );
        mMethod = getIntent().getStringExtra( "method" );
        mQuery = getIntent().getStringExtra( "query" );
        
        // get cscope result from web
        HttpClient client = new DefaultHttpClient();
        StringBuilder sb = new StringBuilder( "http://192.168.0.102/codeview/index.py/cscope?prj=").append(mProject)
                .append("&method=").append(mMethod)
                .append("&query=").append(mQuery);
        Log.i( TAG, sb.toString() );
        
        HttpGet request = new HttpGet( sb.toString() );
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            queryReuslt = client.execute(request, responseHandler);
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        if( queryReuslt == null ){
            Log.e( TAG, "response str is null : " + sb.toString() );
        }
        // Log.i(TAG, queryReuslt);
    }
    
    private class CscopeAdapter extends ArrayAdapter<CscopeResult> {
        
        public CscopeAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId, mCscopeResults);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.cscope_row, null);
            }
            CscopeResult p = mCscopeResults.get(position);
            if (p != null) {
                TextView t1 = (TextView) v.findViewById(R.id.cscope_path);
                TextView t2 = (TextView) v.findViewById(R.id.cscope_method);
                TextView t3 = (TextView) v.findViewById(R.id.cscope_linnum);
                TextView t4 = (TextView) v.findViewById(R.id.cscope_str);
                t1.setText( "*** " + p.getPath() );
                t2.setText( "<" + p.getFuntion() + ">" );
                t3.setText( "[" + p.getLinnum() + "]" );
                t4.setText( p.getLineStr() );
            }
            return v;
        }
    }
    
    class CscopeResult {
        private String mPath;
        private String mLinnum;
        private String mFunction;
        private String mLineStr;
        
        public CscopeResult(String line){
            
            Pattern p = Pattern.compile("([^ ]+) ([^ ]+) ([^ ]+) (.+)");
            Matcher m = p.matcher(line);
            while( m.find() && m.groupCount() == 4 ){
                mPath = m.group(1);
                mFunction = m.group(2);
                mLinnum = m.group(3);
                mLineStr = m.group(4);
            }
        }
        
        public String getPath() {
            return mPath;
        }

        public String getLinnum() {
            return mLinnum;
        }

        public String getFuntion() {
            return mFunction;
        }
        
        public String getLineStr() {
            return mLineStr;
        }
    }
}
