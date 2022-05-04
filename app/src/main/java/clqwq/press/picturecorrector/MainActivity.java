package clqwq.press.picturecorrector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Button take;
    private Button save;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化webView
        webView = findViewById(R.id.mainView);
        webView.getSettings().setJavaScriptEnabled(true);
        initWebView();
        webView.loadUrl("file:///android_asset/test.html");
        webView.loadUrl("javascript:alert(123)");
        take = findViewById(R.id.take);
        save = findViewById(R.id.save);
        System.out.println();
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryAddPic();
            }
        });

    }




    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        System.out.println(contentUri);
        System.out.println(contentUri);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }





    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

        WebSettings settings = webView.getSettings();
        //设置webview支持javascript
        settings.setJavaScriptEnabled(true);
        //增加 设置脚本是否允许自动打开弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //支持自动加载图片
        settings.setLoadsImagesAutomatically(true);
        //设置webview推荐使用的窗口，使html界面自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        //设置webview保存表单数据
        settings.setSaveFormData(true);
        //设置webview保存密码
        settings.setSavePassword(true);


        int mDensity = getResources().getDisplayMetrics().densityDpi;
        if (mDensity == 120) {
            settings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        } else if (mDensity == 160) {
            settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if (mDensity == 240) {
            settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }
        //支持缩放
        settings.setSupportZoom(true);
        settings.setSupportMultipleWindows(true);
        //设置APP可以缓存
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        //返回上个界面不刷新  允许本地缓存
        settings.setDomStorageEnabled(true);
        //增加 设置缓存LOAD_DEFAULT   LOAD_CACHE_ONLY,LOAD_NO_CACHE
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 设置可以访问文件
        settings.setAllowFileAccess(true);
        //不支持放大缩小
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //不支持放大缩小
        settings.setDisplayZoomControls(false);

        //增加 设置编码格式
        settings.setDefaultTextEncodingName("utf-8");

        webView.setLongClickable(true);
        webView.setScrollbarFadingEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setDrawingCacheEnabled(true);
    }


}