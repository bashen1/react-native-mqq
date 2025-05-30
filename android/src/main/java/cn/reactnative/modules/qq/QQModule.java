package cn.reactnative.modules.qq;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by tdzl2_000 on 2015-10-10.
 * <p>
 * Modified by Renguang Dong on 2016-05-25.
 */
public class QQModule extends ReactContextBaseJavaModule implements IUiListener, ActivityEventListener {
    public final String TAG = "mqq";
    private Context mContext;
    private String appId;
    private Tencent api;
    private final static String INVOKE_FAILED = "QQ API invoke returns false.";
    private static final String NOT_INIT = "initApp required";
    private boolean isLogin;

    private static final String RCTQQShareTypeNews = "news";
    private static final String RCTQQShareTypeImage = "image";
    private static final String RCTQQShareTypeText = "text";
    private static final String RCTQQShareTypeVideo = "video";
    private static final String RCTQQShareTypeAudio = "audio";

    private static final String RCTQQShareType = "type";
    private static final String RCTQQShareText = "text";
    private static final String RCTQQShareTitle = "title";
    private static final String RCTQQShareDescription = "description";
    private static final String RCTQQShareWebpageUrl = "webpageUrl";
    private static final String RCTQQShareImageUrl = "imageUrl";

    private static final int SHARE_RESULT_CODE_SUCCESSFUL = 0;
    private static final int SHARE_RESULT_CODE_FAILED = 1;
    private static final int SHARE_RESULT_CODE_CANCEL = 2;

    public QQModule(ReactApplicationContext context) {
        super(context);
        mContext = context;
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new Error(e);
        }
        if (!appInfo.metaData.containsKey("QQ_APPID")) {
            throw new Error("meta-data QQ_APPID not found in AndroidManifest.xml");
        }
        this.appId = appInfo.metaData.get("QQ_APPID").toString();
    }

    @Override
    public void initialize() {
        super.initialize();
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {

        if (api != null) {
            api = null;
        }
        getReactApplicationContext().removeActivityEventListener(this);

        super.onCatalystInstanceDestroy();
    }

    @Override
    public String getName() {
        return "RCTQQAPI";
    }

    @ReactMethod
    public void initSDK(Promise promise) {
        if (api == null) {
            Tencent.setIsPermissionGranted(true); //标识已经同意隐私合规，否则无法调用API
            String packageNames = null;
            try {
                PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                packageNames = info.packageName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (packageNames != null) {
                Log.d(TAG, "packageNames: " + packageNames);
                api = Tencent.createInstance(appId, getReactApplicationContext().getApplicationContext(), packageNames + ".fileprovider");
            } else {
                api = Tencent.createInstance(appId, getReactApplicationContext().getApplicationContext());
            }
        }
    }

    @ReactMethod
    public void isQQInstalled(Promise promise) {
        if (api == null) {
            promise.reject(NOT_INIT);
            return;
        }
        if (api.isSupportSSOLogin(getCurrentActivity())) {
            promise.resolve(true);
        } else {
            promise.reject("not installed");
        }
    }

    @ReactMethod
    public void isQQSupportApi(Promise promise) {
        if (api == null) {
            promise.reject(NOT_INIT);
            return;
        }
        if (api.isSupportSSOLogin(getCurrentActivity())) {
            promise.resolve(true);
        } else {
            promise.reject("not support");
        }
    }

    @ReactMethod
    public void login(String scopes, Promise promise) {
        if (api == null) {
            promise.reject(NOT_INIT);
            return;
        }
        this.isLogin = true;
        if (!api.isSessionValid()) {
            api.login(getCurrentActivity(), scopes == null ? "get_simple_userinfo" : scopes, this);
            promise.resolve(null);
        } else {
            promise.reject(INVOKE_FAILED);
        }
    }

    @ReactMethod
    public void shareToQQ(ReadableMap data, Promise promise) {
        if (api == null) {
            promise.reject(NOT_INIT);
            return;
        }
        this._shareToQQ(data, 0);
        promise.resolve(null);
    }

    @ReactMethod
    public void shareToQzone(ReadableMap data, Promise promise) {
        if (api == null) {
            promise.reject(NOT_INIT);
            return;
        }
        this._shareToQQ(data, 1);
        promise.resolve(null);
    }

    private void _shareToQQ(ReadableMap data, int scene) {
        this.isLogin = false;
        Bundle bundle = new Bundle();
        if (data.hasKey(RCTQQShareTitle)) {
            bundle.putString(QQShare.SHARE_TO_QQ_TITLE, data.getString(RCTQQShareTitle));
        }
        if (data.hasKey(RCTQQShareDescription)) {
            bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, data.getString(RCTQQShareDescription));
        }
        if (data.hasKey(RCTQQShareWebpageUrl)) {
            bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, data.getString(RCTQQShareWebpageUrl));
        }
        if (data.hasKey(RCTQQShareImageUrl)) {
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, data.getString(RCTQQShareImageUrl));
        }
        if (data.hasKey("appName")) {
            bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, data.getString("appName"));
        }

        String type = RCTQQShareTypeNews;
        if (data.hasKey(RCTQQShareType)) {
            type = data.getString(RCTQQShareType);
        }

        if (type.equals(RCTQQShareTypeNews)) {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        } else if (type.equals(RCTQQShareTypeImage)) {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, data.getString(RCTQQShareImageUrl));
        } else if (type.equals(RCTQQShareTypeAudio)) {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
            if (data.hasKey("flashUrl")) {
                bundle.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, data.getString("flashUrl"));
            }
        } else if (type.equals("app")) {
            // 腾讯SDK 3.5.2.15 弃用了分享APP功能
        }

        Log.e("QQShare", bundle.toString());

        if (scene == 0) {
            // Share to QQ.
            bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
            api.shareToQQ(getCurrentActivity(), bundle, this);
        } else if (scene == 1) {
            // Share to Qzone.
            bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
            api.shareToQQ(getCurrentActivity(), bundle, this);
        }
    }

    private String _getType() {
        return (this.isLogin ? "QQAuthorizeResponse" : "QQShareResponse");
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, this);
    }

    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onComplete(Object o) {

        WritableMap resultMap = Arguments.createMap();

        if (isLogin) {
            resultMap.putString("type", "QQAuthorizeResponse");
            try {
                JSONObject obj = (JSONObject) (o);
                resultMap.putInt("errCode", 0);
                resultMap.putString("openid", obj.getString(Constants.PARAM_OPEN_ID));
                resultMap.putString("access_token", obj.getString(Constants.PARAM_ACCESS_TOKEN));
                resultMap.putString("oauth_consumer_key", this.appId);
                resultMap.putDouble("expires_in", (new Date().getTime() + obj.getLong(Constants.PARAM_EXPIRES_IN)));
            } catch (Exception e) {
                WritableMap map = Arguments.createMap();
                map.putInt("errCode", Constants.ERROR_UNKNOWN);
                map.putString("errMsg", e.getLocalizedMessage());

                getReactApplicationContext().getJSModule(RCTNativeAppEventEmitter.class).emit("QQ_Resp", map);
            }
        } else {
            resultMap.putString("type", "QQShareResponse");
            resultMap.putInt("errCode", SHARE_RESULT_CODE_SUCCESSFUL);
            resultMap.putString("message", "Share successfully.");
        }

        this.resolvePromise(resultMap);
    }

    @Override
    public void onError(UiError uiError) {
        WritableMap resultMap = Arguments.createMap();
        resultMap.putInt("errCode", SHARE_RESULT_CODE_FAILED);
        resultMap.putString("message", "Share failed." + uiError.errorDetail);

        this.resolvePromise(resultMap);
    }

    @Override
    public void onCancel() {
        WritableMap resultMap = Arguments.createMap();
        resultMap.putInt("errCode", SHARE_RESULT_CODE_CANCEL);
        resultMap.putString("message", "Share canceled.");

        this.resolvePromise(resultMap);
    }

    @Override
    public void onWarning(int i) {

    }

    private void resolvePromise(ReadableMap resultMap) {
        getReactApplicationContext().getJSModule(RCTNativeAppEventEmitter.class).emit("QQ_Resp", resultMap);

    }
}
