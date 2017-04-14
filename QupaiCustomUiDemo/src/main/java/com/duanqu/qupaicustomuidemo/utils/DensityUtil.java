package com.duanqu.qupaicustomuidemo.utils;

import android.content.Context;

public class DensityUtil {
	
	public static int dip2px(Context paramContext, float paramFloat){
	    return (int)(0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density);
	}

    public static int px2dip(Context paramContext, float paramFloat){
	    return (int)(0.5F + paramFloat / paramContext.getResources().getDisplayMetrics().density);
	}

	  /**
		 * 将sp值转换为px值，保证文字大小不变
		 * 
		 * @param spValue
		 *            （DisplayMetrics类中属性scaledDensity）
		 * @return
		 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
	
	public static int dip2px(float value){
		return (int)(0.5F + value * MySystemParams.getInstance().scale);
	}
	
	public static int px2dip(float value){
		return (int)(0.5F + value / MySystemParams.getInstance().scale);
	}
	
	public static int sp2px(float value){
		return (int)(0.5F + value * MySystemParams.getInstance().fontScale);
	}
	
	public static int getActualScreenWidth(){
		int width = 0;
	    MySystemParams systemparams = MySystemParams.getInstance();
		if(systemparams.screenOrientation == MySystemParams.SCREEN_ORIENTATION_HORIZONTAL){
			width = systemparams.screenHeight;
		}else{
			width = systemparams.screenWidth;
		}
		return width;
	}
	
	public static int getActualScreenHeight(){
		int height = 0;
	    MySystemParams systemparams = MySystemParams.getInstance();
		if(systemparams.screenOrientation == MySystemParams.SCREEN_ORIENTATION_HORIZONTAL){
			height = systemparams.screenWidth;
		}else{
			height = systemparams.screenHeight;
		}
		return height;
	}

}
