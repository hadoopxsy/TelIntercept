package com.dean.phonesafe.db;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015/11/10.
 */
public class DataDaoTest extends AndroidTestCase {
    
    public void testGetCities() throws Exception {

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        copyFile("data.db");
    }

    /**
     * 将资产目录中的fileName文件写到getFileDir()目录
     *
     * @param fileName
     */
    private void copyFile(final String fileName) {
        try {
            InputStream inputStream = getContext().getAssets().open(fileName);
            OutputStream outputStream = new FileOutputStream(new File(getContext().getFilesDir(), fileName));
            int len;
            byte[] buffer = new byte[1024 * 4];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}