package org.epsda.pets.utils;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/19
 * Time: 20:17
 *
 * @Author: 憨八嘎
 */
public class RecommendUtils {
    /**
     * 余弦相似度计算
     * cos(A, B) = (A · B) / (|A| * |B|)
     */
    public static double cosineSimilarity(Map<Long, Double> vecA, Map<Long, Double> vecB) {
        // 点积：只遍历较小的向量
        double dotProduct = 0.0;
        for (Map.Entry<Long, Double> entry : vecA.entrySet()) {
            Double bVal = vecB.get(entry.getKey());
            if (bVal != null) {
                dotProduct += entry.getValue() * bVal;
            }
        }

        if (dotProduct == 0) return 0.0;

        // 模长
        double normA = Math.sqrt(vecA.values().stream().mapToDouble(v -> v * v).sum());
        double normB = Math.sqrt(vecB.values().stream().mapToDouble(v -> v * v).sum());

        if (normA == 0 || normB == 0) return 0.0;

        return dotProduct / (normA * normB);
    }
}
