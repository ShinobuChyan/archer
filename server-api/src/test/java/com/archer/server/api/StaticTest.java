package com.archer.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;

/**
 * @author Shinobu
 * @since 2018/3/2
 */
public class StaticTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticTest.class);

    // main here
    public static void main(String[] args) throws InterruptedException, IOException {

        var a = Calendar.getInstance();
        System.out.println(a.get(Calendar.HOUR_OF_DAY));

    }

}
