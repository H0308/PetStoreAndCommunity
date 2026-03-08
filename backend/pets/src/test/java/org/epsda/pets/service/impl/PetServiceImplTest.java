package org.epsda.pets.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 12:16
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class PetServiceImplTest {

    @Autowired
    private PetServiceImpl productDetailService;

    @Test
    void getPetDetail() {
        System.out.println(productDetailService.getPetDetail(1L));
    }
}