package com.arms.api.analysis.cost.dto;

import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class 버전별_요구사항별_활성화된_요구사항 {
    private Map<Long, 요구사항별_그룹> 버전별_그룹 = new HashMap<>();

    @Getter
    @Setter
    public static class 요구사항별_그룹 {
        private Map<Long, List<요구사항_데이터>> 요구사항별_그룹 = new HashMap<>();
    }
    @Getter
    @Setter
    public static class 요구사항_데이터{
        private Long c_id;
        private String c_title;
        private Long c_pdservice_link;
        private String c_pdservice_name;
        private Long c_pds_version_link;
        private String c_pds_version_name;
        private Long c_req_link;
        private String c_req_name;
        private String c_issue_key;
        private Long cost;
    }
    public static 요구사항_데이터 필요데이터(ReqStatusEntity entity) {
        요구사항_데이터 요구사항_데이터 = new 요구사항_데이터();
        요구사항_데이터.setC_id(entity.getC_id());
        요구사항_데이터.setC_title(entity.getC_title());
        요구사항_데이터.setC_pdservice_link(entity.getC_pdservice_link());
        요구사항_데이터.setC_pdservice_name(entity.getC_pdservice_name());
        요구사항_데이터.setC_pds_version_link(entity.getC_pds_version_link());
        요구사항_데이터.setC_pds_version_name(entity.getC_pds_version_name());
        요구사항_데이터.setC_req_link(entity.getC_req_link());
        요구사항_데이터.setC_req_name(entity.getC_req_name());
        요구사항_데이터.setC_issue_key(entity.getC_issue_key());

        return 요구사항_데이터;
    }

}
