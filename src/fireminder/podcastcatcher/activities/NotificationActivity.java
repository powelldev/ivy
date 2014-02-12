package fireminder.podcastcatcher.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class NotificationActivity extends Activity{
    
    @Override
    public void onCreate(Bundle icile){
        super.onCreate(icile);
        Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
    }

}
