package ml.kelp.kelpsplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileObj {
    private String FileID;
    private String FileName;
    private String FileType;

    public FileObj(String fileid, String filename, String filetype){
        FileID = fileid;
        FileName = filename;
        FileType = filetype;
    }


    public String getFileID(){
        return FileID;
    }

    public String getFileName(){
        return FileName;
    }

    public String getFileType(){
        return FileType;
    }

}
