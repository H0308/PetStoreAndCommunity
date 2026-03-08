package org.epsda.pets.service.impl;

import org.epsda.pets.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/16
 * Time: 13:29
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class CartServiceImplTest {

    @Autowired
    private CartService cartService;

    @Test
    void getCounts() {
        System.out.println(cartService.getCounts(1L));
    }
}