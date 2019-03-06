package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import edu.dartmouth.cs65.dartmouthnaps.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import edu.dartmouth.cs65.dartmouthnaps.listeners.AuthOnCompleteListener;

/*Written by the Dartmouth Naps Team*/
public class LoginActivity extends AppCompatActivity {

    private EditText loginID; //the email input
    private EditText loginPassword; //pw input
    private FirebaseAuth auth; //the firebase authority
    private Task<AuthResult> task; //the task

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //gets the edit texts
        loginID = (EditText)findViewById(R.id.login_id);
        loginPassword = (EditText)findViewById(R.id.login_password);
        auth = FirebaseAuth.getInstance();

        //formats the action bar to be invisible
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    //when log in is clicked
    public void onLoginClicked(View view) {
        String textEmail = loginID.getText().toString().trim();
        String textPassword = loginPassword.getText().toString().trim();
        if(!(textEmail.isEmpty()) && !(textPassword.isEmpty())) {
            task = auth.signInWithEmailAndPassword(textEmail, textPassword); //log in
            AuthOnCompleteListener listener = new AuthOnCompleteListener(this, "login");
            task.addOnCompleteListener(listener);
        } else {
            //if log in information is empty, prompt user to input it
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.empty_fields_text).setTitle(R.string.oops_title).setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
