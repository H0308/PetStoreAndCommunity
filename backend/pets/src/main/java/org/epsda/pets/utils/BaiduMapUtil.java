package org.epsda.pets.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 16:32
 *
 * @Author: 憨八嘎
 */
@Component
public class BaiduMapUtil {
    @Value("${baidu.map.base-url}")
    private String baseUrl;
    @Value("${baidu.map.ak}")
    private String ak;

    // 逆地址编码
    public String ReverseGeographyEncoding(String latitude, String longitude){
        String reverseAddrUrl = baseUrl.concat("reverse_geocoding/v3/?");
        WebClient client = WebClient.builder().baseUrl(reverseAddrUrl).build();
        return client.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("ak", ak)
                    .queryParam("output", "json")
                    .queryParam("coordtype", "bd09ll")
                    .queryParam("extensions_poi", "1")
                    .queryParam("sort_strategy", "distance")
                    .queryParam("entire_poi", "1")
                    .queryParam("location", latitude + "," + longitude)
                .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 地理编码
    public String geographyEncoding(String address) {
        String geographyUrl = baseUrl.concat("geocoding/v3/?");
        WebClient client = WebClient.builder().baseUrl(geographyUrl).build();
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("address", address)
                        .queryParam("output", "json")
                        .queryParam("ak", ak)
                        .queryParam("callback", "showLocation")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 获取精确地址
    public String getFormattedAddressPOI(String mapJson) {
        JSONObject jsonObject = JSON.parseObject(mapJson);
        JSONObject result = jsonObject.getJSONObject("result");
        return result.getString("formatted_address_poi");
    }

    // 获取经纬度
    public List<String> getLatitudeAndLongitude(String mapJson) {
        // 先从对象中取出JSON
        int start = mapJson.indexOf("(") + 1;
        int end = mapJson.lastIndexOf(")");
        String json = mapJson.substring(start, end);

        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject result = jsonObject.getJSONObject("result");
        JSONObject location = result.getJSONObject("location");
        // 获取经度
        String lng = location.getString("lng");
        String lat = location.getString("lat");

        return List.of(lat, lng);
    }
}
