package com.opentaxi.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import com.opentaxi.android.asynctask.RegionsTask;
import com.opentaxi.models.NewRequest;
import com.opentaxi.models.Users;
import org.codehaus.jackson.map.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppPreferences {
    private static final String APP_SHARED_PREFS = "com.opentaxi.android.driver_preferences"; //  Name of the file -.xml
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    ObjectMapper mapper = new ObjectMapper();

    private static final String SOCKET_TYPE = "SOCKET_TYPE";
    private static final String APP_VERSION = "APP_VERSION";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

    /**
     * Singleton reference to this class.
     */
    private static AppPreferences instance;
    private static final Object mutex = new Object();
    private Map<Integer, String> regionsMap;
    private Integer cloudMessageId;
    private NewRequest currentRequest;
    private NewRequest nextRequest;
    private Double north;
    private Double east;
    private long currentLocationTime; //datetime received from GPS
    private long gpsLastTime = 0; //local Android datetime of last received coordinates

    private String appVersion;
    private String token;
    private Integer socketType;
    private Context context;
    private Users users;

    public AppPreferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.context = context;
    }

    public static synchronized AppPreferences getInstance() {
        return instance;
    }

    public static synchronized AppPreferences getInstance(Context context) {

        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) instance = new AppPreferences(context);
            }
        }

        return instance;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public String encrypt(String value, String salt) {
        if (value != null && salt != null) {
            try {
                final byte[] bytes = value.getBytes("UTF8"); //value != null ? value.getBytes("UTF8") : new byte[0];
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
                SecretKey key = keyFactory.generateSecret(new PBEKeySpec(salt.toCharArray()));
                Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
                pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes("UTF8"), 20));
                String ret = new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), "UTF8");
                Log.i("encrypt", "value:" + value + " to:" + ret);
                return ret;

            } catch (Exception e) {
                if (e.getMessage() != null) Log.e("encrypt:" + value + " salt:" + salt, e.getMessage());
            }
        }
        return null;
    }

    public String decrypt(String value, String salt) {
        if (value != null && salt != null) {
            try {
                final byte[] bytes = Base64.decode(value, Base64.DEFAULT); //value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
                SecretKey key = keyFactory.generateSecret(new PBEKeySpec(salt.toCharArray()));
                Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
                pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes("UTF8"), 20));
                String ret = new String(pbeCipher.doFinal(bytes), "UTF8");
                Log.i("decrypt", "value:" + value + " to:" + ret);
                return ret;

            } catch (Exception e) {
                if (e.getMessage() != null) Log.e("decrypt:" + value + " salt:" + salt, e.getMessage());
            }
        }
        return null;
    }

    public synchronized void setRegions() {
        if (regionsMap == null || regionsMap.isEmpty()) {
            new RegionsTask(new RegionsTask.OnTaskCompleted() {

                @Override
                public void onTaskCompleted(Map<Integer, String> regMap) {
                    regionsMap = regMap;
                }
            }).execute();
        }
    }

    public Map<Integer, String> getRegions() {
        if (regionsMap == null || regionsMap.isEmpty()) {
            setRegions();

            regionsMap = new LinkedHashMap<Integer, String>();
            regionsMap.put(8, "Славейков");
            regionsMap.put(9, "Изгрев");
            regionsMap.put(10, "Зорница");
            regionsMap.put(7, "Лазур");
            regionsMap.put(5, "Бр. Миладинови");
            regionsMap.put(6, "Центъра");
            regionsMap.put(4, "Възраждане");
            regionsMap.put(3, "Акациите");
            regionsMap.put(2, "Победа");
            regionsMap.put(1, "Меден Рудник");
            regionsMap.put(11, "Сарафово");
            regionsMap.put(15, "Крайморие");
            regionsMap.put(13, "Долно Езерово");
            regionsMap.put(14, "Горно Езерово");
            regionsMap.put(12, "Лозово");
            regionsMap.put(17, "Ветрен");
            regionsMap.put(18, "Банево");
        }
        return regionsMap;
    }

    public NewRequest getCurrentRequest() {
        return this.currentRequest;
    }

    public void setCurrentRequest(NewRequest currentRequest) {
        this.currentRequest = currentRequest;
        if (currentRequest != null) Log.i("setCurrentRequest", "setCurrentRequest:" + currentRequest.getRequestsId());
        else Log.i("setCurrentRequest", "setCurrentRequest:null");
    }

    public NewRequest getNextRequest() {
        return nextRequest;
    }

    public void setNextRequest(NewRequest nextRequest) {
        this.nextRequest = nextRequest;
        if (nextRequest != null) Log.i("nextRequest", "nextRequest:" + nextRequest.getRequestsId());
        else Log.i("nextRequest", "nextRequest:null");
    }

    public synchronized Integer getLastCloudMessage() {
        //if (cloudMessageId == null) cloudMessageId = appSharedPrefs.getInt(CLOUD_MESSAGE_ID, 0);
        return cloudMessageId;
    }

    public synchronized void setLastCloudMessage(Integer cloudMessageId) {
        // if (cloudMessageId != null && (this.cloudMessageId == null || !this.cloudMessageId.equals(cloudMessageId))) {
        this.cloudMessageId = cloudMessageId;
       /*     this.prefsEditor = appSharedPrefs.edit();
            prefsEditor.putInt(CLOUD_MESSAGE_ID, cloudMessageId);
            prefsEditor.commit();
        }*/
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth(Double north) {
        this.north = north;
    }

    public Double getEast() {
        return east;
    }

    public void setEast(Double east) {
        this.east = east;
    }

    public long getGpsLastTime() {
        return gpsLastTime;
    }

    public void setGpsLastTime(long gpsLastTime) {
        this.gpsLastTime = gpsLastTime;
    }

    public void setCurrentLocationTime(long currentLocationTime) {
        this.currentLocationTime = currentLocationTime;
    }

    public String getAppVersion() {
        if (appVersion == null) {
            appVersion = appSharedPrefs.getString(APP_VERSION, "");
        }
        return appVersion;
    }

    public void setAppVersion(String version) {
        this.appVersion = version;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(APP_VERSION, version);
        prefsEditor.commit();
    }

    public String getAccessToken() {
        if (token == null) {
            token = appSharedPrefs.getString(ACCESS_TOKEN, "");
        }
        return token;
    }

    public void setAccessToken(String token) {
        this.token = token;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(ACCESS_TOKEN, token);
        prefsEditor.commit();
    }

    public Integer getSocketType() {
        if (socketType == null) {
            socketType = appSharedPrefs.getInt(SOCKET_TYPE, 1); //2); todo
        }
        return socketType;
    }

    public void setSocketType(Integer type) {
        if (type != null) {
            this.socketType = type;
            this.prefsEditor = appSharedPrefs.edit();
            prefsEditor.putInt(SOCKET_TYPE, type);
            prefsEditor.commit();
        }
    }
}
