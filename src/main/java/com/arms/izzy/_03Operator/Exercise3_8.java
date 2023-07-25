package com.arms.izzy._03Operator;

public class Exercise3_8 {
    public static void main(String[] args) {
        byte a = 10;
        byte b = 20;
        byte c = (byte) (a + b);

        char ch = 'A';
        ch = (char) (ch +2);

        float f = 3/2f;
        //long l = (long)3000 * 3000 * 3000;
        long l = 3000 * 3000 * 3000L;
        //logn 8 byte


        float f2 = 0.1f;
        double d = 0.1;

        boolean result = (float)d ==f2;

        //float 4byte double 8byte 형변환은 큰쪽에서 작은쪽으로해야 값손실 x

        System.out.println("c= " + c);
        System.out.println("ch= " + ch);
        System.out.println("f= " + f);
        System.out.println("l= "+l);
        System.out.println("result= " + result);

    }
}
