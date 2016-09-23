package com.android.statistics.request;

import org.apache.http.protocol.HTTP;

import com.android.statistics.PerfectService;



public class RequestInformation {
    
    public static String RESOURCE_ROOT_URL="";
    
    public static boolean GZIP_ENCODING=true;
    
    public static final String USER_AGENT=HTTP.USER_AGENT;
    
    public static final String API_Version="0.1b";
    
    public static final String USER_ID="user_id";
    
    public static final String CLIENT_ID="Client-ID";
    
  	public static final String DEVICE_ID="imei";
    
    public static final String SUBSCRIBER_ID="imsi";
    
    public static final String ACCESS_TOKEN="Access_Token";
    
    public static final String VERSION_NAME="version_name";
    public static final String VERSION_CODE="version_code";
    public static final String NETWORK="network";
    
    public static final String USER_ATTR="user_attr";
    public static final String PHONE_MODEL="phone_model";
    public static final String CHANNEL="channel";
    public static final String PLMN="plmn";
    public static final String COUNTRY="country";
    public static final String IS_SDK="is_sdk";
    public static final String LOCALE="locale";
  
    
    static{
        setHeader(USER_AGENT, "App-Market/"+API_Version);
        setHeader("API-Version", API_Version);
    }
    
    public static void setHeader(String name,String value){
        PerfectService.customHeaders.put(name, value);
    }
    
    public static String getApiVersion(){
        return API_Version;
    }
    
    public static String getHeaderValue(String name){
        return PerfectService.customHeaders.get(name);
    }
    
   public static void setClientId(String value){
        setHeader(CLIENT_ID, value);
    }
    
    public static void setIMEI(String value){
        setHeader(DEVICE_ID, value);
    }
    
    public static void setIMSI(String value){
        setHeader(SUBSCRIBER_ID, value);
    }
    
    public static void setUserId(String value){
        setHeader(USER_ID, value);
    }
    
    public static void setAccessToken(String value){
        setHeader(ACCESS_TOKEN, value);
    }
    
    public static void setRootResourceUrl(String rootResourceUrl){
        RESOURCE_ROOT_URL=rootResourceUrl;
    }
    
    public static void setGzipEncoding(boolean gzip){
        GZIP_ENCODING=gzip;
    }
     
    public static void setVersionName(String versionname){
        setHeader(VERSION_NAME, versionname);
    }
    public static void setVersionCode(String versioncode){
        setHeader(VERSION_CODE, versioncode);
    }
    public static void setNetwork(String network){
        setHeader(NETWORK, network);
    } 
    public static void setUserAttr(String userattr){
        setHeader(USER_ATTR, userattr);
    } 
    public static void setPhoneModel(String phonemodel){
        setHeader(PHONE_MODEL, phonemodel);
    } 
    public static void setChannel(String channel){
        setHeader(CHANNEL, channel);
    } 
    public static void setPlmn(String plmn){
        setHeader(PLMN, plmn);
    } 
    public static void setCountry(String country){
        setHeader(COUNTRY, country);
    } 
    public static void setIsSDK(String is_sdk){
        setHeader(IS_SDK, is_sdk);
    } 
    public static void setLocale(String locale){
        setHeader(LOCALE, locale);
    } 
}
