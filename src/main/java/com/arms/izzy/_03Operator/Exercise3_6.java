package com.arms.izzy._03Operator;

public class Exercise3_6 {
    public static void main(String[] args) {
        /*num보다 크면서 10의 배수에서 num의 값을 뺀 나머지를 구하는 코드.*/

        int num = 24;
        int mok = num/10;                        //몫
        int remain = num%10;                     //나머지
        mok = (remain > 0) ? mok += 1 : mok;    //나머지가 0이상일경우
        System.out.println((mok *10) -num);

        System.out.println("================================");
        double mok2 = num/10.0;                   //double형으로 변환
        int result =(int) Math.ceil(mok2)*10;   //Math.ceil(올림)
        System.out.println(result-num);
    }
}
