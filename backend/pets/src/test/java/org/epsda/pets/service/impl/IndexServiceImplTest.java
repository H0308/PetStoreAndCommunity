package org.epsda.pets.service.impl;

import org.epsda.pets.service.IndexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 17:24
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class IndexServiceImplTest {

    @Autowired
    private IndexService indexService;

    @Test
    void getCarousels() {
        System.out.println(indexService.getCarousels());
    }

    @Test
    void getSuperPetCategories() {
        long start = System.currentTimeMillis();
        System.out.println(indexService.getSuperCategories());
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    void getSubPetCategories() {
        long start = System.currentTimeMillis();
        System.out.println(indexService.getSubCategories(1L));
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    void getLatestPets() {
        long start = System.currentTimeMillis();
        System.out.println(indexService.getLatestProducts());
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    void getProductsUnderMainCategory() {
        // System.out.println(indexService.getProductsUnderMainCategory(1L));
    }
}