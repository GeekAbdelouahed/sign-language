package asl.abdelouahed;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by abdelouahed on 2/10/18.
 */

public class BaseFragment extends Fragment {

    protected Handler handler;
    protected HandlerThread handlerThread;

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    private Toast toast;

    protected void makeToast(String msg) {

        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}