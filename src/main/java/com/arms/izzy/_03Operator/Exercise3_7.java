package com.arms.izzy._03Operator;

public class Exercise3_7 {
    public static void main(String[] args) {
        /*화씨를 섭씨로 변환
        * 변환공식 'C = 5/9 * (F -32)'
        * 결과값은 소수점 셋째자리에서 반올림
        * Math.round()사용금지.*/
        int fahrenheit = 100;
        float celcius =  (int)((5/9f * (fahrenheit-32))*100 +0.5)/100f;


        System.out.println("Fahhrenheit: " + fahrenheit);
        System.out.println("Celcius: " + celcius);

    }
}
