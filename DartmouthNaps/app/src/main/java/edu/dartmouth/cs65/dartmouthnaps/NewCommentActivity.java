package edu.dartmouth.cs65.dartmouthnaps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class NewCommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_comment);
    }

    public void submitComment(View view) {
        // Send comment to Firebase database
        Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
        finish();
    }
}
