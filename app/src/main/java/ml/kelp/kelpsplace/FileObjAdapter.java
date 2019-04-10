package ml.kelp.kelpsplace;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FileObjAdapter extends RecyclerView.Adapter<FileObjAdapter.ViewHolder>  {


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView filename;
        public TextView filetype;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            filename = itemView.findViewById(R.id.file_name);
            filetype = itemView.findViewById(R.id.file_type);
        }
    }

    private ArrayList<FileObj> Files;

    public FileObjAdapter() {

    }


    public void UpdateAdapter(ArrayList<FileObj> files){
        Files = files;
    }


    @Override
    public FileObjAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View fileView = inflater.inflate(R.layout.file_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(fileView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FileObjAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        FileObj file = Files.get(position);

        // Set item views based on your views and data model
        TextView fname = viewHolder.filename;
        fname.setText(file.getFileName());


        TextView ftype = viewHolder.filetype;
        ftype.setText(file.getFileType());
    }

    @Override
    public int getItemCount() {
        try {
            return Files.size();
        }catch (Exception e){
            return 0;
        }
    }
}

/*
public class ContactsAdapter extends
        RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            messageButton = (Button) itemView.findViewById(R.id.message_button);
        }
    }
}*/