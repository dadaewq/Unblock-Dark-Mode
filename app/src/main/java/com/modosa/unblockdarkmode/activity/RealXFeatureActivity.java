package com.modosa.unblockdarkmode.activity;

import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.modosa.unblockdarkmode.R;
import com.modosa.unblockdarkmode.fragment.XFeatureFragment;

/**
 * @author dadaewq
 */
public class RealXFeatureActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preference);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.alertdialog_background);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new XFeatureFragment(), "XFeature").commit();


    }

}
