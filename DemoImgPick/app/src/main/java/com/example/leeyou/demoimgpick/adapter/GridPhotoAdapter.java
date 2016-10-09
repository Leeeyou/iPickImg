package com.example.leeyou.demoimgpick.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.leeyou.demoimgpick.R;
import com.example.leeyou.imgpick.PickImageParams;
import com.example.leeyou.imgpick.listener.PreviewImageListener;
import com.example.leeyou.imgpick.listener.SelectImageChangeListener;
import com.example.leeyou.imgpick.utils.CommonAdapter;
import com.example.leeyou.imgpick.utils.ViewHolder;

import java.util.List;

import static com.example.leeyou.imgpick.PickImageParams.MAX_PICK_NUM;


public class GridPhotoAdapter extends CommonAdapter<String> {

    private Context context;

    private String mDirPath;

    private SelectImageChangeListener mSelectImageChangeListener;
    private PreviewImageListener mPreviewImageListener;

    public GridPhotoAdapter(Context context, List<String> mDatas, int itemLayoutId, SelectImageChangeListener mSelectImageChangeListener, PreviewImageListener previewImageListener) {
        super(context, mDatas, itemLayoutId);
        mDirPath = PickImageParams.selectedImageFile.getAbsolutePath();
        this.context = context;
        this.mSelectImageChangeListener = mSelectImageChangeListener;
        this.mPreviewImageListener = previewImageListener;
    }

    @Override
    public void convert(final ViewHolder helper, final String item) {

        helper.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
        helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);

        final ImageView mImageView = helper.getView(R.id.id_item_image);
        final ImageView mSelect = helper.getView(R.id.id_item_select);

        unchoosedImageUIShow(mSelect, mImageView);

        clickSelectImage(item, mImageView, mSelect);

        clickPreviewImage(helper, mImageView);

        //已经选择过的图片，显示出选择过的效果
        if (PickImageParams.selectedImageAbsolutePaths.contains(mDirPath + "/" + item)) {
            choosedImageUIShow(mSelect, mImageView);
        }

    }

    private void clickPreviewImage(final ViewHolder helper, ImageView mImageView) {
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreviewImageListener != null)
                    mPreviewImageListener.previewImage(helper.getPosition(), PickImageParams.FROM_ADAPTER_PREVIEW);
            }
        });
    }

    private void clickSelectImage(final String item, final ImageView mImageView, final ImageView mSelect) {
        mSelect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PickImageParams.selectedImageAbsolutePaths.contains(mDirPath + "/" + item)) {
                    removeImage();
                } else {
                    choosedImage();
                }

                if (mSelectImageChangeListener != null) {
                    mSelectImageChangeListener.selectImageChange();
                }
            }

            private void removeImage() {
                PickImageParams.selectedImageAbsolutePaths.remove(mDirPath + "/" + item);
                PickImageParams.selectedImageCount--;
                if (PickImageParams.selectedImageCount <= 0)
                    PickImageParams.selectedImageCount = 0;

                unchoosedImageUIShow(mSelect, mImageView);
            }

            private void choosedImage() {
                if (PickImageParams.selectedImageCount >= MAX_PICK_NUM) {
                    Toast.makeText(context, context.getResources().getString(R.string.desc_most_photo, MAX_PICK_NUM), Toast.LENGTH_SHORT).show();
                } else {
                    PickImageParams.selectedImageAbsolutePaths.add(mDirPath + "/" + item);
                    PickImageParams.selectedImageCount++;
                    if (PickImageParams.selectedImageCount >= MAX_PICK_NUM)
                        PickImageParams.selectedImageCount = MAX_PICK_NUM;

                    choosedImageUIShow(mSelect, mImageView);
                }
            }
        });
    }

    private void unchoosedImageUIShow(ImageView mSelect, ImageView mImageView) {
        mSelect.setImageResource(R.drawable.picture_unselected);
        mImageView.setColorFilter(null);
    }

    private void choosedImageUIShow(ImageView mSelect, ImageView mImageView) {
        mSelect.setImageResource(R.drawable.pictures_selected);
        mImageView.setColorFilter(Color.parseColor("#77000000"));
    }

    public void setmDirPath(String mDirPath) {
        this.mDirPath = mDirPath;
    }
}
