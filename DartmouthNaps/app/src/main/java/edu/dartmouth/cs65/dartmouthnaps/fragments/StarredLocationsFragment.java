package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

public class StarredLocationsFragment extends Fragment {

    private ListView listView;
    private StarredLocationsAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uID;
    public DatabaseReference dbReference;

    private ArrayList<Boolean> placesSelection;
    private ChildEventListener listener;

    private RecyclerView horizontalRecyclerView;
    private ArrayList<String> starredPlaces;
    private HorizontalAdapter horizontalAdapter;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = user.getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("users").child(uID).child("starred");

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.starred_locations_fragment, container, false);

        final String[] placeNames = PlaceUtil.PLACE_NAMES;
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView = (RecyclerView) view.findViewById(R.id.horizontal_recycler_view);
        starredPlaces = new ArrayList<String>();

        listView = (ListView) view.findViewById(R.id.starred_all_list_view);
        placesSelection = new ArrayList<Boolean>(placeNames.length);
        for(int i =0;i<placeNames.length;i++){
            placesSelection.add(false);
        }

        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                int key = Integer.parseInt(dataSnapshot.getKey());
                boolean bool = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                placesSelection.set(key, bool);
                adapter.notifyDataSetChanged();
                if(bool) {
                    starredPlaces.add(placeNames[key]);
                    horizontalAdapter.notifyItemInserted(starredPlaces.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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

        adapter = new StarredLocationsAdapter(getActivity(), android.R.layout.simple_list_item_1, placeNames, dbReference, placesSelection);
        listView.setAdapter(adapter);
        horizontalRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(10));

        horizontalAdapter=new HorizontalAdapter(starredPlaces, dbReference, placeNames);
        horizontalRecyclerView.setLayoutManager(layoutManager);
        horizontalRecyclerView.setAdapter(horizontalAdapter);



        return view;
    }
}

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
        Holder holder = null;
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
        holder.checkBox.setFocusable(false);
        holder.checkBox.setChecked(boolArray.get(position));

//        Log.d("tag2", "" + position + " " + boolArray[position]);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    boolArray.set(position, true);
                    dbRef.setValue(boolArray);

                }else {
                    boolArray.set(position, false);
                    dbRef.setValue(boolArray);
                }
            }
        });

        TextView textView = (TextView)view.findViewById(R.id.place_name);
        textView.setText(places[position]);

        return view;
    }

    private class Holder {
        CheckBox checkBox;
    }
}

class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

    public List<String> horizontalList;
    public DatabaseReference dbRef;
    public String[] placeNames;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtView;
        public CheckBox checkBox;

        public MyViewHolder(View view) {
            super(view);
            txtView = (TextView) view.findViewById(R.id.txtView);
            checkBox = (CheckBox)view.findViewById(R.id.starred_location_checkbox);
            checkBox.setChecked(true);
        }
    }


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


        holder.txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

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
                dbRef.child("" + index).setValue(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return horizontalList.size();
    }
}

class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int horizontalSpace;

    public HorizontalSpaceItemDecoration(int horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.right = horizontalSpace;
    }
}



