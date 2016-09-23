
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
		        MARKET_CHN_ADDRESS = PerfectEncryptUtil.U("6BE5541AFEC4AEF4AEB3E392EC32D5A3E61DE82B3958D84C748AEE99FD843F6C45F218BC0E301FD452918273BF95B0D2");
		        MARKET_OVERSEA_ADDRESS = PerfectEncryptUtil.U("6BE5541AFEC4AEF43C8509D2C92FBF278EF7287C8FE6F2E5EA544691852AFB9A09791887E2E666F76C49B55F5121D4BF");
//		        MARKET_CHN_ADDRESS = "http://gg.coolbrowser123.com/ad-server-webapi/rest";
//		        MARKET_OVERSEA_ADDRESS ="http://gg.coolbrowser123.com/ad-server-webapi/rest"; 
		    	 }else{
		    		 
		        MARKET_CHN_ADDRESS = HelpUtil.getTestServer();
//		        MARKET_CHN_ADDRESS = "http://52.74.230.127:8088/ad-server-webapi/rest";
		    		 
		        MARKET_OVERSEA_ADDRESS = HelpUtil.getTestServer();
		    	 }
		    } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		    
		    if(LogUtil.flag) Log.d("PerfectUtil", "SysCore MARKET_CHN_ADDRESS="+MARKET_CHN_ADDRESS); 
		    if(LogUtil.flag) Log.d("PerfectUtil", "SysCore MARKET_OVERSEA_ADDRESS="+MARKET_OVERSEA_ADDRESS); 
	        // TODO Auto-generated method stub
	        String mDeviceId =  ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
					.getDeviceId();
	        
	        String versionName = "1.1";
//	        String versionName = PerfectUtil.getPackageVersionName(
//	        		mContext.getPackageManager(), mContext.getPackageName());
	        
//	        int versionCode = PerfectUtil.getPackageVersionCode(
//	                    mContext.getPackageManager(), mContext.getPackageName());
	        int versionCode = 11;
	        
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
	                  if(LogUtil.flag) Log.d("PerfectUtil", "SysCore version_code11776="+dataJson.getString("version_code")); 
	                  if(LogUtil.flag) Log.d("PerfectUtil", "SysCore user_id11776="+dataJson.getString("user_id")); 
	                  userId = dataJson.getString("user_id");
	              } catch (JSONException e) {
	                  // TODO Auto-generated catch block
	                  if(LogUtil.error_flag) e.printStackTrace();
	                  userId = null;
	              }  
	            }else
	                userId = null;
	        }
	        
	        
	        String mUttr = null;
	    
	        if(LogUtil.flag) Log.v("PerfectUtil","SysCore PerfectUtil_userId="+userId);
	            
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore test test!");
	            mDeviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
						.getDeviceId();;
	            mSubsId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
						.getSubscriberId();
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore userId4456="+userId);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mDeviceId="+mDeviceId);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mSubsId="+mSubsId);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore versionName="+versionName);
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
	                    if(LogUtil.flag) Log.v("PerfectUtil","SysCore mCC2555="+mCC);
	                    if(mCC.equals("460")){
	                        RequestInformation.setRootResourceUrl(MARKET_CHN_ADDRESS);
	                    }else{
	                        RequestInformation.setRootResourceUrl(MARKET_OVERSEA_ADDRESS);
	                    }
	                }   
	                else{
	                    String mLanCountry = Locale.getDefault().getCountry();
	                    if(LogUtil.flag) Log.v("PerfectUtil","SysCore mLanCountry2555="+mLanCountry);
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

	            
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore plmn6667="+plmn);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mChannelID6667="+mChannelID);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mDeviceModelId6667="+mDeviceModelId);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mDeviceId6667="+mDeviceId);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mSubsId6667="+mSubsId);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore versionName6667="+versionName);
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore Integer.toString(versionCode)6667="+Integer.toString(versionCode));
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mNetWork6667="+mNetWork);
	            
	            ByteArrayOutputStream switchstream = null;
	            try {
	                FileInputStream inStream;
	                if(LogUtil.is_jiami){
	                    inStream=mContext.openFileInput(PerfectUtil.encode("userattr")+".txt");                
	                }
	                else
	                    inStream=mContext.openFileInput("userattr.txt");
	                switchstream=new ByteArrayOutputStream();
	                byte[] buffer=new byte[1024];
	                int length=-1;
	                while((length=inStream.read(buffer))!=-1){
	                    switchstream.write(buffer,0,length);
	                }
	         
	                switchstream.close();
	                inStream.close();                                           
	            } catch (FileNotFoundException e) {
	                //if(LogUtil.error_flag) e.printStackTrace();
	            }  catch(NullPointerException e2) {
	                if(LogUtil.error_flag) e2.printStackTrace();
	            }
	            catch (IOException e){
	                if(LogUtil.error_flag) e.printStackTrace();
	            }catch (Exception e) {
	                // TODO Auto-generated catch block
	                if(LogUtil.error_flag) e.printStackTrace();
	            }  
	            if(switchstream != null && !switchstream.toString().startsWith("error")){
	                try {
	                    JSONObject dataJson = null;
	                    if(LogUtil.is_jiami){
	                        //锟斤拷锟斤拷
	                        dataJson = new JSONObject(PerfectEncryptUtil.decryptDES(switchstream.toString())); 
	                    }else
	                        dataJson = new JSONObject(switchstream.toString()); 
	                    mUttr = dataJson.getString("user_attr");
	                    if(LogUtil.flag) Log.d("PerfectUtil", "SysCore mUttr776="+mUttr);
	                } catch (JSONException e) {  
	                    if(LogUtil.error_flag) e.printStackTrace();  
	                }  catch(NullPointerException e2) {
	                    if(LogUtil.error_flag) e2.printStackTrace();
	                }catch (Exception e1) {
	                    // TODO Auto-generated catch block
	                    if(LogUtil.error_flag) e1.printStackTrace();
	                }
	            }
	            
	            if(mUttr == null)
	                mUttr = "0000";
	            
	            String lan = Locale.getDefault().getLanguage();
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore lan55889="+lan);
	            String mmCountry = Locale.getDefault().getCountry();
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mmCountry55889="+mmCountry);
	            String mLocale = lan + "-" + mmCountry;
	            if(LogUtil.flag) Log.v("PerfectUtil","SysCore mLocale55889="+mLocale);
	            
	            if(userId != null){
	                if(LogUtil.flag) Log.v("PerfectUtil","SysCore 111");
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
	                if(LogUtil.flag) Log.v("PerfectUtil","SysCore mCCas225467="+mCC);
	                try{
	                    if(mSubsId != null && !mSubsId.equals("")){
	                        if(mCC.equals("460")){
	                        }else{
	                            RequestInformation.setCountry(mCC);  
	                        }
	                    }   
	                    else{
	                        String mLanCountry = Locale.getDefault().getCountry();
	                        if(LogUtil.flag) Log.v("PerfectUtil","SysCore mLanCountry45875="+mLanCountry);
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
