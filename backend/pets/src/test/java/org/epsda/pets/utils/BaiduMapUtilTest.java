package org.epsda.pets.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 16:52
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class BaiduMapUtilTest {

    @Autowired
    private BaiduMapUtil baiduMapUtil;

    @Test
    void reverseAddressEncoding() {
        String ret = baiduMapUtil.ReverseGeographyEncoding("30.485183656608754", "114.31630816883417");
        System.out.println(baiduMapUtil.getFormattedAddressPOI(ret));
    }

    @Test
    void geographyEncoding() {
        String ret = baiduMapUtil.geographyEncoding("湖北省武汉市湖北工业大学");
        System.out.println(baiduMapUtil.getLatitudeAndLongitude(ret));
    }
}