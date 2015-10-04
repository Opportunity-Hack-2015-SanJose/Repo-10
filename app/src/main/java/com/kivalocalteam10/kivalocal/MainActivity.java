package com.kivalocalteam10.kivalocal;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends ActionBarActivity
        implements OnMapReadyCallback {

    List<ParseObject> mBusinessData;
    ViewGroup listContent;
    ViewGroup mapContent;
    ContentType contentType = ContentType.LIST;
    enum ContentType { LIST, MAP };
    List<ParseObject> mBusinesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText text = (EditText)findViewById(R.id.search_edit_text);
        listContent = (ViewGroup)findViewById(R.id.result_list_view);
        mapContent = (ViewGroup)findViewById(R.id.result_map_view);

        updateContentView(createQuery(""));

        Button b = (Button)findViewById(R.id.search_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText)findViewById(R.id.search_edit_text);
                updateContentView(createQuery(text.getText().toString()));
            }
        });

        b = (Button)findViewById(R.id.list_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Switch to List View", LENGTH_SHORT).show();
                if(contentType != ContentType.LIST) {
                    contentType = ContentType.LIST;
                    updateContentView(createQuery(text.getText().toString()));
                }
            }
        });

        b = (Button)findViewById(R.id.map_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Switch to Map View", LENGTH_SHORT).show();
                if (contentType != ContentType.MAP) {
                    contentType = ContentType.MAP;

                    updateContentView(createQuery(text.getText().toString()));

                    //
                }
            }
        });



    }

    private void ConfigureMap(GoogleMap map, List<ParseObject> parseObjects) {

        LatLng testLocation = new LatLng(37.3770091, -121.9227009);
Log.d(">>>>>>>>>>>>>>>>>>", parseObjects.size()+"");

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(testLocation, 13));

        for (ParseObject o :parseObjects) {
            ParseGeoPoint point = o.getParseGeoPoint("Coordinate");
            if(point == null) continue;
            double x = point.getLatitude();
            double y = point.getLongitude();
            Log.d(">>>>>>>>>>>>>>>>>>", String.format("%s(%f, %f)", o.getString("Name"), x, y));
            LatLng xyLocation = new LatLng(x,y);
            map.addMarker(new MarkerOptions()
                    .title(o.getString("Name"))
                    .snippet("")
                    .position(xyLocation));
        }

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onMapReady(GoogleMap map){
        ConfigureMap(map, mBusinesses);
    }


    private ParseQuery createQuery(final String keyword) {
        ParseQuery query = new ParseQuery("TestObject");
        query.whereContains("Name", keyword);
        return query;
    }

    private void updateContentView(ParseQuery query) {
        if(contentType == ContentType.LIST) {
            createListViewAdapter(query);
            listContent.setVisibility(View.VISIBLE);
            mapContent.setVisibility(View.GONE);
        } else {
            query.findInBackground(new FindCallback<ParseObject>( ) {

                @Override
                public void done(List<ParseObject>  objects, ParseException e) {
                    //TODO: add map code here
                    //create map fragment
                    MapFragment mapFragment = (MapFragment) getFragmentManager()
                            .findFragmentById(R.id.result_map_view);

                    mapFragment.getMapAsync(MainActivity.this);
                    mBusinesses = objects;

                }




            });
            listContent.setVisibility(View.GONE);
            mapContent.setVisibility(View.VISIBLE);
        }
    }

    private void createListViewAdapter(final ParseQuery query) {
        // Initialize a ParseQueryAdapter
        BusinessAdapter adapter = new BusinessAdapter(this,
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery<ParseObject> create() {
                        // Here we can configure a ParseQuery to our heart's desire.
                        return query;
                    }
                });
        // Custom ParseQueryAdapter, for all ParseQueryAdapter setting please check our doc
        adapter.setTextKey("Name");

        //adapter.addOnQueryLoadListener();
        //adapter.setImageKey("photo");
        ListView listView = (ListView) findViewById(R.id.result_list_view);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
class BusinessAdapter extends ParseQueryAdapter<ParseObject> {

    public BusinessAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_item, null);
        }

        // Take advantage of ParseQueryAdapter's getItemView logic for
        // populating the main TextView/ImageView.
        // The IDs in your custom layout must match what ParseQueryAdapter expects
        // if it will be populating a TextView or ImageView for you.
        super.getItemView(object, v, parent);

        // Do additional configuration before returning the View.
        TextView addressView = (TextView) v.findViewById(R.id.address);
        addressView.setText(object.getString("Address"));
        return v;
    }
}