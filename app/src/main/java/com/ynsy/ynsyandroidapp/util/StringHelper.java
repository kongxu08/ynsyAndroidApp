package com.ynsy.ynsyandroidapp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


public class StringHelper {

	/**
	 * test search in str
	 * 
	 * @param str
	 * @return
	 */
	public static String makeSafe(String str) {
		if(isEmpty(str))
			return "";
		return str;
	}

	/**
	 * get first letter and to uppercase
	 *
	 * @param str
	 * @return
	 */
	public static String getFirstLetter(String str) {
		if (isEmpty(str))
			return null;
		return str.substring(0, 1).toUpperCase();
	}

	/**
	 * 第一个字母大写
	 * 
	 * @param str
	 * @return
	 */
	public static String upper1stLetter(String str) {

		if (isEmpty(str))
			return null;

		str = trim(str);

		str = str.substring(0, 1).toUpperCase() + str.substring(1);

		return str;
	}

	/**
	 * 第一个字母小写
	 * 
	 * @param str
	 * @return
	 */
	public static String lower1stLetter(String str) {

		if (isEmpty(str))
			return null;

		str = trim(str);

		str = str.substring(0, 1).toLowerCase() + str.substring(1);

		return str;
	}

	/**
	 * 1 : trim 2 : if "" , return null
	 * 
	 * @param str
	 * @return
	 */
	public static String trim(String str) {
		if (str == null)
			return null;
		str = str.trim();
		if (str.equalsIgnoreCase(""))
			return null;
		return str;
	}

	public static boolean isEmpty(String[] str, boolean isAnd) {
		if (str == null)
			return true;
		for (int idx = 0; idx < str.length; ++idx) {
			if (trim(str[idx]) == null && !isAnd) {
				return true;
			}
			if (trim(str[idx]) != null && isAnd) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(String str) {
		if (str == null)
			return true;
		if (trim(str) == null)
			return true;
		if ("null".equals(trim(str)))
			return true;
		return false;
	}

	public static String arrayToString(Object[] objs) {
		return arrayToString(objs, false);
	}

	public static String arrayToString(Object[] objs, boolean isQuotes) {
		StringBuffer buf = new StringBuffer("");
		if (objs == null || objs.length == 0)
			return buf.toString();
		for (int idx = 0; idx < objs.length; ++idx) {
			if (objs[idx] == null)
				continue;

			String value = "";
			if (objs[idx] instanceof String)
				value = (String) objs[idx];
			else if (objs[idx] instanceof Byte)
				value = ((Byte) objs[idx]).intValue() + "";
			else if (objs[idx] instanceof Long)
				value = ((Long) objs[idx]).longValue() + "";
			else if (objs[idx] instanceof Integer)
				value = ((Integer) objs[idx]).intValue() + "";
			else if (objs[idx] instanceof Double)
				value = ((Double) objs[idx]).doubleValue() + "";
			else
				value = objs[idx].toString();
			if (trim(buf.toString()) != null)
				buf.append(",");

			if (isQuotes)
				buf.append("'");
			buf.append(trim(value));

			if (isQuotes)
				buf.append("'");

		}

		return buf.toString();

	}

	public static String arrayToString(byte[] objs) {
		StringBuffer buf = new StringBuffer("");
		if (objs == null || objs.length == 0)
			return buf.toString();
		for (int idx = 0; idx < objs.length; ++idx) {
			if (idx != 0)
				buf.append(":");
			buf.append(objs[idx]);

		}

		return buf.toString();

	}

	public static byte[] arrayToByte(String originValue) {

		originValue = trim(originValue);
		if (originValue == null)
			return null;

		String[] values = originValue.split(":");
		byte[] bytes = new byte[values.length];

		int idx = 0;
		for (String value : values) {
			bytes[idx] = Byte.valueOf(value);
			++idx;
		}

		return bytes;

	}

	/**
	 * get random
	 * 
	 * @return
	 */
	public static String getRandom() {
		// String random = System.currentTimeMillis() + "";
		// return random.substring(8);
		return "888888";
	}

	/**
	 * convert string
	 * 
	 * @param str
	 * @param oldCharset
	 * @param newCharset
	 * @return
	 */
	public static String convert(String str, String oldCharset,
                                 String newCharset) {
		try {
			if (str != null) {
				byte[] bs = str.getBytes(oldCharset);
				return new String(bs, newCharset);
			}
		} catch (UnsupportedEncodingException ex) {
			ex.getMessage();
		}
		return null;
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换编码的字符串
	 * @param oldCharset
	 *            原编码
	 * @param newCharset
	 *            目标编码
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String changeCharset(String str, String oldCharset,
                                       String newCharset) {
		try {
			if (str != null) {
				// 用旧的字符编码解码字符串。解码可能会出现异常。
				byte[] bs = str.getBytes(oldCharset);
				// 用新的字符编码生成字符串
				return new String(bs, newCharset);
			}
		} catch (UnsupportedEncodingException ex) {
			;
		}
		return null;
	}

	/**
	 * 判断是不是双字节字符串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isDoubleByte(String str) {
		if (str == null)
			return false;

		if (str.length() == str.getBytes().length)
			return false;
		else
			return true;
	}

	public static String convertNull(String string) {
		string = trim(string);
		if (string == null)
			return "";
		else
			return string;
	}

	/**
	 * 汉字转拼音缩写
	 * 
	 * @param str
	 *            //要转换的汉字字符串
	 * @return String //拼音缩写
	 */
	public static String getPYString(String str) {
		String tempStr = "";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c >= 33 && c <= 126) {// 字母和符号原样保留
				tempStr += String.valueOf(c);
			} else {// 累加拼音声母
				tempStr += getPYChar(String.valueOf(c));
			}
		}
		return tempStr;
	}

	/**
	 * 取单个字符的拼音声母
	 * 
	 * @param c
	 *            //要转换的单个汉字
	 * @return String 拼音声母
	 */
	public static String getPYChar(String c) {
		byte[] array = new byte[2];
		array = String.valueOf(c).getBytes();
		int i = (short) (array[0] - '\0' + 256) * 256
				+ ((short) (array[1] - '\0' + 256));
		if (i < 0xB0A1)
			return "*";
		if (i < 0xB0C5)
			return "a";
		if (i < 0xB2C1)
			return "b";
		if (i < 0xB4EE)
			return "c";
		if (i < 0xB6EA)
			return "d";
		if (i < 0xB7A2)
			return "e";
		if (i < 0xB8C1)
			return "f";
		if (i < 0xB9FE)
			return "g";
		if (i < 0xBBF7)
			return "h";
		if (i < 0xBFA6)
			return "j";
		if (i < 0xC0AC)
			return "k";
		if (i < 0xC2E8)
			return "l";
		if (i < 0xC4C3)
			return "m";
		if (i < 0xC5B6)
			return "n";
		if (i < 0xC5BE)
			return "o";
		if (i < 0xC6DA)
			return "p";
		if (i < 0xC8BB)
			return "q";
		if (i < 0xC8F6)
			return "r";
		if (i < 0xCBFA)
			return "s";
		if (i < 0xCDDA)
			return "t";
		if (i < 0xCEF4)
			return "w";
		if (i < 0xD1B9)
			return "x";
		if (i < 0xD4D1)
			return "y";
		if (i < 0xD7FA)
			return "z";
		return "*";
	}
	
	
	/**
	 * MD5单向加密，32位，用于加密密码，因为明文密码在信道中传输不安全，明文保存在本地也不安全
	 * 保存本地密码用
	 * @param str
	 * @return
	 */
	public static String md5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);

		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = (md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	/**
	 * test
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// String sss = "123,234,345,456,";
		// String[] str = sss.split(",");
		//
		// System.out.print(arrayToString(str,true));

		System.out.println(getPYString("汉字转拼音缩写"));

	}
	

	  public static String objectToString(Object obj){
	      try {
	          ByteArrayOutputStream bos=new ByteArrayOutputStream();
	          ObjectOutputStream os=new ObjectOutputStream(bos);
	          //将对象序列化写入byte缓存
	          os.writeObject(obj);
	          //将序列化的数据转为16进制保存
	          String bytesToHexString = bytesToHexString(bos.toByteArray());
	          return bytesToHexString;
	          //保存该16进制数组
	      } catch (IOException e) {
	          e.printStackTrace();
//	          Log.e("", "保存obj失败");
	      }
	      return null;
	  }
	  /**
	   * desc:将数组转为16进制
	   * @param bArray
	   * @return
	   * modified:    
	   */
	  public static String bytesToHexString(byte[] bArray) {
	      if(bArray == null){
	          return null;
	      }
	      if(bArray.length == 0){
	          return "";
	      }
	      StringBuffer sb = new StringBuffer(bArray.length);
	      String sTemp;
	      for (int i = 0; i < bArray.length; i++) {
	          sTemp = Integer.toHexString(0xFF & bArray[i]);
	          if (sTemp.length() < 2)
	              sb.append(0);
	          sb.append(sTemp.toUpperCase());
	      }
	      return sb.toString();
	  }

}
