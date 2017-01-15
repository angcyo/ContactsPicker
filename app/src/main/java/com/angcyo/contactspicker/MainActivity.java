package com.angcyo.contactspicker;

import android.Manifest;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import com.angcyo.contactspicker.util.ContactsPickerHelper;
import com.angcyo.contactspicker.util.L;
import com.angcyo.contactspicker.util.T2;
import com.angcyo.contactspicker.widget.GlideCircleTransform;
import com.angcyo.contactspicker.widget.RBaseViewHolder;
import com.angcyo.contactspicker.widget.RGroupItemDecoration;
import com.angcyo.contactspicker.widget.RModelAdapter;
import com.angcyo.contactspicker.widget.RRecyclerView;
import com.angcyo.contactspicker.widget.WaveSideBarView;
import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RRecyclerView mRecyclerView;
    private RModelAdapter<ContactsPickerHelper.ContactsInfo> mModelAdapter;

    private static List<ContactsPickerHelper.ContactsInfo> sort(List<ContactsPickerHelper.ContactsInfo> list) {
        Collections.sort(list, new Comparator<ContactsPickerHelper.ContactsInfo>() {
            @Override
            public int compare(ContactsPickerHelper.ContactsInfo o1, ContactsPickerHelper.ContactsInfo o2) {
                return o1.letter.compareTo(o2.letter);
            }
        });
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK,
//                        ContactsContract.Contacts.CONTENT_URI);
//                MainActivity.this.startActivityForResult(intent, 1);

//                startLoading();

                final List<ContactsPickerHelper.ContactsInfo> selectorData = mModelAdapter.getSelectorData();
                L.w("共选中:" + selectorData.size());
                for (int i = 0; i < selectorData.size(); i++) {
                    L.w(i + "-->" + selectorData.get(i).toString());
                }
            }
        });

        initView();
    }

    private void scrollToLetter(String letter) {
        if (TextUtils.equals(letter, "#")) {
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
            return;
        }
        for (int i = 0; i < mModelAdapter.getAllDatas().size(); i++) {
            if (TextUtils.equals(letter, mModelAdapter.getAllDatas().get(i).letter)) {
                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                break;
            }
        }
    }

    private void initView() {
        mRecyclerView = (RRecyclerView) findViewById(R.id.recycler_view);
        mModelAdapter = new RModelAdapter<ContactsPickerHelper.ContactsInfo>(this) {

            @Override
            protected int getItemLayoutId(int viewType) {
                return R.layout.item_contacts_layout;
            }

            @Override
            protected void onBindCommonView(final RBaseViewHolder holder, final int position, final ContactsPickerHelper.ContactsInfo bean) {
                holder.fillView(bean);
                holder.v(R.id.item_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSelectorPosition(position, (CompoundButton) holder.v(R.id.checkbox));
                    }
                });
                holder.cV(R.id.checkbox).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSelectorPosition(position);
                    }
                });
                Glide.with(MainActivity.this)
                        .load(ContactsPickerHelper.getPhotoByte(MainActivity.this, bean.contactId))
//                        .load(ContactsPickerHelper.getPhoto(getContentResolver(), bean.contactId))
                        .transform(new GlideCircleTransform(MainActivity.this))
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.imgV(R.id.image_view));

//                Observable.just("")
//                        .map(new Func1<Object, Bitmap>() {
//                            @Override
//                            public Bitmap call(Object o) {
//                                return ContactsPickerHelper.getPhoto(getContentResolver(), bean.contactId);
//                            }
//                        })
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Action1<Bitmap>() {
//                            @Override
//                            public void call(Bitmap bitmap) {
//                                holder.imageView(R.id.image_view).setImageBitmap(bitmap);
//                            }
//                        });
            }

            @Override
            protected void onBindModelView(int model, boolean isSelector, RBaseViewHolder holder, int position, ContactsPickerHelper.ContactsInfo bean) {
                ((CompoundButton) holder.v(R.id.checkbox)).setChecked(isSelector);
            }

            @Override
            protected void onBindNormalView(RBaseViewHolder holder, int position, ContactsPickerHelper.ContactsInfo bean) {

            }
        };
        mModelAdapter.setModel(RModelAdapter.MODEL_MULTI);//多选模式
        mRecyclerView.setAdapter(mModelAdapter);

        WaveSideBarView sideBarView = (WaveSideBarView) findViewById(R.id.side_bar_view);
        sideBarView.setOnTouchLetterChangeListener(new WaveSideBarView.OnTouchLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                scrollToLetter(letter);
            }
        });

        final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 20);
        final RectF rectF = new RectF();
        final Rect rect = new Rect();
        mRecyclerView.addItemDecoration(new RGroupItemDecoration(new RGroupItemDecoration.GroupCallBack() {
            @Override
            public int getGroupHeight() {
                return dp2px(20);
            }

            @Override
            public String getGroupText(int position) {
                return mModelAdapter.getAllDatas().get(position).letter;
            }

            @Override
            public void onGroupDraw(Canvas canvas, View view, int position) {
                paint.setColor(Color.parseColor("#969696"));

                if (isHorizontal()) {
                    rectF.set(view.getLeft() - getGroupHeight(), view.getTop(), view.getLeft(), view.getBottom());
                } else {
                    rectF.set(view.getLeft(), view.getTop() - getGroupHeight(), view.getRight(), view.getTop());
                }

                canvas.drawRoundRect(rectF, dp2px(2), dp2px(2), paint);
                paint.setColor(Color.WHITE);

                final String letter = mModelAdapter.getAllDatas().get(position).letter;
                paint.getTextBounds(letter, 0, letter.length(), rect);

                if (isHorizontal()) {
                    canvas.drawText(letter, view.getLeft() - getGroupHeight() / 2 - rect.width() / 2, view.getBottom() - dp2px(10), paint);
                } else {
                    canvas.drawText(letter, view.getLeft() + dp2px(10), view.getTop() - (getGroupHeight() - rect.height()) / 2, paint);
                }
            }

            @Override
            public void onGroupOverDraw(Canvas canvas, View view, int position, int offset) {
                paint.setColor(Color.parseColor("#969696"));

                if (isHorizontal()) {
                    rectF.set(-offset, view.getTop(), getGroupHeight() - offset, view.getBottom());
                } else {
                    rectF.set(view.getLeft(), -offset, view.getRight(), getGroupHeight() - offset);
                }

                canvas.drawRoundRect(rectF, dp2px(2), dp2px(2), paint);
                paint.setColor(Color.WHITE);

                final String letter = mModelAdapter.getAllDatas().get(position).letter;
                paint.getTextBounds(letter, 0, letter.length(), rect);

                if (isHorizontal()) {
                    canvas.drawText(letter, (getGroupHeight() - rect.width()) / 2 - offset, view.getBottom() - dp2px(10), paint);
                } else {
                    canvas.drawText(letter, view.getLeft() + dp2px(10), (getGroupHeight() + rect.height()) / 2 - offset, paint);
                }
            }
        }));
    }

    private boolean isHorizontal() {
        return ((LinearLayoutManager) mRecyclerView.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL;
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density) * dp;
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startLoading();
    }

    private void startLoading() {
        new RxPermissions(MainActivity.this)
                .request(Manifest.permission.WRITE_CONTACTS)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
//                                    ContactsPickerHelper.getContactsList(MainActivity.this);
                            //imageView.setImageBitmap(getPhoto(getContentResolver(), "517"));
//                                    Glide.with(MainActivity.this).load(getPhotoByte(getContentResolver(), "518"));

                            ContactsPickerHelper
                                    .getContactsListObservable(MainActivity.this)
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<List<ContactsPickerHelper.ContactsInfo>>() {

                                        @Override
                                        public void onStart() {
                                            super.onStart();
                                            T2.show(MainActivity.this, "开始扫描联系人");
                                        }

                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onNext(List<ContactsPickerHelper.ContactsInfo> contactsInfos) {
                                            mModelAdapter.resetData(sort(contactsInfos));
                                            T2.show(MainActivity.this, "扫描完成:共" + contactsInfos.size() + "个联系人");
                                        }
                                    });
                        }
                    }
                });
    }
}
