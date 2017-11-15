# WilddogRoom Demo

使用野狗 WilddogRoom SDK建立多人视频通话的示例

## 本地运行
首先确认本机已经安装 [Android](http://developer.android.com/index.html)运行环境 ， `git` 和 `Andriod Studio` 开发环境 ，然后执行下列指令：

```
git clone git@github.com:WildDogTeam/video-demo-android-conference.git
cd  video-demo-android-conference
```

我们通过一个简单的视频会议示例来说明 WilddogRoom SDK 的用法。
<div class="env">
    <p class="env-title">环境准备</p>
    <ul>
        <li> Android Studio 1.5 以上版本 </li>
        <li> JDK 7.0 及以上版本 </li>
        <li> Android 手机系统 4.1 以上版本， 即 Android SDK 16 以上版本 </li>
    </ul>
</div>

## 1. 创建应用

首先，在控制面板中创建应用。

<img src="/images/video_quickstart_create.png" alt="video_quickstart_create">

## 2. 开启匿名登录

应用创建成功后，进入 管理应用-身份认证-登录方式，开启匿名登录。

<img src="/images/openanonymous.png" alt="video_quickstart_openanonymous">

## 3. 开启实时视频通话

进入 管理应用-实时视频通话，开启视频通话功能。此处注意记下配置页面的`VideoAppID`

<img src="/images/video_quickstart_openVideo.png" alt="video_quickstart_openVideo">

<blockquote class="notice">
  <p><strong>提示：</strong></p>
  如果之前没有使用过sync服务的需要手动开启
</blockquote>
<img src="/images/opensync.png" alt="video_quickstart_openSync">

## 4. 导入快速入门

Android 快速入门是使用 Android Studio 创建的 Android 工程，使用 `Android Studio File --> New --> Import Project` 导入快速入门。

<blockquote class="notice">
  <p><strong>提示：</strong></p>
  使用 Android Studio 导入安卓项目时，可能会在 Gradle build 时卡顿在 Building gradle project info 界面上，原因及解决方案请<a href='https://github.com/WildDogTeam/wilddog-doc2/blob/master/Android%20Studio%20Gradle%20%E9%85%8D%E7%BD%AE%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88.md'> 参考该文档 </a>。
</blockquote>

## 5. 配置应用APPID

快速入门 Constants 中的 WILDDOG_VIDEO_ID 如下图所示

<img src="/images/video_quickstart_videoappid.png" alt="video_quickstart_videoappid">

## 6. 运行快速入门

连接安卓手机（4.1 以上版本），运行快速入门。

### 版本声明

本应用使用的是 WilddogRoom 2.0+ SDK。

### 联系方式

如果发现有bug,请及时和我们取得联系。

工单地址 : https://wilddog.kf5.com/hc/request/new/

客服电话： 400-616-0980

客服邮箱：support@wilddog.com

## 更多示例

这里分类汇总了 WildDog平台上的示例程序和开源应用，　[链接地址](https://github.com/WildDogTeam/wilddog-demos)
　　

## License
MIT
http://wilddog.mit-license.org/
## 文档

[完整文档](https://docs.wilddog.com/conference/Android/guide/0-concepts.html)


