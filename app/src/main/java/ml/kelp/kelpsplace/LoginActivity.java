package ml.kelp.kelpsplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class LoginActivity extends AppCompatActivity {

    Button   login_button;

    RequestQueue requestQueue;
    String ApiEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("kelpml", Context.MODE_PRIVATE);

        String ApiChk = sharedPreferences.getString("apikey", "null");

        if (!ApiChk.equals("null")){
            Context context = getApplicationContext();

            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);
        }


        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);


    }



    public void Login(View view){


        final String username_input   = ((EditText)findViewById(R.id.input_username)).getText().toString();
        final String password_input   = ((EditText)findViewById(R.id.input_password)).getText().toString();



        ApiEndpoint = "https://kelp.ml/api/login";

        requestQueue = Volley.newRequestQueue(this);

        // Creating the JsonObjectRequest class called obreq, passing required parameters:
        //GET is used to fetch data from the server, JsonURL is the URL to be fetched from.


        StringRequest loginRequest = new StringRequest(Request.Method.POST, ApiEndpoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String success = jsonObj.getString("success");

                    if (success.equals("true")){

                        String ApiKey = jsonObj.getString("api_key");

                        SharedPreferences.Editor editor = getSharedPreferences("kelpml", MODE_PRIVATE).edit();
                        editor.putString("apikey", ApiKey);
                        editor.apply();

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        finish();  //Kill the activity from which you will go to next activity
                        startActivity(i);

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
                Toast toast = Toast.makeText(context, "Networking error occured" , duration);
                toast.show();
                Log.e("Volley", "Error");

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();


                params.put("username", username_input);
                params.put("password", password_input);

                return params;
            }
        };

        requestQueue.add(loginRequest);

    }


}


