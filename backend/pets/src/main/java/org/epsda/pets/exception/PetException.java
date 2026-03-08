package org.epsda.pets.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 10:19
 *
 * @Author: 憨八嘎
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetException extends RuntimeException{
    public Integer code;
    public String message;

    public PetException() {
    }

    public PetException(Integer code) {
        this.code = code;
    }

    public PetException(String message) {
        this.message = message;
    }

    public PetException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
