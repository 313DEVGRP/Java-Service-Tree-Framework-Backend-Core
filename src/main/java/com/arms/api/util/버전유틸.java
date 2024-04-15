package com.arms.api.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class 버전유틸 {

    public static Long[] convertToLongArray(String input) {
        // 입력이 null이거나 비어있을 때, 길이 0 배열 반환
        if (input == null || input.isEmpty()) {
            return new Long[0];
        }
        // 문자열에서 대괄호 및 쌍따옴표를 제거하고 쉼표로 구분하여 문자열 배열로 변환
        String[] stringArray = input.substring(1, input.length() - 1).split(",");

        // 예외 처리: stringArray의 길이가 0인 경우
        if (stringArray.length == 0) {
            throw new IllegalArgumentException("[버전유틸 :: convertToLongArray] :: stringArray의 입력이 올바른 형식이 아닙니다.");
        }

        // Long 배열 생성
        Long[] longArray = new Long[stringArray.length];

        // 문자열 배열을 Long 배열로 변환
        for (int i = 0; i < stringArray.length; i++) {
            try {
                longArray[i] = Long.parseLong(stringArray[i].replaceAll("\"", "").trim());
            } catch (NumberFormatException e) {
                // 숫자로 변환할 수 없는 경우에는 null을 할당
                longArray[i] = null;
            } catch (ArrayIndexOutOfBoundsException e) {
                // 배열 인덱스가 범위를 벗어나는 경우, 예외 처리
                log.error("[버전유틸 :: convertToLongArray] :: longArray[{}]에서 배열 인덱스가 범위를 벗어났습니다.", i);
            } catch (Exception e) {
                log.error("[버전유틸 :: convertToLongArray] :: longArray[{}]에서 예상치 못한 예외가 발생했습니다 => {}", i, e.getMessage());
            }
        }

        return longArray;
    }
}
