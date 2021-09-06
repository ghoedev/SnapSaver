package com.ghoe.snapsaver.activity;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.ghoe.snapsaver.whatsapp_code.Sticker;
import com.ghoe.snapsaver.whatsapp_code.StickerPack;
import com.ghoe.snapsaver.whatsapp_code.adapter.StickerAdapter;
import com.ghoe.snapsaver.whatsapp_code.model.StickerModel;
import com.ghoe.snapsaver.whatsapp_code.task.GetStickers;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.ghoe.snapsaver.R;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class StickerMakerActivity extends BaseActivity implements GetStickers.Callbacks {
    private Toolbar toolbar;

    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final String EXTRA_STICKERPACK = "stickerpack";
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String path;
    private ArrayList<String> strings;
    private StickerAdapter adapter;
    private ArrayList<StickerPack> stickerPacks = new ArrayList<>();
    private List<Sticker> mStickers;
    private ArrayList<StickerModel> stickerModels = new ArrayList<>();
    private RecyclerView recyclerView;
    private List<String> mEmojis,mDownloadFiles;
    private String android_play_store_link;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_maker);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        initView();
        initLogic();
        initListener();
    }

    @Override
    public void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void initLogic() {
        stickerPacks = new ArrayList<>();

        path = Environment.getExternalStorageDirectory() + "/" + "snapsaver/sticker";
        mStickers = new ArrayList<>();
        stickerModels = new ArrayList<>();
        mEmojis = new ArrayList<>();
        mDownloadFiles = new ArrayList<>();
        mEmojis.add("");
        adapter = new StickerAdapter(this, stickerPacks);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        new GetStickers(this, this, getResources().getString(R.string.json_link)).execute();
    }

    @Override
    public void initListener() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                onBackPressed();
            }
        });

    }



    @Override
    public void onListLoaded(String jsonResult, boolean jsonSwitch) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        try {
            if (jsonResult != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(jsonResult);
                    android_play_store_link = jsonResponse.getString("android_play_store_link");
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("sticker_packs");
                    Log.d(TAG, "onListLoaded: " + jsonMainNode.length());
                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        Log.d(TAG, "onListLoaded: " + jsonChildNode.getString("name"));

                        stickerPacks.add(new StickerPack(
                                jsonChildNode.getString("identifier"),
                                jsonChildNode.getString("name"),
                                jsonChildNode.getString("publisher"),
                                getLastBitFromUrl(jsonChildNode.getString("tray_image_file")).replace(" ","_"),
                                jsonChildNode.getString("publisher_email"),
                                jsonChildNode.getString("publisher_website"),
                                jsonChildNode.getString("privacy_policy_website"),
                                jsonChildNode.getString("license_agreement_website")
                        ));
                        JSONArray stickers = jsonChildNode.getJSONArray("stickers");
                        Log.d(TAG, "onListLoaded: " + stickers.length());
                        for (int j = 0; j < stickers.length(); j++) {
                            JSONObject jsonStickersChildNode = stickers.getJSONObject(j);
                            mStickers.add(new Sticker(
                                    getLastBitFromUrl(jsonStickersChildNode.getString("image_file")).replace(".png",".webp"),
                                    mEmojis
                            ));
                            mDownloadFiles.add(jsonStickersChildNode.getString("image_file"));
                        }
                        Log.d(TAG, "onListLoaded: " + mStickers.size());
                        Hawk.put(jsonChildNode.getString("identifier"), mStickers);
                        stickerPacks.get(i).setAndroidPlayStoreLink(android_play_store_link);
                        stickerPacks.get(i).setStickers(Hawk.get(jsonChildNode.getString("identifier"),new ArrayList<Sticker>()));
                        /*stickerModels.add(new StickerModel(
                                jsonChildNode.getString("name"),
                                mStickers.get(0).imageFileName,
                                mStickers.get(1).imageFileName,
                                mStickers.get(2).imageFileName,
                                mStickers.get(2).imageFileName,
                                mDownloadFiles
                        ));*/
                        mStickers.clear();
                    }
                    Hawk.put("sticker_packs", stickerPacks);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter = new StickerAdapter(this, stickerPacks);
                recyclerView.setAdapter(adapter);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onListLoaded: " + stickerPacks.size());
    }

    private static String getLastBitFromUrl(final String url) {
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }



    public static void SaveImage(Bitmap finalBitmap, String name, String identifier) {

        String root = path + "/" + identifier;
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = name;
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveTryImage(Bitmap finalBitmap, String name, String identifier) {

        String root = path + "/" + identifier;
        File myDir = new File(root + "/" + "try");
        myDir.mkdirs();
        String fname = name.replace(".png","").replace(" ","_") + ".png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 40, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}