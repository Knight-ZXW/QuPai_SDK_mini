package com.duanqu.qupaicustomuidemo.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;

public class ToastUtil {
	public static final int DEFAULT_LENGTH = 5000;
	private static Toast mToast = null;

	public static void showDIYToast(Context paramContext, int layout, int duration, IinitViewListener paramIinitViewListener){
	    View localView = FontUtil.applyFontByInflate(paramContext, layout, null, false);
	    paramIinitViewListener.initView(localView);
		Toast localToast = new Toast(paramContext);
		localToast.setGravity(17, 0, 0);
		localToast.setDuration(duration);
		localToast.setView(localView);
		localToast.show();
	}

	public static void showLoginSuccessToast(Context paramContext){
		View localView = FontUtil.applyFontByInflate(paramContext, 2130903258, null, false);
		Toast localToast = Toast.makeText(paramContext, "", Toast.LENGTH_LONG);
		localToast.setGravity(17, 0, 0);
		localToast.setDuration(Toast.LENGTH_LONG);
		localToast.setView(localView);
		localToast.show();
	}

	public static void showNetErrorToast(Context paramContext){
		View localView = FontUtil.applyFontByInflate(paramContext, 2130903259, null, false);
		Toast localToast = Toast.makeText(paramContext, "", Toast.LENGTH_LONG);
		localToast.setGravity(17, 0, 0);
		localToast.setDuration(Toast.LENGTH_LONG);
		localToast.setView(localView);
		localToast.show();
	}

	public static void showToast(Context paramContext){
		  showToast(paramContext, "上传失败");
	  }

	public static void showToast(Context context, int rsid){
		showToast(context, context.getResources().getString(rsid), DEFAULT_LENGTH);
	}

	public static void showToast(Context context, int rsid, int gravity){
		showToast(context, context.getResources().getString(rsid), DEFAULT_LENGTH, gravity);
	}

	public static void showToast(Context paramContext, String paramString){
		showToast(paramContext, paramString, DEFAULT_LENGTH);
	}

	public static void showToast(Context paramContext, String paramString, int paramInt){
		showToast(paramContext, paramString, paramInt, Gravity.CENTER);
	}

	public static void showToast(final Context paramContext,
				final String paramString, final int paramInt, final int gravity){
		final View toastRoot = FontUtil.applyFontByInflate(
				  paramContext, R.layout.toast_default_layout, null, false);
		TextView message = (TextView) toastRoot.findViewById(R.id.toast_info);
		message.setText(paramString);

		if(mToast == null) {
			mToast = new Toast(paramContext);
		}

		mToast.setGravity(gravity, 0, 0);
		mToast.setDuration(paramInt);
		mToast.setView(toastRoot);
		mToast.show();
	}

	public static void showToastCanCancel(final Context paramContext,
				final String paramString, final int paramInt, final int gravity) {
		final View toastRoot = FontUtil.applyFontByInflate(
				paramContext, R.layout.toast_default_layout, null, false);
		TextView message = (TextView) toastRoot.findViewById(R.id.toast_info);
		message.setText(paramString);

		if(mToast == null) {
			mToast = new Toast(paramContext);
		}

		mToast.setGravity(gravity, 0, 0);
		mToast.setDuration(paramInt);
		mToast.setView(toastRoot);
		mToast.show();
	}

	  static abstract interface IinitViewListener{
		  public abstract void initView(View paramView);
	  }

}
