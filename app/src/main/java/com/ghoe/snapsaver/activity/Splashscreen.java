package com.ghoe.snapsaver.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ghoe.snapsaver.R;
import com.ghoe.snapsaver.utils.Constants;

import java.io.File;

import in.codeshuffle.typewriterview.TypeWriterView;

public class Splashscreen extends BaseActivity {

    private static final int LOADING = 3500;
    private ImageView imgLogo;
    private TextView tvVersion;
    private ProgressBar progressSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        Untuk Menghilangkan Status Bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);


        initView();
        showPermission();
        initListener();
    }

    @Override
    public void initView() {
        imgLogo = findViewById(R.id.img_logo);
        progressSplash = findViewById(R.id.progress_splash);
        tvVersion = findViewById(R.id.tv_version);
    }

    @Override
    public void initLogic() {


        tvVersion.setText(getString(R.string.version) + " " +showVersionApp());
        //Create Object and refer to layout view
        TypeWriterView typeWriterView=(TypeWriterView)findViewById(R.id.typeWriterView);

        //Setting each character animation delay
        typeWriterView.setDelay(3);

        //Setting music effect On/Off
        typeWriterView.setWithMusic(false);

        //Animating Text
        typeWriterView.animateText("Snapsaver");



//      Loading pindah activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Remove Animation. This is required to be called when you want to minimize the app while animation is going on. Call this in onPause() or onStop()

                typeWriterView.removeAnimation();

                Intent in = new Intent();
                in.setAction(Intent.ACTION_VIEW);
                in.setClass(Splashscreen.this, MainActivity.class);
                startActivity(in);
                finish();
            }
        }, LOADING);

    }

    @Override
    public void initListener() {

    }

    private void showPermission(){
        if (ActivityCompat.checkSelfPermission(Splashscreen.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Log.d("Permission", "Permission is not granted requesting ");
            new AlertDialog.Builder(Splashscreen.this)
                    .setTitle("App Permission")
                    .setMessage("This app need permission to read and save whatsapp folder app")
                    .setCancelable(false)
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Splashscreen.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 123);

                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finishAffinity();
                        }
                    })
                    .show();
        } else {
            Log.d("Permission", "Permission is granted");
            initLogic();
        }
    }

    private String showVersionApp(){
        String versionName = null;
        try{
            versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission", "Permission has been Granted");
                initLogic();
                File dir = new File(Constants.SnapSaverPath);
                if (!dir.exists()){
                    dir.mkdirs();
                    Toast.makeText(this, "Success"+ dir, Toast.LENGTH_SHORT).show();
                    Log.d("Path Created", "Success");
                }
            } else {
                Log.d("Permission", "Permission has been denied or request cancelled");
                finishAffinity();
            }
        }
    }
}