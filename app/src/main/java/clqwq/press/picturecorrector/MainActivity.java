package clqwq.press.picturecorrector;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {


    private Button take;        // 拍照
    private Button choose;      // 从相册选择

    private ImageView ivShowPicture;
    private static final int REQUEST_CHOOSE = 1;    // 相册标识
    private static final int REQUEST_CAMERA = 2;    // 相机标识
    private String mFilePath;
    private String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化控件
        take = (Button) findViewById(R.id.take);
        choose = (Button) findViewById(R.id.choose);
        ivShowPicture = (ImageView) findViewById(R.id.ivShowPicture);

        // 控件绑定点击事件
        addListener();

    }

    // 获取权限
    private void getPermission() {
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
    }


    // 控件绑定点击事件
    private void addListener() {
        // 打开相机
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openCamera();
                System.out.println(path);
            }
        });
        // 打开相册
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermission();
                //在这里跳转到手机系统相册里面
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
        });
    }


    // 拍照后存储并显示图片
    private void openCamera() {
        File fileDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        mFilePath = fileDir.getAbsolutePath() + "/" + fileName;
        Uri uri = null;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Pictures");
        } else {
            contentValues.put(MediaStore.Images.Media.DATA, mFilePath);
        }
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");
        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    private void afterCamera() {
        try {
            //查询的条件语句
            String selection = MediaStore.Images.Media.DISPLAY_NAME + "=? ";
            //查询的sql
            @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, selection, new String[]{fileName}, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ivShowPicture.setImageBitmap(bitmap);// 显示图片
                    // 打开一个新的处理页面，也就是JS页面

                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "no photo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
String path;
    private void afterChoose(Intent data) {
        try {
            Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            path = cursor.getString(columnIndex);  //获取照片路径
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivShowPicture.setImageBitmap(bitmap);
            // 界面跳转

        } catch (Exception e) {
            // TODO Auto-generatedcatch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回数据
            // 如果是从相机中回退的
            if (requestCode == REQUEST_CAMERA) {
                afterCamera();
            } else if (requestCode == REQUEST_CHOOSE) {
                afterChoose(data);
            }
            Intent intent = new Intent(getApplicationContext(), CorrectActivity.class);
            startActivity(intent);
        }
    }

}

