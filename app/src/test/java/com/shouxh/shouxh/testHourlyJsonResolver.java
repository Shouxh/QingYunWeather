package com.shouxh.shouxh;

import org.junit.Test;

import java.util.Random;

public class testHourlyJsonResolver {


    @Test
    public void testInitRandomWallpaper(){
        Random random = new Random();
        for(int i=0;i<50;i++) {
            int pic = random.nextInt(4);
            System.out.println(pic);
        }
    }
}
