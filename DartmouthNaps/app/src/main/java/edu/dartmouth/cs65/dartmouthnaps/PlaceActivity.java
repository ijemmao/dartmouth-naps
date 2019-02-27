package edu.dartmouth.cs65.dartmouthnaps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import edu.dartmouth.cs65.dartmouthnaps.models.Comment;

public class PlaceActivity extends AppCompatActivity {

    ImageView imageView;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        imageView = findViewById(R.id.location_image);
        listView = findViewById(R.id.comment_list_view);

        // Customizing Image View
        imageView.getLayoutParams().height = 620;

        // Populating List of Comments
        ArrayList<Comment> comments = getComments();
        ListView listView = this.findViewById(R.id.comment_list_view);
        listView.setAdapter(new CommentAdapter(this, comments));

    }

    public void onAddComment(View view) {
        Intent intent = new Intent(this, NewCommentActivity.class);
        startActivity(intent);
    }

    private ArrayList<Comment> getComments() {
        ArrayList<Comment> comments = new ArrayList<>();
        Comment comment = new Comment();

        comment.setAuthor("XD");
        comment.setTimestamp(new Date().toString());
        comment.setLocation("First Floor 007");
        comment.setBody("this is a terrible location to sleep during 12s");
        comments.add(comment);

        comment.setAuthor("XD");
        comment.setTimestamp(new Date().toString());
        comment.setLocation("First Floor 007");
        comment.setBody("this is a terrible location to sleep during 12s");
        comments.add(comment);

        comment.setAuthor("XD");
        comment.setTimestamp(new Date().toString());
        comment.setLocation("First Floor 007");
        comment.setBody("this is a terrible location to sleep during 12s");
        comments.add(comment);

        comment.setAuthor("XD");
        comment.setTimestamp(new Date().toString());
        comment.setLocation("First Floor 007");
        comment.setBody("this is a terrible location to sleep during 12s");
        comments.add(comment);

        return comments;
    }
}
