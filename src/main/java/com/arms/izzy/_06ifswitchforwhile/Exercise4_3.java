package com.arms.izzy._06ifswitchforwhile;

public class Exercise4_3 {
    public static void main(String[] args) {

    /*
    * 1-10 각 숫자까지의 합의 합*/
        int sum = 0;
        int totalsum = 0;
        for(int i = 1; i<=3; i++) {
                sum +=i;
                totalsum += sum;

        }
        System.out.println("totalsum"+totalsum);
    }
}
