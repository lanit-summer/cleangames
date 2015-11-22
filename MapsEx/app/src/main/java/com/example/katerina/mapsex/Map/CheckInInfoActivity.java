package com.example.katerina.mapsex.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import com.datamodel.datamodels.CheckIn;
import com.datamodel.datamodels.Param;
import com.google.android.gms.maps.model.LatLng;
import com.example.katerina.mapsex.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.example.katerina.mapsex.R;


public class CheckInInfoActivity extends Activity{
    String a, b;
    Bitmap imageBitmap;
    EditText commentText;
    private static final int CAMERA_REQUEST = 1888;
    static final int SELECT_FILE = 1;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        commentText = (EditText) findViewById(R.id.EditText01);

        final Button submitButton = (Button) findViewById(R.id.button_submit);
        final EditText garbage1=(EditText)findViewById(R.id.garbage1);
        final EditText garbage2=(EditText)findViewById(R.id.garbage2);
        final EditText garbage3=(EditText)findViewById(R.id.garbage3);
        final EditText garbage4=(EditText)findViewById(R.id.garbage4);
        final EditText garbage5=(EditText)findViewById(R.id.garbage5);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                    LocationProvider locationProvider=LocationProvider.Initialize();
                    LatLng location=locationProvider.getLocataion();
                    ArrayList<Param> garbage=new ArrayList<Param>();
                    garbage.add(new Param("Пластик",Integer.parseInt(garbage1.getText().toString())));
                    garbage.add(new Param("Металл",Integer.parseInt(garbage2.getText().toString())));
                    garbage.add(new Param("Стекло",Integer.parseInt(garbage3.getText().toString())));
                    garbage.add(new Param("Смешанный мусор",Integer.parseInt(garbage4.getText().toString())));
                    garbage.add(new Param("Батарейки",Integer.parseInt(garbage5.getText().toString())));
                    CheckIn checkIn=new CheckIn(commentText.getText().toString(),garbage,location,imageBitmap);
                    locationProvider.setCheckin(checkIn);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("comment",a);
                    returnIntent.putExtra("garbage",b);
                    setResult(RESULT_OK,returnIntent);
                    finish();
            }


        });

        final Button button = (Button) findViewById(R.id.button);
         button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Integer temp= Integer.parseInt(garbage1.getText().toString());
               if (temp>0){
                   temp--;
                   garbage1.setText(temp.toString());
               }
            }

            });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage1.getText().toString());
                temp++;
                garbage1.setText(temp.toString());
            }
        });

        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage2.getText().toString());
                if (temp>0){
                    temp--;
                    garbage2.setText(temp.toString());
                }
            }

        });

        final Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage2.getText().toString());
                temp++;
                garbage2.setText(temp.toString());
            }

        });

        final Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage3.getText().toString());
                if (temp>0){
                    temp--;
                    garbage3.setText(temp.toString());
                }
            }

        });

        final Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage3.getText().toString());
                temp++;
                garbage3.setText(temp.toString());
            }

        });

        final Button button7 = (Button) findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage4.getText().toString());
                if (temp>0){
                    temp--;
                    garbage4.setText(temp.toString());
                }
            }

        });

        final Button button8 = (Button) findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage4.getText().toString());
                temp++;
                garbage4.setText(temp.toString());
            }

        });

        final Button button9 = (Button) findViewById(R.id.button9);
        button9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp= Integer.parseInt(garbage5.getText().toString());
                if (temp>0){
                    temp--;
                    garbage5.getText().clear();
                    garbage5.getText().append(temp.toString());}
            }

        });

        final Button button10 = (Button) findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer temp = Integer.parseInt(garbage5.getText().toString());
                temp++;
                garbage5.setText(temp.toString());
            }

        });

        final ImageButton photo = (ImageButton) findViewById(R.id.TakeAPhoto);
        photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectImage();

            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageBitmap = thumbnail;

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                imageBitmap = BitmapFactory.decodeFile(selectedImagePath, options);
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckInInfoActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
