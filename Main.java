package com.cipher;

import java.awt.*;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    final private static int PLAYFAIR_SIZE = 5;
    final private static int SIZE = 4; // max index size (PLAYFAIR_SIZE -1)
    final private static char LETTER_TO_OMIT = 'z'; // gets replaces with letter at [0, 0] which is 'a'
    final private static char EVENIZE_LETTER = 'a';

    static boolean checkInput(String input) {
        return Pattern.matches("^[a-zA-Z]+$", input);
    }

    static String getMessageFromCmdLine() {
        String message = "";
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your message:");
            if (!scanner.hasNext()){
                break;
            }
            message = scanner.nextLine();
            message = message.replaceAll("\\s+","").toLowerCase();
            if (checkInput(message)) {
                break;
            } else {
                System.out.println("Invalid message. Letters and spaces only.");
            }
        }
        scanner.close();
        return message;
    }

    /**
     * Let's create the letters of the alphabet in a more interesting way.
     */
    static char[] generateAlphabet() {
        int asciiLowerCaseA = 97;
        int alphabetLength = 26;
        char[] alphabet = new char[alphabetLength];
        for(int i=asciiLowerCaseA; i < asciiLowerCaseA + alphabetLength; i++) {
            int index = i - asciiLowerCaseA;
            alphabet[index] = (char) i;
        }
        return alphabet;
    }

    /**
     * Returns
     * [
     * [a, b, c, d, e],
     * [f, g, h, i, j],
     * [k, l, m, n, o],
     * [p, q, r, s, t],
     * [u, v, w, x, y]
     * ]
     */
    static char[][] generatePlayfairKey() {
        char[][] playfairKey = new char[PLAYFAIR_SIZE][PLAYFAIR_SIZE];
        char[] alphabet = generateAlphabet();

        for(int i=0; i<PLAYFAIR_SIZE; i++) {
            for(int j=0; j<PLAYFAIR_SIZE; j++) {
                char letter = alphabet[i*PLAYFAIR_SIZE + j];
                if (letter != LETTER_TO_OMIT) {
                    playfairKey[i][j] = letter;
                }
            }
        }
        return playfairKey;
    }

    static int[] encryptShift(int cord1, int cord2) {
        // shift down 1 row, return to top row if at bottom row.
        // likewise, shift left 1 col, return to the right col if at leftmost col.
        if (cord1 == SIZE) {
            cord1 = 0;
        } else {
            cord1++;
        }
        if (cord2 == SIZE) {
            cord2 = 0;
        } else {
            cord2++;
        }
        return new int[] {cord1, cord2};
    }

    static int[] decryptShift(int cord1, int cord2) {
        // shift up 1 row, return to bottom row if at top row.
        // likewise, shift right 1 col, return to the left col if at rightmost col.
        if (cord1 == 0) {
            cord1 = SIZE;
        } else {
            cord1--;
        }
        if (cord2 == 0) {
            cord2 = SIZE;
        } else {
            cord2--;
        }
        return new int[] {cord1, cord2};
    }

    static class LetterPoint extends Point {
        private char letter;
        private boolean found = false;

        public LetterPoint(char letter) {
            this.letter = letter;
            if (this.letter == LETTER_TO_OMIT) {
                this.x = 0;
                this.y = 0;
                this.found = true;
            }
        }
    }

    /**
     * col => x, row => y
     *   x0 x1 ... xN
     * y0
     * y1
     * ...
     * yN
     *
     */
    static LetterPoint[] getLetterPoints(char let1, char let2, final char[][] playfairKey) throws Exception {
        LetterPoint letter1 = new LetterPoint(let1);
        LetterPoint letter2 = new LetterPoint(let2);
        // get character positions relative to one another
        for (int row=0; row<PLAYFAIR_SIZE; row++) {
            for (int col=0; col<PLAYFAIR_SIZE; col++) {
                if (playfairKey[row][col] == letter1.letter) {
                    letter1.y = row;
                    letter1.x = col;
                    letter1.found = true;
                }
                if (playfairKey[row][col] == letter2.letter) {
                    letter2.y = row;
                    letter2.x = col;
                    letter2.found = true;
                }
                if (letter1.found && letter2.found) {
                    return new LetterPoint[] {letter1, letter2};
                }
            }
        }
        throw new Exception("Didn't find both letters.");
    }

    /**
     * Used for both encrypting and decrypting ("crypting") messages, given a key.
     */
    static String cryptMessage(final char[][] playfairKey, String crypted_message, boolean encrypt) throws Exception {
        // Learned that there are no keyword arguments in Java... I would have liked to use encrypt=true by default.
        // Message needs to have an even number of letters.
        crypted_message = crypted_message.replaceAll("\\s+","").toLowerCase();
        boolean isEven = crypted_message.length() % 2 != 0;
        if (isEven && encrypt) {
            crypted_message+=EVENIZE_LETTER;
        } else if (isEven && !encrypt) {
            throw new Exception("Encrypted message is not even.");
        }
        char[] message = crypted_message.toCharArray();

        String output_message = "";
        // apply key to message
        for (int i=0; i<message.length; i+=2) {
            LetterPoint[] letterPoints = null;
            try {
                letterPoints = getLetterPoints(message[i], message[i + 1], playfairKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LetterPoint letter1 = letterPoints[0];
            LetterPoint letter2 = letterPoints[1];

            // overlapping || same column - shift rows down, columns remain the same
            if (((letter1.y == letter2.y) && (letter1.x == letter2.x))
                    || ((letter1.y == letter2.y) && (letter1.x != letter2.x))) {
                int[] new_rows;
                if (encrypt) {
                    new_rows = encryptShift(letter1.x, letter2.x);
                } else {
                    new_rows = decryptShift(letter1.x, letter2.x);
                }
                letter1.x = new_rows[0];
                letter2.x = new_rows[1];
            }
            // same row - shift columns right, rows remain the same
            else if ((letter1.x == letter2.x) && (letter1.y != letter2.y)) {
                int[] new_cols;
                if (encrypt) {
                    new_cols = encryptShift(letter1.y, letter2.y);
                } else{
                    new_cols = decryptShift(letter1.y, letter2.y);
                }
                letter1.y = new_cols[0];
                letter2.y = new_cols[1];
            }
            // neither same row or column - take values from the same rows from the box created from the two points
            else if ((letter1.y != letter2.y) && (letter1.x != letter2.x)) {
                if (encrypt) {
                    int temp = letter1.x;
                    letter1.x = letter2.x;
                    letter2.x = temp;
                } else {
                    int temp = letter2.x;
                    letter2.x = letter1.x;
                    letter1.x = temp;
                }
            }
            output_message += ("" + playfairKey[letter1.y][letter1.x] + playfairKey[letter2.y][letter2.x]);
        }
        return output_message;
    }

    public static void main(String[] args) throws Exception {
        // Gets user input, encrypts it using a Playfair Cipher key, decrypts it using the same key.
        String message_unencrypted = getMessageFromCmdLine();
        char[][] key = generatePlayfairKey();

        String message_encrypted = cryptMessage(key, message_unencrypted, true);
        System.out.println(MessageFormat.format("The encrypted message: {0}", message_encrypted));

        try {
            String unencrypted_message = cryptMessage(key, message_encrypted, false);
            System.out.println(MessageFormat.format("The decrypted message: {0}", unencrypted_message));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}