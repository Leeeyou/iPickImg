package com.example.leeyou.imgpick.utils;

import android.content.Context;
import android.view.WindowManager;

/**
 * 转换手机分辨率的类
 * @author
 *
 */
public class DisplayUtil 
{
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	 
	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	/**
	 * 获得屏幕高度
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context){
		WindowManager wm = (WindowManager) context
	            .getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getHeight();
	}
	/**
	 * 获得屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context){
		WindowManager wm = (WindowManager) context
	            .getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getWidth();
	}
	
	
}