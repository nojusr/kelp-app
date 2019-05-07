package ml.kelp.kelpsplace;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

public class FileLoaderTask extends AsyncTask{

    private Context context;
    private String link;

    ConstraintLayout fileView;
    ImageView imgView;
    ProgressBar prog;

    public FileLoaderTask(Context context, String link) {
        this.context = context;
        this.link = link;
    }


    @Override
    protected  void onPreExecute(){
        this.imgView = ((Activity) context).findViewById(R.id.file_img_view);
        this.prog = ((Activity) context).findViewById(R.id.file_view_progress);
        prog.setVisibility(View.VISIBLE);

    }


    @Override
    protected Object doInBackground(Object[] objects) {
        URL url = null;
        try {
            url = new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            return bmp;

        }catch (Exception e){

        }

        return null;
    }

    @Override
    protected void onPostExecute(Object result){
        if (result instanceof Bitmap){
            imgView.setImageBitmap((Bitmap) result);
            imgView.setVisibility(View.VISIBLE);
            prog.setVisibility(View.GONE);

        }

        else{
            imgView.setImageDrawable(context.getDrawable(R.drawable.ic_view_file));
            imgView.setVisibility(View.VISIBLE);
            prog.setVisibility(View.GONE);

        }

    }

}