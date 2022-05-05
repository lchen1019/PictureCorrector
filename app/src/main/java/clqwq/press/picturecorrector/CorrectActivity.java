package clqwq.press.picturecorrector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CorrectActivity extends AppCompatActivity {

    private WebView webView;
    private Button check;
    private Button save;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct);
        // 初始化组件
        webView = findViewById(R.id.webView);
        check = findViewById(R.id.check);
        save = findViewById(R.id.save);

        //允许webview对文件的操作
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl("file:///android_asset/test2.html");

        // 注册监听
        addListener();
    }

    String url = "https://alifei02.cfp.cn/creative/vcg/800/new/VCG41560336195.jpg";

    private void addListener() {
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(url);
                System.out.println(url);
                System.out.println(url);
                System.out.println(url);
                System.out.println(url);
                System.out.println("javascript:show('" + url + "')");
                System.out.println("javascript:show('" + url + "')");
                System.out.println("javascript:show('" + url + "')");
                System.out.println("javascript:show('" + url + "')");
                webView.loadUrl("javascript:show('" + url + "')");
            }
        });
    }




}
