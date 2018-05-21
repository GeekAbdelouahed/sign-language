package asl.abdelouahed.ui.fragment;

import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by abdelouahed on 2/10/18.
 */

public abstract class BaseFragment extends Fragment {

    private Toast toast;

    protected void makeToast(String msg) {

        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}