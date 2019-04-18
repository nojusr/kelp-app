package ml.kelp.kelpsplace;

import android.support.annotation.Nullable;

import androidx.recyclerview.selection.ItemDetailsLookup;


public class FileDetails extends ItemDetailsLookup.ItemDetails {

    private int position;
    private String item;

    public FileDetails(int position, FileObj item) {
        this.position = position;
        this.item = item.getFileID();
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Object getSelectionKey() {
        return item;
    }
}