package asl.abdelouahed;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "Asl::HomeActivity";
    private Fragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        mFragment = new TouchFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();

    }

 /*   public void switchFragment(View view) {

        mFragment = mFragment.getClass() == CascadeFragment.class ? new TouchFragment() : new CascadeFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();

    }*/
}
