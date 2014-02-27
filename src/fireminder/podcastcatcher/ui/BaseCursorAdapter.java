package fireminder.podcastcatcher.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;

public abstract class BaseCursorAdapter extends CursorAdapter{

    Cursor cursor;

    public BaseCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.cursor = c;
    }

    @Override
    public void notifyDataSetChanged() {
        cursor.requery();
        super.notifyDataSetChanged();
    }


}
