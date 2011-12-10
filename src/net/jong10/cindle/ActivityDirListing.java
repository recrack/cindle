package net.jong10.cindle;

import java.io.IOException;
import java.util.ArrayList;

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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityDirListing extends ListActivity {
    final private String TAG = "cindle";
    
    private String mProject = "test";
    private String mPath = "/";
    private String mIp = "";
    
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
        
        updateDirInfo();

        m_adapter = new DirListingAdapter(this, R.layout.dir_listing_row );
        setListAdapter(m_adapter);
    }
    
    private void updateDirInfo() {
        Log.i(TAG, "start update dir info...");
        mDirListing.clear();
        mDirListing.add( new DirItem("..", EntryType.UP) );
        DirListing("get_dir_list", EntryType.DIR);
        DirListing("get_file_list", EntryType.FILE);
        Log.i(TAG, "finish update dir info");
        
        // set title
        TextView tv = (TextView)this.findViewById(R.id.dir_listing_title);
        tv.setText( mPath );
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        DirItem item = mDirListing.get(position);
        switch( item.getType() ){
        case DIR:
            if( mPath.equalsIgnoreCase("/") )
                mPath = mPath + item.getName();
            else
                mPath = mPath + "/" + item.getName();
            updateDirInfo();
            m_adapter.notifyDataSetInvalidated();
            return;
        case UP:
            if( mPath.equalsIgnoreCase("/") ){
                // is root dir
                // TODO toast message
                return;
            }
            // for dynamic listing code
            StringBuilder sb = new StringBuilder();
            String[] pathList = mPath.split("/");
            for( int index = 1; index< pathList.length - 1; index++ )
                sb.append("/").append( pathList[index] );
            mPath = sb.toString();
            updateDirInfo();
            m_adapter.notifyDataSetInvalidated();
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
    
    private void DirListing(String dirType, EntryType type) {
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
