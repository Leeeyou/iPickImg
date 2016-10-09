package com.example.leeyou.demoimgpick;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leeyou.demoimgpick.adapter.GridPhotoAdapter;
import com.example.leeyou.imgpick.PickImageParams;
import com.example.leeyou.imgpick.bean.ImageFloder;
import com.example.leeyou.imgpick.event.NotifyImageSelectedEvent;
import com.example.leeyou.imgpick.listener.PreviewImageListener;
import com.example.leeyou.imgpick.listener.SelectImageChangeListener;
import com.example.leeyou.imgpick.ui.component.ListImageDirPopupWindow;
import com.example.leeyou.imgpick.utils.MyUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.example.leeyou.imgpick.PickImageParams.MAX_PICK_NUM;

/**
 * 图片选择界面
 */
public class ImageGridShowActivity extends AppCompatActivity implements ListImageDirPopupWindow.OnImageDirSelected {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    ProgressDialog mProgressDialog;

    int mPicsSize;//存储文件夹中的图片数量
    int totalCount = 0;

    GridView mGirdView;
    GridPhotoAdapter mAdapter;

    HashSet<String> mDirPaths = new HashSet<>();//临时的辅助类，用于防止同一个文件夹的多次扫描

    List<ImageFloder> mImageFloders = new ArrayList<>();//扫描拿到所有的图片文件夹
    List<String> tempPreviewImageAbsolutePaths = null;//零时保存预览图片集合
    List<String> tempSelectImageAbsolutePaths = null;//零时保存选中图片集合
    int tempSelectImageAbsolutePathsSize;//零时保存选中图片集合

    RelativeLayout mBottomLy;

    TextView mChooseDir;//选择文件夹按钮
    TextView mImagePreview;//预览按钮

    int mScreenHeight;

    ListImageDirPopupWindow mListImageDirPopupWindow;

    MenuItem completeMenuItem;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            data2View();// 为View绑定数据

            initListDirPopupWindw(); // 初始化展示文件夹的popupWindw
        }
    };


    /**
     * 为View绑定数据
     */
    private void data2View() {
        if (PickImageParams.selectedImageFile == null) {
            Toast.makeText(getApplicationContext(), "擦，一张图片都没扫描到", Toast.LENGTH_SHORT).show();
            return;
        }

        getPreviewImagePaths();

        createAdapter();
    }

    //初始化展示文件夹的popupWindw
    private void initListDirPopupWindw() {
        mListImageDirPopupWindow = new ListImageDirPopupWindow(
                LayoutParams.MATCH_PARENT,
                (int) (mScreenHeight * 0.7),
                mImageFloders,
                LayoutInflater.from(getApplicationContext()).inflate(R.layout.list_dir, null));

        mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp); // 设置背景颜色变暗
            }
        });

        mListImageDirPopupWindow.setOnImageDirSelected(this);
    }

    @Override
    public void selected(ImageFloder floder) {
        PickImageParams.selectedImageFile = new File(floder.getDir());

        getPreviewImagePaths();

        createAdapter();

        mChooseDir.setText(floder.getName());
        mListImageDirPopupWindow.dismiss();
    }

    private void getPreviewImagePaths() {
        PickImageParams.previewImageAbsolutePaths.clear();
        Collections.addAll(PickImageParams.previewImageAbsolutePaths, PickImageParams.selectedImageFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".jpg")
                        || filename.toLowerCase().endsWith(".png")
                        || filename.toLowerCase().endsWith(".jpeg");
            }
        }));
        Collections.sort(PickImageParams.previewImageAbsolutePaths, new Comparator<String>() {
            File leftFile, rightFile;

            @Override
            public int compare(String lhs, String rhs) {
                leftFile = new File(PickImageParams.selectedImageFile + "/" + lhs);
                rightFile = new File(PickImageParams.selectedImageFile + "/" + rhs);

                return leftFile.lastModified() > rightFile.lastModified() ? -1 : 1;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        getScreenHeight();
        initActionbar();
        initView();

        int targetSdkVersion = 0;
        try {
            final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            int hasReadExternalStorage;

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                hasReadExternalStorage = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                hasReadExternalStorage = PermissionChecker.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (hasReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showMessageOKCancel("请允许" + getString(R.string.app_name) + "访问您设备上的照片、媒体内容和文件",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                getImages();
            }
        } else {
            getImages();
        }

    }

    private void getScreenHeight() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenHeight = outMetrics.heightPixels;
    }


    private void initActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.desc_photo);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setLogo(R.drawable.line_vertical);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        completeMenuItem = menu.getItem(0);
        imageSelectedChangeUIDesc();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_completion:
                finish();

                for (String imgPath : PickImageParams.selectedImageAbsolutePaths) {
                    Log.e("ImageGridShow", imgPath);
                }

                Intent intent = new Intent();
                intent.setClass(ImageGridShowActivity.this, ShowPictureActivity.class);
                intent.putStringArrayListExtra("imgList", PickImageParams.selectedImageAbsolutePaths);
                startActivity(intent);

                Toast.makeText(ImageGridShowActivity.this, "选择了 " + PickImageParams.selectedImageAbsolutePaths.size() + " 张图片", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                getImageFromMediaStore();
            }
        }).start();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ImageGridShowActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void getImageFromMediaStore() {
        String firstImage = null;

        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = ImageGridShowActivity.this.getContentResolver();

        Cursor mCursor = mContentResolver.query(mImageUri,
                null,
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED + " desc ");

        while (mCursor != null && mCursor.moveToNext()) {

            // 获取图片的路径
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

            // 拿到第一张图片的路径
            if (firstImage == null) {
                firstImage = path;
            }


            // 获取该图片的父路径名
            File parentFile = new File(path).getParentFile();
            if (parentFile == null)
                continue;

            String dirPath = parentFile.getAbsolutePath();
            ImageFloder imageFloder;

            // 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
            if (mDirPaths == null) {
                mDirPaths = new HashSet<>();
            }

            if (!TextUtils.isEmpty(dirPath) && mDirPaths.contains(dirPath)) {
                continue;
            } else {
                mDirPaths.add(dirPath);
                // 初始化imageFloder
                imageFloder = new ImageFloder();
                imageFloder.setDir(dirPath);
                imageFloder.setFirstImagePath(path);
            }

            int picSize = parentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.toLowerCase().endsWith(".jpg")
                            || filename.toLowerCase().endsWith(".png")
                            || filename.toLowerCase().endsWith(".jpeg");
                }
            }).length;
            totalCount += picSize;

            if (PickImageParams.selectedImageFile == null) {
                PickImageParams.selectedImageFile = parentFile;
            }

            imageFloder.setCount(picSize);
            mImageFloders.add(imageFloder);

            if (picSize > mPicsSize) {
                mPicsSize = picSize;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            MyUtils.close(mCursor);
        }

        mDirPaths = null; // 扫描完成，释放辅助的HashSet内存

        mHandler.sendEmptyMessage(0x110);// 通知Handler扫描图片完成
    }

    private void initView() {
        mGirdView = (GridView) findViewById(R.id.id_gridView);
        mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
        mImagePreview = (TextView) findViewById(R.id.id_total_count);

        mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);

        initEvent();
    }

    private void initEvent() {
        mChooseDir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListImageDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
                mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);

                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = .3f;
                getWindow().setAttributes(lp);
            }
        });

        mImagePreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPreviewImage(0, PickImageParams.FROM_PREVIEW_BUTTON);//点击预览按钮，默认从第一张开始看
            }
        });
    }

    private void goToPreviewImage(int position, int fromWhere) {
        if (fromWhere == PickImageParams.FROM_PREVIEW_BUTTON) {
            tempPreviewImageAbsolutePaths = new ArrayList<>();
            Collections.addAll(tempPreviewImageAbsolutePaths, new String[PickImageParams.previewImageAbsolutePaths.size()]);
            Collections.copy(tempPreviewImageAbsolutePaths, PickImageParams.previewImageAbsolutePaths);

            PickImageParams.previewImageAbsolutePaths.clear();
            for (String path : PickImageParams.selectedImageAbsolutePaths) {
                PickImageParams.previewImageAbsolutePaths.add(path);
            }
        }

        tempSelectImageAbsolutePathsSize = PickImageParams.selectedImageAbsolutePaths.size();
        if (tempSelectImageAbsolutePathsSize > 0) {
            tempSelectImageAbsolutePaths = new ArrayList<>();
            Collections.addAll(tempSelectImageAbsolutePaths, new String[PickImageParams.selectedImageAbsolutePaths.size()]);
            Collections.copy(tempSelectImageAbsolutePaths, PickImageParams.selectedImageAbsolutePaths);
        }

        startActivity(new Intent()
                .putExtra("initImagePosition", position)
                .putExtra("isFromPreviewButton", fromWhere == PickImageParams.FROM_PREVIEW_BUTTON)
                .setClass(ImageGridShowActivity.this, ImagePreviewActivity.class));
    }


    public void onEventMainThread(NotifyImageSelectedEvent event) {
        if (mAdapter != null) {
            if (tempPreviewImageAbsolutePaths != null && !tempPreviewImageAbsolutePaths.isEmpty()) {
                PickImageParams.previewImageAbsolutePaths.clear();
                Collections.addAll(PickImageParams.previewImageAbsolutePaths, new String[tempPreviewImageAbsolutePaths.size()]);
                Collections.copy(PickImageParams.previewImageAbsolutePaths, tempPreviewImageAbsolutePaths);
                tempPreviewImageAbsolutePaths = null;
            }

            if (tempSelectImageAbsolutePathsSize != PickImageParams.selectedImageAbsolutePaths.size()) {
                notifyAdapter();
            } else {
                if (!PickImageParams.selectedImageAbsolutePaths.isEmpty()
                        && !PickImageParams.selectedImageAbsolutePaths.containsAll(tempSelectImageAbsolutePaths)) {
                    notifyAdapter();
                }
            }
        }

        imageSelectedChangeUIDesc();
    }

    private void notifyAdapter() {
        mAdapter.notifyDataSetChanged();
        tempSelectImageAbsolutePathsSize = 0;
        if (tempSelectImageAbsolutePaths != null) {
            tempSelectImageAbsolutePaths.clear();
            tempSelectImageAbsolutePaths = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void createAdapter() {
        if (mAdapter == null) {
            mAdapter = new GridPhotoAdapter(this,
                    PickImageParams.previewImageAbsolutePaths,
                    R.layout.grid_item,
                    new SelectImageChangeListener() {
                        @Override
                        public void selectImageChange() {
                            imageSelectedChangeUIDesc();
                        }
                    },
                    new PreviewImageListener() {
                        @Override
                        public void previewImage(int position, int fromWhere) {
                            goToPreviewImage(position, fromWhere);
                        }
                    });

            mGirdView.setAdapter(mAdapter);
        } else {
            mAdapter.setList(PickImageParams.previewImageAbsolutePaths);
            mAdapter.setmDirPath(PickImageParams.selectedImageFile.getAbsolutePath());
            mAdapter.notifyDataSetChanged();
        }
    }

    private void imageSelectedChangeUIDesc() {
        if (PickImageParams.selectedImageCount <= 0) {
            completeMenuItem.setEnabled(false);
            PickImageParams.selectedImageCount = 0;
            completeMenuItem.setTitleCondensed(getResources().getString(R.string.selected_img_desc2));

            mImagePreview.setEnabled(false);
            mImagePreview.setText(getResources().getString(R.string.preview_img_desc2));
            mImagePreview.setTextColor(getResources().getColor(R.color.font_gray));
            return;
        }

        completeMenuItem.setEnabled(true);
        if (PickImageParams.selectedImageCount >= MAX_PICK_NUM) {
            PickImageParams.selectedImageCount = MAX_PICK_NUM;
        }
        completeMenuItem.setTitleCondensed(getResources().getString(R.string.selected_img_desc, PickImageParams.selectedImageCount, MAX_PICK_NUM));

        mImagePreview.setEnabled(true);
        mImagePreview.setTextColor(getResources().getColor(R.color.white));
        mImagePreview.setText(getResources().getString(R.string.preview2_img_desc, PickImageParams.selectedImageCount));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getImageFromMediaStore();
                        }
                    }).start();
                } else {
                    Toast.makeText(ImageGridShowActivity.this, "READ_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}
