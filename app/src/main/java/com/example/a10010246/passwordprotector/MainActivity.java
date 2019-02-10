/*

right now, program forgets what mCurrentPhotoPath is if I close the app and re-open it, so I need to
save the mCurrentPhotoPath string to a text file that I store within the app (this could also be the
place where I store the usernames/password info

when the app opens, if it is not the first time setup, it looks at this file, reads the photoPath,
follows the photoPath to the picture, displays the picture in an imageView at the top. There will be
an imageView2, button, and textView at the bottom. When the button is pressed, the user will be prompted to take
a picture (same code I already have), that picture will be placed within the imageView. The pictures
will then be compared using the Face API. The results of the comparison will be set in the TextView,
and if successful, a Button at the bottom will become active, and clicking it will take the user to
the main passwords screen


line 183 and line 129 are commented out for api < 23

1) draw face rectangles over bitmaps once the photos are taken         2) if the second picture doesnt see a face, treat it the same as if it saw a face but doesn't match

let user reset app from inside - setContentView to stage 1 and reset preferences to false         ---   add fail cases for when pictures arent recognized the first time

//new imageBitMap framing - line 207, 226, 276, 651


in the constraint layout, make it so the title, username, password, etc of the custom adapter are better linked so they dont move
do the thing with the incorrect photos taken, because the passwords are DONE!!!!

 */

package com.example.a10010246.passwordprotector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.*;
import java.text.SimpleDateFormat;
//import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;
import com.microsoft.projectoxford.face.rest.ClientException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final int PICK_IMAGE = 1;
    private final int CAMERA_REQUEST = 2;
    private final int MY_CAMERA_PERMISSION_CODE = 3;
    private final int CAMERA_REQUEST_SETUP = 4;
    private final int MY_CAMERA_PERMISSION_SETUP_CODE = 5;
    static final int REQUEST_TAKE_PHOTO = 6;
    //private final String pref_previously_started = "hello";
    //String pref_previously_started = "hiu";
    int pref_previously_started = 45;
    private ProgressDialog detectionProgressDialog;
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "2c42e759836c40c2b7a0f7925ef56169"); //endpoint, key
    //Button button1;
    //Button button2;
    //Button button3;
    Button buttonMisc;
    TextView textViewMisc;
    Button buttonProceed;
    Button button1;
    TextView textView1;
    //ImageView imageView1;
    //ImageView imageView2;
    //ImageView imageView3;
    ImageView imageViewOrig;
    ImageView imageViewTest;
    int lastButtonPressed = 2;
    String mCurrentPhotoPath;
    String passwordFile = "passwordProtectorData.txt";         //the file for the photo path
    String passwordGroupsFile = "passwordProtectorData2.txt";  //the file for the password groups
    boolean loggedIn = false;
    ArrayList<UUID> faceIDs;
    ArrayList<Face> faces;
    int count = 0;

    ImageView imageViewPic;
    Button buttonAdd;
    Button buttonReset;
    ListView listView;
    ArrayList<PasswordGroup> passwordGroups;
    JSONArray passwordGroupsJSONArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //writeToFile("o", this);

        button1 = (Button) findViewById(R.id.button1);
        //button2 = (Button)findViewById(R.id.button2);
        //button3 = (Button)findViewById(R.id.button3);
        buttonMisc = (Button)findViewById(R.id.buttonMisc);
        buttonProceed = (Button)findViewById(R.id.buttonProceed);
        textViewMisc = (TextView)findViewById(R.id.textViewMisc);
        textView1 = (TextView)findViewById(R.id.textView1);
        imageViewOrig = (ImageView)findViewById(R.id.imageViewOrig);
        imageViewTest = (ImageView)findViewById(R.id.imageViewTest);

        faceIDs = new ArrayList<UUID>();
        faces = new ArrayList<Face>();
        //imageView1 = (ImageView)findViewById(R.id.imageView1);
        //imageView2 = (ImageView)findViewById(R.id.imageView2);
        //imageView3 = (ImageView)findViewById(R.id.imageView3);

        //imageViewPic = (ImageView)findViewById(R.id.imageViewPic);
        //buttonAdd = (Button)findViewById(R.id.buttonAdd);
        //listView = (ListView)findViewById(R.id.listView);

        /*button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(gallIntent, "Select Picture"), PICK_IMAGE);
            }
        });*/
        textView1.setText("Press button to take picture");

        button1.setVisibility(View.INVISIBLE);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_PERMISSION_CODE);
                } else {*/
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                //}
            }
        });
        /*button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //File imgFile = new  File(mCurrentPhotoPath);
                File imgFile = new File(readFromFile());

                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView3.setImageBitmap(myBitmap);

                }
            }
        });*/
        buttonMisc.setVisibility(View.INVISIBLE);
        textViewMisc.setVisibility(View.INVISIBLE);
        buttonMisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewMisc.setText(readFromFile(passwordFile));

                /*File imgFile = new File(readFromFile());
                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageViewOrig.setImageBitmap(myBitmap);
                    Log.d("TAG", "image put up through button");
                }*/
            }
        });
        buttonProceed.setVisibility(View.INVISIBLE);
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStageThree();
            }
        });


        detectionProgressDialog = new ProgressDialog(this);



        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.commit();
            //Toast.makeText(this, "First time ayy", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "A");

            /*if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "B");
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_SETUP_CODE);
            } else {*/
                dispatchTakePictureIntent();
            //}
            loggedIn = true;

            File imgFile = new File(readFromFile(passwordFile));
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageViewOrig.setImageBitmap(myBitmap);
                //detectAndFrame(myBitmap, imageViewOrig);
                Log.d("TAG", "image put up");
            }
            Log.d("TAG", "Z");

        }
        else {   //if app has been setup already

            //Log.d("TAG", "YUP");
            //TODO: set up the intent which lets the user take a picture and compares it - set layout to stage 2

            //set layout to stage 2
            //set imageViewOrig to original picture
            //start the asyncTask for the faceID for the original picture

            File imgFile = new File(readFromFile(passwordFile));       //read mCurrrentPhotoPath from the textFile, follow path to image, set imageView as image
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageViewOrig.setImageBitmap(myBitmap);
                //detectAndFrame(myBitmap, imageViewOrig);
                Log.d("TAG", "image put up through 1");
                Log.d("TAG", "HELLO");
                Log.d("TAG", "L");
                Log.d("TAG", readFromFile(passwordFile));
                getFaceID(myBitmap);
            }


            /*if (passesFaceTest) {
                   loggedIn = true;
            }*/
        }
        if (loggedIn) { //if finishes setting up app for first time or successfully logs in

            //TODO: stuff for the regular password manager screen once logged in
        }

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                imageView1.setImageBitmap(bitmap);

                // This is the new addition.
                detectAndFrame(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        //coming from taking photo in LOGIN, stage 2 phase (after pressing button1)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            Bitmap photo = (Bitmap)data.getExtras().get("data");   //bitmap of the photo just taken
            imageViewTest.setImageBitmap(photo);
            //detectAndFrame(photo, imageViewTest);

            //File imgFile = new File(readFromFile());
            //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());   //bitmap of the stored image

            //double confidence = compare(myBitmap, photo);
            getFaceID(photo);
            Log.d("TAG", "GCount: " + count);

            /*double confidence = newCompare();
            Log.d("TAG", "HCount: " + count);

            if (confidence > 0.5) {
                textView1.setText("Confidence Value: " + confidence + " - Identity Confirmed");
                setStageThree();
            }
            else {
                textView1.setText("Confidence Value: " + confidence + " - Identity Test Failed");
            }*/
        }

        //coming from taking photo in SETUP, stage 1 phase
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setStageThree();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == MY_CAMERA_PERMISSION_SETUP_CODE) {
            Log.d("TAG", "C");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "E");
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void getFaceID(Bitmap imageBitmap1) {

        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
        imageBitmap1.compress(Bitmap.CompressFormat.JPEG, 100, outputStream1);
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(outputStream1.toByteArray());

        //constructs AsnycTask that will determine the faceID of the face within the inputStream, and add it to the faceIDs ArrayList
        final AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {
            @Override
            protected Face[] doInBackground(InputStream... params) {
                try {
                    publishProgress("Detecting...");
                    Face[] result = faceServiceClient.detect(
                            params[0],
                            true,
                            false,
                            null
                    );
                    if (result == null) {
                        publishProgress("Detection Finished. Nothing detected");
                        Log.d("TAG", "1");
                        return null;
                    }
                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    Log.d("TAG", "2");
                    return result;
                } catch (Exception e) {
                    publishProgress("Detection Failed");
                    Log.d("TAG", "3");
                    return null;
                }
            }
            @Override
            protected void onPreExecute() {
                detectionProgressDialog.show();
            }
            @Override
            protected void onProgressUpdate(String... progress) {
                detectionProgressDialog.setMessage(progress[0]);
            }
            @Override
            protected void onPostExecute(Face[] result) {
                detectionProgressDialog.dismiss();
                count++;
                Log.d("TAG", "count increased");
                Log.d("TAG", "result: " + result.toString());
                Log.d("TAG", "result length: " + result.length);
                button1.setVisibility(View.VISIBLE);
                if (result.length == 0) {
                    Toast.makeText(MainActivity.this, "No faces found", Toast.LENGTH_LONG).show();
                    faceIDs.add(null);
                    Log.d("TAG", "ACount: "+count);
                }
                else if (result.length > 1) {
                    Toast.makeText(MainActivity.this, "More than 1 face found", Toast.LENGTH_LONG).show();
                    faceIDs.add(null);
                    Log.d("TAG", "BCount: "+count);
                }
                else if (result.length == 1) {
                    faceIDs.add(result[0].faceId);
                    Log.d("TAG", "CCount: "+count);
                }
                if (count != 2) {
                    Log.d("TAG", "faceIDs.size(): " + faceIDs.size());
                    Log.d("TAG", "HCount: " + count);
                    return;
                }
                else {
                    Log.d("TAG", "ICount: " + count);
                    /*double confidence = newCompare();
                    Log.d("TAG", "HCount: " + count);

                    if (confidence > 0.5) {
                        textView1.setText("Confidence Value: " + confidence + " - Identity Confirmed");
                        setStageThree();
                    }
                    else {
                        textView1.setText("Confidence Value: " + confidence + " - Identity Test Failed");
                    }*/
                    newCompare();
                }
            }
        };
        detectTask.execute(inputStream1);
    }

    private double newCompare() {
        Log.d("TAG", "faceIds: " + faceIDs.toString());
        if (faceIDs.size() < 2) {
            return -1;
        }
        //if (faceIDs.get(0).toString().equals("") || faceIDs.get(1).toString().equals("")) {
        if (faceIDs.contains(null)) {
            Log.d("TAG", "DCount: " + count);
            faceIDs.remove(1);
            count--;
            textView1.setText("Face match failed");
            return -1;
        }
        else{ //if faceIds has two faceIds
            /*try {
                Log.d("TAG", "ECount: " + count);
                return faceServiceClient.verify(faceIDs.get(0), faceIDs.get(1)).confidence;
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            Log.d("TAG", "PCount");
            class CompareTask extends AsyncTask<Void, Void, Double> {

                private Exception exception;

                protected Double doInBackground(Void... voids) {
                    try {
                        return faceServiceClient.verify(faceIDs.get(0), faceIDs.get(1)).confidence;
                    } catch (Exception e) {
                        this.exception = e;
                        return null;
                    }
                }

                protected void onPostExecute(Double confidence) {
                    Log.d("TAG", "HCount: " + count);

                    if (confidence > 0.7) {
                        textView1.setText("Confidence Value: " + confidence + " - Identity Confirmed");
                        //setStageThree();
                        buttonProceed.setVisibility(View.VISIBLE);
                    }
                    else {
                        textView1.setText("Confidence Value: " + confidence + " - Identity Test Failed");
                        faceIDs.remove(1);
                        count --;
                    }
                }
            }
            Log.d("TAG", "OCount");
            new CompareTask().execute();
        }
        return -1;
    }



    // Detect faces by uploading face images
    // Frame faces after detection
    private void detectAndFrame(final Bitmap imageBitmap, final ImageView imageViewTemp)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTaskBoxes =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            //publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            if (result == null)
                            {
                                //publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            /*publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));*/
                            return result;
                        } catch (Exception e) {
                            //publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        detectionProgressDialog.dismiss();
                        if (result == null) return;
                        imageViewTemp.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();
                    }
                };
        detectTaskBoxes.execute(inputStream);
    }

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();   //define mCurrentPhotoPath in current run cycle
        writeToFile(passwordFile, mCurrentPhotoPath, this);   //immediately copy the mCurrentPhotoPath to a textfile

        return image;
    }

    private void dispatchTakePictureIntent() {
        Log.d("TAG", "F");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d("TAG", "J");
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("TAG", "G");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                Log.d("TAG", "K");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d("TAG", "H");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.d("TAG", "I");
            }
        }
    }

    private void writeToFile(String file, String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readFromFile(String file) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    private JSONArray readFromFileJSONArray(String file) {
        String temp = readFromFile(file);
        if (temp.length() > 0) {
            try {
                JSONArray tempArray = new JSONArray(temp);
                return tempArray;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private void setStageThree() {
        Log.d("TAG2", "HAHAHAHAH");
        setContentView(R.layout.password_screen);
        imageViewPic = (ImageView)findViewById(R.id.imageViewPic);
        buttonAdd = (Button)findViewById(R.id.buttonAdd);
        buttonReset = (Button)findViewById(R.id.buttonReset);
        listView = (ListView)findViewById(R.id.listView);
        passwordGroups = new ArrayList<PasswordGroup>();
        //TODO: read password groups from the text file

        /*try {  //experimenting by adding password groups to textfile
            JSONObject temp1 = new JSONObject();
            temp1.put("title", "Apple");
            temp1.put("username", "userCool");
            temp1.put("password", "superSecret");

            JSONObject temp2 = new JSONObject();
            temp2.put("title", "Google");
            temp2.put("username", "person");
            temp2.put("password", "myNumber");

            JSONArray tempArray = new JSONArray();
            tempArray.put(temp1);
            tempArray.put(temp2);

            String tempString = tempArray.toString();
            Log.d("TAG1", "Intial tempString: " + tempString);

            writeToFile(passwordGroupsFile, tempString, this);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        String stringPasswords = readFromFile(passwordGroupsFile); //eventually will be the string format of the JSONArray which contains all the passwordGroups
        Log.d("TAG1", "finall tempString: " + stringPasswords);
        //Log.d("TAG1", "stringPasswords: " + stringPasswords);
        if (stringPasswords.length() > 0) { //if there are password groups in the file
            try {
                passwordGroupsJSONArray = new JSONArray(stringPasswords);
                for (int i = 0; i < passwordGroupsJSONArray.length(); i ++) {
                    JSONObject passwordGroupJSON = passwordGroupsJSONArray.getJSONObject(i);
                    passwordGroups.add(new PasswordGroup(passwordGroupJSON.getString("title"), passwordGroupJSON.getString("username"), passwordGroupJSON.getString("password")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (passwordGroups.size() > 0) {
            for (int i = 0; i < passwordGroups.size(); i ++) {
                Log.d("TAG1", "PasswordGroup: " + passwordGroups.get(i));
            }
        }

        Log.d("TAG", "Thing 1");

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG", "Thing 2");
                DialogFragment newFragment = new EnterInfoDialogFragment();
                newFragment.show(getFragmentManager(), "passwordAdd");
                Log.d("TAG", "Thing 3");
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    writeToFile(passwordFile, "", MainActivity.this);
                    writeToFile(passwordGroupsFile, "", MainActivity.this);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(getString(R.string.pref_previously_started), Boolean.FALSE);
                    edit.commit();

                    //restarts app
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    }
                });
            }
        });

        Log.d("TAG", "Thing 4");
        /*passwordGroups.add(new PasswordGroup("Google", "nahor123", "nahor321"));     //temporary for testing password group
        passwordGroups.add(new PasswordGroup("Apple", "appleUser", "safePass"));
        passwordGroups.add(new PasswordGroup("Microsoft", "myName", "superSecret"));
        passwordGroups.add(new PasswordGroup("Company", "user4", "Sack!$"));*/

        Log.d("TAG", "Thing 5");
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.list_layout, passwordGroups);
        listView.setAdapter(customAdapter);
        Log.d("TAG", "Thing 6");

        File imgFile = new File(readFromFile(passwordFile));       //read mCurrrentPhotoPath from the textFile, follow path to image, set imageView as image
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageViewPic.setImageBitmap(myBitmap);
            //detectAndFrame(myBitmap, imageViewPic);
            Log.d("TAG", "image put up through 0");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "Thing 7");
    }

    public class CustomAdapter extends ArrayAdapter<PasswordGroup> {

        Context context;
        List<PasswordGroup> list;

        public CustomAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PasswordGroup> objects) {
            super(context, resource, objects);
            this.context = context;
            list = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Log.d("TAG", "Thing 8");

            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View layoutView = layoutInflater.inflate(R.layout.list_layout, null);

            TextView textViewTitle = (TextView)layoutView.findViewById(R.id.list_textViewTitle);
            TextView textViewUsername = (TextView)layoutView.findViewById(R.id.list_textViewUsername);
            TextView textViewPassword = (TextView)layoutView.findViewById(R.id.list_textViewPassword);
            Button buttonEdit = (Button)layoutView.findViewById(R.id.list_buttonEdit);
            Button buttonDelete = (Button)layoutView.findViewById(R.id.list_buttonDelete);

            Log.d("TAG", "Thing 9");

            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG", "Thing 10");
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);

                    DialogFragment newFragment = new EditInfoDialogFragment();
                    newFragment.setArguments(bundle);
                    Log.d("TAG", "Thing 11");
                    newFragment.show(getFragmentManager(), "passwordEdit");
                    Log.d("TAG", "Thing 12");
                }
            });

            Log.d("TAG", "Thing 13");

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG", "Thing 14");
                    //passwordGroups.remove(position);
                    String tempString = "";
                    if (passwordGroups.size() == 1) {  //if there is only one passwordGroup left, delete the entire file completely
                        tempString = "";
                    }
                    else  { //if there is more than one passwordGroup left, delete only the one selected
                        JSONArray oldArray = readFromFileJSONArray(passwordGroupsFile);
                        JSONArray newArray = new JSONArray();
                        for (int i = 0; i < oldArray.length(); i ++) {
                            if (position != i) {
                                try {
                                    newArray.put(oldArray.get(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        tempString = newArray.toString();
                    }
                    writeToFile(passwordGroupsFile, tempString, MainActivity.this);
                    setStageThree();
                    //notifyDataSetChanged();
                }
            });

            Log.d("TAG", "Thing 15");

            textViewTitle.setText("" + list.get(position).getTitle());
            textViewUsername.setText("" + list.get(position).getUsername());
            textViewPassword.setText("" + list.get(position).getPassword());

            Log.d("TAG", "Thing 16");

            return layoutView;
        }
    }

    @SuppressLint("ValidFragment")
    public class EnterInfoDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Log.d("TAG", "Thing 17");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            final View layoutView = layoutInflater.inflate(R.layout.enterinfo_layout, null);

            Log.d("TAG", "Thing 18");

            builder.setView(layoutView);

                    final EditText editTextTitle = (EditText)layoutView.findViewById(R.id.editTextTitle);
                    final EditText editTextUsername = (EditText)layoutView.findViewById(R.id.editTextUsername);
                    final EditText editTextPassword = (EditText)layoutView.findViewById(R.id.editTextPassword);
                    Button buttonRandomize = (Button)layoutView.findViewById(R.id.buttonRandomize);

                    buttonRandomize.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editTextPassword.setText(randomString(10));
                        }
                    });

                    builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("TAG", "Thing 19");

                            JSONArray tempArray = readFromFileJSONArray(passwordGroupsFile);
                            if (tempArray == null) {
                                tempArray = new JSONArray();
                            }
                            JSONObject tempJSON = new JSONObject();
                            try {
                                tempJSON.put("title", editTextTitle.getText().toString());
                                tempJSON.put("username", editTextUsername.getText().toString());
                                tempJSON.put("password", editTextPassword.getText().toString());

                                tempArray.put(tempJSON);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String tempString = tempArray.toString();
                            writeToFile(passwordGroupsFile, tempString, MainActivity.this);
                            setStageThree();
                            //passwordGroups.add(new PasswordGroup(editTextTitle.getText().toString(), editTextUsername.getText().toString(), editTextPassword.getText().toString()));
                            Log.d("TAG", "Thing 20");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            Log.d("TAG", "Thing 21");
            return builder.create();
        }
    }

    @SuppressLint("ValidFragment")
    public class EditInfoDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Log.d("TAG", "Thing 22");
            final int position = getArguments().getInt("position");
            Log.d("TAG1", "HEY THE POSITION IS " + position);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            final View layoutView = layoutInflater.inflate(R.layout.enterinfo_layout, null);

            Log.d("TAG", "Thing 23");

            builder.setView(layoutView);

                    final EditText editTextTitle = (EditText)layoutView.findViewById(R.id.editTextTitle);
                    final EditText editTextUsername = (EditText)layoutView.findViewById(R.id.editTextUsername);
                    final EditText editTextPassword = (EditText)layoutView.findViewById(R.id.editTextPassword);
                    Button buttonRandomize = (Button)layoutView.findViewById(R.id.buttonRandomize);

                    buttonRandomize.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editTextPassword.setText(randomString(10));
                        }
                    });

                    editTextTitle.setText(passwordGroups.get(position).getTitle());
                    editTextUsername.setText(passwordGroups.get(position).getUsername());
                    editTextPassword.setText(passwordGroups.get(position).getPassword());

                    builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("TAG", "Thing 24");

                            JSONArray oldArray = readFromFileJSONArray(passwordGroupsFile);
                            JSONArray newArray = new JSONArray();

                            for (int i = 0; i < oldArray.length(); i ++) { //runs through old array
                                if (i != position) { //this isn't the one I have to replace
                                    try {
                                        newArray.put(oldArray.get(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else { //this is the one that needs to be replaced
                                    JSONObject tempJSON = new JSONObject();
                                    try {
                                        tempJSON.put("title", editTextTitle.getText().toString());
                                        tempJSON.put("username", editTextUsername.getText().toString());
                                        tempJSON.put("password", editTextPassword.getText().toString());

                                        newArray.put(tempJSON);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }

                            String tempString = newArray.toString();
                            writeToFile(passwordGroupsFile, tempString, MainActivity.this);
                            setStageThree();

                            //passwordGroups.set(position, new PasswordGroup(editTextTitle.getText().toString(), editTextUsername.getText().toString(), editTextPassword.getText().toString()));
                            Log.d("TAG", "Thing 26");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            Log.d("TAG", "Thing 27");
            return builder.create();
        }
    }

    public String randomString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        String subset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!?@#$%";

        for (int i = 0; i < length; i ++) {
            char c = subset.charAt((int)(Math.random()*68));
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}