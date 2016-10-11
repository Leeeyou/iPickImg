package com.example.leeyou.demoimgpick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dinuscxj.itemdecoration.GridOffsetsItemDecoration;
import com.example.leeyou.imgpick.PickImageParams;
import com.example.leeyou.imgpick.utils.DisplayUtil;

import java.io.File;
import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.zibin.luban.Luban;

/**
 * 图片展示界面
 */
public class ShowPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        ArrayList<String> imgList = getIntent().getStringArrayListExtra("imgList");

        GridOffsetsItemDecoration offsetsItemDecoration = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
        offsetsItemDecoration.setVerticalItemOffsets(DisplayUtil.dip2px(this, 10));
        offsetsItemDecoration.setHorizontalItemOffsets(DisplayUtil.dip2px(this, 10));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(offsetsItemDecoration);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new BaseQuickAdapter<String>(R.layout.item_picture, imgList) {
            @Override
            protected void convert(final BaseViewHolder baseViewHolder, final String imgPath) {
                Luban.get(ShowPictureActivity.this)
                        .load(new File(imgPath))
                        .putGear(Luban.THIRD_GEAR)
                        .asObservable()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        })
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends File>>() {
                            @Override
                            public Observable<? extends File> call(Throwable throwable) {
                                return Observable.empty();
                            }
                        })
                        .subscribe(new Action1<File>() {
                            @Override
                            public void call(File file) {

                                Glide.with(ShowPictureActivity.this)
                                        .load(file)
                                        .into((ImageView) baseViewHolder.getView(R.id.img));

//                                Log.e("ImageGridShow", file.length() / 1024 + " -- " + file.getAbsolutePath());
                            }
                        });

//                Bitmap bitmap = PickImageParams.imageResizer.decodeSampledBitmapFromFilePath(ShowPictureActivity.this, imgPath, 480, 800);
//
//                baseViewHolder.setImageBitmap(R.id.img, bitmap);

            }


        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PickImageParams.selectedImageAbsolutePaths.clear();
        PickImageParams.previewImageAbsolutePaths.clear();
    }
}
