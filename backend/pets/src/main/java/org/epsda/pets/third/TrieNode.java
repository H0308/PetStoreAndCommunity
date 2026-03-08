package org.epsda.pets.third;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:07
 *
 * @Author: 憨八嘎
 */
public class TrieNode {
    public Map<Character, TrieNode> node;
    public boolean isEnd;
    TrieNode() {
        this.node = new HashMap<>();
        this.isEnd = false;
    }
}
