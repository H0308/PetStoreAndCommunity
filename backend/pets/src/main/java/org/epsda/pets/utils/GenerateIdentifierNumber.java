package org.epsda.pets.utils;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 18:24
 *
 * @Author: 憨八嘎
 */
public class GenerateIdentifierNumber {

    // 生成商品编号后缀，前缀为商品主键ID
    public static String generateIdentifierNumber() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            int num = random.nextInt(10);
            stringBuilder.append(num);
        }

        return stringBuilder.toString();
    }
}
