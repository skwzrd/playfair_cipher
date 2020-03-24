package com.cipher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void test_checkInput() {
        assertFalse(Main.checkInput("hello world"));
        assertFalse(Main.checkInput("helloworld\n"));
        assertFalse(Main.checkInput("hello1world"));
        assertFalse(Main.checkInput("hello world!"));

        assertTrue(Main.checkInput("helloworld"));
    }

    @Test
    void test_generateAlphabet() {
        char[] expected = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] result =  Main.generateAlphabet();
        assertArrayEquals(expected, result);
    }

    @Test
    void test_generatePlayfairKey() {
        char[][] playfairKey = Main.generatePlayfairKey();
        ArrayList<Character> foundLetters = new ArrayList<>();
        for (char[] i_chars : playfairKey) {
            Assertions.assertEquals(5, i_chars.length);
            for (char j_chars : i_chars) {
                assertFalse(foundLetters.contains(j_chars));
                foundLetters.add(j_chars);
            }
        }
    }

    @Test
    void test_cryptMessage() throws Exception {
        char[][] playfairKey = Main.generatePlayfairKey();

        String[][] args = {
                {"playfair cipher", "qkeukfhsdhrfct", "playfaircipher"},
                {"playfair ciphe",  "qkeukfhsdhrfab", "playfairciphea"},
                {"a",               "bb",             "aa"},
                {"e",               "ab",             "ea"},
                {"ao",              "ek",             "ao"},
                {"jjjjeers",        "ffffaast",       "jjjjeers"},
        };

        for (String[] arg : args) {
            String encrypted = Main.cryptMessage(playfairKey, arg[0], true);
            String decrypted = Main.cryptMessage(playfairKey, arg[1], false);
            assertEquals(arg[1], encrypted);
            assertEquals(arg[2], decrypted);
        }
    }
}
