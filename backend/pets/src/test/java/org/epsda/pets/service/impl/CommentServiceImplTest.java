package org.epsda.pets.service.impl;

import org.epsda.pets.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 21:05
 *
 * @Author: 憨八嘎
 */
@SpringBootTest
public class CommentServiceImplTest {
    @Autowired
    private CommentService commentService;

    @Test
    void getProductComment() {
        System.out.println(commentService.getProductComment(1L));
    }
}