package org.epsda.pets.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/24
 * Time: 20:18
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class PasswordEncoderTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void encode() {
        System.out.println(passwordEncoder.encode("nANJE*Z0s"));
    }
}
