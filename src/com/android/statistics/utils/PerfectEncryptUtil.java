package com.android.statistics.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;*/

/**
 * Java 加解密工具类
 * 
 *
 */
public class PerfectEncryptUtil {

    private static final char[] legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray(); 
    private static String key = "saW1@Z_p";

    private static final String UTF8 = "utf-8";
    //定义 加密算法,可用 DES,DESede,Blowfish
    private static final String ALGORITHM_DESEDE = "DESede";
    /**
     * 3DES加密
     * 
     * @param src
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptDES(String src) throws Exception {
        SecretKey secretKey = new SecretKeySpec(build3DesKey("AdADD"), ALGORITHM_DESEDE);
        Cipher cipher = Cipher.getInstance(ALGORITHM_DESEDE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] b = cipher.doFinal(src.getBytes(UTF8));
        
        return byte2HexStr(b);
    }
    
    /**
     * 3DES解密
     * 
     * @param dest
     * @param key
     * @return
     * @throws Exception
     */
    public static String decryptDES(String dest) throws Exception {
        SecretKey secretKey = new SecretKeySpec(build3DesKey("AdADD"), ALGORITHM_DESEDE);
        Cipher cipher = Cipher.getInstance(ALGORITHM_DESEDE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] b = cipher.doFinal(str2ByteArray(dest));
        
        return new String(b, UTF8);
    
    }
    
    /**
     * 字节数组转化为大写16进制字符串
     * 
     * @param b
     * @return
     */
    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }
            
            sb.append(s.toUpperCase());
        }
        
        return sb.toString();
    }
    
    
    /**
     * 字符串转字节数组
     * 
     * @param s
     * @return
     */
    private static byte[] str2ByteArray(String s) {
        int byteArrayLength = s.length()/2;
        byte[] b = new byte[byteArrayLength];
        for (int i = 0; i < byteArrayLength; i++) {
            byte b0 = (byte) Integer.valueOf(s.substring(i*2, i*2+2), 16).intValue();
            b[i] = b0;
        }
        
        return b;
    }
    /**
     * 构造3DES加解密方法key
     * 
     * @param keyStr
     * @return
     * @throws Exception
     */
    private static byte[] build3DesKey(String keyStr) throws Exception {
        byte[] key = new byte[24];
        byte[] temp = keyStr.getBytes(UTF8);
        if (key.length > temp.length) {
            System.arraycopy(temp, 0, key, 0, temp.length);
        } else {
            System.arraycopy(temp, 0, key, 0, key.length);
        }
        
        return key;
    }
//  //加密
  public static String encryptDES_test(String encryptString)
          throws Exception {
      String encryptKey = key;
      byte[] iv = {2,5,4,1,6,8,3,7};//这个很重要不能更改
      IvParameterSpec zeroIv = new IvParameterSpec(iv);
      SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
      Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
      byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
      return encode(encryptedData);
  }
  
  //解密
  public  static String decryptDES_test(String decryptString)
          throws Exception {
      String decryptKey = key;
      byte[] iv = {2,5,4,1,6,8,3,7};
      byte[] byteMi = decode(decryptString);
      IvParameterSpec zeroIv = new IvParameterSpec(iv);
      SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
      Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
      byte decryptedData[] = cipher.doFinal(byteMi);
      return new String(decryptedData);
  }
  
  private static int decode(char c) {
      if (c >= 'A' && c <= 'Z')
          return ((int) c) - 65;
      else if (c >= 'a' && c <= 'z')
          return ((int) c) - 97 + 26;
      else if (c >= '0' && c <= '9')
          return ((int) c) - 48 + 26 + 26;
      else
          switch (c) {
          case '+':
              return 62;
          case '/':
              return 63;
          case '=':
              return 0;
          default:
              throw new RuntimeException("unexpected code: " + c);
          }
  }
  private static byte[] decode(String s) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try {
          decode(s, bos);
      } catch (IOException e) {
          throw new RuntimeException();
      }
      byte[] decodedBytes = bos.toByteArray();
      try {
          bos.close();
          bos = null;
      } catch (IOException ex) {
          System.err.println("Error while decoding BASE64: " + ex.toString());
      }
      return decodedBytes;
  }

  private static void decode(String s, OutputStream os) throws IOException {
      int i = 0;
      int len = s.length();
      while (true) {
          while (i < len && s.charAt(i) <= ' ')
              i++;
          if (i == len)
              break;
          int tri = (decode(s.charAt(i)) << 18)
                  + (decode(s.charAt(i + 1)) << 12)
                  + (decode(s.charAt(i + 2)) << 6)
                  + (decode(s.charAt(i + 3)));
          os.write((tri >> 16) & 255);
          if (s.charAt(i + 2) == '=')
              break;
          os.write((tri >> 8) & 255);
          if (s.charAt(i + 3) == '=')
              break;
          os.write(tri & 255);
          i += 4;
      }
  }   
  
  private static String encode(byte[] data) {
      int start = 0;
      int len = data.length;
      StringBuffer buf = new StringBuffer(data.length * 3 / 2);
      int end = len - 3;
      int i = start;
      int n = 0;
      while (i <= end) {
          int d = ((((int) data[i]) & 0x0ff) << 16)
                  | ((((int) data[i + 1]) & 0x0ff) << 8)
                  | (((int) data[i + 2]) & 0x0ff);
          buf.append(legalChars[(d >> 18) & 63]);
          buf.append(legalChars[(d >> 12) & 63]);
          buf.append(legalChars[(d >> 6) & 63]);
          buf.append(legalChars[d & 63]);
          i += 3;
          if (n++ >= 14) {
              n = 0;
              buf.append(" ");
          }
      }
      if (i == start + len - 2) {
          int d = ((((int) data[i]) & 0x0ff) << 16)
                  | ((((int) data[i + 1]) & 255) << 8);
          buf.append(legalChars[(d >> 18) & 63]);
          buf.append(legalChars[(d >> 12) & 63]);
          buf.append(legalChars[(d >> 6) & 63]);
          buf.append("=");
      } else if (i == start + len - 1) {
          int d = (((int) data[i]) & 0x0ff) << 16;
          buf.append(legalChars[(d >> 18) & 63]);
          buf.append(legalChars[(d >> 12) & 63]);
          buf.append("==");
      }
      return buf.toString();
  }
  
  public static String decryptDESMchMsData(String dest) throws Exception {
      SecretKey secretKey = new SecretKeySpec(build3DesKey("sdf&&345400@"), ALGORITHM_DESEDE);
      Cipher cipher = Cipher.getInstance(ALGORITHM_DESEDE);
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] b = cipher.doFinal(str2ByteArray(dest));
      
      return new String(b, UTF8);
  }
  
  public static String U(String dest) throws Exception {
      SecretKey secretKey = new SecretKeySpec(build3DesKey("AdADD"), ALGORITHM_DESEDE);
      Cipher cipher = Cipher.getInstance(ALGORITHM_DESEDE);
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] b = cipher.doFinal(str2ByteArray(dest));
      
      return new String(b, UTF8);
  
  }
}
