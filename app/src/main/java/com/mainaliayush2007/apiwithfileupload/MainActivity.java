package com.mainaliayush2007.apiwithfileupload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imgView;
    Button btnSelect, btnUpload;
    String imagePath;
    boolean permissionGranted = false;
    Retrofit retrofit;
    Bitmap bitmap;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imgView = findViewById(R.id.imgView);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpload = findViewById(R.id.btnUpload);


        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.imgur.com/ ")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        btnUpload.setOnClickListener(this);
        btnSelect.setOnClickListener(this);
        imgView.setVisibility(View.GONE);
        btnUpload.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading File!");
        progressDialog.setTitle("API");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            permissionGranted = true;
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSelect:
                if (permissionGranted) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 0);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Grant File Read Permission", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnUpload:
                uploadImage();
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(getApplicationContext(), "Please select an Image", Toast.LENGTH_SHORT).show();
            }
        }

        Uri uri = data.getData();
        imagePath = getRealPathFromURI(uri);
        previewImage(imagePath);
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(colIndex);
        System.out.println("Image Path is " + result);
        cursor.close();
        return result;

    }

    private void previewImage(String imagePath) {

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            imgView.setImageBitmap(bitmap);
            imgView.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(true);
        }
    }

    private void uploadImage() {

        progressDialog.show();
        File file = new File(imagePath);
        System.out.println("The image name is " + file.getName());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "image",    /* type */
                file.getName(),    /*name of image file */
                RequestBody.create(MediaType.parse("image/*"),  /* media type */
                        byteArray    /* actual content which is image here*/
                )
        );
        ImageAPI imageAPI = retrofit.create(ImageAPI.class);
        Call<ResponseBody> call = imageAPI.upload("Client-ID 9cdb0fba5f6b1d0", body);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.isSuccessful()) {
                    System.out.println("Response Success " + response.toString());
                    progressDialog.hide();
                    Toast.makeText(getApplicationContext(), "Successfully uploaded Image " + response.message(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                System.out.println("Response Errror" + t.getLocalizedMessage());
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permissionGranted = true;
                }
                break;

            default:

                break;
        }
    }
}
