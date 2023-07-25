package com.arms.izzy._03Operator;

public class Exercise3_5 {
    public static void main(String[] args) {
        /*num 의 1의자리를 1 로 바꾸는 코드
        * ex )) 333-> 331
        *       777 -> 771*/
        int num =333;

        System.out.println(((num/10)*10)+(num/num));
    }
}
