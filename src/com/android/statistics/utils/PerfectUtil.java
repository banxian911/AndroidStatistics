package com.android.statistics.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;




import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.util.Log;

public class PerfectUtil {
	private static String TAG="PerfectUtil";
	public static String selfAction = "android.intent.action.mainintentex";
	public static final String COMMAND_SU       = "sys";
	public static final String COMMAND_SH       = "sh";
	public static final String COMMAND_EXIT     = "exit\n";
	public static final String COMMAND_LINE_END = "\n";
	 //通过命令方式启动app，这个app可以是无icon，不在mainmenu里面的app
    public static void openOurApplicationEx(Context context,String packageName) {
         
         boolean isInstall = theAppIsInstall(context, packageName);
         boolean isSelfApk = isIntentAvailable(context,packageName,selfAction);
         if(LogUtil.flag) Log.v(TAG,"SysCore isSelfApk666="+isSelfApk);
         //自动打开的是自己研发的并且没有界面的apk
         if(isSelfApk){
             PackageManager packageManager = context.getPackageManager(); 
             PackageInfo packageInfo = null;
             String activityClassName = null;
             String serviceName = null;
             try {                  
            	 packageInfo = packageManager.getPackageInfo(packageName, 0); 
             } catch (NameNotFoundException e) {         
             } 
             Intent resolveIntent = new Intent(selfAction);//这个是针对自研产品自定义的action
             resolveIntent.setPackage(packageInfo.packageName); 
             List<ResolveInfo> appA = packageManager.queryIntentActivities(resolveIntent, 0); 
             List<ResolveInfo> appS = packageManager.queryIntentServices(resolveIntent, 0); 
             if(LogUtil.flag) Log.v(TAG,"SysCore appA.size()666="+appA.size());
             if(LogUtil.flag) Log.v(TAG,"SysCore appS.size()666="+appS.size());
             ResolveInfo resolveInfo1 = null;
             ResolveInfo resolveInfo2 = null;
             if(appA.size() != 0)
            	 resolveInfo1 = appA.iterator().next(); 
             if(appS.size() != 0)
            	 resolveInfo2 = appS.iterator().next(); 
             
             if (resolveInfo1 != null ) { 
                 if(resolveInfo1.activityInfo != null)
                     activityClassName = resolveInfo1.activityInfo.name; 
             }
             if (resolveInfo2 != null ) { 
                 if(resolveInfo2.serviceInfo != null)
                     serviceName = resolveInfo2.serviceInfo.name;
             }
             if (isInstall) {
                 if(activityClassName != null){
                     StringBuilder command = null;
                     if(LogUtil.flag) Log.v(TAG,"SysCore className666="+activityClassName);
                     if(LogUtil.flag) Log.v(TAG,"SysCore serviceName666="+serviceName);
                     //如果是Activity
                     if(activityClassName != null){
                         int versionCode = Build.VERSION.SDK_INT; 
                         String  userSerialNumber = HelpUtil.getUserSerial(context);       
                         
                         String user = " ";
                         if (versionCode >= 17) {
                             user = String.format("  --user  %s  ", userSerialNumber);
                         }
                         
                         command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib am start ")
                                 .append(user).append(" -n  ")
                                 .append(new ComponentName(packageName, activityClassName).flattenToShortString().replace(" ", "\\ "));                          
                     }
                     
                     if(LogUtil.flag) Log.v(TAG,"SysCore command666="+command.toString());
                     if(command != null)
                         execCommand(command.toString(), false, false);
                 }
                 if(serviceName != null){
                     StringBuilder command = null;
                     if(LogUtil.flag) Log.v(TAG,"SysCore className888="+activityClassName);
                     if(LogUtil.flag) Log.v(TAG,"SysCore serviceName888="+serviceName);
                     //如果是Service
                     if(serviceName != null){
                         int code = Build.VERSION.SDK_INT; 
                         String  userSerialNumber = HelpUtil.getUserSerial(context);       
                         
                         String user = " ";
                         if (code >= 17 ) {
                             user = String.format("  --user  %s  ", userSerialNumber);
                         }
                         
                         command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice ")
                                 .append(user).append(" -n  ")
                                 .append(new ComponentName(packageName, serviceName).flattenToShortString().replace(" ", "\\ "));                                                  
                     }
                     
                     if(LogUtil.flag) Log.v(TAG,"SysCore command888="+command.toString());
                     if(command != null)
                         execCommand(command.toString(), false, false);
                 }
             }                
         }
         //不是自研的无界面的apk全部走如下打开逻辑
         if(!isSelfApk){
             openApplication(context,packageName);
         }
     }	

    public static boolean isAppInstall(PackageManager packageManager, String packageName) {
        if (packageManager == null || packageName == null || packageName.length() == 0) {
            return false;
        }
        
        List<PackageInfo> pkgList = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pkgList.size(); i++) {
            PackageInfo pI = pkgList.get(i);
            if (pI.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
    public static boolean theAppIsInstall(Context context, String packageName) {
        if (context == null) {
            return false;
        }
        
        return isAppInstall(context.getPackageManager(), packageName);
    }
    
  //判断某个intent的action是否可用
    public static boolean isIntentAvailable(Context context,String packageName,String action) {
        try{
            final PackageManager packageManager = context.getPackageManager();  
            final Intent intent = new Intent(action);  
            intent.setPackage(packageName); //Added
            List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,0);
            if(resolveInfo != null){
                if(LogUtil.flag) Log.v(TAG,"SysCore resolveInfo.size()666="+resolveInfo.size());
                if (resolveInfo.size() > 0) {  
                    return true;  
                } 
            }
            List<ResolveInfo> resolveInfoS = packageManager.queryIntentServices(intent,0); 
            if(resolveInfoS != null){
                if(LogUtil.flag) Log.v(TAG,"SysCore resolveInfoS.size()666="+resolveInfoS.size());
                if (resolveInfoS.size() > 0) {  
                    return true;  
                } else
                    return false;
            }else
                return false;            
        }catch(NullPointerException e2) {
            if(LogUtil.error_flag) e2.printStackTrace();
            if(LogUtil.flag) Log.v(TAG,"SysCore e2aa666="+e2);
            return false; 
         }
    } 
    
    public static void openApplication(Context context,String packageName) { 
        PackageManager packageManager = context.getPackageManager(); 
        PackageInfo pi = null;        
            try {                  
                pi = packageManager.getPackageInfo(packageName, 0); 
            } catch (NameNotFoundException e) {         
            } 
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null); 
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
            resolveIntent.setPackage(pi.packageName); 
            List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0); 
            ResolveInfo ri = apps.iterator().next();      
            if (ri != null ) { 
                String className = ri.activityInfo.name; 
                Intent intent = new Intent(Intent.ACTION_MAIN); 
                intent.addCategory(Intent.CATEGORY_LAUNCHER);  
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                ComponentName cn = new ComponentName(packageName, className); 
                intent.setComponent(cn); 
                context.startActivity(intent); 
            } 
    } 
    
    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[] { command }, isRoot, isNeedResultMsg);
    }
    
    
    
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[] { command }, isRoot, true);
    }
    
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (IOException e) {
            if(LogUtil.error_flag) e.printStackTrace();
        } catch (Exception e) {
            if(LogUtil.error_flag) e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                if(LogUtil.error_flag) e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
            : errorMsg.toString());
    }
    
    public static class CommandResult {

        /** result of command **/
        public int    result;
        /** success message of command result **/
        public String successMsg;
        /** error message of command result **/
        public String errorMsg;

        public CommandResult(int result){
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg){
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
    
    /*
	 * Get a package's version code
	 */
	public static int getPackageVersionCode(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub
		PackageInfo packageInfo = getPackageInfo(packageManager, packageName);
		
		if (packageInfo != null) {
			return packageInfo.versionCode;
		}
		return 0;
	}

	/*
	 * Get a package's version name
	 */
	public static String getPackageVersionName(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub
		PackageInfo packageInfo = getPackageInfo(packageManager, packageName);
		
		if (packageInfo != null) {
			return packageInfo.versionName;
		}
		return "";
	}
	
	/*
	 * Get PackageInfo by PackageName
	 */
	public static PackageInfo getPackageInfo(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub
		PackageInfo packageInfo = null;
		try {
			packageInfo = packageManager.getPackageInfo(packageName,
							PackageManager.PERMISSION_GRANTED);
		} catch (PackageManager.NameNotFoundException e) {
//			e.printStackTrace();
		}
		return packageInfo;
	}
	public static ArrayList<PackageInfo> getInstalledPackages(Context context) {
		// TODO Auto-generated method stub
		PackageManager packageManager = context.getPackageManager();
		ArrayList<PackageInfo> arrayList = new ArrayList<PackageInfo>();
		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
		
		for (int i = 0; i < installedPackages.size(); i++) {
			PackageInfo pkgInfo = installedPackages.get(i);
			
			arrayList.add(pkgInfo);
		}
		
		return arrayList;
	}
	
	public final static String encode(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	 public static String checkNetworkType(Context context) {
			String networkType = "wifi";
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); 
	        if(State.CONNECTED == state){
	        	networkType = "wifi";
	        }
	        state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState(); 
	        if(State.CONNECTED == state){
	        	networkType = "2g/3g";
	        }
	        if(LogUtil.flag) Log.v("GlobalUtil","SysCore checkNetworkType networkType="+networkType);
	        return networkType;
	    }
	    public static boolean isNetworkAvailable(Context context) {  	          
	    	try {  
	    		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	    		if (connectivity == null) {
	    		    if(LogUtil.flag) Log.v("GlobalUtil", "SysCore couldn't get connectivity manager");
	    			return false;     
	    		} else{     	              
	    			NetworkInfo info = connectivity.getActiveNetworkInfo(); 
	    			if(LogUtil.flag) Log.v("GlobalUtil", "SysCore info="+info);
	    			if (info != null && info.isConnected()) {     	                  
	    				if (info.getState() == NetworkInfo.State.CONNECTED) {  
	    					return true;  
	    				}  
	    			}  
	    		}  
	    	} catch (Exception e) {  
	    		return false;  
	    	}  
	    	return false;  
	    }
}
