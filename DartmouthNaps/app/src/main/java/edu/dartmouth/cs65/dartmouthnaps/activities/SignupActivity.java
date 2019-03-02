package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import edu.dartmouth.cs65.dartmouthnaps.AuthOnCompleteListener;
import edu.dartmouth.cs65.dartmouthnaps.R;

public class SignupActivity extends AppCompatActivity {

    private EditText signupID; //email field
    private EditText signupPassword; //password field
    private FirebaseAuth auth; //the firebase authority
    private Task<AuthResult> task; //the task

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        //gets the edit texts and sets the auth
        signupID = (EditText)findViewById(R.id.signup_id);
        signupPassword = (EditText)findViewById(R.id.signup_password);
        auth = FirebaseAuth.getInstance();
    }

    //if the final sign up button is clicked, sign up
    public void onSignupClicked(View view){
        String textEmail = signupID.getText().toString();
        String textPassword = signupPassword.getText().toString();
        if(!(textEmail.isEmpty()) && !(textPassword.isEmpty())) {
            task = auth.createUserWithEmailAndPassword(textEmail, textPassword);
            AuthOnCompleteListener listener = new AuthOnCompleteListener(this, "signup"); //listen for completion
            task.addOnCompleteListener(listener);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.empty_fields_text).setTitle(R.string.oops_title).setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}