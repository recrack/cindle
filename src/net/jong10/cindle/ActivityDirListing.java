package net.jong10.cindle;

import android.app.ListActivity;
import android.os.Bundle;

public class ActivityDirListing extends ListActivity {
    final private String TAG = "cindle";
    
    private String mProject = "test";
    private int mMethod = -1;
    private String mQuery = "0";
    
    // http://www.vogella.de/articles/AndroidListView/ar01s04.html
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProject = getIntent().getStringExtra( "prj" );
        mQuery = getIntent().getStringExtra( "query" );
        mMethod = getIntent().getIntExtra( "method", -1 );

    }

}
