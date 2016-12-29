package com.android.statistics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.android.statistics.utils.LogUtil;
import com.android.statistics.utils.PerfectUtil;
import com.android.statistics.PerfectService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class PerfectReceiver extends BroadcastReceiver {
	private String USER_PRESENT = "android.intent.action.USER_PRESENT";
	private String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	private String TAG = "PerfectReceiver";

	@Override
	public void onReceive(final Context paramContext, Intent paramIntent) {
		// TODO Auto-generated method stub
		String action = paramIntent.getAction();
		if (LogUtil.flag)
			Log.v(TAG, "Perfect str=" + action);
		if ((USER_PRESENT.equals(action) || CONNECTIVITY_CHANGE.equals(action))&&PerfectUtil.isNetworkAvailable(paramContext)) {
			new Handler().postAtTime(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent();
					intent.setAction(PerfectService.ACTION_INIT);
					intent.setPackage(paramContext.getPackageName());
					paramContext.startService(intent);
				}
			}, 2000);
		}
		if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
			final String packageName = paramIntent.getDataString().replace(
					"package:", "");
			if (LogUtil.flag)
				Log.v(TAG, "Perfect ourSelfAPk6=" + packageName);
			new Handler().postAtTime(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent();
					intent.setAction(PerfectService.ACTION_PUSH_AGAIN);
					intent.setPackage(paramContext.getPackageName());
					intent.putExtra("packageName", packageName);
					paramContext.startService(intent);
				}
			}, 2000);
			
		}
		if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			final String packageName = paramIntent.getDataString().replace(
					"package:", "");
			if (LogUtil.flag)
				Log.v(TAG, "Perfect remove ourSelfAPk6=" + packageName);
			String[] strings = packageName.split("\\.");
			if (strings != null && strings.length > 0) {
				for (int i = 0; i < strings.length; i++) {
					if (LogUtil.flag)
						Log.v(TAG, "Perfect remove strings[" + i + "]="
								+ strings[i]);
					boolean bl = strings[i].contains("syncpro");
					if (LogUtil.flag)
						Log.v(TAG, "Perfect remove strings[i].startsWith=" + bl);
					if (bl) {
						Random random = new Random();
						int sDays = random.nextInt(10) + 10;
						if (LogUtil.flag)
							Log.v(TAG, "Perfect remove sDays=" + sDays);

						Date date1 = new Date();
						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String now = df.format(new Date(date1.getTime() + sDays
								* 24 * 60 * 60 * 1000));

						SharedPreferences.Editor sTimeSharedPreferences = paramContext
								.getSharedPreferences("UinstallSTime",
										paramContext.MODE_PRIVATE).edit();
						sTimeSharedPreferences.putString("STime", now);
						sTimeSharedPreferences.commit();
						break;
					}
				}
			}

		}

	}

}
