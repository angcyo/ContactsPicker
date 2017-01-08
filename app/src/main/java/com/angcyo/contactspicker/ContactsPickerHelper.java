package com.angcyo.contactspicker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * Created by angcyo on 2017-01-08.
 */

public class ContactsPickerHelper {
    public static void getContactsList(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"_id"}, null, null, null);
        L.w("联系人总数量:" + cursor.getCount()); //就是联系人的总数

        //枚举所有联系人的id
        if (cursor.getCount() > 0) {
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    int contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);//获取 id 所在列的索引
                    String contactId = cursor.getString(contactIdIndex);//联系人id

                    L.e("-------------------------" + count + "----------------------");
                    L.w("联系人ID:" + contactId);
                    final String name = getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                    L.w("联系人名称:" + Pinyin.toPinyin(name.charAt(0)).toUpperCase().charAt(0) + " " + name);
                    L.w("联系人电话:" + getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
//                    logData(contentResolver, contactId);
                    count++;
                } while (cursor.moveToNext());
            }
        }

        cursor.close();
    }

    /**
     * 获取联系人的图片
     */
    public static Bitmap getPhoto(final ContentResolver contentResolver, String contactId) {
        Bitmap photo = null;
        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                new String[]{"data15"},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null) {
            if (dataCursor.getCount() > 0) {
                dataCursor.moveToFirst();
                byte[] bytes = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));
                if (bytes != null) {
                    photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
            }
            dataCursor.close();
        }
        return photo;
    }

    /**
     * 根据MIMETYPE类型, 返回对应联系人的data1字段的数据
     */
    private static String getData1(final ContentResolver contentResolver, String contactId, final String mimeType) {
        StringBuilder stringBuilder = new StringBuilder();

        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "='" + mimeType + "'",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null) {
            if (dataCursor.getCount() > 0) {
                if (dataCursor.moveToFirst()) {
                    do {
                        final String data = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DATA1));
                        if (TextUtils.isEmpty(data)) {
                            continue;
                            //stringBuilder.append("空");
                        } else {
                            stringBuilder.append(data);
                        }
                        stringBuilder.append("_");//多个值,之间的分隔符.可以自定义;
                    } while (dataCursor.moveToNext());
                }
            }
            dataCursor.close();
        }

        return stringBuilder.subSequence(0, Math.max(0, stringBuilder.length() - 1)).toString();
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
                            } else if (type == Cursor.FIELD_TYPE_STRING) {
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
}
