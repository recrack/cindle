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
    private int mMethod = -1;
    private String mQuery = "0";
    private String mIp = "";
    
    private String mQueryReuslt = ""; //TODO. remove it
    private ArrayList<CscopeResult> mCscopeResults;
    private CscopeAdapter m_adapter = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cscope);
        
        mProject = getIntent().getStringExtra( "prj" );
        mQuery = getIntent().getStringExtra( "query" );
        mMethod = getIntent().getIntExtra( "method", -1 );
        mIp = getIntent().getStringExtra( "ip" );

        
        mCscopeResults = new ArrayList<CscopeResult>();
        CscopeQuery( savedInstanceState );
        
        // set title
        String[] findTypeString = this.getResources().getStringArray(R.array.findBy);
        StringBuilder sb = new StringBuilder( findTypeString[ (-mMethod) - 1 ] ).append(" : ").append(mQuery);
        TextView tv = (TextView)this.findViewById(R.id.cscopeResultTitle);
        tv.setText( sb.toString() );
        
        // process query result
        String lines[] = mQueryReuslt.split("\\r?\\n");
        for( String line : lines )
            if( line != null && line.length() > 0 )
                mCscopeResults.add( new CscopeResult(line) );
        
        m_adapter = new CscopeAdapter(this, R.layout.cscope_row);
        setListAdapter(m_adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        CscopeResult item = mCscopeResults.get(position);
        Intent i = new Intent(this, ActivityCodeview.class);
        i.putExtra("prj", "android-platform");
        i.putExtra("filename", item.getPath());
        i.putExtra("linnum", item.getLinnum());
        i.putExtra("ip", mIp);
        startActivity(i);
    }

    private void CscopeQuery( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cscope);
        
        // get cscope result from web
        HttpClient client = new DefaultHttpClient();
        StringBuilder sb = new StringBuilder( "http://59.18.159.96/codeview/index.py/cscope?prj=").append(mProject)
                .append("&method=").append(mMethod)
                .append("&query=").append(mQuery);
        Log.i( TAG, sb.toString() );
        
        HttpGet request = new HttpGet( sb.toString() );
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            mQueryReuslt = client.execute(request, responseHandler);
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        if( mQueryReuslt == null ){
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
    
    private class CscopeResult {
        private String mPath;
        private String mLinnum;
        private String mFunction;
        private String mLineStr;
        
        public CscopeResult(String line) {
            Pattern p = Pattern.compile("([^ ]+) ([^ ]+) ([^ ]+) (.+)");
            Matcher m = p.matcher(line);
            while( m.find() && m.groupCount() == 4 ){
                mPath = m.group(1);
                mFunction = m.group(2);
                mLinnum = m.group(3);
                mLineStr = m.group(4);
            }
        }
        
        public String getPath() { return mPath; }
        public String getLinnum() { return mLinnum; }
        public String getFuntion() { return mFunction; }
        public String getLineStr() { return mLineStr; }
    }
}
