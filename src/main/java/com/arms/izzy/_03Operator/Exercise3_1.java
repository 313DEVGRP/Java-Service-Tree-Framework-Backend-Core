package com.arms.izzy._03Operator;

public class Exercise3_1 {
    public static void main(String[] args) {
        int x = 2;
        int y = 5;
        char c = 'A';// 'A'의 문자코드는 65

        System.out.println(1 + x << 33);                //6
        System.out.println(y >= 5 || x < 0 && x > 2);   //true
        System.out.println(y += 10 - x++);              //13
        System.out.println(x += 2);                     //5
        System.out.println(!('A' <= c && c <= 'Z'));    //false
        System.out.println('C' - c);                    //2
        System.out.println('5' - '0');                  //5
        System.out.println(c + 1);                      //66
        System.out.println(++c);                        //B
        System.out.println(c);                          //B
    }

}
