package edu.dartmouth.cs65.dartmouthnaps;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AuthOnCompleteListener implements OnCompleteListener {

    private Context context; //keeps track of the context
    private String type; //keeps track of what type of activity this came from

    //constructor
    public AuthOnCompleteListener(Context context, String type){
        this.context = context;
        this.type = type;
    }

    //if task is successfully completed
    public void onComplete(Task task){
        if(task.isSuccessful()) {
            Intent intent = new Intent(context, MainActivity.class); //open main activity with a message to get new database
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear the previous activities
            context.startActivity(intent);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if(type.equals("login")) {
                builder.setMessage(R.string.invalid_login_text).setTitle(R.string.oops_title).setPositiveButton(android.R.string.ok, null);
            } else if(type.equals("signup")) {
                builder.setMessage(R.string.invalid_signup_text).setTitle(R.string.oops_title).setPositiveButton(android.R.string.ok, null);
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}