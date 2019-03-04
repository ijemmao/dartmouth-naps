package edu.dartmouth.cs65.dartmouthnaps.fragments;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.ReviewActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewCardFragment extends Fragment implements View.OnClickListener {

    private Drawable[] drawables;

    private String title;
    private int noise;
    private int comfort;
    private int light;
    private int convenience;

    ImageView reviewImage;
    TextView headerTitle;
    Button soundQuickStatus;
    Button comfortQuickStatus;
    Button lightQuickStatus;
    Button convenienceQuickStatus;

    Bundle extras;

    public ReviewCardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_card, container, false);

        view.findViewById(R.id.review_card_fragment).setOnClickListener(this);

        extras = getArguments();

        title = extras.getString("title");
        noise = extras.getInt("noise", 1);
        comfort = extras.getInt("comfort", 1);
        light = extras.getInt("light", 1);
        convenience = extras.getInt("convenience", 0);

        System.out.println("this is the convenience: " + convenience);

        drawables = new Drawable[]{
                view.getContext().getResources().getDrawable(R.drawable.red, null),
                view.getContext().getResources().getDrawable(R.drawable.red, null),
                view.getContext().getResources().getDrawable(R.drawable.red, null),
                view.getContext().getResources().getDrawable(R.drawable.yellow, null),
                view.getContext().getResources().getDrawable(R.drawable.blue, null),
                view.getContext().getResources().getDrawable(R.drawable.blue, null)
        };

        reviewImage = view.findViewById(R.id.review_image);
        headerTitle = view.findViewById(R.id.card_fragment_title);
        soundQuickStatus = view.findViewById(R.id.sound_quick_status);
        comfortQuickStatus = view.findViewById(R.id.comfort_quick_status);
        lightQuickStatus = view.findViewById(R.id.light_quick_status);
        convenienceQuickStatus = view.findViewById(R.id.convenience_quick_status);

        headerTitle.setText(title);
        assignColors();


        return view;
    }

    // Applying the correct color to the quick status labels
    private void assignColors() {
        soundQuickStatus.setBackground(drawables[noise]);
        comfortQuickStatus.setBackground(drawables[comfort]);
        lightQuickStatus.setBackground(drawables[light]);
        convenienceQuickStatus.setBackground(drawables[convenience]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_card_fragment:
                Intent intent = new Intent(getContext(), ReviewActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
                break;
        }
    }
}
