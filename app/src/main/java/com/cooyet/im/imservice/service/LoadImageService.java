package com.cooyet.im.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;

import com.cooyet.im.DB.sp.SystemConfigSp;
import com.cooyet.im.config.SysConstant;
import com.cooyet.im.imservice.entity.ImageMessage;
import com.cooyet.im.imservice.event.MessageEvent;
import com.cooyet.im.ui.helper.PhotoHelper;
import com.cooyet.im.utils.FileUtil;
import com.cooyet.im.utils.LocationUtil;
import com.cooyet.im.utils.Logger;
import com.cooyet.im.utils.MoGuHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * @author : ronghua.xie on 15-1-12.
 * @email : ronghua.xie@cooyet.com.
 */
public class LoadImageService extends IntentService {

    private static Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadImageService() {
        super("LoadImageService");
    }

    public LoadImageService(String name) {
        super(name);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ImageMessage messageInfo = (ImageMessage) intent.getSerializableExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS);
        String result = null;
        Bitmap bitmap = null;
        try {
            File file = new File(messageInfo.getPath());
            ExifInterface exi = new ExifInterface(messageInfo.getPath());

            if (file.exists() && FileUtil.getExtensionName(messageInfo.getPath()).toLowerCase().equals(".gif")) {
                MoGuHttpClient httpClient = new MoGuHttpClient();
                SystemConfigSp.instance().init(getApplicationContext());
                result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getPath()), messageInfo.getPath());
            } else {

                try {
                    bitmap = PhotoHelper.revitionImage(messageInfo.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (null != bitmap) {
                    MoGuHttpClient httpClient = new MoGuHttpClient();
                    double longitude = LocationUtil.convertRationalLatLonToFloat(exi.getAttribute(ExifInterface.TAG_GPS_LATITUDE), exi.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
                    double latitude = LocationUtil.convertRationalLatLonToFloat(exi.getAttribute(ExifInterface.TAG_GPS_LONGITUDE), exi.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));

                    if (longitude != 0 || latitude != 0) {
                        FileOutputStream fos = null;
                        ByteArrayOutputStream baos = null;
                        FileInputStream fis = null;
                        ByteArrayOutputStream baos1 = null;
                        try {
                            String path = Environment.getExternalStorageDirectory().getPath() + "/TTIM";
                            String fileName = "temp_upload.jpg";
                            File dir = new File(path);
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            File jpegFile = new File(path + File.separator + fileName);

                            baos = new ByteArrayOutputStream();
                            fos = new FileOutputStream(jpegFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            fos.write(baos.toByteArray());
                            fos.flush();

                            ExifInterface exif = new ExifInterface(jpegFile.getAbsolutePath());
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationUtil.degressToString(longitude));
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                                    longitude > 0 ? "E" : "W");
                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationUtil.degressToString(latitude));
                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                                    latitude > 0 ? "N" : "S");
                            exif.saveAttributes();

                            fis = new FileInputStream(jpegFile);
                            baos1 = new ByteArrayOutputStream(1000);
                            byte[] b = new byte[1000];
                            int n;
                            while ((n = fis.read(b)) != -1) {
                                baos1.write(b, 0, n);
                            }
                            fis.close();
                            baos1.close();
                            byte[] bytes = baos1.toByteArray();
//                            result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getPath());
                            messageInfo.setUrl(messageInfo.getPath());
                            EventBus.getDefault().post(new MessageEvent(
                                    MessageEvent.Event.IMAGE_UPLOAD_SUCCESS
                                    , messageInfo));

                        } catch (Exception e) {
                            if (fos != null) {
                                fos.close();
                            }
                            if (baos != null) {
                                baos.close();
                            }
                            if (fis != null) {
                                fis.close();
                            }
                            if (baos1 != null) {
                                baos1.close();
                            }
                            e.printStackTrace();
                        }
                    } else {
                        byte[] bytes = PhotoHelper.getBytes(bitmap);
                        result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getPath());
                        if (TextUtils.isEmpty(result)) {
                            logger.i("upload image faild,cause by result is empty/null");
                            EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILD
                                    , messageInfo));
                        } else {
                            logger.i("upload image succcess,imageUrl is %s", result);
                            String imageUrl = result;
                            messageInfo.setUrl(imageUrl);
                            EventBus.getDefault().post(new MessageEvent(
                                    MessageEvent.Event.IMAGE_UPLOAD_SUCCESS
                                    , messageInfo));
                        }
                    }
                }

            }


        } catch (IOException e) {
            logger.e(e.getMessage());
        }


    }

    private void printMessage(String tag, ExifInterface e) {
        System.out.println(tag + "\n"
                + LocationUtil.convertRationalLatLonToFloat(e.getAttribute(ExifInterface.TAG_GPS_LATITUDE), e.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)) + "\n"
                + e.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "\n"
                + e.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD)
                + "\n" + e.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) + "\n"
                + e.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
    }
}
