package com.ynsy.ynsyandroidapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Base64Util {
    /**
     * 通过Base32将Bitmap转换成Base64字符串
     *
     * @param bit
     * @return
     */
    public static String Bitmap2StrByBase64(Bitmap bit) {
        String result = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        result = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
        result = result.replaceAll("[\\s*\t\n\r]", "");
        return result;
    }

    /**
     * base64编码字符集转化成图片文件。
     *
     * @param base64Str
     * @return Bitmap
     */
    public static Bitmap base64ToFile(String base64Str) {
        if (base64Str.indexOf("data:image/png;base64,") != -1) {
            base64Str = base64Str.substring("data:image/png;base64,".length());
        }
        if (base64Str.indexOf("data:image/jpg;base64,") != -1) {
            base64Str = base64Str.substring("data:image/jpg;base64,".length());
        }
        if (base64Str.indexOf("data:image/jpeg;base64,") != -1) {
            base64Str = base64Str.substring("data:image/jpeg;base64,".length());
        }
        byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                //调整异常数据
                data[i] += 256;
            }
        }
        if (data.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        result = "data:image/jpeg;base64," + result;
        result = result.replaceAll("[\\s*\t\n\r]", "");
        return result;
    }

}
