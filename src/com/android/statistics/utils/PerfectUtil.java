package com.android.statistics.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;



import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
	
	 /**
     * Installation return code<br/>
     * install success.
     */
    public static final int INSTALL_SUCCEEDED                              = 100;
    /**
     * Installation return code<br/>
     * the package is already installed.
     */
    public static final int INSTALL_FAILED_ALREADY_EXISTS                  = -1;

    /**
     * Installation return code<br/>
     * the package archive file is invalid.
     */
    public static final int INSTALL_FAILED_INVALID_APK                     = -2;

    /**
     * Installation return code<br/>
     * the URI passed in is invalid.
     */
    public static final int INSTALL_FAILED_INVALID_URI                     = -3;

    /**
     * Installation return code<br/>
     * the package manager service found that the device didn't have enough storage space to install the app.
     */
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE            = -4;

    /**
     * Installation return code<br/>
     * a package is already installed with the same name.
     */
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE               = -5;

    /**
     * Installation return code<br/>
     * the requested shared user does not exist.
     */
    public static final int INSTALL_FAILED_NO_SHARED_USER                  = -6;

    /**
     * Installation return code<br/>
     * a previously installed package of the same name has a different signature than the new package (and the old
     * package's data was not removed).
     */
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE             = -7;

    /**
     * Installation return code<br/>
     * the new package is requested a shared user which is already installed on the device and does not have matching
     * signature.
     */
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE        = -8;

    /**
     * Installation return code<br/>
     * the new package uses a shared library that is not available.
     */
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY          = -9;

    /**
     * Installation return code<br/>
     * the new package uses a shared library that is not available.
     */
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE          = -10;

    /**
     * Installation return code<br/>
     * the new package failed while optimizing and validating its dex files, either because there was not enough storage
     * or the validation failed.
     */
    public static final int INSTALL_FAILED_DEXOPT                          = -11;

    /**
     * Installation return code<br/>
     * the new package failed because the current SDK version is older than that required by the package.
     */
    public static final int INSTALL_FAILED_OLDER_SDK                       = -12;

    /**
     * Installation return code<br/>
     * the new package failed because it contains a content provider with the same authority as a provider already
     * installed in the system.
     */
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER            = -13;

    /**
     * Installation return code<br/>
     * the new package failed because the current SDK version is newer than that required by the package.
     */
    public static final int INSTALL_FAILED_NEWER_SDK                       = -14;

    /**
     * Installation return code<br/>
     * the new package failed because it has specified that it is a test-only package and the caller has not supplied
     * the {@link #INSTALL_ALLOW_TEST} flag.
     */
    public static final int INSTALL_FAILED_TEST_ONLY                       = -15;

    /**
     * Installation return code<br/>
     * the package being installed contains native code, but none that is compatible with the the device's CPU_ABI.
     */
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE            = -16;

    /**
     * Installation return code<br/>
     * the new package uses a feature that is not available.
     */
    public static final int INSTALL_FAILED_MISSING_FEATURE                 = -17;

    /**
     * Installation return code<br/>
     * a secure container mount point couldn't be accessed on external media.
     */
    public static final int INSTALL_FAILED_CONTAINER_ERROR                 = -18;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed in the specified install location.
     */
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION        = -19;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed in the specified install location because the media is not available.
     */
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE               = -20;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed because the verification timed out.
     */
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT            = -21;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed because the verification did not succeed.
     */
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE            = -22;

    /**
     * Installation return code<br/>
     * the package changed from what the calling program expected.
     */
    public static final int INSTALL_FAILED_PACKAGE_CHANGED                 = -23;

    /**
     * Installation return code<br/>
     * the new package is assigned a different UID than it previously held.
     */
    public static final int INSTALL_FAILED_UID_CHANGED                     = -24;

    /**
     * Installation return code<br/>
     * if the parser was given a path that is not a file, or does not end with the expected '.apk' extension.
     */
    public static final int INSTALL_PARSE_FAILED_NOT_APK                   = -100;

    /**
     * Installation return code<br/>
     * if the parser was unable to retrieve the AndroidManifest.xml file.
     */
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST              = -101;

    /**
     * Installation return code<br/>
     * if the parser encountered an unexpected exception.
     */
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION      = -102;

    /**
     * Installation return code<br/>
     * if the parser did not find any certificates in the .apk.
     */
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES           = -103;

    /**
     * Installation return code<br/>
     * if the parser found inconsistent certificates on the files in the .apk.
     */
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

    /**
     * Installation return code<br/>
     * if the parser encountered a CertificateEncodingException in one of the files in the .apk.
     */
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING      = -105;

    /**
     * Installation return code<br/>
     * if the parser encountered a bad or missing package name in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME          = -106;

    /**
     * Installation return code<br/>
     * if the parser encountered a bad shared user id name in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID        = -107;

    /**
     * Installation return code<br/>
     * if the parser encountered some structural problem in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED        = -108;

    /**
     * Installation return code<br/>
     * if the parser did not find any actionable tags (instrumentation or application) in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY            = -109;

    /**
     * Installation return code<br/>
     * if the system failed to install the package because of system issues.
     */
    public static final int INSTALL_FAILED_INTERNAL_ERROR                  = -110;
    /**
     * Installation return code<br/>
     * other reason
     */
    public static final int INSTALL_FAILED_OTHER                           = -1000000;

    /**
     * Uninstall return code<br/>
     * uninstall success.
     */
    public static final int DELETE_SUCCEEDED                               = 1;

    /**
     * Uninstall return code<br/>
     * uninstall fail if the system failed to delete the package for an unspecified reason.
     */
    public static final int DELETE_FAILED_INTERNAL_ERROR                   = -1;

    /**
     * Uninstall return code<br/>
     * uninstall fail if the system failed to delete the package because it is the active DevicePolicy manager.
     */
    public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER            = -2;

    /**
     * Uninstall return code<br/>
     * uninstall fail if pcakge name is invalid
     */
    public static final int DELETE_FAILED_INVALID_PACKAGE                  = -3;

    /**
     * Uninstall return code<br/>
     * uninstall fail if permission denied
     */
    public static final int DELETE_FAILED_PERMISSION_DENIED                = -4;
	 //通过命令方式启动app，这个app可以是无icon，不在mainmenu里面的app
    public static int openOurApplicationEx(Context context,String packageName) {
    	 int flag=-100;
         //自动打开的是自己研发的并且没有界面的apk
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
             if(LogUtil.flag) Log.v(TAG,"Perfect appA.size()666="+appA.size());
             if(LogUtil.flag) Log.v(TAG,"Perfect appS.size()666="+appS.size());
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
            
                 if(activityClassName != null){
                     StringBuilder command = null;
                     if(LogUtil.flag) Log.v(TAG,"Perfect className666="+activityClassName);
                     if(LogUtil.flag) Log.v(TAG,"Perfect serviceName666="+serviceName);
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
                     
                     if(LogUtil.flag) Log.v(TAG,"Perfect command666="+command.toString());
                     if(command != null){
                    	 CommandResult commandResult= execCommand(command.toString(), false, false);
                    	 if(commandResult!=null){
                        	 flag=commandResult.result;
                        	 }
                     }
                 }
                 if(serviceName != null){
                     StringBuilder command = null;
                     if(LogUtil.flag) Log.v(TAG,"Perfect className888="+activityClassName);
                     if(LogUtil.flag) Log.v(TAG,"Perfect serviceName888="+serviceName);
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
                     
                     if(LogUtil.flag) Log.v(TAG,"Perfect command888="+command.toString());
                     if(command != null){
                    	 
                    	 CommandResult commandResult= execCommand(command.toString(), false, false);
                    	 if(commandResult!=null){
                    	 flag=commandResult.result;
                    	 }
                     }
                 }
         return flag;
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
                if(LogUtil.flag) Log.v(TAG,"Perfect resolveInfo.size()666="+resolveInfo.size());
                if (resolveInfo.size() > 0) {  
                    return true;  
                } 
            }
            List<ResolveInfo> resolveInfoS = packageManager.queryIntentServices(intent,0); 
            if(resolveInfoS != null){
                if(LogUtil.flag) Log.v(TAG,"Perfect resolveInfoS.size()666="+resolveInfoS.size());
                if (resolveInfoS.size() > 0) {  
                    return true;  
                } else
                    return false;
            }else
                return false;            
        }catch(NullPointerException e2) {
            if(LogUtil.error_flag) e2.printStackTrace();
            if(LogUtil.flag) Log.v(TAG,"Perfect e2aa666="+e2);
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
	public static List<PackageInfo> getInstalledPackages(Context context) {
		// TODO Auto-generated method stub
		List<PackageInfo> installedPackages=null;
		PackageManager packageManager = context.getPackageManager();
		installedPackages = packageManager.getInstalledPackages(0);
		
		return installedPackages;
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
			String networkType = "Wifi";
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); 
	        if(State.CONNECTED == state){
	        	networkType = "Wifi";
	        }
	        state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState(); 
	        if(State.CONNECTED == state){
	        	networkType = "2Gor3G";
	        }
	        if(LogUtil.flag) Log.v(TAG,"Perfect checkNetworkType networkType="+networkType);
	        return networkType;
	    }
	    public static boolean isNetworkAvailable(Context context) {  	          
	    	try {  
	    		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	    		if (connectivity == null) {
	    		    if(LogUtil.flag) Log.v(TAG, "Perfect couldn't get connectivity manager");
	    			return false;     
	    		} else{     	              
	    			NetworkInfo info = connectivity.getActiveNetworkInfo(); 
	    			if(LogUtil.flag) Log.v(TAG, "Perfect info="+info);
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
	    

	    //Added-s by Perfect 判断是否为系统内置的应用
		public static  boolean checkAppType(Context context,String pname) {
			try {
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(pname, 0);
				// 是系统软件或者是系统软件更新
				if (isSystemApp(pInfo) || isSystemUpdateApp(pInfo)) {
					return true;
				} else {
					return false;
				}

			} catch (NameNotFoundException e) {
			    if(LogUtil.error_flag) e.printStackTrace();
			}
			return false;
		}
		/**
		 * 是否是系统软件或者是系统软件的更新软件
		 * 
		 */
		public static boolean isSystemApp(PackageInfo pInfo) {
			return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
		}
		public static boolean isSystemUpdateApp(PackageInfo pInfo) {
			return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
		}
		//判断的这个应用是否是已经安装的
		public static boolean isInstalled(Context context,String pkgName) {
			PackageManager manager = context.getPackageManager();
			List<PackageInfo> pkgList = manager.getInstalledPackages(0);
			for (int i = 0; i < pkgList.size(); i++) {
				PackageInfo pI = pkgList.get(i);
				if (pI.packageName.equalsIgnoreCase(pkgName))//根据安装的应用的包名判断
					return true;
			}
				return false;
			}
		 public static final int install(Context context, String filePath) {
//	        if(ShellUtils.checkRootPermission()){
//	            return installSilent(context, filePath);
//	        }
	    	if (isSystemApplication(context)) {
	        	return installSilent(context, filePath);
	        }
//	        if (PackageUtil.isSystemApplication(context)) {
//	            if(isInSystemCorrect(context,context.getPackageName()))
//	                return installSilent(context, filePath);
//	            else
//	                return installNormal(context, filePath) ? INSTALL_SUCCEEDED : INSTALL_FAILED_INVALID_URI;
//	        }

	        return installNormal(context, filePath) ? INSTALL_SUCCEEDED : INSTALL_FAILED_INVALID_URI;
	    }
		    /**
		     * whether context is system application
		     * 
		     * @param context
		     * @return
		     */
		    public static boolean isSystemApplication(Context context) {
		        if (context == null) {
		            return false;
		        }

		        return isSystemApplication(context, context.getPackageName());
		    }

		    /**
		     * whether packageName is system application
		     * 
		     * @param context
		     * @param packageName
		     * @return
		     */
		    public static boolean isSystemApplication(Context context, String packageName) {
		        if (context == null) {
		            return false;
		        }

		        return isSystemApplication(context.getPackageManager(), packageName);
		    }

		    /**
		     * whether packageName is system application
		     * 
		     * @param packageManager
		     * @param packageName
		     * @return <ul>
		     * <li>if packageManager is null, return false</li>
		     * <li>if package name is null or is empty, return false</li>
		     * <li>if package name not exit, return false</li>
		     * <li>if package name exit, but not system app, return false</li>
		     * <li>else return true</li>
		     * </ul>
		     */
		    public static boolean isSystemApplication(PackageManager packageManager, String packageName) {
		        if (packageManager == null || packageName == null || packageName.length() == 0) {
		            return false;
		        }

		        try {
		            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
		            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
		        } catch (NameNotFoundException e) {
		            if(LogUtil.error_flag) e.printStackTrace();
		        }
		        return false;
		    }
		    /**
		     * install package normal by system intent
		     * 
		     * @param context
		     * @param filePath file path of package
		     * @return whether apk exist
		     */
		    @SuppressLint("InlinedApi")
		    public static boolean installNormal(Context context, String filePath) {
		        Intent i = new Intent(Intent.ACTION_VIEW);
		        File file = new File(filePath);
		        if (file == null || !file.exists() || !file.isFile() || file.length() <= 0) {
		            return false;
		        }
		          i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
		          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		          context.startActivity(i);

		        return true;
		    }
		    /**
		     * install package silent by root
		     * <ul>
		     * <strong>Attentions:</strong>
		     * <li>Don't call this on the ui thread, it may costs some times.</li>
		     * <li>You should add <strong>android.permission.INSTALL_PACKAGES</strong> in manifest, so no need to request root
		     * permission, if you are system app.</li>
		     * </ul>
		     * 
		     * @param context
		     * @param filePath file path of package
		     * @return {@link PackageUtils#INSTALL_SUCCEEDED} means install success, other means failed. details see
		     * {@link PackageUtils}.INSTALL_FAILED_*. same to {@link PackageManager}.INSTALL_*
		     */
		    public static int installSilent(Context context, String filePath) {
		        if(LogUtil.flag) Log.v(TAG,"Perfect enter into installSilent!");
		        if (filePath == null || filePath.length() == 0) {
		            return INSTALL_FAILED_INVALID_URI;
		        }

		        File file = new File(filePath);
		        if (file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
		            return INSTALL_FAILED_INVALID_URI;
		        }

		        /**
		         * if context is system app, don't need root permission, but should add <uses-permission
		         * android:name="android.permission.INSTALL_PACKAGES" /> in mainfest
		         **/
		        int m_os_Id = android.os.Build.VERSION.SDK_INT;
		        if(LogUtil.flag) Log.v(TAG,"Perfect m_os_Id1116668="+m_os_Id);
		        StringBuilder command = null;
		        //android 5.1以下
		        if(m_os_Id < 22){
		            command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r ")
		                    .append(filePath.replace(" ", "\\ "));
		        }else{
		            command = new StringBuilder().append("pm install -r ")
		                    .append(filePath.replace(" ", "\\ "));
		            }
		        
		        StringBuilder command1 = new StringBuilder().append("chmod 755 ")
		                .append(filePath.replace(" ", "\\ "));
		        if(LogUtil.flag) Log.i(TAG, "command: " + command);        
		        CommandResult commandResult = execCommand( new String[] { command1.toString(), command.toString() }, !isSystemApplication(context), true);
		        if (commandResult.successMsg != null
		            && (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
		            return INSTALL_SUCCEEDED;
		        }        
		       

		        if(LogUtil.flag) Log.e(TAG,
		              new StringBuilder().append("installSilent successMsg:").append(commandResult.successMsg)
		                                 .append(", ErrorMsg:").append(commandResult.errorMsg).toString());
		        if (commandResult.errorMsg == null) {
		            return INSTALL_FAILED_OTHER;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_ALREADY_EXISTS")) {
		            return INSTALL_FAILED_ALREADY_EXISTS;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_APK")) {
		            return INSTALL_FAILED_INVALID_APK;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_URI")) {
		            return INSTALL_FAILED_INVALID_URI;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
		            return INSTALL_FAILED_INSUFFICIENT_STORAGE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_DUPLICATE_PACKAGE")) {
		            return INSTALL_FAILED_DUPLICATE_PACKAGE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_NO_SHARED_USER")) {
		            return INSTALL_FAILED_NO_SHARED_USER;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
		            return INSTALL_FAILED_UPDATE_INCOMPATIBLE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE")) {
		            return INSTALL_FAILED_SHARED_USER_INCOMPATIBLE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_MISSING_SHARED_LIBRARY")) {
		            return INSTALL_FAILED_MISSING_SHARED_LIBRARY;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_REPLACE_COULDNT_DELETE")) {
		            return INSTALL_FAILED_REPLACE_COULDNT_DELETE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_DEXOPT")) {
		            return INSTALL_FAILED_DEXOPT;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_OLDER_SDK")) {
		            return INSTALL_FAILED_OLDER_SDK;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_CONFLICTING_PROVIDER")) {
		            return INSTALL_FAILED_CONFLICTING_PROVIDER;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_NEWER_SDK")) {
		            return INSTALL_FAILED_NEWER_SDK;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_TEST_ONLY")) {
		            return INSTALL_FAILED_TEST_ONLY;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_CPU_ABI_INCOMPATIBLE")) {
		            return INSTALL_FAILED_CPU_ABI_INCOMPATIBLE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_MISSING_FEATURE")) {
		            return INSTALL_FAILED_MISSING_FEATURE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_CONTAINER_ERROR")) {
		            return INSTALL_FAILED_CONTAINER_ERROR;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_INSTALL_LOCATION")) {
		            return INSTALL_FAILED_INVALID_INSTALL_LOCATION;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_MEDIA_UNAVAILABLE")) {
		            return INSTALL_FAILED_MEDIA_UNAVAILABLE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_VERIFICATION_TIMEOUT")) {
		            return INSTALL_FAILED_VERIFICATION_TIMEOUT;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_VERIFICATION_FAILURE")) {
		            return INSTALL_FAILED_VERIFICATION_FAILURE;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_PACKAGE_CHANGED")) {
		            return INSTALL_FAILED_PACKAGE_CHANGED;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_UID_CHANGED")) {
		            return INSTALL_FAILED_UID_CHANGED;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_NOT_APK")) {
		            return INSTALL_PARSE_FAILED_NOT_APK;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_MANIFEST")) {
		            return INSTALL_PARSE_FAILED_BAD_MANIFEST;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION")) {
		            return INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_NO_CERTIFICATES")) {
		            return INSTALL_PARSE_FAILED_NO_CERTIFICATES;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES")) {
		            return INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING")) {
		            return INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME")) {
		            return INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID")) {
		            return INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_MANIFEST_MALFORMED")) {
		            return INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_MANIFEST_EMPTY")) {
		            return INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
		        }
		        if (commandResult.errorMsg.contains("INSTALL_FAILED_INTERNAL_ERROR")) {
		            return INSTALL_FAILED_INTERNAL_ERROR;
		        }
		        return INSTALL_FAILED_OTHER;
		    }
		    public static boolean isWorked(Context context,String serviceClassName)  
	          {  
	           ActivityManager myManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);  
	           ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager.getRunningServices(15);  
	           for(int i = 0 ; i<runningService.size();i++)  
	           {  
	            if(runningService.get(i).service.getClassName().toString().equals(serviceClassName))  
	            {  
	                if(LogUtil.flag) Log.v(TAG,"SysCore serviceClassName="+serviceClassName);
	                if(LogUtil.flag) Log.v(TAG,"SysCore this Service isWorked!");
	             return true;  
	            }  
	           } 
	           if(LogUtil.flag) Log.v(TAG,"SysCore this Service is not Worked!");
	           return false;  
	          }
}
