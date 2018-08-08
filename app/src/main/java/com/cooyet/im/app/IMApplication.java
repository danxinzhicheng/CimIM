package com.cooyet.im.app;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.cooyet.im.imservice.manager.IMSocketManager;
import com.cooyet.im.imservice.service.IMService;
import com.cooyet.im.utils.ImageLoaderUtil;
import com.cooyet.im.utils.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.ArrayList;

public class IMApplication extends Application {

    private Logger logger = Logger.getLogger(IMApplication.class);
    public static String mapOfflinePath;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        logger.i("Application starts");
        CrashReport.initCrashReport(getApplicationContext(), "1dc47b25ec", true);

        startIMService();
        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());

        String path = getSDPath() + "/" + "wbMap";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        mapOfflinePath = path;
        MapsInitializer.sdcardDir = mapOfflinePath;
        OfflineMapManager amapManager = new OfflineMapManager(this, new OfflineMapManager.OfflineMapDownloadListener() {
            @Override
            public void onDownload(int i, int i1, String s) {

            }

            @Override
            public void onCheckUpdate(boolean b, String s) {

            }

            @Override
            public void onRemove(boolean b, String s, String s1) {

            }
        });
        try {
            amapManager.downloadByCityCode("025");
        } catch (AMapException e) {
            e.printStackTrace();
        }
        ArrayList<OfflineMapCity> list = amapManager.getDownloadingCityList();
        Log.i("mmm", "list:" + list.size());

//        IMSocketManager.instance().connectMsgServer();
    }


	private void startIMService() {
		logger.i("start IMService");
		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

    public static boolean gifRunning = true;//gif是否运行
}
