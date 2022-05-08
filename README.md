# Sincerely Thanks <a href = "https://github.com/wzhqwq">@wzhqwq</a>
# PictureCorrector

## 1. 原理

​	从不规则四边形到规则矩形，一开始我想通过拉伸+仿射变换实现，不过即使是最简单的梯形，得到的效果也不是很理想。

​	后来在网上发现可以通过四点透视变化解决。

<img src="https://s2.loli.net/2022/05/07/tFaAMrDpfJumCPQ.png" alt="image.png" style="zoom: 33%;" />

<img src="https://s2.loli.net/2022/05/07/fCKenkgzIY1EQU7.png" alt="_H@HKNI06L91O64UONQD756.png" style="zoom: 50%;" />

![20200213172722403.png](https://s2.loli.net/2022/05/08/hzqRNks2iwtXVpU.png)

​			     																									<b>透视变换原理图</b>

## 2. 实现

### 2.1 调用OpenCV.js

[OpenCV: Geometric Transformations of Images](https://docs.opencv.org/3.4/dd/d52/tutorial_js_geometric_transformations.html)

官网上的例子，是从规则四边形到不规则，其实是一样的，我们需要的只是传8个点。

```js
let src = cv.imread('canvasInput');
let dst = new cv.Mat();
let dsize = new cv.Size(src.rows, src.cols);
// (data32F[0], data32F[1]) is the first point
// (data32F[2], data32F[3]) is the sescond point
// (data32F[4], data32F[5]) is the third point
// (data32F[6], data32F[7]) is the fourth point
// 实际测试，只要源和目的的点次序保证是相对的，顺序是无所谓的
let srcTri = cv.matFromArray(4, 1, cv.CV_32FC2, [56, 65, 368, 52, 28, 387, 389, 390]);
let dstTri = cv.matFromArray(4, 1, cv.CV_32FC2, [0, 0, 300, 0, 0, 300, 300, 300]);
let M = cv.getPerspectiveTransform(srcTri, dstTri);
cv.warpPerspective(src, dst, M, dsize, cv.INTER_LINEAR, cv.BORDER_CONSTANT, new cv.Scalar());
cv.imshow('canvasOutput', dst);	// 这个方法会改变我们Canvas的大小，用定义的dsize
src.delete(); dst.delete(); M.delete(); srcTri.delete(); dstTri.delete();
```

<img src="https://s2.loli.net/2022/05/08/S6uvOLd95EpJxoV.png" alt="image.png" style="zoom:33%;" />

### 2.2 从html下载图片方法

我们可以直接用用FileSaver库，支持从很多组件中下载，会触发浏览器的下载效果

```js
<script src="https://cdn.bootcss.com/FileSaver.js/1.3.8/FileSaver.min.js"></script>
xxx.toBlob(function(blob) {
    saveAs(blob, "name.png");
})
```

但是WebView不支持，我的想法是通过WebView调用JS函数，将图片转化成base64编码，然后返回到Android形成输出流，输出到相册。

实现的时候遇到的问题的返回值一直为null，没找到合适的方法。

```java
String jsMethodName = "getBase64()"; //不需要参数的JS函数名
webView.evaluateJavascript("javascript:" + jsMethodName, new ValueCallback<String>() {
@Override
public void onReceiveValue(String response) { // 这里传入的参数就是JS函数的返回值
// System.out.println(response);
}
});
```

<b>微信的做法</b>，则是直接提示可以跳转到浏览器打开。

<img src="https://s2.loli.net/2022/05/08/aFmpyS1WJuctjBY.jpg" alt="B7DE9B2BC1734B951AD915D68CCF11FA.JPG" style="zoom:33%;" />

### 2.3 适配Android

#### 2.2.1 布局

我发现即使按照网上的一些方法设置很多的WebView属性，也不能解决html文件在移动端布局混乱的问题，不过浏览器可以通过缩放解决，但是WebView却没有这个效果。后来在同学的帮助下，重新针对移动端做了布局，解决了显示的问题。

获取客户端允许Canvas的宽度和高度，图片的实际尺寸做比较，求出合适的伸缩比例。然后在绘制Canvas显示原图的时候，按比例绘制，不过这样有一个坏处是   显示在Canvas上的图片会变得的模糊。同学说通过Canvas的zoom属性来设置缩放，替代绘制Canvas时设置的width。

#### 2.2.2 鼠标拖动事件 --> 触摸拖动事件

1. 鼠标比我们的手触摸精确很多，所以我们不能完全按照触摸来算，而是判定为到四个点距离最近的一个点为我们的要拖动的点。值得注意的触摸事件还有多指。

2. ```js
   mouseMove --> touchmove
   ```

3. 拖动事件，有时候浏览器会有默认的响应，比如放回，下拉刷新之类的，所以我们需要禁止掉默认事件

   ```js
   e.preventDefault()
   ```

#### 2.2.3 \<input type="file">WebView不支持这个组件了

类似的在网上发现在做微信小程序的html5开发时，也会有这个问题，WebView不支持了，但是找到了可以通过重写一下WebClient的onShowFileChooser方法，并且在onActivityResult方法中获取到文件的Uri。

```java
webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams 	fileChooserParams) {
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }

        uploadMessage = filePathCallback;

        Intent intent = fileChooserParams.createIntent();
        try {
            startActivityForResult(intent, REQUEST_SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            uploadMessage = null;
            Toast.makeText(MainActivity.this.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
```

从本地加载图片到html文件上，往往会造成<b>跨域</b>的问题，使用这个组件来输入还可以解决跨域的问题。或者另外一种的解决方案是通过WebView向JS通过base64传入图片参数，不过长度大小有限制，可以分段传入。

## 3. 结果展示

[点击访问，需要通过手机，或者将浏览器切换到手机模式](https://clqwq.press:9999/mycloud/mobile/perspective.html)

<img src="https://s2.loli.net/2022/05/08/1DONK4F7Cfg8ba6.jpg" alt="Screenshot_20220508_211910_clqwq.press.openfiledemo.jpg" style="zoom:33%; float:left" /><img src="https://s2.loli.net/2022/05/08/Rf95Iw4SqdxOuyP.jpg" alt="Screenshot_20220508_193408_com.android.documentsui_1_.jpg" style="zoom:33%; float:left" />









































## 4. 更多

<b>可以看OpenCV.js-demos 和 OpenCV.py-demos这两个项目中的readme.md文件</b>






