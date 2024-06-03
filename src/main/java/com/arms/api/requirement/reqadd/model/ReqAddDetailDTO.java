package com.arms.api.requirement.reqadd.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

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

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private Date reqAdd_c_req_create_date;//최근 작성일

	private String reqAdd_c_req_reviewer01;//리뷰어1

	private String reqAdd_c_req_reviewer02;//리뷰어2

	private String reqAdd_c_req_reviewer03;//리뷰어3

	private String reqAdd_c_req_reviewer04;//리뷰어4

	private String reqAdd_c_req_reviewer05;//리뷰어5

	private String reqAdd_c_req_contents;//설명

	private Long reqAdd_c_req_priority_link; // 우선순위

	private Long reqAdd_c_req_state_link; // 상태

	private Long reqAdd_c_req_difficulty_link; // 난이도

	private Date reqAdd_c_req_start_date; // 시작일

	private Date reqAdd_c_req_end_date; // 종료일

}
