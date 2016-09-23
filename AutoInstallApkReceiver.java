/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.VerificationParams;
import android.os.Bundle;
import android.os.ServiceManager;
import android.content.pm.PackageManager;
import java.io.File;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;

public class AutoInstallApkReceiver extends BroadcastReceiver {


	private String ACTION_NAME = "com.android.AutoInstallApkReceiver.install";
    private Context mContext;
    private IPackageManager mPm;
    private PackageManager mPackageManager;
    private LocalPackageInstallObserver obs = new LocalPackageInstallObserver();
    private String apkFilePath = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
		String action = intent.getAction(); 
        mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        mPackageManager = mContext.getPackageManager();
       // apkFilePath = Environment.getExternalStoragePath()+"/anzhuo.apk";
	   apkFilePath = intent.getExtras().getString("installPak");
	   /*
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
          if(new File(apkFilePath).exists() && !isAppInstalled("cn.goapk.market",apkFilePath)){
            installApk();
          }
        }*/
		Log.d("yunlong", " yunlong-----install> " + apkFilePath);
		if(action.equals(ACTION_NAME)){
			if(new File(apkFilePath).exists()){
				installApk();
			}
		}
    }

    private void installApk(){
        try {
            VerificationParams verificationParams = new VerificationParams(null,
                    null, null, VerificationParams.NO_UID, null);
            mPm.installPackageAsUser(apkFilePath, obs.getBinder(),PackageManager.INSTALL_REPLACE_EXISTING,
                    null, verificationParams, null, 0);
        } catch (Exception e){
             e.printStackTrace();
        }
   }
/*
   private boolean isAppInstalled(String packageName,String apkFilePath) {
     PackageInfo newInfo;
     PackageInfo oldInfo;        
     try {
         oldInfo = mPackageManager.getPackageInfo(packageName, 0);
     } catch (Exception e) {
         oldInfo = null;
         e.printStackTrace();
         return false;
     }
     try {
         newInfo = mPackageManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
     } catch (Exception e) {
         newInfo = null;
         e.printStackTrace();
		 return true;
     }
     if(oldInfo == null){
       return false;
     } else if (newInfo != null && oldInfo.applicationInfo.versionCode < newInfo.applicationInfo.versionCode){
	   return false;
     } else {
	   return true;
	 }
    }
*/
    class LocalPackageInstallObserver extends PackageInstallObserver {
        public void onPackageInstalled(String name, int status, String msg, Bundle extras) {
          {
             if(status == PackageManager.INSTALL_SUCCEEDED){
                mContext.startActivity(mPackageManager.getLaunchIntentForPackage(name));
             }
          }
       }
    }
}
