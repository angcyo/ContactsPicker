package com.angcyo.contactspicker;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;

import static com.angcyo.contactspicker.ContactsPickerHelper.getPhoto;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ImageView imageView = (ImageView) findViewById(R.id.image_view);

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
                                    ContactsPickerHelper.getContactsList(MainActivity.this);
                                    imageView.setImageBitmap(getPhoto(getContentResolver(), "517"));
                                }
                            }
                        });

            }
        });
    }
}
