package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.RatingFragment;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class NewReviewActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private StorageReference storageReference;

    static final int CAMERA_REQUEST_CODE = 200;

    ImageView uploadImage;
    ImageView locationImage;

    EditText reviewHeader;

    RatingFragment noiseFragment;
    RatingFragment comfortFragment;
    RatingFragment lightFragment;

    String mCurrentPhotoPath;
    File photoFile;
    Uri photoURI;
    byte[] imageBytes;
    String imageFileName;
    LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_review);

        intent = getIntent();
        location = new LatLng(
                intent.getDoubleExtra(KEY_LATITUDE, PLACE_COORDINATES_AVG[1][LAT]),
                intent.getDoubleExtra(KEY_LONGITUDE, PLACE_COORDINATES_AVG[1][LNG]));
        checkPermission();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        reviewHeader = findViewById(R.id.review_title);

        uploadImage = findViewById(R.id.upload_image);
        locationImage = findViewById(R.id.location_image);

        noiseFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.noise_fragment);
        comfortFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.comfort_fragment);
        lightFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.light_fragment);

    }

    public void submitReview(View view) {

        // Notifies user if there are empty fields
        // Review cannot be sent with empty fields
        if (user.getUid() == null
                        || noiseFragment.getRating() == 0
                        || comfortFragment.getRating() == 0
                        || lightFragment.getRating() == 0
                        || reviewHeader.getText().toString().equals("")
                        || imageFileName == null) {
            Toast.makeText(this, "Must complete all fields", Toast.LENGTH_SHORT).show();
        } else {
            Review newReview = new Review(
                    user.getUid(),
                    noiseFragment.getRating(),
                    comfortFragment.getRating(),
                    lightFragment.getRating(),
                    reviewHeader.getText().toString(),
                    imageFileName,
                    Review.getTimestampFromCalendar(Calendar.getInstance()),
                    location);

            // Send review to Firebase database
            MainForFragmentActivity.sFirebaseDataSource.createReview(newReview);

            UploadTask uploadTask = storageReference.child("images/" + user.getUid() + "-" +  imageFileName + ".jpg").putBytes(imageBytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(getApplicationContext(), "Error with creating Review", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Toast.makeText(getApplicationContext(), "Review successfully sent", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    public void handleImage(View view) {
        prepImage();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return;

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void prepImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.e("NewReviewActivity", "Photo file creation error: " + e);
            }
            if (photoFile != null) {
                try {
                    photoURI = FileProvider.getUriForFile(this, "edu.dartmouth.cs65.dartmouthnaps.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                } catch (Exception error) {
                    Log.e("NewReviewActivity", "Starting camera error: " + error);
                }
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            imageFileName = null;
            return;
        }

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                Crop.of(photoURI, photoURI).asSquare().start(NewReviewActivity.this);
                break;
            }
            case Crop.REQUEST_PICK: {
                break;
            }
            case Crop.REQUEST_CROP: {
                Bitmap takenImage = BitmapFactory.decodeFile(mCurrentPhotoPath);
                locationImage.setImageBitmap(takenImage);
                try {
                    FileOutputStream fos = new FileOutputStream(mCurrentPhotoPath);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    takenImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    imageBytes = stream.toByteArray();
                    fos.write(imageBytes);
                    fos.flush();
                    fos.close();

                    // Hide Upload icon after setting first image
                    uploadImage.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e("NewReviewActivity", "Saving image error" + e);
                }
                break;
            }
        }
    }
}
