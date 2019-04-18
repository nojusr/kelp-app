package ml.kelp.kelpsplace;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;

public class FileObjKeyProvider extends ItemKeyProvider<String> {

    private List<String> items;

    public FileObjKeyProvider() {
        super(SCOPE_MAPPED);

        this.items = new ArrayList<>();

    }

    public void updateData(FileObjAdapter adp){

        List<FileObj> files = adp.Files;

        this.items.clear();

        Log.d("test1", "files:"+files.size());

        for (int i = 0; i < files.size(); i++){
            FileObj buf = files.get(i);
            items.add(buf.getFileID());
        }

        Log.d("test1", "keys:"+items.size());
    }

    @Nullable
    @Override
    public String getKey(int i) {
        return this.items.get(i);


    }

    @Override
    public int getPosition(@NonNull String key) {
        return this.items.indexOf(key);
    }
}