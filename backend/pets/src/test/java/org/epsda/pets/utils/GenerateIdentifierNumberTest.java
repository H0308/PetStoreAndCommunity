package org.epsda.pets.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 18:28
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class GenerateIdentifierNumberTest {

    @Test
    void generateIdentifierNumber() {
        System.out.println(GenerateIdentifierNumber.generateIdentifierNumber());
    }
}