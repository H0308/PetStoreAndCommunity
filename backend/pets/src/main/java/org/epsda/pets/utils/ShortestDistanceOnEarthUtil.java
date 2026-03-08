package org.epsda.pets.utils;

import org.epsda.pets.constants.Constants;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/05
 * Time: 19:18
 *
 * @Author: 憨八嘎
 */
public class ShortestDistanceOnEarthUtil {

    // 判断经纬度是否在指定阈值内
    public static boolean isWithinThreshold(String lat1, String lng1, String lat2, String lng2, double threshold) {
        try {
            double dLat1 = Double.parseDouble(lat1);
            double dLng1 = Double.parseDouble(lng1);
            double dLat2 = Double.parseDouble(lat2);
            double dLng2 = Double.parseDouble(lng2);

            // 计算两点之间的距离（米）
            double distance = calculateDistance(dLat1, dLng1, dLat2, dLng2);

            // 判断距离是否在阈值范围内
            return distance <= threshold;
        } catch (NumberFormatException e) {
            // 如果经纬度格式不正确，返回false
            return false;
        }
    }

    // 根据经纬度计算地球两点距离
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 将角度转换为弧度
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);

        // Haversine公式，当对映两点连线过球心时，该公式存在问题
        // 但是由于中国版图横向距离最长两端连线并不会经过地球球心
        // 此处可以忽略上述问题
        // 两点距离d = R*Θ
        // haversine(Θ) = sin(lat2 - lat1)^2 + cos(lat1)cos(lat2)*(sin(lng2 - ln1))^2
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(latRad1) * Math.cos(latRad2) *
                        Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        // Θ = 2 * R * arcsin(sqrt(haversine))
        double c = 2 * Math.asin(Math.sqrt(a));

        return Constants.EARTH_RADIUS * c;
    }

}
