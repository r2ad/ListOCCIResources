package com.r2ad.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.r2ad.android.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ComputerListViewActivity extends Activity {
  
  private ListView mainListView ;
  private ArrayAdapter<String> listAdapter ;
  String urlValue="";
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.listing);
    Context context = this.getApplicationContext();
    // Find the ListView resource. 
    mainListView = (ListView) findViewById( R.id.mainListView );
  
    ArrayList<String> computerList = new ArrayList<String>();
    
    listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, computerList);

    Bundle extras = getIntent().getExtras(); 
    if(extras !=null) {
    	urlValue = extras.getString("url");
    }
    
    Computers computers = new com.r2ad.android.Computers(urlValue);
    ArrayList<String> moreItems = computers.parseComputers(context);
    
	for (int i = 0; i < moreItems.size(); i++) {
    	listAdapter.add(moreItems.get(i));
    }
	
	final Comparator<String> comp = new Comparator<String>() {
	    public int compare(String e1, String e2) {
	        return e1.toString().compareTo(e2.toString());
	    }
	};
	
	((ArrayAdapter<String>) listAdapter).sort(comp);  
	// Set the ArrayAdapter as the ListView's adapter.
    mainListView.setAdapter( listAdapter );      
  }
}