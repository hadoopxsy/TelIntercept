package com.dean.phonesafe.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/13.
 */
public class ContactUtil {
    /**
     * 获取通讯录中所有未被删除的联系人的姓名和电话
     *
     * @return
     */
    public static List<Contact> getAllContacts(Context context) {
        List<Contact> list = new ArrayList<>();
        //1.首先查询有几个联系人，每个联系人在Raw_contacts表中只有一条记录
        Cursor contactCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._ID}, null, null, null);
        while (contactCursor.moveToNext()) {
            try {
                int id = contactCursor.getInt(0);
                Contact contact = new Contact();
                //查出联系人ID的数据
                Cursor dataCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data.DATA1, ContactsContract.Data.MIMETYPE}, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(id)}, null);
                while (dataCursor.moveToNext()) {
                    //分析MIMETYPE类型
                    switch (dataCursor.getString(1)) {
                        //数据为姓名
                        case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                            contact.name = dataCursor.getString(0);//读出姓名
                            break;
                        //数据为号码
                        case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                            contact.numbers.add(dataCursor.getString(0).replace(" ", "").replace("-", ""));//读出号码
                            //DebugLog.d("contact.number:"+contact.number+",contact.name:"+contact.name);
                            break;
                    }
                }
                dataCursor.close();
                list.add(contact);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        contactCursor.close();
        return list;
    }

    /**
     * 判断某个号码是否存在于联系人中
     *
     * @param contactList
     * @param number
     * @return
     */
    public static boolean existsNumber(List<Contact> contactList, String number) {
        if (contactList == null)
            return false;
        for (Contact contact : contactList) {
            for (String num : contact.numbers) {
                if (number.equals(num))
                    return true;
            }
        }

        return false;
    }

    /**
     * 新增一个联系人
     *
     * @param name   姓名
     * @param number 电话号码
     * @return
     */
    public static int addContact(Context context, String name, String number) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("display_name", name);
        //在raw_contact表中添加一条记录
        Uri uri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        DebugLog.d("lastPathSegment:"+uri.getLastPathSegment());
        if(!uri.getLastPathSegment().matches("^\\d+$"))
            return 0;
        int id = Integer.parseInt(uri.getLastPathSegment());
        if (id == 0)
            return 0;
        //在view_data表添加一条记录
        DebugLog.d("添加电话");
        contentValues.clear();
        //添加电话号码记录
        contentValues.put("raw_contact_id",id);
        contentValues.put("mimetype","vnd.android.cursor.item/phone_v2");
        contentValues.put("data1", number);
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
        //添加姓名记录
        DebugLog.d("添加号码");
        contentValues.clear();
        contentValues.put("raw_contact_id", id);
        contentValues.put("mimetype", "vnd.android.cursor.item/name");
        contentValues.put("data1",name);
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
        return id;
    }

    public static class Contact {
        public String name;
        public List<String> numbers = new ArrayList<>();
    }
}
