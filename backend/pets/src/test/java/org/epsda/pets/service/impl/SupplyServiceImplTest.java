package org.epsda.pets.service.impl;

import org.epsda.pets.service.SupplyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 11:58
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class SupplyServiceImplTest {

    @Autowired
    private SupplyService supplyService;

    @Test
    void getSupplyDetail() {
        System.out.println(supplyService.getSupplyDetail(4L));
    }
}