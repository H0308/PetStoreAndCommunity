package org.epsda.pets.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 17:37
 *
 * @Author: 憨八嘎
 */
public class GenerateRandomIndices {
    // 根据列表生成随机下标
    public static List<Integer> generateRandomIndices(Integer length, Integer count) {
        Random random = new Random();
        Set<Integer> selectedIndices = new HashSet<>();
        int bound = length - 1;
        // 先插入到第一个数据
        int index_1 = random.nextInt(bound);
        selectedIndices.add(index_1);

        // 生成其余数据
        do {
            int index = 0;
            do {
                index = random.nextInt(bound);
            } while (selectedIndices.contains(index) &&
                    selectedIndices.size() != count);
            selectedIndices.add(index);
        } while (selectedIndices.size() != count);

        return selectedIndices.stream().toList();
    }
}
