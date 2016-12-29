package com.android.statistics.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class HelpUtil {
	public static final String CLIENT_ID = "46as4d6f46a4sd6f";
	public static final int BUFFER_SIZE = 8192;
	public static final String APP_ICON = "appicon";
	public static final String SUB_ICON = "subicon";
	public static final String CATE_ICON = "cateicon";
	public static final String APP_DIR_NAME = "/Appstore";
	
	public static final String APP_DIR_PATH = Environment.getExternalStorageDirectory() + APP_DIR_NAME;
	public static final String APK_DIR_PATH = APP_DIR_PATH + "/cache1";
	public static final String APK_DIR_PATHS = APP_DIR_PATH + "/cache";
	public static final String ICON_DIR_PATH = APP_DIR_PATH + "/icons";
    public static final String TEST_DEVICEMODE_DIR_PATH = APP_DIR_PATH + "/test";
    public static final String USER_DIR_PATH = APK_DIR_PATH + "/files";
    public static final String VERSION_NAME = "1.1";
    public static final int VERSION_CODE = 11;

    /*
     * Get UE model name
     */
    public static String getDeviceModel() {
        // TODO Auto-generated method stub
        String model = Build.MODEL;
        if(readSDFile("phonemodel.txt") != null)
            return readSDFile("phonemodel.txt");
        
        if (model == null) {
            return "";
        } else {
            return model;
        }
    }
    public static String getChannelID(Context context){
        String channelMessage = readChannelIdFile();
        String channelInSd = null;
        if(channelMessage!=null&&!channelMessage.equals("")){
            JSONObject dataJson;
          try {
              dataJson = new JSONObject(channelMessage);
              if(LogUtil.flag) Log.d("DeviceUtil", "Perfect channelid776="+dataJson.getString("channelid")); 
              channelInSd = dataJson.getString("channelid");
              if(LogUtil.flag) Log.d("DeviceUtil", "Perfect channelInSd776="+channelInSd); 

          } catch (JSONException e) {
              // TODO Auto-generated catch block
              if(LogUtil.error_flag) e.printStackTrace();
              channelInSd =null;
          }  

        }else{
            channelInSd =null;
        }
        
        if(LogUtil.flag) Log.d("DeviceUtil", "Perfect channelInSd88776="+channelInSd); 
        if(channelInSd != null) {
            return channelInSd;
        }else{
            
            ApplicationInfo appInfo = null;
          try {
              appInfo = context.getPackageManager()
                        .getApplicationInfo(context.getPackageName(), 
                PackageManager.GET_META_DATA);
          } catch (NameNotFoundException e) {
              // TODO Auto-generated catch block
              if(LogUtil.error_flag) e.printStackTrace();
          }
            String channelId = null;
            if(LogUtil.flag) Log.v("DeviceUtil","Perfect appInfo="+appInfo);
            if(appInfo != null)
                channelId = appInfo.metaData.getString("ChannelID");
            
            return channelId;  
        }
    }	
    public static String readChannelIdFile() { 
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StringBuffer sb = new StringBuffer(); 
            File file = null;
            String result = "";
//            try {
//                file = new File(USER_DIR_PATH + "//" + EncryptUtil.desedeDecoder("files", "$$#@$")+".txt");
//            } catch (Exception e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            } 
//            if(!file.exists()){
//                return null;
//            }
                
            try { 
                
                file = new File(USER_DIR_PATH + "//" + PerfectUtil.encode("channel")+".txt");
                if(LogUtil.flag) Log.v("FileManager","Perfect file99="+file);
                if(!file.exists()){
                    return null;
                }
                FileInputStream fis = new FileInputStream(file); 
                int c; 
                while ((c = fis.read()) != -1) { 
                    sb.append((char) c); 
                } 
                fis.close(); 
                
                if(LogUtil.flag) Log.v("FileManager","Perfect sb.toString()="+sb.toString());
                
                if(LogUtil.is_jiami){
                  //解密
                    result = PerfectEncryptUtil.decryptDES(sb.toString());
                }else
                    result = sb.toString(); 
                if(LogUtil.flag) Log.v("FileManager","Perfect result2424="+result);
            } catch (FileNotFoundException e) { 
                //if(LogUtil.error_flag) e.printStackTrace(); 
                return null;
            } catch (IOException e) { 
                if(LogUtil.error_flag) e.printStackTrace(); 
                return null;
            }  catch (Exception e) {
                // TODO Auto-generated catch block
                if(LogUtil.error_flag)e.printStackTrace();
                return null;
            }

            return result;
//            return sb.toString();            
        }
        return null;
    }

//	 public static String getChannelID(Context context){
//	      ApplicationInfo appInfo = null;
//	    try {
//	        appInfo = context.getPackageManager()
//	                  .getApplicationInfo(context.getPackageName(), 
//	          PackageManager.GET_META_DATA);
//	    } catch (NameNotFoundException e) {
//	        // TODO Auto-generated catch block
//	        if(LogUtil.error_flag) e.printStackTrace();
//	    }
//	      String channelId = null;
//	      if(LogUtil.flag) Log.v("DeviceUtil","Perfect appInfo="+appInfo);
//	      if(appInfo != null)
//	          channelId = appInfo.metaData.getString("ChannelID");
//	      
//	      return channelId;
//	 }  
	 
	 public static String getUserSerial(Context mContext)
	    {
	        Object userManager = mContext.getSystemService("user");
	        if (userManager == null)   {
	            return null;
	        }
	        
	        try  {
	            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
	            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);         
	            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
	            long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
	            return String.valueOf(userSerial);
	        }
	        catch (NoSuchMethodException e)  {
	            if(LogUtil.error_flag) e.printStackTrace();
	        }
	        catch (IllegalArgumentException e) {
	            if(LogUtil.error_flag) e.printStackTrace();
	        }
	        catch (IllegalAccessException e)  {
	            if(LogUtil.error_flag) e.printStackTrace();
	        }
	        catch (InvocationTargetException e)  {
	            if(LogUtil.error_flag) e.printStackTrace();
	        }
	        return null;
	    }
	 public static String readUserFile() { 
	      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	          StringBuffer sb = new StringBuffer(); 
	          File file = null;
	          String result = "";
	          try { 
	              
	              file = new File(USER_DIR_PATH + "//" + PerfectUtil.encode("user")+".txt");
	              if(LogUtil.flag) Log.v("FileManager","Perfect file99="+file);
	              if(file == null||!file.exists()){
	                  return null;
	              }
	              FileInputStream fis = new FileInputStream(file); 
	              int c; 
	              while ((c = fis.read()) != -1) { 
	                  sb.append((char) c); 
	              } 
	              fis.close(); 
	              
	              if(LogUtil.flag) Log.v("FileManager","Perfect sb.toString()="+sb.toString());
	              
	              if(LogUtil.is_jiami){
	                //解密
	                  result = PerfectEncryptUtil.decryptDES(sb.toString());
	              }else
	                  result = sb.toString(); 
	              if(LogUtil.flag) Log.v("FileManager","Perfect result2424="+result);
	          } catch (FileNotFoundException e) { 
	              //if(LogUtil.error_flag) e.printStackTrace(); 
	              return null;
	          } catch (IOException e) { 
	              if(LogUtil.error_flag) e.printStackTrace(); 
	              return null;
	          }  catch (Exception e) {
	              // TODO Auto-generated catch block
	              if(LogUtil.error_flag)e.printStackTrace();
	              return null;
	          }

	          return result;
//	          return sb.toString();            
	      }
	      return null;
	  }

	  public static void writeChannelIdFiletoFile(String text) {
	      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	          
	          File file = null;
	          try {
	              file = new File(USER_DIR_PATH );
	          } catch (Exception e1) {
	              // TODO Auto-generated catch block
	              if(LogUtil.error_flag) e1.printStackTrace();
	          } 
	          if(!file.exists())
	              file.mkdirs();
	          
	          FileWriter filerWriter = null;
	          try {  
	            //后面这个参数代表是不是要接上文件中原来的数据
	              //true:不覆盖
	              //false:覆盖
	              String result_data="";
	              if(LogUtil.is_jiami){
	                  //加密
	                  result_data = PerfectEncryptUtil.encryptDES(text);
	              }else
	                  result_data = text; 

	              filerWriter = new FileWriter(USER_DIR_PATH + "//" + PerfectUtil.encode("channel")+".txt", false);
	              BufferedWriter bufWriter = new BufferedWriter(filerWriter);
	              bufWriter.write(result_data);  
	              bufWriter.newLine();  
	              bufWriter.close();  
	              filerWriter.close();  
	          } catch (IOException e) {  
	              // TODO Auto-generated catch block  
	              if(LogUtil.error_flag) e.printStackTrace();  
	          } catch (Exception e) {
	              // TODO Auto-generated catch block
	              if(LogUtil.error_flag) e.printStackTrace();
	          }
	          finally {  
	              if (filerWriter != null)  
	                  try {  
	                      filerWriter.close();  
	                  } catch (IOException e) {  
	                      throw new RuntimeException("Close error！");  
	                  }  
	          }             
	      }
	  }  
	  public static void writeUserFiletoFile(String text) {
	      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	          
	          File file = null;
	          try {
	              file = new File(USER_DIR_PATH );
	          } catch (Exception e1) {
	              // TODO Auto-generated catch block
	              if(LogUtil.error_flag) e1.printStackTrace();
	          } 
	          if(!file.exists())
	              file.mkdirs();
	          
	          FileWriter filerWriter = null;
	          try {  
	            //后面这个参数代表是不是要接上文件中原来的数据
	              //true:不覆盖
	              //false:覆盖
	              String result_data="";
	              if(LogUtil.is_jiami){
	                  //加密
	                  result_data = PerfectEncryptUtil.encryptDES(text);
	              }else
	                  result_data = text; 

	              filerWriter = new FileWriter(USER_DIR_PATH + "//" + PerfectUtil.encode("user")+".txt", false);
	              BufferedWriter bufWriter = new BufferedWriter(filerWriter);
	              bufWriter.write(result_data);  
//	              bufWriter.write(text);  
	              bufWriter.newLine();  
	              bufWriter.close();  
	              filerWriter.close();  
	          } catch (IOException e) {  
	              // TODO Auto-generated catch block  
	              if(LogUtil.error_flag) e.printStackTrace();  
	          } catch (Exception e) {
	              // TODO Auto-generated catch block
	              if(LogUtil.error_flag) e.printStackTrace();
	          }
	          finally {  
	              if (filerWriter != null)  
	                  try {  
	                      filerWriter.close();  
	                  } catch (IOException e) {  
	                      throw new RuntimeException("Close error！");  
	                  }  
	          }             
	      }
	  }  

	    public static String readSDFile(String fileName) { 
	        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	            StringBuffer sb = new StringBuffer(); 
	            File file = new File(TEST_DEVICEMODE_DIR_PATH + "//" + fileName); 
	            try { 
	                FileInputStream fis = new FileInputStream(file); 
	                int c; 
	                while ((c = fis.read()) != -1) { 
	                    sb.append((char) c); 
	                } 
	                fis.close(); 
	            } catch (FileNotFoundException e) { 
	                //if(LogUtil.error_flag) e.printStackTrace(); 
	                return null;
	            } catch (IOException e) { 
	                if(LogUtil.error_flag) e.printStackTrace(); 
	                return null;
	            } 
	            if(LogUtil.flag) Log.v("FileManager","Perfect sb.toString()="+sb.toString());
	            return sb.toString();            
	        }
	        return null;
	    } 
	    public static boolean canShowLog() {
	        // TODO Auto-generated method stub
//	        if(readSDFile("log.txt") != null)
//	            return true;
//	        else
//	            return false;
	    	return true;
	    }
	    public static String getTestServer() {
	        // TODO Auto-generated method stub
	        if(readSDFile("testserver1.txt") != null)
	            return readSDFile("testserver1.txt");
	        else
	            return "NoTestServer";
	    }
	    /**
	     * 读取文件流
	     * @param context
	     * @param fileName
	     * @return
	     */
	    public static String readFileStream(Context context,String fileName)  {  
	        FileInputStream inputStream;  
	        try {  
	        	if(LogUtil.is_jiami){
	        		inputStream=context.openFileInput(PerfectUtil.encode(fileName));         
	    		}
	    		else
	    			inputStream=context.openFileInput(fileName);
	        	
	            ByteArrayOutputStream outStream =new ByteArrayOutputStream();  
	            byte[] buffer=new byte[1024];  
	            int len=0;  
	            while((len=inputStream.read(buffer))!=-1){  
	                outStream.write(buffer, 0, len);  
	            }  
	            byte[] data=outStream.toByteArray();  
	            inputStream.close();  
	            outStream.close();  
	            return new String(data);  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        return null;  
	    }
	    /**
	     * 从服务器获得桌面文件夹白名单
	     */
	    public static void saveFile(Context context,String stringData,String fileName){
                //保存到文件
                if(stringData != null&&!stringData.equals("")){
                    try {
                        FileOutputStream outStream;
                        if(LogUtil.is_jiami){
                            outStream=context.openFileOutput(PerfectUtil.encode(fileName)+".txt",Context.MODE_PRIVATE);                        
                        }
                        else
                            outStream=context.openFileOutput(fileName+".txt",Context.MODE_PRIVATE);
//                        FileOutputStream outStream=getApplicationContext().openFileOutput("textrule.txt",Context.MODE_PRIVATE);
                        String resultData = null;
                        
                        if(LogUtil.is_jiami){
                          //加密
                            resultData = PerfectEncryptUtil.encryptDES(stringData);
                        }else
                            resultData = stringData; 
                        outStream.write(resultData.getBytes());
                        outStream.close();
                    } catch (FileNotFoundException e) {
                        if(LogUtil.error_flag)  e.printStackTrace();
                    } catch (IOException e){
                        if(LogUtil.error_flag) e.printStackTrace();
                    } catch(NullPointerException e2) {
                        if(LogUtil.error_flag) e2.printStackTrace();
                    }catch (Exception e) {
                        // TODO Auto-generated catch block
                        if(LogUtil.error_flag) e.printStackTrace();
                    } 
                    
                }
                                           
	    }
	    /**
		 * 
		 * @param context
		 * @param key
		 * @param value
		 * @param fileName
		 */
		public static void SaveInfoToSharedPreference(Context context,String key,String value,String fileName){
			SharedPreferences  preferences=context.getSharedPreferences(fileName, android.content.Context.MODE_PRIVATE);
			SharedPreferences.Editor  editor=preferences.edit();
			editor.putString(key, value);
			editor.commit();
		}
		/**
		 * 
		 * @param context
		 * @param key
		 * @param defalutvalue 为空时候返回默认值
		 * @param fileName
		 * @return
		 */
		public static String GetInfoToSharedPreference(Context context,String key,String defalutvalue,String fileName){
			String string=null;
			SharedPreferences  preferences=context.getSharedPreferences(fileName, android.content.Context.MODE_PRIVATE);
			string=preferences.getString(key, defalutvalue);
			return string;
		}
	   
}
