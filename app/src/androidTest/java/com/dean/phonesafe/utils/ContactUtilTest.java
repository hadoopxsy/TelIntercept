package com.dean.phonesafe.utils;

import android.test.AndroidTestCase;

import java.util.Random;

/**
 * Created by Administrator on 2015/12/5.
 */
public class ContactUtilTest extends AndroidTestCase {
    
    public void testAddContact() throws Exception {
        for (int i=0;i<10000;i++) {
            DebugLog.d("id:" + ContactUtil.addContact(getContext(), getName(), getRandomNumber()));
        }
    }
    private static final String[] second_numbers={"3","5","8"};
    public String getRandomNumber(){
        StringBuilder stringBuilder=new StringBuilder();
        //第1位为1
        stringBuilder.append("1");
        Random random=new Random(System.currentTimeMillis());
        //第2位随机生成，可能值为：【3，5，8】
        stringBuilder.append(second_numbers[random.nextInt(second_numbers.length)]);
        //后面9位数字随机生成
        stringBuilder.append(random.nextInt(1000000000));
        return stringBuilder.toString();
    }

    public String getName(){
        Random random=new Random(System.currentTimeMillis());
        StringBuilder stringBuilder=new StringBuilder();
        for (int i=0;i<8;i++){
            stringBuilder.append((char)(0x61+random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}