package org.epsda.pets.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.epsda.pets.constants.Constants;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 9:59
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
public class ResultWrapper<T> {
    private Integer code;
    private String message;
    private T data;

    // 正常情况
    public static <T> ResultWrapper<T> normal(T data) {
        return new ResultWrapper<>(Constants.NORMAL, "", data);
    }

    // 错误情况
    public static <T> ResultWrapper<T> fail(T data) {
        return new ResultWrapper<>(Constants.SERVER_ERROR, "", data);
    }

    public static <T> ResultWrapper<T> fail(Integer code, String message) {
        return new ResultWrapper<>(Constants.SERVER_ERROR, message, null);
    }

    public static <T> ResultWrapper<T> fail(String message, T data) {
        return new ResultWrapper<>(Constants.SERVER_ERROR, message, data);
    }
}
