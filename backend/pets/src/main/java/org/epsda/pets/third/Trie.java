package org.epsda.pets.third;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:07
 *
 * @Author: 憨八嘎
 */
public class Trie {
    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    public static Trie buildTrie(List<String> words) {
        Trie trie = new Trie();
        words.forEach(trie::insert);

        return trie;
    }

    public void insert(String word) {
        TrieNode cur = this.root;
        for (int i = 0; i < word.length(); i++) {
            // 如果不存在则插入
            if (!cur.node.containsKey(word.charAt(i))) {
                cur.node.put(word.charAt(i), new TrieNode());
            }
            // 否则继续向下遍历
            cur = cur.node.get(word.charAt(i));
        }

        cur.isEnd = true;
    }

    public boolean search(String word) {
        TrieNode cur = this.root;
        for (int i = 0; i < word.length(); i++) {
            // 如果不存在则直接返回false
            if (!cur.node.containsKey(word.charAt(i))) {
                return false;
            }
            // 否则继续向下遍历
            cur = cur.node.get(word.charAt(i));
        }

        return cur.isEnd;
    }

    public boolean startsWith(String prefix) {
        TrieNode cur = this.root;
        for (int i = 0; i < prefix.length(); i++) {
            // 如果不存在则直接返回false
            if (!cur.node.containsKey(prefix.charAt(i))) {
                return false;
            }
            // 否则继续向下遍历
            cur = cur.node.get(prefix.charAt(i));
        }

        return true;
    }
}
