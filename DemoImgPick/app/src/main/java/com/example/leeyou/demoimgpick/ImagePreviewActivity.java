package com.example.leeyou.demoimgpick;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.leeyou.imgpick.PickImageParams;
import com.example.leeyou.imgpick.event.NotifyImageSelectedEvent;
import com.example.leeyou.imgpick.ui.component.HackyViewPager;

import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoView;

import static com.example.leeyou.imgpick.PickImageParams.MAX_PICK_NUM;

/**
 * 图片预览界面,有多个入口进入这个界面
 * 1、图片选择界面,点击图片进入 isFromPreviewButton ==  false
 * 2、图片选择界面,点击预览按钮 isFromPreviewButton == true
 * 3、图片展示界面,点击图片进入 isFromShowActivity == true
 */
public class ImagePreviewActivity extends AppCompatActivity {

    private static final String ISLOCKED_ARG = "isLocked";

    private ViewPager mViewPager;
    private CheckBox cb_select;
    private RelativeLayout bottom_bar;

    private ActionBar actionBar;
    private MenuItem completeMenuItem;

    private int initImagePosition;

    private static StringBuilder sbImagePath;

    private static boolean isFromPreviewButton = false;//是否预览按钮跳转过来

    private static Context context;
    private boolean isFromShowActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        cb_select = (CheckBox) findViewById(R.id.cb_select);
        bottom_bar = (RelativeLayout) findViewById(R.id.bottom_bar);

        initData();

        initActionBar();
        initViewpager();
        initSelectCheckBox();

        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }
    }

    private void initSelectCheckBox() {
        if (isFromShowActivity) {
            bottom_bar.setVisibility(View.GONE);
            return;
        }

        bottom_bar.setVisibility(View.VISIBLE);

        if (!PickImageParams.selectedImageAbsolutePaths.isEmpty()) {
            cb_select.setChecked(PickImageParams.selectedImageAbsolutePaths.contains(getImagePath(initImagePosition)));
        }

        cb_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_select.isChecked()) {
                    if (PickImageParams.selectedImageCount >= MAX_PICK_NUM) {
                        Toast.makeText(ImagePreviewActivity.this, getResources().getString(R.string.desc_most_photo, MAX_PICK_NUM), Toast.LENGTH_SHORT).show();
                        cb_select.setChecked(false);

                        return;
                    }

                    PickImageParams.selectedImageCount++;
                    if (PickImageParams.selectedImageCount >= MAX_PICK_NUM)
                        PickImageParams.selectedImageCount = MAX_PICK_NUM;

                    PickImageParams.selectedImageAbsolutePaths.add(getImagePath(mViewPager.getCurrentItem()));
                } else {
                    PickImageParams.selectedImageCount--;
                    if (PickImageParams.selectedImageCount <= 0)
                        PickImageParams.selectedImageCount = 0;

                    PickImageParams.selectedImageAbsolutePaths.remove(getImagePath(mViewPager.getCurrentItem()));
                }

                initMenuDesc();
            }
        });
    }

    private void initMenuDesc() {
        if (isFromShowActivity) {
            completeMenuItem.setEnabled(true);
            completeMenuItem.setTitleCondensed(getResources().getString(R.string.selected_img_desc2));
        } else {
            if (PickImageParams.selectedImageCount <= 0) {
                completeMenuItem.setEnabled(false);
                completeMenuItem.setTitleCondensed(getResources().getString(R.string.selected_img_desc2));
            } else {
                completeMenuItem.setEnabled(true);
                completeMenuItem.setTitleCondensed(getResources().getString(R.string.selected_img_desc, PickImageParams.selectedImageCount, MAX_PICK_NUM));
            }
        }
    }

    private void initViewpager() {
        mViewPager.setAdapter(new SamplePagerAdapter());
        mViewPager.setCurrentItem(initImagePosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (actionBar != null) {
                    actionBar.setTitle(getString(R.string.preview_img_desc, position + 1, PickImageParams.previewImageAbsolutePaths.size()));
                }

                if (!PickImageParams.selectedImageAbsolutePaths.isEmpty()) {
                    cb_select.setChecked(PickImageParams.selectedImageAbsolutePaths.contains(getImagePath(position)));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private static String getImagePath(int position) {
        if (isFromPreviewButton) {
            return PickImageParams.previewImageAbsolutePaths.get(position);
        } else {
            if (sbImagePath == null) {
                sbImagePath = new StringBuilder();
            }
            sbImagePath.setLength(0);
            sbImagePath.append(PickImageParams.selectedImageFile.getAbsolutePath())
                    .append("/")
                    .append(PickImageParams.previewImageAbsolutePaths.get(position));
            return sbImagePath.toString();
        }
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.preview_img_desc, initImagePosition + 1, PickImageParams.previewImageAbsolutePaths.size()));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setLogo(R.drawable.line_vertical);
        }
    }

    private void initData() {
        initImagePosition = getIntent().getIntExtra("initImagePosition", 0);
        isFromPreviewButton = getIntent().getBooleanExtra("isFromPreviewButton", false);

        String fromWhere = getIntent().getStringExtra("fromWhere");
        isFromShowActivity = !TextUtils.isEmpty(fromWhere) && fromWhere.equals(ShowPictureActivity.class.getSimpleName());

        context = this;
    }

    static class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return PickImageParams.previewImageAbsolutePaths == null ? 0 : PickImageParams.previewImageAbsolutePaths.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            photoView.setImageBitmap(PickImageParams.imageResizer.decodeSampledBitmapFromFilePath(context, getImagePath(position), 480, 800));

            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        completeMenuItem = menu.getItem(0);
        initMenuDesc();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                break;
            case R.id.action_select_completion:
                if (isFromShowActivity) {
                    finish();
                } else {
                    Toast.makeText(ImagePreviewActivity.this, "选择了 " + PickImageParams.selectedImageAbsolutePaths.size() + " 张图片", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack() {
        finish();
        EventBus.getDefault().post(new NotifyImageSelectedEvent());
    }

    private boolean isViewPagerActive() {
        return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }
}
