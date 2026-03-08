package org.epsda.pets.third;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.mapper.SensitiveWordMapper;
import org.epsda.pets.pojo.SensitiveWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/11
 * Time: 19:55
 *
 * @Author: 憨八嘎
 */
@Component
public class TrieHolder {
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    public Trie trie;

    @PostConstruct
    public void init() {
        // 从数据库加载所有敏感词
        List<SensitiveWord> words = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>()
                        .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );
        // 构建 Trie 树
        if (!words.isEmpty()) {
            trie = Trie.buildTrie(words.stream().map(SensitiveWord::getWord).toList());
        }
    }

    public void rebuild() {
        this.init();
    }
}
