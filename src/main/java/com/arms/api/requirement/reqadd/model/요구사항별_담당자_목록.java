package com.arms.api.requirement.reqadd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 요구사항별_담당자_목록 {
    private List<요구사항_담당자> 요구사항별_담당자_목록 = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class 요구사항_담당자{

        private String 요구사항_키;

        private String 담당자_아이디;

        private String 담당자_이름;

        private String 요구사항_아이디;

        private Boolean 요구사항_여부;
    }
}
