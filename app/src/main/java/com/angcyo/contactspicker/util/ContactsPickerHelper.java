package com.angcyo.contactspicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by angcyo on 2017-01-08.
 */

public class ContactsPickerHelper {

    /**
     * 联系人头像缓存
     */
    private static Map<String, Bitmap> photoMap = new HashMap<>();
    private static Map<String, byte[]> photoMap2 = new HashMap<>();

    /**
     * 返回一个可以订阅的对象
     */
    public static Observable<List<ContactsInfo>> getContactsListObservable(final Context context) {
        return Observable.create(new Observable.OnSubscribe<List<ContactsInfo>>() {
            @Override
            public void call(Subscriber<? super List<ContactsInfo>> subscriber) {
                subscriber.onStart();
                final List<ContactsInfo> contactsList = getContactsList(context);
                if (contactsList == null) {
                    subscriber.onNext(new ArrayList<ContactsInfo>());
                } else {
                    subscriber.onNext(contactsList);
                }
                subscriber.onCompleted();
            }
        });
    }

    /**
     * 同步返回联系人列表
     */
    public static List<ContactsInfo> getContactsList(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"_id"}, null, null, null);

        List<ContactsInfo> contactsInfos = new ArrayList<>();

        if (cursor != null) {
            //枚举所有联系人的id
            if (cursor.getCount() > 0) {
                L.w("联系人总数量:" + cursor.getCount()); //就是联系人的总数
                int count = 0;
                if (cursor.moveToFirst()) {
                    do {
                        int contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);//获取 id 所在列的索引
                        String contactId = cursor.getString(contactIdIndex);//联系人id

                        final List<String> phones = getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                        if (phones.isEmpty()) {
                            continue;
                        } else {
                            String name;
                            final List<String> names = getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                            if (names.isEmpty()) {
                                name = phones.get(0);
                            } else {
                                name = names.get(0);
                            }

                            //相同联系人的不同手机号码视为不同的联系人
                            for (String phone : phones) {
                                ContactsInfo io = new ContactsInfo();
                                io.contactId = contactId;
                                io.name = name;
                                io.phone = phone;
                                io.letter = String.valueOf(Pinyin.toPinyin(name.charAt(0)).toUpperCase().charAt(0));
                                contactsInfos.add(io);
                            }
                        }

//                    L.e("-------------------------" + count + "----------------------");
//                    L.w("联系人ID:" + contactId);
//                    final String name = getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
//                    L.w("联系人名称:" + Pinyin.toPinyin(name.charAt(0)).toUpperCase().charAt(0) + " " + name);
//                    L.w("联系人电话:" + getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
//                    logData(contentResolver, contactId);
//                    count++;
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        }
        return contactsInfos;
    }

    /**
     * 获取联系人的图片
     */
    public static Bitmap getPhoto(final Context context, String contactId) {
        Bitmap bitmap = photoMap.get(contactId);
        if (bitmap == null) {
            byte[] bytes = getPhotoByte(context, contactId);
            if (bytes != null) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                photoMap.put(contactId, bitmap);
            }
        }
        return bitmap;
    }

    public static byte[] getPhotoByte(final Context context, String contactId) {
        byte[] bytes = photoMap2.get(contactId);
        if (bytes == null || bytes.length <= 0) {
            Cursor dataCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{"data15"},
                    ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                            + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                    new String[]{String.valueOf(contactId)}, null);
            if (dataCursor != null) {
                if (dataCursor.getCount() > 0) {
                    dataCursor.moveToFirst();
                    bytes = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));
                    photoMap2.put(contactId, bytes);
                    L.w("数据大小:" + Formatter.formatFileSize(context, bytes.length));
                }
                dataCursor.close();
            }
        } else {
            L.i("缓存数据大小:" + Formatter.formatFileSize(context, bytes.length));
        }
        return bytes;
    }

    /**
     * 根据MIMETYPE类型, 返回对应联系人的data1字段的数据
     */
    private static List<String> getData1(final ContentResolver contentResolver, String contactId, final String mimeType) {
        List<String> dataList = new ArrayList<>();

        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "='" + mimeType + "'",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null) {
            if (dataCursor.getCount() > 0) {
                if (dataCursor.moveToFirst()) {
                    do {
                        final int columnIndex = dataCursor.getColumnIndex(ContactsContract.Data.DATA1);
                        final int type = dataCursor.getType(columnIndex);
                        if (type == FIELD_TYPE_STRING) {
                            final String data = dataCursor.getString(columnIndex);
                            if (!TextUtils.isEmpty(data)) {
                                dataList.add(data);
                            }
                        }
                    } while (dataCursor.moveToNext());
                }
            }
            dataCursor.close();
        }

        return dataList;
    }

    /**
     * 打印联系人的所有字段信息
     */
    private static void logData(final ContentResolver contentResolver, String contactId) {
        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)}, null);
        log(dataCursor);
    }

    private static void log(final Cursor cursor) {
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                L.e("----------------------start--------------------");
                L.i("数量:" + cursor.getCount() + " 列数:" + cursor.getColumnCount());
                if (cursor.moveToFirst()) {
                    do {
                        for (int i = 0; i < cursor.getColumnCount(); i++) {
                            final String columnName = cursor.getColumnName(i);
                            final int columnIndex = cursor.getColumnIndex(columnName);
                            final int type = cursor.getType(columnIndex);
                            String data = "", ty = "";
                            if (type == Cursor.FIELD_TYPE_NULL) {
                                ty = "NULL";
                                data = "空值";
                            } else if (type == Cursor.FIELD_TYPE_BLOB) {
                                ty = "BLOB";
                                final byte[] bytes = cursor.getBlob(columnIndex);
                                data = String.valueOf(bytes) + " " + bytes.length * 1f / 1024f + "KB";
                            } else if (type == Cursor.FIELD_TYPE_FLOAT) {
                                ty = "FLOAT";
                                data = String.valueOf(cursor.getFloat(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_INTEGER) {
                                ty = "INTEGER";
                                data = String.valueOf(cursor.getInt(columnIndex));
                            } else if (type == FIELD_TYPE_STRING) {
                                ty = "STRING";
                                data = cursor.getString(columnIndex);
                            }

                            L.i("第" + i + "列->名称:" + columnName + " 索引:" + columnIndex + " 类型:" + ty + " 值:" + data);
                        }
                    } while (cursor.moveToNext());
                }
                L.e("------------------------end---------------------");
            }
            cursor.close();
        }
    }

    public static class ContactsInfo {
        /**
         * 联系人的ID
         */
        public String contactId;

        /**
         * 联系人名称的首字母
         */
        public String letter;

        /**
         * 联系人显示的名称
         */
        public String name;
        /**
         * 联系人的手机号码, 有可能是多个. 同一个联系人的不同手机号码,视为多个联系人
         */
        public String phone;

        @Override
        public String toString() {
            return " [" + contactId + "]" + " [" + letter + "]" + " [" + name + "]" + " [" + phone + "]";
        }
    }
}
