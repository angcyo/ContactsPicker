package com.angcyo.contactspicker;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

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
                                                    Toast.makeText(MainActivity.this, "开始扫描联系人", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onCompleted() {
                                                    Toast.makeText(MainActivity.this, "扫描完成", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                }

                                                @Override
                                                public void onNext(List<ContactsPickerHelper.ContactsInfo> contactsInfos) {
                                                    mModelAdapter.resetData(sort(contactsInfos));
                                                }
                                            });
                                }
                            }
                        });

            }
        });

        initView();
    }

    private void scrollToLetter(String letter) {
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
            protected void onBindCommonView(final RBaseViewHolder holder, final int position, ContactsPickerHelper.ContactsInfo bean) {
                holder.fillView(bean);
                holder.v(R.id.item_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSelectorPosition(position, (CompoundButton) holder.v(R.id.checkbox));
                    }
                });
                Glide.with(MainActivity.this)
                        .load(ContactsPickerHelper.getPhotoByte(getContentResolver(), bean.contactId))
                        .transform(new GlideCircleTransform(MainActivity.this))
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.imgV(R.id.image_view));
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
    }
}
