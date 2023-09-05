package com.arms.requirement.reqadd.model;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReqAddDetailDTO {

	private String pdService_c_title;//제품서비스

	private String pdServiceVersion_c_title;//연관 버전

	private Long pdService_c_id;//아이디

	private String reqAdd_c_title;//제목

	private String reqAdd_c_req_writer;//작성자

	private Date reqAdd_c_req_create_date;//최근 작성일

	private String reqAdd_c_req_reviewer01;//리뷰어1

	private String reqAdd_c_req_reviewer02;//리뷰어2

	private String reqAdd_c_req_reviewer03;//리뷰어3

	private String reqAdd_c_req_reviewer04;//리뷰어4

	private String reqAdd_c_req_reviewer05;//리뷰어5

}
