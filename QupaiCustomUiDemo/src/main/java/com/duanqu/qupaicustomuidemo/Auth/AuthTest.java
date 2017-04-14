package com.duanqu.qupaicustomuidemo.Auth;

import android.content.Context;
import android.util.Log;

import com.duanqu.qupai.auth.AuthService;
import com.duanqu.qupai.auth.QupaiAuthListener;
import com.duanqu.qupaicustomuidemo.utils.Constant;

/**
 * 鉴权示例
 */
public class AuthTest {

    private static final String AUTHTAG = "QupaiAuth";
    private static AuthTest instance;

    public static AuthTest getInstance() {
        if (instance == null) {
            instance = new AuthTest();
        }
        return instance;
    }

    /**
     * 鉴权, 获取accessToken
     *
     * @param context
     * @param appKey    appkey
     * @param appsecret appsecret
     * @param space     space请传递应用的用户ID。这样在后台查看到的是空间就是该用户的流数据
     */
    public void initAuth(Context context, String appKey, String appsecret, String space) {
        Log.e(AUTHTAG, "space" + Constant.SPACE);

        AuthService service = AuthService.getInstance();
        service.setQupaiAuthListener(new QupaiAuthListener() {
            @Override
            public void onAuthError(int errorCode, String message) {
                Log.e(AUTHTAG, "ErrorCode" + errorCode + "message" + message);
            }

            @Override
            public void onAuthComplte(int responseCode, String responseMessage) {
                Log.e(AUTHTAG, "onAuthComplte" + responseCode + "message" + responseMessage);
                Constant.accessToken = responseMessage;
            }
        });
        service.startAuth(context, appKey, appsecret, space);
    }
}
