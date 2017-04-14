package com.duanqu.qupaicustomuidemo.engine.session;

import android.content.Context;

import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;

import java.io.Serializable;

public class VideoSessionClientFactoryImpl extends SessionClientFactory {

    @Override
    public VideoSessionClient createSessionClient(Context context, Serializable data) {
        QupaiApplication app = (QupaiApplication) context.getApplicationContext();
        return app.videoSessionClient;
    }

}
