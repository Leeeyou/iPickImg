package com.example.leeyou.imgpick.ui.component;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.leeyou.imgpick.PickImageParams;
import com.example.leeyou.imgpick.R;
import com.example.leeyou.imgpick.bean.ImageFloder;
import com.example.leeyou.imgpick.utils.BasePopupWindowForListView;
import com.example.leeyou.imgpick.utils.CommonAdapter;
import com.example.leeyou.imgpick.utils.ViewHolder;

import java.util.List;


public class ListImageDirPopupWindow extends BasePopupWindowForListView<ImageFloder> {
    private ListView mListDir;

    public ListImageDirPopupWindow(int width, int height, List<ImageFloder> datas, View convertView) {
        super(convertView, width, height, true, datas);
    }

    @Override
    public void initViews() {
        mListDir = (ListView) findViewById(com.example.leeyou.imgpick.R.id.id_list_dir);
        mListDir.setAdapter(new CommonAdapter<ImageFloder>(context, mDatas, com.example.leeyou.imgpick.R.layout.list_dir_item) {
            @Override
            public void convert(ViewHolder helper, ImageFloder item) {

                if (PickImageParams.selectedImageFile != null && item.getDir().equals(PickImageParams.selectedImageFile.getAbsolutePath())) {
                    helper.setVisible(R.id.iv_selected, View.VISIBLE);
                } else {
                    helper.setVisible(R.id.iv_selected, View.GONE);
                }

                helper.setText(com.example.leeyou.imgpick.R.id.id_dir_item_name, item.getName());
                helper.setImageByUrl(com.example.leeyou.imgpick.R.id.id_dir_item_image, item.getFirstImagePath());
                helper.setText(com.example.leeyou.imgpick.R.id.id_dir_item_count, item.getCount() + "张");
            }
        });
    }

    public interface OnImageDirSelected {
        void selected(ImageFloder floder);
    }

    private OnImageDirSelected mImageDirSelected;

    public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected) {
        this.mImageDirSelected = mImageDirSelected;
    }

    @Override
    public void initEvents() {
        mListDir.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mImageDirSelected != null) {
                    mImageDirSelected.selected(mDatas.get(position));
                }
            }
        });
    }

    @Override
    public void init() {
    }

    @Override
    protected void beforeInitWeNeedSomeParams(Object... params) {
    }

}
