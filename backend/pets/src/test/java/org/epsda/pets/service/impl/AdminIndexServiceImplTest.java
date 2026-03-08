package org.epsda.pets.service.impl;

import org.epsda.pets.service.AdminIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 21:23
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class AdminIndexServiceImplTest {

    @Autowired
    private AdminIndexService adminIndexService;

    @Test
    void newUserCount() {
        System.out.println(adminIndexService.newUserCount(4L));
    }
}