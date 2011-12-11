package net.jong10.cindle;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDirListing extends ListActivity {
    final private String TAG = "cindle";
    
    private String mProject = "test";
    private String mPath = "/";
    private String mIp = "";
    
    ActivityDirListing thisActivity = this;
    public enum EntryType { DIR, FILE, UP };
    private ArrayList<DirItem> mDirListing = new ArrayList<DirItem>();
    private DirListingAdapter m_adapter;
    
    // http://www.vogella.de/articles/AndroidListView/ar01s04.html
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dir_listing);

        mProject = getIntent().getStringExtra( "prj" );
        mPath = getIntent().getStringExtra( "path" );
        mIp = getIntent().getStringExtra( "ip" );
        
        m_adapter = new DirListingAdapter(this, R.layout.dir_listing_row );
        setListAdapter(m_adapter);
        
        updateDirInfo();
    }
    
    private void updateDirInfo() {
        TextView tv = (TextView)this.findViewById(R.id.dir_listing_title);
        tv.setText( mPath );
        new DirInfoUpdater().execute(0);
    }

    ProgressDialog pd = null;
    
    private class DirInfoUpdater extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onProgressUpdate(Integer... progress) {
            pd = ProgressDialog.show(thisActivity, "now loading...", "load dir info from server");
        }

        protected void onPostExecute(Integer result) {
            pd.dismiss();
            m_adapter.notifyDataSetChanged();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Log.i(TAG, "start update dir info...");
            publishProgress(0);
            mDirListing.clear();
            mDirListing.add( new DirItem("..", EntryType.UP) );
            addItemsToDirList("get_dir_list", EntryType.DIR);
            addItemsToDirList("get_file_list", EntryType.FILE);
            Log.i(TAG, "finish update dir info");
            return 0;
        }
    }
    
    private void listUpdateToUpperDir() {
        if( mPath.length() < 2 ){
            Toast.makeText(this, "Root dir.", Toast.LENGTH_SHORT ).show();
            return;
        }
        // for dynamic listing code
        StringBuilder sb = new StringBuilder();
        String[] pathList = mPath.split("/");
        for( int index = 1; index< pathList.length - 1; index++ )
            sb.append("/").append( pathList[index] );
        if( sb.length() == 0 )
            sb.append("/");
        mPath = sb.toString();
        updateDirInfo();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        DirItem item = mDirListing.get(position);
        switch( item.getType() ){
        case DIR:
            if( mPath.length() < 2 )
                mPath = mPath + item.getName();
            else
                mPath = mPath + "/" + item.getName();
            updateDirInfo();
            return;
        case UP:
            listUpdateToUpperDir();
            break;
        case FILE:
            Intent i = null;
            i = new Intent(this, ActivityCodeview.class);
            i.putExtra("filename", mPath + "/" + item.getName());
            i.putExtra("ip", mIp);
            i.putExtra("prj", mProject);
            startActivity(i);
            break;
        default:
            assert(false); return;
        }
    }
    
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("backkey pressed")
        .setMessage("choose behavior")
        .setPositiveButton("exist dir activity", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thisActivity.finish();
            }
        })
        .setNegativeButton("up dir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listUpdateToUpperDir();
            }
        })
        .show();
    }
    
    private void addItemsToDirList(String dirType, EntryType type) {
        HttpClient client = new DefaultHttpClient();
        StringBuilder sb = new StringBuilder( "http://").append(mIp)
                .append("/codeview/index.py/").append(dirType)
                .append("?prj=").append(mProject)
                .append("&path=").append(mPath);
        Log.i( TAG, sb.toString() );
        
        HttpGet request = new HttpGet( sb.toString() );
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String resultFile;
        try {
            resultFile = client.execute(request, responseHandler);
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        if( resultFile == null ){
            Log.e( TAG, "response str is null : " + sb.toString() );
            return;
        }

        String lines[] = resultFile.split("\\r?\\n");
        for( String line : lines )
            if( line != null && line.length() > 0 )
                mDirListing.add( new DirItem(line, type) );
    }
    
    private class DirListingAdapter extends ArrayAdapter<DirItem> {
        DirListingAdapter ( Context context, int textViewResourceId ) {
            super(context, textViewResourceId, mDirListing);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.dir_listing_row, null);
            }
            DirItem p = mDirListing.get(position);
            if (p != null) {
                TextView t1 = (TextView) v.findViewById(R.id.dirListingText);
                t1.setText( p.getName() );
                switch( p.getType() ){
                case DIR:
                    t1.setTextColor( Color.CYAN ); break;
                case FILE:
                    t1.setTextColor( Color.WHITE ); break;
                case UP:
                    t1.setTextColor( Color.CYAN ); break;
                default:
                    assert(false); break;
                }
            }
            return v;
        }
    }
    
    // not support set method 
    private class DirItem {
        private String name;
        private EntryType type;
        DirItem( String name, EntryType type ){
            this.name = name;
            this.type = type;
        }
        public String getName() { return this.name; }
        public EntryType getType() { return this.type; }
    }
}
