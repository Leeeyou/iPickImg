package com.example.leeyou.imgpick;

import com.example.leeyou.imgpick.utils.ImageResizer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PickImageParams {

    public static ArrayList<String> selectedImageAbsolutePaths = new ArrayList<>();//选中图片的绝对路径集合，一定是图片的绝对路径
    public static List<String> previewImageAbsolutePaths = new LinkedList<>();//预览图片的绝对路径集合,可能存储的是图片名称，也可能存储图片的绝对路径
    public static int selectedImageCount = 0;
    public static File selectedImageFile = null;

    public static ImageResizer imageResizer = new ImageResizer();

    public static final int FROM_PREVIEW_BUTTON = 10;
    public static final int FROM_ADAPTER_PREVIEW = 11;


}
