package ml.kelp.kelpsplace;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;

public class FileObjAdapter extends RecyclerView.Adapter<FileObjAdapter.ViewHolder>  {

    private SelectionTracker FileSelectionTracker;



    public ArrayList<FileObj> Files;

    public FileObjAdapter() {
        Files = new ArrayList<FileObj>();


    }

    public void UpdateAdapter(ArrayList<FileObj> files){

        Files = files;
    }


    public void setSelectionTracker(SelectionTracker mSelectionTracker) {
        FileSelectionTracker = mSelectionTracker;
    }

    @Override
    public int getItemCount() {
        try {
            return Files.size();
        }catch (Exception e){
            return -1;
        }
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

        Log.d("vhbind", "FST-HasSelection:"+FileSelectionTracker.hasSelection());
        Log.d("vhbind", "fileName:"+file.getFileName());
        Log.d("vhbind", "isSelected:"+FileSelectionTracker.isSelected(file.getFileID()));
        Log.d("vhbind", "------------------------------------------------------");
        viewHolder.bind(file, FileSelectionTracker.isSelected(file.getFileID()) );
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView filename;
        public TextView filetype;
        public View fileRow;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            filename = itemView.findViewById(R.id.file_name);
            filetype = itemView.findViewById(R.id.file_type);
            fileRow = itemView;


        }

        void bind(FileObj item, boolean isSelected) {
            filename.setText(item.getFileName());
            // If the item is selected then we change its state to activated
            filetype.setText(item.getFileType());



            if (isSelected){
                fileRow.setPressed(true);
            }else{
                fileRow.setPressed(false);
            }

        }


        FileObjDetailLookup.ItemDetails getFileDetails() {
            return new FileDetails(getAdapterPosition(), Files.get(getAdapterPosition()));
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