package ml.kelp.kelpsplace;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;

public class FileObjDetailLookup extends ItemDetailsLookup {


    private final RecyclerView FileObjList;

    FileObjDetailLookup(RecyclerView recyclerView) {
        FileObjList = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails getItemDetails(@NonNull MotionEvent e) {

        View view = FileObjList.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = FileObjList.getChildViewHolder(view);
            if (holder instanceof FileObjAdapter.ViewHolder) {

                Log.d("FileDetails", "getting file details");

                return ((FileObjAdapter.ViewHolder) holder).getFileDetails();
            }
        }
        return null;
    }


}
