package com.arms.izzy._03Operator;

public class Exercise3_9 {
    public static void main(String[] args) {
        char ch = 'z';
        boolean b = 'A'<=ch&&ch<='Z'||'a'<=ch&&ch<='z'||'0'<=ch&&ch<='9';

        System.out.println(b);
    }
}
