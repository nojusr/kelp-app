package ml.kelp.kelpsplace;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;


import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;

public class MainActivity extends AppCompatActivity {


    // misc

    RequestQueue requestQueue;
    String ApiEndpoint;
    RecyclerView FileList;
    int reqcount;

    // files
    FileObjAdapter FileAdp;
    FileObjKeyProvider FileKeys;
    SelectionTracker FileSelectionTracker;
    SelectionTracker.SelectionObserver FileSelectionObserver;
    View FileActionSelector;
    int OldFileCount;

    // tabs

    private TabLayout tabLayout;
    private ViewPager viewPager;

    // constants

    static final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        String ApiChk = sharedPreferences.getString("apikey", "null");

        if (ApiChk.equals("null")){

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        }
        /*
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
        }*/




        FileAdp = new FileObjAdapter();
        FileKeys = new FileObjKeyProvider();
        OldFileCount = 0;






        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        final Context context = getApplicationContext();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2){

                    FileList =  findViewById(R.id.file_layout);
                    FileList.setLayoutManager(new LinearLayoutManager(context));


                    FillViewUploads();



                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2){
                    FileSelectionTracker.clearSelection();
                }

                if (tab.getPosition() == 1){
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View pastemain = findViewById(R.id.paste_main);
                    imm.hideSoftInputFromWindow(pastemain.getWindowToken(), 0);
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2){

                    FileList =  findViewById(R.id.file_layout);
                    FileList.setLayoutManager(new LinearLayoutManager(context));

                    FillViewUploads();


                }
            }
        });




    }




    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new UploadFragment(), getResources().getString(R.string.tab_text_1));
        adapter.addFragment(new PasteFragment(), getResources().getString(R.string.tab_text_3));
        adapter.addFragment(new ViewUploadsFragment(), getResources().getString(R.string.tab_text_2));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // the program cannot run w/o storage permissions!
                    System.exit(0);
                }
                break;
            }
            case WRITE_STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // the program cannot run w/o storage permissions!
                    System.exit(0);
                }
                break;
            }
        }
    }


    public void DownloadFileView(View v){
        ImageButton downloadbt = findViewById(R.id.download_file_view);
        String fileID = null;
        try{
            Object out = downloadbt.getTag(R.id.download_file_view);
            if (out instanceof String){
                fileID = (String)out;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (fileID != null){
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), "Downloading..." , duration);
            toast.show();
            reqcount = 1;
            new DownloadFile(getApplicationContext()).execute(fileID);


        }else{
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), "Failed to download file." , duration);
            toast.show();
        }
    }

    public void CopyFileView(View v){
        ImageButton copybt = findViewById(R.id.copy_file_view);
        String link = null;
        try{
            Object out = copybt.getTag(R.id.copy_file_view);
            if (out instanceof String){
                link = (String)out;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (link != null){

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("link", link);
            clipboard.setPrimaryClip(clip);

            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), "Copied link to clipboard." , duration);
            toast.show();

        }else{
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), "Failed to get link." , duration);
            toast.show();
        }
    }


    public void CloseFileView(View v){
        ConstraintLayout fileView = findViewById(R.id.file_view);
        ImageView imgview = findViewById(R.id.file_img_view);
        fileView.setVisibility(View.GONE);
        imgview.setVisibility(View.GONE);

    }

    // Fill out ViewUploads
    public void FillViewUploads(){
        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        final Context context = getApplicationContext();

        final String ApiKey = sharedPreferences.getString("apikey", "null");

        String ApiEndpoint = "https://kelp.ml/api/fetch/files";

        RequestQueue requestQueue = Volley.newRequestQueue(context);


        final ProgressBar FileProg = findViewById(R.id.file_load_progress);


        FileProg.animate()
                .alpha(1)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        FileProg.setVisibility(View.VISIBLE);
                    }
                });


        StringRequest fileListRequest = new StringRequest(Request.Method.POST, ApiEndpoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String success = jsonObj.getString("success");

                    if (success.equals("true")){

                        String filename = "";
                        String filetype = "";
                        String fileid = "";

                        ArrayList<FileObj> fileList = new ArrayList<FileObj>();

                        JSONArray files = jsonObj.getJSONArray("files");
                        for (int i = 0; i < files.length(); i++) {
                            JSONObject file = files.getJSONObject(i);

                            filename = file.getString("org_filename");
                            filetype = file.getString("filetype");
                            fileid = file.getString("filename");

                            FileObj fobj = new FileObj(fileid, filename, filetype);

                            fileList.add(fobj);
                        }

                        FileProg.animate()
                                .alpha(0)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        FileProg.setVisibility(View.INVISIBLE);
                                    }
                                });

                        int filecount = fileList.size();
                        int listlen = FileList.getChildCount();




                        if (FileSelectionTracker == null || OldFileCount != filecount || listlen < 1) {



                            FileAdp.UpdateAdapter(fileList);
                            FileAdp.notifyDataSetChanged();

                            Context con = getApplicationContext();

                            FileList =  findViewById(R.id.file_layout);
                            FileList.setLayoutManager(new LinearLayoutManager(con));

                            FileList.setAdapter(FileAdp);

                            FileActionSelector =  findViewById(R.id.file_action_select);

                            FileAdp.notifyDataSetChanged();




                            FileKeys.updateData(FileAdp);



                            FileSelectionTracker = new SelectionTracker.Builder<>("file_selection",
                                    FileList,
                                    FileKeys,
                                    new FileObjDetailLookup(FileList),
                                    StorageStrategy.createStringStorage())
                                    .withSelectionPredicate(SelectionPredicates.<String>createSelectAnything())
                                    .build();


                            final TextView FilesSelected = findViewById(R.id.selected_amount);






                            FileSelectionTracker.addObserver(new SelectionTracker.SelectionObserver() {

                                @Override
                                public void onItemStateChanged(@NonNull Object key, boolean selected) {
                                    String buf = "";
                                    if (key.getClass() == FileObj.class) {
                                        FileObj temp = (FileObj) key;
                                        buf = temp.getFileID();
                                    }

                                    super.onItemStateChanged(buf, selected);



                                }

                                @Override
                                public void onSelectionChanged() {
                                    super.onSelectionChanged();
                                    if (FileSelectionTracker.hasSelection()) {
                                        FilesSelected.setText(String.format("Selected: %d", FileSelectionTracker.getSelection().size()));

                                        FileActionSelector.setVisibility(View.VISIBLE);

                                        /*
                                        FileActionSelector.animate()
                                                .alpha(1.0f)
                                                .setDuration(200)
                                                .setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        FileActionSelector.setVisibility(View.VISIBLE);
                                                    }
                                                });*/






                                    } else {


                                        FileActionSelector.setVisibility(View.GONE);

                                        FilesSelected.setText("Selected: 0");

                                        /*



                                        FileActionSelector.animate()
                                                .alpha(0.0f)
                                                .setDuration(200)
                                                .setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        FileActionSelector.setVisibility(View.GONE);
                                                    }
                                                });*/

                                    }

                                }

                            });







                            OldFileCount = filecount;
                            FileAdp.setSelectionTracker(FileSelectionTracker);
                        }




                    }
                    else{

                    }

                }
                // Try and catch are included to handle any errors due to JSON
                catch (JSONException e) {
                    // If an error occurs, this prints the error to the log
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.getMessage());

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();

                params.put("api_key", ApiKey);

                return params;
            }
        };

        requestQueue.add(fileListRequest);







    }

    // update file uploads
    public void UpdateViewUploads(){
        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        final Context context = getApplicationContext();

        final String ApiKey = sharedPreferences.getString("apikey", "null");

        String ApiEndpoint = "https://kelp.ml/api/fetch/files";

        RequestQueue requestQueue = Volley.newRequestQueue(context);


        final ProgressBar FileProg = findViewById(R.id.file_load_progress);


        FileProg.animate()
                .alpha(1)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        FileProg.setVisibility(View.VISIBLE);
                    }
                });


        StringRequest fileListRequest = new StringRequest(Request.Method.POST, ApiEndpoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String success = jsonObj.getString("success");

                    if (success.equals("true")){

                        String filename = "";
                        String filetype = "";
                        String fileid = "";

                        ArrayList<FileObj> fileList = new ArrayList<FileObj>();

                        JSONArray files = jsonObj.getJSONArray("files");
                        for (int i = 0; i < files.length(); i++) {
                            JSONObject file = files.getJSONObject(i);

                            filename = file.getString("org_filename");
                            filetype = file.getString("filetype");
                            fileid = file.getString("filename");

                            FileObj fobj = new FileObj(fileid, filename, filetype);

                            fileList.add(fobj);
                        }

                        FileProg.animate()
                                .alpha(0)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        FileProg.setVisibility(View.INVISIBLE);
                                    }
                                });

                        int filecount = fileList.size();
                        int listlen = FileList.getChildCount();




                        if (FileSelectionTracker == null || OldFileCount != filecount || listlen < 1) {

                            FileAdp.UpdateAdapter(fileList);
                            FileAdp.notifyDataSetChanged();

                            Context con = getApplicationContext();

                            FileList =  findViewById(R.id.file_layout);
                            FileList.setLayoutManager(new LinearLayoutManager(con));

                            FileList.setAdapter(FileAdp);

                            FileActionSelector =  findViewById(R.id.file_action_select);

                            FileAdp.notifyDataSetChanged();

                            FileKeys.updateData(FileAdp);

                            OldFileCount = filecount;
                            FileAdp.setSelectionTracker(FileSelectionTracker);
                        }




                    }
                    else{

                    }

                }
                // Try and catch are included to handle any errors due to JSON
                catch (JSONException e) {
                    // If an error occurs, this prints the error to the log
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.getMessage());

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();

                params.put("api_key", ApiKey);

                return params;
            }
        };

        requestQueue.add(fileListRequest);

    }


    public void ClearFileSelect(View view){
        FileSelectionTracker.clearSelection();
    }


    public void DownloadFileSelect(View view){
        Selection<String> selFileID = FileSelectionTracker.getSelection();


        Iterator<String> selIterator = selFileID.iterator();

        reqcount = 0;

        requestQueue = Volley.newRequestQueue(this);

        while (selIterator.hasNext()){
            reqcount += 1;
            new DownloadFile(getApplicationContext()).execute(selIterator.next());
        }

        FileSelectionTracker.clearSelection();
    }


    private class DownloadFile extends AsyncTask<String, Integer, String> {


        Context context;

        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        final String ApiKey = sharedPreferences.getString("apikey", "null");

        public DownloadFile(Context context) {
            this.context = context;
        }


        @Override
        protected String doInBackground(String... fileID) {
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;

            FileObj sel = new FileObj("null", "null", "null");

            ApiEndpoint = "https://kelp.ml/u/";

            for (int i = 0; i < FileAdp.Files.size(); i++){
                FileObj buf = FileAdp.Files.get(i);
                if (buf.getFileID().equals(fileID[0])){
                    sel = buf;
                    break;
                }
            }

            try {
                URL url = new URL(ApiEndpoint+sel.getFileID()+"."+sel.getFileType());
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                String homedir = String.valueOf(Environment.getExternalStorageDirectory());


                String pathtest = homedir+"/Download/kelp/"+sel.getFileName()+"."+sel.getFileType();
                Log.d("pathtest", pathtest);

                File out = new File(pathtest);
                Log.d("pathtest", "file-creation-success");
                output = new FileOutputStream(out);
                Log.d("pathtest", "problem solved");


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return "good";
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                Log.d("DOWNLOAD EXP", e.toString());
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String res){
            reqcount -= 1;
            if (reqcount == 0){
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "Download(s) finished." , duration);
                toast.show();
            }

        }
    }


    public void DeleteFileSelect(View view){
        Selection<String> selFileID = FileSelectionTracker.getSelection();


        Iterator<String> selIterator = selFileID.iterator();

        reqcount = 0;

        requestQueue = Volley.newRequestQueue(this);

        while (selIterator.hasNext()){
            reqcount += 1;
            DeleteFile(selIterator.next(), requestQueue);
        }

        FileSelectionTracker.clearSelection();

    }


    public void DeleteFile(String fileID, RequestQueue rq){

        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        final String ApiKey = sharedPreferences.getString("apikey", "null");
        final String fileID1 = fileID;

        ApiEndpoint = "https://kelp.ml/api/upload/delete";



        StringRequest deleteRequest = new StringRequest(Request.Method.POST, ApiEndpoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;



                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String success = jsonObj.getString("success");

                    if (success.equals("true")){



                    }
                    else{
                        String Error = jsonObj.getString("reason");

                    }

                }
                // Try and catch are included to handle any errors due to JSON
                catch (JSONException e) {
                    Toast toast = Toast.makeText(context, "Programming error occured." , duration);
                    toast.show();
                    // If an error occurs, this prints the error to the log
                    e.printStackTrace();
                }

                reqcount -= 1;

                if (reqcount == 0){
                    UpdateViewUploads();
                }

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "Networking error occured." , duration);
                toast.show();
                Log.e("Volley", "Error");

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();


                params.put("api_key", ApiKey);
                params.put("file_id", fileID1);
                return params;
            }
        };

        rq.add(deleteRequest);
    }



    // Paste upload func
    public void Paste(View view){

        // getting vars
        final String PasteBody   = ((EditText)findViewById(R.id.paste_main)).getText().toString();
        final String PasteTitle   = ((EditText)findViewById(R.id.paste_title)).getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        final String ApiKey = sharedPreferences.getString("apikey", "null");


        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        ApiEndpoint = "https://kelp.ml/api/paste";

        requestQueue = Volley.newRequestQueue(this);


        // pre-request checks, just for good measure
        if (PasteBody.isEmpty()){
            Toast toast = Toast.makeText(context, "No paste text provided.", duration);
            toast.show();
            return;
        }




        StringRequest pasteRequest = new StringRequest(Request.Method.POST, ApiEndpoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String success = jsonObj.getString("success");

                    if (success.equals("true")){

                        String result = jsonObj.getString("web_link");


                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", result);
                        clipboard.setPrimaryClip(clip);

                        Toast toast = Toast.makeText(context, "Paste successful; copied link to clipboard.", duration);
                        toast.show();


                    }
                    else{
                        String Error = jsonObj.getString("reason");
                        Toast toast = Toast.makeText(context, Error , duration);
                        toast.show();
                    }

                }
                // Try and catch are included to handle any errors due to JSON
                catch (JSONException e) {
                    Toast toast = Toast.makeText(context, "Programming error occured." , duration);
                    toast.show();
                    // If an error occurs, this prints the error to the log
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "Networking error occured." , duration);
                toast.show();
                Log.e("Volley", "Error");

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();


                params.put("api_key", ApiKey);
                params.put("u_paste", PasteBody);
                params.put("paste_name", PasteTitle);

                return params;
            }
        };

        requestQueue.add(pasteRequest);

    }


    // The three functions below handle uploading from the app itself

    public void Upload(View view){
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            final Uri selectedfile = data.getData(); //The uri with the location of the file

            Context context = getApplicationContext();
            String path = FileUtils.getPath(context, selectedfile);

            SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

            String ApiKey = sharedPreferences.getString("apikey", "null");

            if (ApiKey.equals("null")){


                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                finish();  //Kill the activity from which you will go to next activity
                startActivity(i);
            }


            new UploadFileAsync().execute(path, ApiKey);

        }
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
                long progress = 0;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;
                File sourceFile = new File(sourceFileUri);
                int totalSize = (int)sourceFile.length();

                String filename = sourceFile.getName();


                // Set some UI elements to inform user.

                final String infoMsg = "Uploading file: "+filename;


                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {


                        TextView info = findViewById(R.id.info);
                        info.setText(infoMsg);

                        ProgressBar uploadProgress = findViewById(R.id.uploadProgress);

                        info.animate().alpha(1).setDuration(300).setListener(null);
                        uploadProgress.animate().alpha(1).setDuration(300).setListener(null);
                    }

                });


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


                            // Update progress bar

                            progress += bytesRead;

                            publishProgress((int)((progress*100)/totalSize));
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


            // clean up UI elements

            try {
                ProgressBar uploadProgress = (ProgressBar) findViewById(R.id.uploadProgress);
                TextView info = findViewById(R.id.info);

                uploadProgress.setAlpha(0);

                info.animate()
                        .alpha(0)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                TextView temp = findViewById(R.id.info);
                                temp.setText("");
                            }
                        });

                uploadProgress.animate()
                        .alpha(0)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ProgressBar temp = findViewById(R.id.uploadProgress);
                                temp.setProgress(0);
                            }
                        });
            } catch (Exception e){
                Log.e("UI", "Failed to clean up UploadFragment UI elements");
            }

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

        @Override
        protected void onProgressUpdate(Integer... values) {
            ProgressBar uploadProgress = (ProgressBar)findViewById(R.id.uploadProgress);


            ObjectAnimator animation = ObjectAnimator.ofInt(uploadProgress, "progress", values[0]);
            animation.setDuration(500); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setStartDelay(50);
            animation.start();

            uploadProgress.setProgress(values[0]);

        }
    }





}

