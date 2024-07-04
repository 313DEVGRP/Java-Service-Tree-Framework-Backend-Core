/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqadd.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.analysis.common.model.IsReqType;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.FollowReqLinkDTO;
import com.arms.api.requirement.reqadd.model.ReqAddDetailDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.model.요구사항별_담당자_목록.요구사항_담당자;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.internal.InternalService;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service("reqAdd")
public class ReqAddImpl extends TreeServiceImpl implements ReqAdd {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EngineService engineService;

	@Autowired
	private InternalService internalService;

	@Autowired
	private GlobalTreeMapService globalTreeMapService;

	@Autowired
	@Qualifier("pdServiceVersion")
	private PdServiceVersion pdServiceVersion;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

	@Autowired
	@Qualifier("jiraServer")
	private JiraServer jiraServer;

	@Autowired
	protected Chat chat;

	@Autowired
	private ArmsDetailUrlConfig armsDetailUrlConfig;

	@Autowired
	private PdService pdService;

	@Autowired
	@Qualifier("reqStatus")
	private ReqStatus reqStatus;

	@Override
	@Transactional
	public ReqAddEntity addReqNode(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		SessionUtil.setAttribute("addNode",changeReqTableName);

		ReqAddEntity savedReqAddEntity = this.addNode(reqAddEntity);

		SessionUtil.removeAttribute("addNode");

		return savedReqAddEntity;
	}

	@Override
	@Transactional
	public ReqAddEntity moveReqNode(ReqAddEntity reqAddEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		SessionUtil.setAttribute("moveNode", changeReqTableName);

		ReqAddEntity savedReqAddEntity = this.moveNode(reqAddEntity, request);

		SessionUtil.removeAttribute("moveNode");

		return savedReqAddEntity;
	}

	@Override
	@Transactional
	public int removeReqNode(ReqAddEntity reqAddEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		SessionUtil.setAttribute("removeNode", changeReqTableName);
		int removedReqAddEntity = this.removeNode(reqAddEntity);
		SessionUtil.removeAttribute("removeNode");

		return removedReqAddEntity;
	}

	@Override
	public ReqAddDetailDTO getDetail(FollowReqLinkDTO followReqLinkDTO, String changeReqTableName) throws Exception {

		Long targetTableId = followReqLinkDTO.getPdService();

		String targetReqAddTableName = changeReqTableName + targetTableId;

		SessionUtil.setAttribute("getDetail",targetReqAddTableName);
		ReqAddEntity searchReqAddEntity = new ReqAddEntity();
		searchReqAddEntity.setC_id(followReqLinkDTO.getReqAdd());
		ReqAddEntity reqAddEntity = this.getNode(searchReqAddEntity);
		SessionUtil.removeAttribute("getDetail");

		PdServiceEntity pdServiceEntity = reqAddEntity.getPdServiceEntity();

		String pdServiceVersionTitles = pdServiceEntity.getPdServiceVersionEntities()
			.stream()
			.filter(a -> followReqLinkDTO.getPdServiceVersion().contains(a.getC_id()))
			.map(PdServiceVersionEntity::getC_title)
			.collect(Collectors.joining(","));

		if(followReqLinkDTO.getJiraProject()!=null){
			JiraProjectEntity jiraProjectSearchEntity = new JiraProjectEntity();
			jiraProjectSearchEntity.setC_id(followReqLinkDTO.getJiraProject());
			JiraProjectEntity jiraProjectEntity = jiraProject.getNode(jiraProjectSearchEntity);
		}

		if(followReqLinkDTO.getJiraServer()!=null) {
			JiraServerEntity searchJiraServerEntity = new JiraServerEntity();
			searchJiraServerEntity.setC_id(followReqLinkDTO.getJiraServer());
			JiraServerEntity jiraServerEntity = jiraServer.getNode(searchJiraServerEntity);
		}


		return ReqAddDetailDTO.builder()
			.pdService_c_title(pdServiceEntity.getC_title())
			.pdServiceVersion_c_title(pdServiceVersionTitles)
			.pdService_c_id(pdServiceEntity.getC_id())
			.reqAdd_c_title(reqAddEntity.getC_title())
			.reqAdd_c_req_writer(reqAddEntity.getC_req_writer())
			.reqAdd_c_req_create_date(reqAddEntity.getC_req_create_date())
			.reqAdd_c_req_reviewer01(reqAddEntity.getC_req_reviewer01())
			.reqAdd_c_req_reviewer02(reqAddEntity.getC_req_reviewer02())
			.reqAdd_c_req_reviewer03(reqAddEntity.getC_req_reviewer03())
			.reqAdd_c_req_reviewer04(reqAddEntity.getC_req_reviewer04())
			.reqAdd_c_req_reviewer05(reqAddEntity.getC_req_reviewer05())
			.reqAdd_c_req_contents(reqAddEntity.getC_req_contents())
			.reqAdd_c_req_state_link(reqAddEntity.getReqStateEntity().getC_id())
			.reqAdd_c_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id())
			.reqAdd_c_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id())
			.reqAdd_c_req_start_date(reqAddEntity.getC_req_start_date())
			.reqAdd_c_req_end_date(reqAddEntity.getC_req_end_date())
			.build();
	}

	@Override
	@Transactional
	public Integer updateReqNode(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		SessionUtil.setAttribute("updateNode", changeReqTableName);
		this.updateNode(reqAddEntity);
		SessionUtil.removeAttribute("updateNode");

		return 1;
	}

	@Override
	@Transactional
	public Integer updateDataBase(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		// reqAdd 업데이트 (상태, 우선순위, 난이도, 시작일, 종료일)
		int 요구사항_디비_업데이트_결과;
		요구사항_디비_업데이트_결과 = 요구사항_디비_업데이트(reqAddEntity,changeReqTableName);

		// reqStatus 업데이트 (상태, 우선순위, 난이도)
		int 요구사항_상태_디비_업데이트_결과;
		요구사항_상태_디비_업데이트_결과 = 요구사항_상태_디비_업데이트(reqAddEntity, changeReqTableName);

		int 요구사항_업데이트_결과 = 요구사항_디비_업데이트_결과 * 요구사항_상태_디비_업데이트_결과;

		return 요구사항_업데이트_결과;
	}

	private Integer 요구사항_디비_업데이트(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception{
		// reqAdd 업데이트
		SessionUtil.setAttribute("updateDataBase", changeReqTableName);

		int 요구사항_업데이트_결과 = this.updateNode(reqAddEntity);

		SessionUtil.removeAttribute("updateDataBase");

		if (요구사항_업데이트_결과 == 0) {
			logger.info("ReqAddImpl :: updateDataBase :: 요구사항 업데이트에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 업데이트에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		return 요구사항_업데이트_결과;
	}

	private Integer 요구사항_상태_디비_업데이트(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception{

		String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", "");
		String 요구사항_상태_테이블 = "T_ARMS_REQSTATUS_"+pdServiceId;
		int 업데이트_결과 = 0;

		SessionUtil.setAttribute("updateDataBase", 요구사항_상태_테이블);
		ReqStatusEntity searchEntity = new ReqStatusEntity();

		Criterion criterion = Restrictions.eq("c_req_link", reqAddEntity.getC_id());
		searchEntity.getCriterions().add(criterion);
		List<ReqStatusEntity> reqStatusEntityList = reqStatus.getNodesWithoutRoot(searchEntity);

		for(ReqStatusEntity reqStatusEntity : reqStatusEntityList){
			if (reqAddEntity.getReqStateEntity() != null) {
				reqStatusEntity.setC_req_state_name(reqAddEntity.getReqStateEntity().getC_title());
				reqStatusEntity.setC_req_state_link(reqAddEntity.getReqStateEntity().getC_id());
			}

			if (reqAddEntity.getReqPriorityEntity() != null) {
				reqStatusEntity.setC_req_priority_name(reqAddEntity.getReqPriorityEntity().getC_title());
				reqStatusEntity.setC_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id());
			}

			if (reqAddEntity.getReqDifficultyEntity() != null) {
				reqStatusEntity.setC_req_difficulty_name(reqAddEntity.getReqDifficultyEntity().getC_title());
				reqStatusEntity.setC_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id());
			}

			// ALM 요구사항 상태 변경 요청 (상태 값 변경이 있을 경우)
			reqStatus.ALM_이슈상태_업데이트(reqStatusEntity);

			업데이트_결과 = reqStatus.updateNode(reqStatusEntity);
			업데이트_결과 *= 업데이트_결과;

		}

		SessionUtil.removeAttribute("updateDataBase");

		if (업데이트_결과 == 0) {
			logger.info("ReqAddImpl :: updateDataBase :: 요구사항 업데이트에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 업데이트에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		return 업데이트_결과;
	}

	public List<요구사항_담당자> getRequirementAssignee(PdServiceEntity pdServiceEntity)  {
		List<요구사항_담당자> 요구사항_담당자_목록 = new ArrayList<>();

		요구사항_담당자_목록.addAll(집계데이터_가져오기(pdServiceEntity, IsReqType.REQUIREMENT, true));
		요구사항_담당자_목록.addAll(집계데이터_가져오기(pdServiceEntity, IsReqType.ISSUE, false));

		return 요구사항_담당자_목록;
	}

	private List<요구사항_담당자> 집계데이터_가져오기(PdServiceEntity pdServiceEntity, IsReqType isReqType, boolean isRequirement) {
		List<요구사항_담당자> 결과_목록 = new ArrayList<>();

		AggregationRequestDTO 집계요청 = new AggregationRequestDTO();
		집계요청.setPdServiceLink(pdServiceEntity.getC_id());
		집계요청.setIsReqType(isReqType);

		Optional<List<검색결과>> 집계데이터 = Optional.ofNullable(engineService.제품_요구사항_담당자(집계요청).getBody());
		집계데이터.ifPresent(esData -> {
			esData.forEach(result -> {
				String 요구사항_키 = result.get필드명();
				result.get하위검색결과().get("assignees").forEach(assignee -> {
					String 담당자_아이디 = assignee.get필드명();
					String 담당자_이름 = assignee.get하위검색결과().get("displayNames").stream().findFirst().orElse(new 검색결과()).get필드명();
					String 요구사항_아이디 = assignee.get하위검색결과().get("cReqLink").stream().findFirst().orElse(new 검색결과()).get필드명();
					결과_목록.add(new 요구사항_담당자(요구사항_키, 담당자_아이디, 담당자_이름, 요구사항_아이디, isRequirement));
				});
			});
		});

		return 결과_목록;
	}
}
