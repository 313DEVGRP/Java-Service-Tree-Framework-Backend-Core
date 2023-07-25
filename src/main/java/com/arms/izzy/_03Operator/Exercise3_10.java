package com.arms.izzy._03Operator;

public class Exercise3_10 {

    public static void main(String[] args) {
        /*대문자를 소문자로
         * 소문자가 대문자보다 32 더 크다.
         * 'A' = 65
         * 'a' = 97*/

        char ch = 'A';
       // char lowerCase = (ch<97)? (char) (ch + 32) :ch;  //대문자아닌문자 구분x
        char lowerCase = ('A'<=ch&&ch<='Z')? (char) (ch + 32) :ch;
        System.out.println("ch: " + ch);
        System.out.println("ch to lowerCase: " + lowerCase);
    }
}
