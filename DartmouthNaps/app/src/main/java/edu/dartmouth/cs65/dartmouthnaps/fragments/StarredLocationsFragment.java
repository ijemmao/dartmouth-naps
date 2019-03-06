package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.content.Context;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

/*Written by the Dartmouth Naps Team*/
public class StarredLocationsFragment extends Fragment {

    private ListView listView; //holds the allLocations list view
    private StarredLocationsAdapter adapter; //adapter for the listview

    //firebase information
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uID;
    public DatabaseReference dbReference;

    private ArrayList<Boolean> placesSelection; //bool array of starred locations
    private ChildEventListener listener; //listens for data changes

    //recycler view to hold the horizontal view of all starred locations
    private RecyclerView horizontalRecyclerView;
    private ArrayList<String> starredPlaces;
    private HorizontalAdapter horizontalAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //intializes firebase references
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = user.getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("users").child(uID).child("starred");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.starred_locations_fragment, container, false);

        final String[] placeNames = PlaceUtil.PLACE_NAMES; //gets the list of all locations

        //gets the recycler view
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView = (RecyclerView) view.findViewById(R.id.horizontal_recycler_view);
        starredPlaces = new ArrayList<String>(); //list of all starred locations

        //gets list of all locations, and initializes their selection to false
        listView = (ListView) view.findViewById(R.id.starred_all_list_view);
        placesSelection = new ArrayList<Boolean>(placeNames.length);
        for(int i =0;i<placeNames.length;i++){
            placesSelection.add(false);
        }

        //listens for selection changes
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { //initializes all of the values in the firebase into the local data
                int key = Integer.parseInt(dataSnapshot.getKey());
                boolean bool = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                placesSelection.set(key, bool);
                adapter.notifyDataSetChanged();
                if(bool) { //if starred, add to list of starred places
                    starredPlaces.add(placeNames[key]);
                    horizontalAdapter.notifyItemInserted(starredPlaces.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { //if bool values is changed, change it to the correct star state
                int key = Integer.parseInt(dataSnapshot.getKey());
                boolean bool = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                placesSelection.set(key, bool);
                adapter.notifyDataSetChanged();
                if(bool) {
                    starredPlaces.add(placeNames[key]);
                    horizontalAdapter.notifyItemInserted(starredPlaces.size() - 1);
                } else {
                    int index = starredPlaces.indexOf(placeNames[key]);
                    starredPlaces.remove(placeNames[key]);
                    horizontalAdapter.notifyItemRemoved(index);
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //unused
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //unused
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //unused
            }
        };

        dbReference.addChildEventListener(listener); //sets the listener in the correct level

        //sets the adapter for the list view
        adapter = new StarredLocationsAdapter(getActivity(), android.R.layout.simple_list_item_1, placeNames, dbReference, placesSelection);
        listView.setAdapter(adapter);

        //creates the horizontal recycler view
        horizontalRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(30));
        horizontalAdapter=new HorizontalAdapter(starredPlaces, dbReference, placeNames);
        horizontalRecyclerView.setLayoutManager(layoutManager);
        horizontalRecyclerView.setAdapter(horizontalAdapter);

        //gets and sets the onclick function of the open drawer button
        ImageButton imageButton = (ImageButton)view.findViewById(R.id.open_drawer_starred);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.drawer.openDrawer(GravityCompat.START);
            }
        });

        return view;
    }
}

//adapter class to be used by the allLocations listview that displays all locations, and if they are starred or not
class StarredLocationsAdapter extends ArrayAdapter<String> {

    private Context context;
    private int resource;
    private String[] places;
    private DatabaseReference dbRef;
    ArrayList<Boolean> boolArray;

    //Constructor
    public StarredLocationsAdapter(Context context, int resource, String[] places, DatabaseReference dbRef, ArrayList<Boolean> boolArray) {
        super(context, resource, places);

        this.context = context;
        this.resource = resource;
        this.places = places;
        this.dbRef = dbRef;
        this.boolArray = boolArray;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Holder holder = null; //uses holder to handle reused views and positions
        final CheckBox checkBox;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(context).inflate(R.layout.starred_all_list_view_item, parent, false);
            holder.checkBox = (CheckBox) view.findViewById(R.id.starred_checkbox);
            view.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
            holder.checkBox.setOnCheckedChangeListener(null);
        }

        //allows checkbox to be checked, aka star to be starred
        holder.checkBox.setFocusable(false);
        holder.checkBox.setChecked(boolArray.get(position));

        //creates the onchange listener to listen for star state changes
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    boolArray.set(position, true); //sets to true if checked
                    dbRef.setValue(boolArray);
                }else {
                    boolArray.set(position, false); //sets to false if unchecked
                    dbRef.setValue(boolArray);
                }
            }
        });

        //sets the textview for the location
        TextView textView = (TextView)view.findViewById(R.id.place_name);
        textView.setText(places[position]);

        return view;
    }

    //holder class that holds the checkbox
    private class Holder {
        CheckBox checkBox;
    }
}

//adapter class for the horizontal recycler view for the starred locations
class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

    public List<String> horizontalList;
    public DatabaseReference dbRef;
    public String[] placeNames;

    //holder to hold the checkbox and the textview
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtView;
        public CheckBox checkBox;

        //constructor for the holder
        public MyViewHolder(View view) {
            super(view);
            txtView = (TextView) view.findViewById(R.id.txtView);
            checkBox = (CheckBox)view.findViewById(R.id.starred_location_checkbox);
            checkBox.setChecked(true);
        }
    }

    //constructor for the adapter
    public HorizontalAdapter(List<String> horizontalList, DatabaseReference dbRef, String[] placeNames) {
        this.horizontalList = horizontalList;
        this.dbRef = dbRef;
        this.placeNames = placeNames;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.starred_location_starred_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.txtView.setText(horizontalList.get(position));
        final String text = (String) holder.txtView.getText();

        //sets the onclick listener of the checkbox
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = -1;
                for(int i = 0; i < placeNames.length; i++) {
                    if(placeNames[i].equals(text)) {
                        index = i;
                    }
                }
                Log.d("tag2", "removing " + index);
                dbRef.child("" + index).setValue(false); //only removes because these locations are in the starred locations
            }
        });
    }

    @Override
    public int getItemCount() {
        return horizontalList.size();
    }
}

//decoration to be used by the recyclerview. formats the spacing between each recyclerview item.
class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int horizontalSpace;

    public HorizontalSpaceItemDecoration(int horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.right = horizontalSpace;
    }
}



