package edu.dartmouth.cs65.dartmouthnaps;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.dartmouth.cs65.dartmouthnaps.models.Review;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewCardFragment extends Fragment {

    private Drawable[] drawables;

    private String title;
    private int noise;
    private int comfort;
    private int light;

    Button soundQuickStatus;
    Button comfortQuickStatus;
    Button lightQuickStatus;
    Button convenienceQuickStatus;

    public ReviewCardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_card, container, false);

        Bundle extras = getArguments();

        title = extras.getString("title");
        noise = extras.getInt("noise", 1);
        comfort = extras.getInt("comfort", 1);
        light = extras.getInt("light", 1);

        drawables = new Drawable[]{
                view.getContext().getResources().getDrawable(R.drawable.red, null),
                view.getContext().getResources().getDrawable(R.drawable.red, null),
                view.getContext().getResources().getDrawable(R.drawable.red, null),
                view.getContext().getResources().getDrawable(R.drawable.yellow, null),
                view.getContext().getResources().getDrawable(R.drawable.blue, null),
                view.getContext().getResources().getDrawable(R.drawable.blue, null)
        };

        soundQuickStatus = view.findViewById(R.id.sound_quick_status);
        comfortQuickStatus = view.findViewById(R.id.comfort_quick_status);
        lightQuickStatus = view.findViewById(R.id.light_quick_status);
        convenienceQuickStatus = view.findViewById(R.id.convenience_quick_status);

        assignColors();


        return view;
    }

    private void assignColors() {
        soundQuickStatus.setBackground(drawables[noise]);
        comfortQuickStatus.setBackground(drawables[comfort]);
        lightQuickStatus.setBackground(drawables[light]);
    }

}
