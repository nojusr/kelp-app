package ml.kelp.kelpsplace;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.webkit.URLUtil;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class Upload extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        String ApiChk = sharedPreferences.getString("apikey", "null");

        if (ApiChk.equals("null")){
            Context context = getApplicationContext();

            Intent i = new Intent(Upload.this, LoginActivity.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);
        }

        // INTENT HANDLING
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleIntentUpload(intent);
        }

        Intent i = new Intent(Upload.this, MainActivity.class);
        finish();  //Kill the activity from which you will go to next activity
        startActivity(i);

        super.onCreate(savedInstanceState);

    }

    // Handles uploads when coming from other apps

    private void handleIntentUpload(Intent intent) {
        Uri selectedfile = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        String p = selectedfile.getPath(); // "/mnt/sdcard/FileName.mp3"
        Context context = getApplicationContext();
        File file=FileUtils.getFile(context, selectedfile);

        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        String ApiKey = sharedPreferences.getString("apikey", "null");

        if (ApiKey.equals("null")){


            Intent i = new Intent(Upload.this, LoginActivity.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);
        }

        String path = file.getPath();

        new UploadFileAsync().execute(path, ApiKey);
    }


    private class UploadFileAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String sourceFileUri = params[0];

                HttpsURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);


                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = "https://kelp.ml/api/upload";

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpsURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"api_key\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(params[1] + lineEnd);



                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"u_file\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);



                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();

                        if (serverResponseCode == 200) {




                            try {

                                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(conn.getInputStream()));
                                String inputLine;
                                StringBuffer response = new StringBuffer();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                in.close();

                                JSONObject jsonObj = new JSONObject(response.toString());


                                String success = jsonObj.getString("success");

                                // close the streams //

                                fileInputStream.close();
                                dos.flush();
                                dos.close();

                                if (success.equals("true")){


                                    String link = jsonObj.getString("link");
                                    return link;


                                }
                                else{
                                    String error = jsonObj.getString("reason");
                                    return error;
                                }

                            }
                            // Try and catch are included to handle any errors due to JSON
                            catch (JSONException e) {

                                // close the streams //

                                fileInputStream.close();
                                dos.flush();
                                dos.close();

                                e.printStackTrace();
                                return "JSON exception occured.";
                            }

                        }

                        // close the streams //

                        fileInputStream.close();
                        dos.flush();
                        dos.close();



                    } catch (Exception e) {


                        dos.flush();
                        dos.close();

                        // dialog.dismiss();
                        e.printStackTrace();
                        return "Failed to grab image.";

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
                return "Unknown error occured.";
            }

            return "You are not supposed to be seeing this";
        }

        @Override
        protected void onPostExecute(String result) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;



            // check if asyncthread output is a link; copy link to clipboard if true; show error msg if false

            boolean isValid = URLUtil.isValidUrl(result);

            if (isValid) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("link", result);
                clipboard.setPrimaryClip(clip);

                Toast toast = Toast.makeText(context, "Upload successful; copied link to clipboard.", duration);
                toast.show();

            } else {

                Toast toast = Toast.makeText(context, result, duration);
                toast.show();
            }

        }

    }
}
