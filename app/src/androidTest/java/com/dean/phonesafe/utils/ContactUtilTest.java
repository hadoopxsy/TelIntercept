package com.dean.phonesafe.utils;

import android.test.AndroidTestCase;
import android.util.Log;

import junit.framework.Assert;

import java.util.List;

/**
 * Created by Administrator on 2015/12/5.
 */
public class ContactUtilTest extends AndroidTestCase {
    
    public void testExistContact() throws Exception {
        List<ContactUtil.Contact> allContacts = ContactUtil.getAllContacts(getContext());
        for (ContactUtil.Contact contact:allContacts) {
            Log.d("ContactUtilTest", contact.name+":"+contact.numbers);
        }
        Assert.assertEquals(true, ContactUtil.existsNumber(allContacts, "110"));
    }
}