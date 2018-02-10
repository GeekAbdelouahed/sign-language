package asl.abdelouahed;

import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by abdelouahed on 2/10/18.
 */

public class BaseFragment extends Fragment {

    protected void makeToast(String msg){
        Toast.makeText(getContext() , msg , Toast.LENGTH_SHORT).show();
    }
}
