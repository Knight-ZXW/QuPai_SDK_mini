package com.duanqu.qupaicustomuidemo.editor;

import android.view.Gravity;
import android.widget.FrameLayout;

public class VideoScaleHelper {

    private static final float SCALE_SQUARE = 1;
    private static final float SCALE_BROAD = 16f / 9f;
    private static final float SCALE_NARROW = 4f / 5f;
    private static final float SCALE_BROAD_FIXED = 9f / 16f;

    private int screenWidth;
    private int screenHeight;

    private int videoWidth;
    private int videoHeight;

    private int displayWidth;
    private int displayHeight;

    private int layoutWidth;
    private int layoutHeight;

    public VideoScaleHelper setScreenWidthAndHeight(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        return this;
    }

    public VideoScaleHelper setVideoWidthAndHeight(int width, int height) {
        this.videoWidth = width;
        this.videoHeight = height;
        return this;
    }

    public void generateDisplayWidthAndHeight() {
        if (screenHeight == 0 || screenWidth == 0
                || videoHeight == 0 || videoWidth == 0) {
            return;
        }
        int width = 0;
        int height = 0;
        //视频的宽高比
        float scaleWH = (float) videoWidth / (float) videoHeight;

        if (scaleWH > SCALE_BROAD) {
            //宽高比大于16：9 1.77
            //高 = 屏幕宽度 /（16：9）  宽=视频宽高比 * 高  这样视频宽高比大于16：9的话裁剪的宽高比就是16：9
            height = (int) (screenWidth / SCALE_BROAD);
            width = (int) (scaleWH * height);
        } else if (scaleWH <= SCALE_BROAD && scaleWH >= SCALE_SQUARE) {
            //宽高比<16:9 且 宽高比大于1 宽 = 屏幕宽度 高 = 宽 * 宽高比
            width = screenWidth;
            height = (int) (width / scaleWH);
        } else if (scaleWH < SCALE_SQUARE && scaleWH >= SCALE_NARROW) {
            //宽高比 < 1 且 宽高比 > 4:5 （0.8）
            height = screenHeight;
            width = (int) (height * scaleWH);
        } else {
            //其他 宽 = 屏幕的高 * 宽高比
            width = (int) (screenHeight * scaleWH);
            height = (int) (width / scaleWH);
        }
        displayWidth = width;
        displayHeight = height;
    }

    public void generateLayoutWidthAndHeight() {
        if (screenHeight == 0 || screenWidth == 0
                || videoHeight == 0 || videoWidth == 0) {
            return;
        }
        int width = 0;
        int height = 0;
        float scaleWH = (float) videoWidth / (float) videoHeight;
        if (scaleWH > SCALE_BROAD) {
            height = (int) (screenWidth / SCALE_BROAD);
            width = (int) (SCALE_BROAD * height);
        } else if (scaleWH <= SCALE_BROAD && scaleWH >= SCALE_SQUARE) {
            width = screenWidth;
            height = (int) (width / scaleWH);
        } else if (scaleWH < SCALE_SQUARE && scaleWH >= SCALE_NARROW) {
            height = screenHeight;
            width = (int) (height * scaleWH);
        } else {
            width = (int) (screenHeight * SCALE_NARROW);
            height = (int) (width / SCALE_NARROW);
        }
        layoutWidth = width;
        layoutHeight = height;
    }

    public void generateLayoutScale(FrameLayout.LayoutParams layoutParams) {
        if (screenHeight == 0 || screenWidth == 0
                || videoHeight == 0 || videoWidth == 0) {
            return;
        }
        float scale = ((float) screenHeight / (float) videoHeight);
        layoutParams.height = (int) (videoHeight * scale);
        layoutParams.width = (int) (videoWidth * scale);
        layoutParams.gravity = Gravity.CENTER;
    }

    public void generateLayoutScaleFixed(FrameLayout.LayoutParams layoutParams) {
        if (screenHeight == 0 || screenWidth == 0
                || videoHeight == 0 || videoWidth == 0) {
            return;
        }

        float scale_x = (float) screenWidth / videoWidth;
        float scale_y = (float) screenHeight / videoHeight;

        if (scale_x == 0 || scale_y == 0) {
            return;
        }

        float scale = Math.max(scale_x, scale_y);

        int width = 0;
        int height =0;

        //其他 宽 = 屏幕的高 * 宽高比
        width = (int) (screenHeight * scale);
        height = (int) (width / scale);

        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = width;
        layoutParams.height = height;
    }

    public void generatePhotoLayoutParams(FrameLayout.LayoutParams layoutParams) {
        generateLayoutWidthAndHeight();
        if (layoutWidth == 0 || layoutHeight == 0) {
            return;
        }
        layoutParams.width = layoutWidth;
        layoutParams.height = layoutHeight;
        layoutParams.gravity = Gravity.CENTER;
    }

    public void generateVideoLayoutParams(FrameLayout.LayoutParams layoutParams) {
        if (screenHeight == 0 || screenWidth == 0
                || videoHeight == 0 || videoWidth == 0) {
            return;
        }
        int width = 0;
        int height = 0;
        float scaleWH = (float) videoWidth / (float) videoHeight;
        if (scaleWH > SCALE_BROAD) {
            height = (int) (screenWidth / SCALE_BROAD);
            width = (int) (scaleWH * height);
        } else if (scaleWH <= SCALE_BROAD && scaleWH >= SCALE_SQUARE) {
            width = screenWidth;
            height = (int) (width / scaleWH);
        } else if (scaleWH < SCALE_SQUARE && scaleWH >= SCALE_NARROW) {
            height = screenHeight;
            width = (int) (height * scaleWH);
        } else {
            width = (int) (screenHeight * SCALE_NARROW);
            height = (int) (width / scaleWH);
        }
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.gravity = Gravity.CENTER;
    }

    public void generateDisplayLayoutParams(FrameLayout.LayoutParams layoutParams) {
        generateDisplayWidthAndHeight();
        if (displayHeight == 0 || displayWidth == 0) {
            return;
        }
        layoutParams.width = displayWidth;
        layoutParams.height = displayHeight;
        layoutParams.gravity = Gravity.CENTER;
    }

    public void generateSquareVideoLayout(FrameLayout.LayoutParams layoutParams) {
        if (screenHeight == 0 || screenWidth == 0
                || videoHeight == 0 || videoWidth == 0) {
            return;
        }

        float scale_x = (float) screenWidth / videoWidth;
        float scale_y = (float) screenHeight / videoHeight;

        if (scale_x == 0 || scale_y == 0) {
            return;
        }

        float scale = Math.max(scale_x, scale_y);

        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = (int) (scale * videoWidth);
        layoutParams.height = (int) (scale * videoHeight);
    }

    public int getLayoutHeight() {
        return layoutHeight;
    }

    public int getLayoutWidth() {
        return layoutWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }
}
