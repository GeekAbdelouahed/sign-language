package asl.abdelouahed;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import asl.abdelouahed.recognition.mvp.RecognitionFragment;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "Asl::HomeActivity";
    private Fragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        mFragment = new RecognitionFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();

    }
}
