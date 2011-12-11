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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
    private ArrayList<CscopeResultItem> mCscopeResults;
    private CscopeAdapter m_adapter = null;
    private final ListActivity thisActivity = this;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cscope);
        
        mProject = getIntent().getStringExtra( "prj" );
        mQuery = getIntent().getStringExtra( "query" );
        mMethod = getIntent().getIntExtra( "method", -1 );
        mIp = getIntent().getStringExtra( "ip" );

        mCscopeResults = new ArrayList<CscopeResultItem>();
        
        // set title
        String[] findTypeString = this.getResources().getStringArray(R.array.findBy);
        StringBuilder sb = new StringBuilder( findTypeString[ (-mMethod) - 1 ] ).append(" : ").append(mQuery);
        TextView tv = (TextView)this.findViewById(R.id.cscopeResultTitle);
        tv.setText( sb.toString() );

        new CscopeQuery().execute(0);
        
        m_adapter = new CscopeAdapter(this, R.layout.cscope_row);
        setListAdapter(m_adapter);
    }

    private class CscopeQuery extends AsyncTask<Integer, Integer, Integer> {
        ProgressDialog pd = null;
        @Override
        protected void onProgressUpdate(Integer... progress) {
            pd = ProgressDialog.show(thisActivity, "now loading...", "load cscope result from server");
        }

        protected void onPostExecute(Integer result) {
            pd.dismiss();
            m_adapter.notifyDataSetChanged();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress(0);
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
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if( mQueryReuslt == null ){
                Log.e( TAG, "response str is null : " + sb.toString() );
            }

            // process query result
            String lines[] = mQueryReuslt.split("\\r?\\n");
            for( String line : lines )
                if( line != null && line.length() > 0 )
                    mCscopeResults.add( new CscopeResultItem(line) );
            return 0;
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        CscopeResultItem item = mCscopeResults.get(position);
        Intent i = new Intent(this, ActivityCodeview.class);
        i.putExtra("prj", mProject);
        i.putExtra("filename", "/"+item.getPath());
        i.putExtra("linnum", item.getLinnum());
        i.putExtra("ip", mIp);
        startActivity(i);
    }
    
    private class CscopeAdapter extends ArrayAdapter<CscopeResultItem> {
        
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
            CscopeResultItem p = mCscopeResults.get(position);
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
    
    // not support set method
    private class CscopeResultItem {
        private String mPath;
        private String mLinnum;
        private String mFunction;
        private String mLineStr;
        
        public CscopeResultItem(String line) {
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
