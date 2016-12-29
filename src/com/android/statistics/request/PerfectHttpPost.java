
package com.android.statistics.request;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.statistics.request.RequestInformation;
import com.android.statistics.utils.HelpUtil;
import com.android.statistics.utils.LogUtil;
import com.android.statistics.utils.PerfectEncryptUtil;
import com.android.statistics.utils.PerfectUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
public class PerfectHttpPost  {
	public static Map<String, String> customHeaders=new HashMap<String, String>();
    private static String MARKET_CHN_ADDRESS = "";
 
    private static String MARKET_OVERSEA_ADDRESS = "";
            
	public static String url;
	private static String mSubsId;
	private static String plmn;
	private static String mCC;

	 public static  void initLogin(Context mContext) {
		    try{
		    	
		    	 if(HelpUtil.getTestServer().equals("NoTestServer")){
		        MARKET_CHN_ADDRESS = "http://nl.nbbrowser.com:88/ad-server-webapi/rest";
		        MARKET_OVERSEA_ADDRESS = "http://nl.nbbrowser.com:88/ad-server-webapi/rest";
		    	 }else{
		        MARKET_CHN_ADDRESS = HelpUtil.getTestServer();
		        MARKET_OVERSEA_ADDRESS = HelpUtil.getTestServer();
		    	 }
		    } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		    
		    if(LogUtil.flag) Log.d("PerfectUtil", "Perfect MARKET_CHN_ADDRESS="+MARKET_CHN_ADDRESS); 
		    if(LogUtil.flag) Log.d("PerfectUtil", "Perfect MARKET_OVERSEA_ADDRESS="+MARKET_OVERSEA_ADDRESS); 
	        // TODO Auto-generated method stub
	        String mDeviceId =  ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
					.getDeviceId();
	        
	        String versionName = HelpUtil.VERSION_NAME;
//	        String versionName = PerfectUtil.getPackageVersionName(
//	        		mContext.getPackageManager(), mContext.getPackageName());
	        
//	        int versionCode = PerfectUtil.getPackageVersionCode(
//	                    mContext.getPackageManager(), mContext.getPackageName());
	        int versionCode = HelpUtil.VERSION_CODE;
	        
	        String mDeviceModelId = HelpUtil.getDeviceModel(); 
	        
	        
	        String mNetWork = PerfectUtil.checkNetworkType(mContext);
	        
	        String mChannelID = HelpUtil.getChannelID(mContext);
	        
	        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Report", 0);
	        String userId = sharedPreferences.getString("aduserId", null);
	        if((userId != null && userId.equals(""))||userId == null){
	            String userMessage = HelpUtil.readUserFile();
	            if(userMessage!=null&&!userMessage.equals("")){
	                JSONObject dataJson;
	              try {
	                  dataJson = new JSONObject(userMessage);
	                  if(LogUtil.flag) Log.d("PerfectUtil", "Perfect version_code11776="+dataJson.getString("version_code")); 
	                  if(LogUtil.flag) Log.d("PerfectUtil", "Perfect user_id11776="+dataJson.getString("user_id")); 
	                  userId = dataJson.getString("user_id");
	              } catch (JSONException e) {
	                  // TODO Auto-generated catch block
	                  if(LogUtil.error_flag) e.printStackTrace();
	                  userId = null;
	              }  
	            }else
	                userId = null;
	        }
	        
	        
	    
	        if(LogUtil.flag) Log.v("PerfectUtil","Perfect PerfectUtil_userId="+userId);
	            
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect test test!");
	            mDeviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
						.getDeviceId();;
	            mSubsId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
						.getSubscriberId();
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect userId4456="+userId);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mDeviceId="+mDeviceId);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mSubsId="+mSubsId);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect versionName="+versionName);
	            try{
	                if(mSubsId != null && !mSubsId.equals(""))
	                    plmn = mSubsId.substring(0,5);
	                else
	                    plmn = "NPlmn"; 
	            }catch(ArrayIndexOutOfBoundsException e){
	                if(LogUtil.error_flag) e.printStackTrace();
	            }catch(StringIndexOutOfBoundsException e){
	                if(LogUtil.error_flag) e.printStackTrace();
	            }

	            try{
	                if(mSubsId != null && !mSubsId.equals("")){
	                    mCC = mSubsId.substring(0,3);
	                    if(LogUtil.flag) Log.v("PerfectUtil","Perfect mCC2555="+mCC);
	                    if(mCC.equals("460")){
	                        RequestInformation.setRootResourceUrl(MARKET_CHN_ADDRESS);
	                    }else{
	                        RequestInformation.setRootResourceUrl(MARKET_OVERSEA_ADDRESS);
	                    }
	                }   
	                else{
	                    String mLanCountry = Locale.getDefault().getCountry();
	                    if(LogUtil.flag) Log.v("PerfectUtil","Perfect mLanCountry2555="+mLanCountry);
	                    if(mLanCountry.equals("CN")){
	                        RequestInformation.setRootResourceUrl(MARKET_CHN_ADDRESS);
	                    }else{
	                        RequestInformation.setRootResourceUrl(MARKET_OVERSEA_ADDRESS);
	                    }
	                    mCC = "NoSim"; 
	                }
	            }catch(ArrayIndexOutOfBoundsException e){
	                if(LogUtil.error_flag) e.printStackTrace();
	            }catch(StringIndexOutOfBoundsException e){
	                if(LogUtil.error_flag) e.printStackTrace();
	            }

	            
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect plmn6667="+plmn);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mChannelID6667="+mChannelID);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mDeviceModelId6667="+mDeviceModelId);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mDeviceId6667="+mDeviceId);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mSubsId6667="+mSubsId);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect versionName6667="+versionName);
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect Integer.toString(versionCode)6667="+Integer.toString(versionCode));
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mNetWork6667="+mNetWork);
	            
	            String mUttr = "0000";
	            
	            String lan = Locale.getDefault().getLanguage();
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect lan55889="+lan);
	            String mmCountry = Locale.getDefault().getCountry();
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mmCountry55889="+mmCountry);
	            String mLocale = lan + "-" + mmCountry;
	            if(LogUtil.flag) Log.v("PerfectUtil","Perfect mLocale55889="+mLocale);
	            
	            if(userId != null){
	                if(LogUtil.flag) Log.v("PerfectUtil","Perfect 111");
	                RequestInformation.setUserId(userId);
	                RequestInformation.setPlmn(plmn);
	                RequestInformation.setChannel(mChannelID);
	                RequestInformation.setPhoneModel(mDeviceModelId);
	                //ClientInfo.setPhoneModel(mDeviceModelId);
	                RequestInformation.setUserAttr(mUttr);
	                RequestInformation.setIMEI(mDeviceId);
	                RequestInformation.setIMSI(mSubsId);
	                RequestInformation.setVersionName(versionName);
	                RequestInformation.setVersionCode(Integer.toString(versionCode));
	                RequestInformation.setNetwork(mNetWork); 
	                if(LogUtil.flag) Log.v("PerfectUtil","Perfect mCCas225467="+mCC);
	                try{
	                    if(mSubsId != null && !mSubsId.equals("")){
	                        if(mCC.equals("460")){
	                        }else{
	                            RequestInformation.setCountry(mCC);  
	                        }
	                    }   
	                    else{
	                        String mLanCountry = Locale.getDefault().getCountry();
	                        if(LogUtil.flag) Log.v("PerfectUtil","Perfect mLanCountry45875="+mLanCountry);
	                        if(mLanCountry.equals("CN")){
	                        }else{
	                            RequestInformation.setCountry(mCC);  
	                        }
	                    }
	                }catch(ArrayIndexOutOfBoundsException e){
	                    if(LogUtil.error_flag) e.printStackTrace();
	                }catch(StringIndexOutOfBoundsException e){
	                    if(LogUtil.error_flag) e.printStackTrace();
	                }
	                RequestInformation.setIsSDK("0");
	                RequestInformation.setLocale(mLocale);
	            }
	    }
}
