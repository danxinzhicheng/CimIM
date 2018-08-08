package com.cooyet.im.utils;

import android.text.TextUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by user on 2018/6/8.
 */

public class ByteUtils {
    private ByteUtils() {

    }

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 将byte数组转换为十六进制：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun2(byte[] bytes) {
        char[] buf = new char[bytes.length * 3];
        int index = 0;
        for (byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
            buf[index++] = ' ';
        }
        return new String(buf);
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            if (TextUtils.isEmpty(subStr)) {
                continue;
            }
            bytes[i] = (byte) Integer.parseInt(subStr.trim(), 16);
        }

        return bytes;
    }


    public static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') return (c - '0');
        if (c >= 'A' && c <= 'F') return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f') return (c - 'a' + 10);
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    //十六进制字符串转换成字节数组
    public static byte[] hexStringToBytes(String s) {
        byte[] ret;
        if (s == null) return null;
        int sz = s.length();
        ret = new byte[sz / 2];
        for (int i = 0; i < sz; i += 2) {

            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4)
                    | hexCharToInt(s.charAt(i + 1)));
        }
        return ret;
    }

    //字节数组转换成十六进制字符串
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder ret = new StringBuilder(2 * bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            int b;
            b = 0x0f & (bytes[i] >> 4);
            ret.append("0123456789abcdef".charAt(b));
            b = 0x0f & bytes[i];
            ret.append("0123456789abcdef".charAt(b));
        }
        return ret.toString();
    }

    public static byte[] littletobig(byte[] bytes) throws IOException {
        int dataLength = bytes.length;
        int shortlength = dataLength / 2;
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, dataLength);
        ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.BIG_ENDIAN).asShortBuffer();//此处设置大端

        short[] shorts = new short[shortlength];
        shortBuffer.get(shorts, 0, shortlength);
        byte[] bb = CommonUtil.toByteArray(shorts);
        return bb;
    }

    public static byte[] bigtolittle(byte[] bytes) throws IOException {

////        File file = new File(fileName);    //filename为pcm文件，请自行设置
////        InputStream in = null;
//        byte[] bytes = null;
////        in = new FileInputStream(file);
//        bytes = new byte[inputStream.available()];//in.available()是得到文件的字节数
//        int length = bytes.length;
//        while (length != 1) {
//            long i = inputStream.read(bytes, 0, bytes.length);
//            if (i == -1) {
//                break;
//            }
//            length -= i;
//        }
        int dataLength = bytes.length;
        int shortlength = dataLength / 2;
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, dataLength);
        ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();//此处设置小端

        short[] shorts = new short[shortlength];
        shortBuffer.get(shorts, 0, shortlength);
        byte[] bb = CommonUtil.toByteArray(shorts);
//        short[] shorts = new short[shortlength];
//        shortBuffer.get(shorts, 0, shortlength);
//        File file1 = File.createTempFile("pcm", null);//输出为临时文件
//        String pcmtem = file1.getPath();
//        FileOutputStream fos1 = new FileOutputStream(file1);
//        BufferedOutputStream bos1 = new BufferedOutputStream(fos1);
//        DataOutputStream dos1 = new DataOutputStream(bos1);
//        for (int i = 0; i < shorts.length; i++) {
//            dos1.writeShort(shorts[i]);
//        }
//        dos1.close();
        return bb;
    }


}
