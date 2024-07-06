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
import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.model.enums.TextFormattingType;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.FollowReqLinkDTO;
import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddDetailDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.model.요구사항별_담당자_목록.요구사항_담당자;
import com.arms.api.requirement.reqstatus.model.CRUDType;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.TreeServiceUtils;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.internal.InternalService;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.DateUtils;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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

	@Override
	public void 요구사항_생성_이후_상태정보_처리_프로세스(ReqAddEntity savedReqAddEntity) throws Exception {

		//신규 저장된 요구사항 정보 수집
		Long 요구사항_아이디 = savedReqAddEntity.getC_id();
		PdServiceEntity 요구사항_제품서비스 = savedReqAddEntity.getPdServiceEntity();
		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

		ObjectMapper objectMapper = new ObjectMapper();
		String 요구사항_제품서비스_버전목록_JSON = savedReqAddEntity.getC_req_pdservice_versionset_link();
		List<String> 요구사항_제품서비스_버전목록 = Arrays.asList(objectMapper.readValue(요구사항_제품서비스_버전목록_JSON, String[].class));

		//버전 목록 정보로 지라 프로젝트 아이디 추출
		Set<Long> 지라프로젝트_아이디_셋 = new HashSet<>();
		for (String 디비에저장된_제품서비스하위_버전 : 요구사항_제품서비스_버전목록) {
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
			List<GlobalTreeMapEntity> 버전_지라프로젝트_목록 = globalTreeMapService.findAllBy(globalTreeMap).stream()
					.filter(엔티티 -> 엔티티.getJiraproject_link() != null)
					.collect(Collectors.toList());

			for (GlobalTreeMapEntity 엔티티 : 버전_지라프로젝트_목록) {
				Long 지라프로젝트_아이디 = 엔티티.getJiraproject_link();
				지라프로젝트_아이디_셋.add(지라프로젝트_아이디);
			}
		}

		// 연결된 버전의 프로젝트 목록으로 REQSTATUS 데이터 추가
		this.연결된_버전의_프로젝트별로_REQSTATUS_데이터추가(savedReqAddEntity, 지라프로젝트_아이디_셋, 요구사항_제품서비스);

		ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
		reqStatusDTO.setC_req_link(요구사항_아이디);

		internalService.요구사항_상태_확인후_ALM처리_및_REQSTATUS_업데이트("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
	}

	public void 연결된_버전의_프로젝트별로_REQSTATUS_데이터추가(ReqAddEntity savedReqAddEntity, Set<Long> 지라프로젝트_아이디_셋, PdServiceEntity 요구사항_제품서비스) throws Exception {

		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

		// 추가되는 프로젝트 목록을 순회하며 REQSTATUS 데이터 생성처리
		for (Long ALM프로젝트_아이디 : 지라프로젝트_아이디_셋) {

			// REQSTATUS 데이터 세팅
			ReqStatusDTO reqStatusDTO = this.REQSTATUS_데이터_설정(ALM프로젝트_아이디, savedReqAddEntity, 요구사항_제품서비스, CRUDType.생성.getType());

			// 없을 경우 REQSTATUS addNode API 호출
			ResponseEntity<?> 결과 = internalService.요구사항_상태_정보_저장하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

			if (!결과.getStatusCode().is2xxSuccessful()) {
				logger.error("T_ARMS_REQSTATUS_" + 제품서비스_아이디 + " :: 생성 오류 :: " + reqStatusDTO.toString());
			}
		}
	}

	@Override
	public void 요구사항_수정_이후_상태정보_처리_프로세스(String changeReqTableName, ReqAddEntity reqAddEntity, LoadReqAddDTO loadReqAddDTO) throws Exception {
		String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", "");

		// 3. 수정 전 후 비교
		ObjectMapper objectMapper = new ObjectMapper();
		Set<String> 수정전버전셋 = objectMapper.readValue(loadReqAddDTO.getC_req_pdservice_versionset_link(), Set.class);
		Set<String> 현재버전셋 = objectMapper.readValue(reqAddEntity.getC_req_pdservice_versionset_link(), Set.class);
		Set<String> 루프용버전셋 = objectMapper.readValue(loadReqAddDTO.getC_req_pdservice_versionset_link(), Set.class);
		루프용버전셋.addAll(현재버전셋);

		List<Long> 수정전버전셋리스트 = 수정전버전셋.stream().map(Long::valueOf).collect(Collectors.toList()); // 1,2
		List<Long> 현재버전셋리스트 = 현재버전셋.stream().map(Long::valueOf).collect(Collectors.toList()); // 2,3
		List<Long> 루프용버전셋리스트 = 루프용버전셋.stream().map(Long::valueOf).collect(Collectors.toList()); // 1,2,3

		logger.info("수정전버전셋리스트 : {}", 수정전버전셋리스트);
		logger.info("현재버전셋리스트 : {}", 현재버전셋리스트);
		logger.info("루프용버전셋리스트 : {}", 루프용버전셋리스트);

		List<GlobalTreeMapEntity> globalTreeMapEntities = globalTreeMapService.findAllByIds(루프용버전셋리스트, "pdserviceversion_link")
				.stream()
				.filter(globalTreeMap -> globalTreeMap.getJiraproject_link() != null)
				.collect(Collectors.toList());

		Set<Long> 수정전버전에연결된지라프로젝트아이디 = globalTreeMapEntities.stream()
				.filter(globalTreeMap -> 수정전버전셋리스트.contains(globalTreeMap.getPdserviceversion_link()))
				.map(GlobalTreeMapEntity::getJiraproject_link)
				.collect(Collectors.toSet());

		logger.info("수정전버전에연결된지라프로젝트아이디 : {}", 수정전버전에연결된지라프로젝트아이디);

		Set<Long> 현재버전에연결된지라프로젝트아이디 = globalTreeMapEntities.stream()
				.filter(globalTreeMap -> 현재버전셋리스트.contains(globalTreeMap.getPdserviceversion_link()))
				.map(GlobalTreeMapEntity::getJiraproject_link)
				.collect(Collectors.toSet());

		logger.info("현재버전에연결된지라프로젝트아이디 : {}", 현재버전에연결된지라프로젝트아이디);

		Set<Long> 추가된지라프로젝트아이디 = 추가된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);
		Set<Long> 유지된지라프로젝트아이디 = 유지된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);
		Set<Long> 삭제된지라프로젝트아이디 = 삭제된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);

		logger.info("유지된지라프로젝트아이디 : {}", 유지된지라프로젝트아이디);
		logger.info("추가된지라프로젝트아이디 : {}", 추가된지라프로젝트아이디);
		logger.info("삭제된지라프로젝트아이디 : {}", 삭제된지라프로젝트아이디);

		PdServiceEntity 요구사항_제품서비스 = 제품데이터조회(pdServiceId);
		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();		// 연결된 버전의 프로젝트 목록으로 REQSTATUS 데이터 추가

		ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
		reqStatusDTO.setC_req_link(reqAddEntity.getC_id());

		// 기존 REQSTATUS 데이터 목록 조회
		ResponseEntity<List<ReqStatusEntity>> 결과
				= internalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceId, reqStatusDTO);
		List<ReqStatusEntity> reqStatusEntityList = 결과.getBody();

		List<ReqStatusEntity> 유지된_REQSTATUS_목록 = Optional.ofNullable(reqStatusEntityList)
				.orElse(Collections.emptyList())
				.stream()
				.filter(reqStatusEntity -> 유지된지라프로젝트아이디.contains(reqStatusEntity.getC_jira_project_link()))
				.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
				.collect(Collectors.toList());

		List<ReqStatusEntity> 삭제된_REQSTATUS_목록 = Optional.ofNullable(reqStatusEntityList)
				.orElse(Collections.emptyList())
				.stream()
				.filter(reqStatusEntity -> 삭제된지라프로젝트아이디.contains(reqStatusEntity.getC_jira_project_link()))
				.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
				.collect(Collectors.toList());

		// 새로운 버전의 프로젝트의 경우 REQSTATUS 추가로직 수행
		this.연결된_버전의_프로젝트별로_REQSTATUS_데이터추가(reqAddEntity, 추가된지라프로젝트아이디, 요구사항_제품서비스);
		this.연결된_버전의_프로젝트별_REQSTATUS_데이터수정(reqAddEntity, 유지된_REQSTATUS_목록, 요구사항_제품서비스, CRUDType.수정.getType());
		// 요구사항 수정 시 연결 해제된 프로젝트의 요구사항 이슈의 경우 soft delete 처리
		this.연결된_버전의_프로젝트별_REQSTATUS_데이터수정(reqAddEntity, 삭제된_REQSTATUS_목록, 요구사항_제품서비스, CRUDType.소프트_삭제.getType());

		internalService.요구사항_상태_확인후_ALM처리_및_REQSTATUS_업데이트("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
	}

	@Override
	public void 요구사항_삭제_이후_상태정보_처리_프로세스(String changeReqTableName, ReqAddEntity reqAddEntity, LoadReqAddDTO loadReqAddDTO) throws Exception {
		// 삭제에 필요한 데이터 설정
		reqAddEntity.setC_req_pdservice_versionset_link(loadReqAddDTO.getC_req_pdservice_versionset_link());
		reqAddEntity.setC_title(loadReqAddDTO.getC_title());
		reqAddEntity.setC_req_contents(loadReqAddDTO.getC_req_contents());

		String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", "");
		PdServiceEntity 요구사항_제품서비스 = 제품데이터조회(pdServiceId);
		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

		ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
		reqStatusDTO.setC_req_link(loadReqAddDTO.getC_id());

		// 삭제타입 application.yml에 설정된 값에 따라 변경, default - soft delete
		String 삭제_타입 = Optional.ofNullable(armsDetailUrlConfig.getDeleteTYpe())
				.filter(type -> !type.isEmpty()).orElse(CRUDType.소프트_삭제.getType());

		// 기존 REQSTATUS 데이터 목록 조회
		ResponseEntity<List<ReqStatusEntity>> 결과
				= internalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceId, reqStatusDTO);
		List<ReqStatusEntity> reqStatusEntityList = 결과.getBody();

		this.연결된_버전의_프로젝트별_REQSTATUS_데이터수정(reqAddEntity, reqStatusEntityList, 요구사항_제품서비스, 삭제_타입);

		internalService.요구사항_상태_확인후_ALM처리_및_REQSTATUS_업데이트("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
	}

	public void 연결된_버전의_프로젝트별_REQSTATUS_데이터수정(ReqAddEntity reqAddEntity, List<ReqStatusEntity> reqStatusEntityList, PdServiceEntity 요구사항_제품서비스, String CRUD_타입) throws Exception {

		// 유지 또는 삭제되는 프로젝트 목록을 순회하며 REQSTATUS 데이터 생성처리
		for (ReqStatusEntity reqStatusEntity : reqStatusEntityList) {

			Long ALM프로젝트_아이디 = reqStatusEntity.getC_jira_project_link();

			// REQSTATUS 수정 데이터 세팅
			ReqStatusDTO reqStatusDTO = this.REQSTATUS_데이터_설정(ALM프로젝트_아이디, reqAddEntity, 요구사항_제품서비스, CRUD_타입);

			// 업데이트할 c_id 설정
			reqStatusDTO.setC_id(reqStatusEntity.getC_id());
			// ALM 서버의 요구사항 생성이 아직 진행되기 전에 삭제를 할 경우 삭제 처리로 전환 / 수정이 발생할 경우 방어코드
			if ( (StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType()) || StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType()))
											&& reqStatusEntity.getC_issue_key() == null) {
				reqStatusDTO.setC_etc(CRUD_타입);
			}
			else if (reqStatusEntity.getC_issue_key() == null) {
				// ISSUE KEY가 NULL일 경우 생성 상태로 유지
				reqStatusDTO.setC_etc(CRUDType.생성.getType());
			}
			else {
				reqStatusDTO.setC_etc(CRUD_타입);
			}

			// REQSTATUS 데이터 updateNode API 호출
			ResponseEntity<?> 결과 = internalService.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 요구사항_제품서비스.getC_id(), reqStatusDTO);

			if (!결과.getStatusCode().is2xxSuccessful()) {
				logger.error("T_ARMS_REQSTATUS_" + 요구사항_제품서비스.getC_id() + " :: 삭제 오류 :: " + reqStatusDTO.toString());
			}
		}
	}

	private ReqStatusDTO REQSTATUS_데이터_설정(Long ALM프로젝트_아이디, ReqAddEntity savedReqAddEntity, PdServiceEntity 요구사항_제품서비스, String CRUD_타입) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();

		Long 요구사항_아이디 = savedReqAddEntity.getC_id();

		String 요구사항_제품서비스_버전목록_JSON = savedReqAddEntity.getC_req_pdservice_versionset_link();
		List<String> 요구사항_제품서비스_버전목록 = Arrays.asList(objectMapper.readValue(요구사항_제품서비스_버전목록_JSON, String[].class));

		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

		Set<PdServiceVersionEntity> 제품서비스_버전_세트 = 요구사항_제품서비스.getPdServiceVersionEntities();
		Map<Long, String> 제품_버전아이디_버전명_맵 = 제품서비스_버전_세트.stream().collect(Collectors.toMap(PdServiceVersionEntity::getC_id, PdServiceVersionEntity::getC_title));
		Map<Long,Set<Long>> 지라프로젝트_버전아이디_맵 = this.지라프로젝트_버전아이디_맵만들기(요구사항_제품서비스_버전목록);
		Set<Long> 버전아이디_세트 = 지라프로젝트_버전아이디_맵.get(ALM프로젝트_아이디);

		GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
		globalTreeMap.setJiraproject_link(ALM프로젝트_아이디);
		List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);

		GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
				.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null)
				.findFirst()
				.orElse(null);

		Long ALM서버_아이디 = null;
		if (지라서버_글로벌트리맵 != null) {
			ALM서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();
		}

		ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
		//-- 추가된 프로젝트의 경우 설정
		if (StringUtils.equals(CRUD_타입, CRUDType.생성.getType())) {
			reqStatusDTO.setRef(TreeConstant.First_Node_CID);
			reqStatusDTO.setC_type(TreeConstant.Leaf_Node_TYPE);
		}

		List<Long> 버전아이디_내림차순_목록 = null;
		String 버전아이디_내림차순_문자열 = null;
		String 버전명_내림차순_문자열 = null;
		String 버전ID목록 = null;

		if (버전아이디_세트 != null) {
			버전아이디_내림차순_목록 = 버전아이디_세트.stream()
					.sorted(Comparator.reverseOrder())
					.collect(Collectors.toList());

			버전아이디_내림차순_문자열 = 버전아이디_내림차순_목록.stream()
					.map(String::valueOf)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			버전명_내림차순_문자열 = 버전아이디_내림차순_목록.stream()
					.map(제품_버전아이디_버전명_맵::get)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			버전ID목록 = 버전아이디_내림차순_목록.stream()
					.map(String::valueOf)
					.collect(Collectors.joining(","));
		}

		//-- ARMS 요구사항의 매핑버전목록
		reqStatusDTO.setC_pds_version_name(버전명_내림차순_문자열);
		reqStatusDTO.setC_req_pdservice_versionset_link(버전아이디_내림차순_문자열);

		//-- 버전 연결 alm 프로젝트
		JiraProjectEntity 검색된_ALM프로젝트 = this.ALM프로젝트_검색(ALM프로젝트_아이디);
		Optional.ofNullable(검색된_ALM프로젝트).ifPresent(프로젝트 -> {
			Optional.ofNullable(프로젝트.getC_id()).ifPresent(reqStatusDTO::setC_jira_project_link);
			Optional.ofNullable(프로젝트.getC_jira_name()).ifPresent(reqStatusDTO::setC_jira_project_name);
			Optional.ofNullable(프로젝트.getC_jira_key()).ifPresent(reqStatusDTO::setC_jira_project_key);
			Optional.ofNullable(프로젝트.getC_jira_url()).ifPresent(reqStatusDTO::setC_jira_project_url);
		});

		//-- 프로젝트의 alm server
		JiraServerEntity 검색된_ALM서버 = this.ALM서버_검색(ALM서버_아이디);
		Optional.ofNullable(검색된_ALM서버).ifPresent(ALM서버 -> {
			Optional.ofNullable(ALM서버.getC_id()).ifPresent(reqStatusDTO::setC_jira_server_link);
			Optional.ofNullable(ALM서버.getC_jira_server_name()).ifPresent(reqStatusDTO::setC_jira_server_name);
			Optional.ofNullable(ALM서버.getC_jira_server_base_url()).ifPresent(reqStatusDTO::setC_jira_server_url);
		});

		String 요구사항_제목 = savedReqAddEntity.getC_title();
		if (StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType()) || StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType())) {
			요구사항_제목 = "[삭제된 요구사항 이슈] :: " + 요구사항_제목;
		}
		reqStatusDTO.setC_title(요구사항_제목);

		TextFormattingType 본문형식 = Optional.ofNullable(검색된_ALM서버)
				.map(JiraServerEntity::getC_server_contents_text_formatting_type)
				.map(TextFormattingType::fromString)
				.orElse(TextFormattingType.TEXT);
		String 이슈본문 = 이슈_본문_설정(본문형식, CRUD_타입, savedReqAddEntity, 제품서비스_아이디, ALM서버_아이디, ALM프로젝트_아이디, 버전ID목록);
		reqStatusDTO.setC_contents(이슈본문);

		//-- 제품 서비스
		reqStatusDTO.setC_pdservice_link(제품서비스_아이디);
		reqStatusDTO.setC_pdservice_name(요구사항_제품서비스.getC_title());

		//-- ARMS REQADD 데이터를 설정
		reqStatusDTO.setC_req_link(요구사항_아이디);
		reqStatusDTO.setC_req_name(savedReqAddEntity.getC_title());

		//-- ARMS 요구사항 오너를 제품 서비스 오너로 설정
		String 요구사항_오너 = "admin";
		if (요구사항_제품서비스.getC_pdservice_owner() != null) {
			요구사항_오너 = 요구사항_제품서비스.getC_pdservice_owner();
		}
		reqStatusDTO.setC_req_owner(요구사항_오너);

		//-- ARMS 요구사항 요청자를 기본적으로 요구사항 REQSTATUS 보고자로 설정
		Optional.ofNullable(savedReqAddEntity.getC_req_writer()).ifPresent(reqStatusDTO::setC_issue_reporter);

		//-- ARMS 요구사항 시간 데이터
		Optional.ofNullable(savedReqAddEntity.getC_req_start_date()).ifPresent(reqStatusDTO::setC_req_start_date);
		Optional.ofNullable(savedReqAddEntity.getC_req_end_date()).ifPresent(reqStatusDTO::setC_req_end_date);

		//-- ARMS 요구사항 우선순위
		Optional.ofNullable(savedReqAddEntity.getReqPriorityEntity()).ifPresent(reqPriority -> {
			Optional.ofNullable(reqPriority.getC_id()).ifPresent(reqStatusDTO::setC_req_priority_link);
			Optional.ofNullable(reqPriority.getC_title()).ifPresent(reqStatusDTO::setC_req_priority_name);
		});
		//-- ARMS 요구사항 상태
		Optional.ofNullable(savedReqAddEntity.getReqStateEntity()).ifPresent(reqState -> {
			Optional.ofNullable(reqState.getC_id()).ifPresent(reqStatusDTO::setC_req_state_link);
			Optional.ofNullable(reqState.getC_title()).ifPresent(reqStatusDTO::setC_req_state_name);
		});
		//-- ARMS 요구사항 난이도
		Optional.ofNullable(savedReqAddEntity.getReqDifficultyEntity()).ifPresent(reqDifficulty -> {
			Optional.ofNullable(reqDifficulty.getC_id()).ifPresent(reqStatusDTO::setC_req_difficulty_link);
			Optional.ofNullable(reqDifficulty.getC_title()).ifPresent(reqStatusDTO::setC_req_difficulty_name);
		});

		//-- ARMS 요구사항 작업량 데이터
		Optional.ofNullable(savedReqAddEntity.getC_req_total_resource()).ifPresent(reqStatusDTO::setC_req_total_resource);
		Optional.ofNullable(savedReqAddEntity.getC_req_plan_resource()).ifPresent(reqStatusDTO::setC_req_plan_resource);
		Optional.ofNullable(savedReqAddEntity.getC_req_total_time()).ifPresent(reqStatusDTO::setC_req_total_time);
		Optional.ofNullable(savedReqAddEntity.getC_req_plan_time()).ifPresent(reqStatusDTO::setC_req_plan_time);

		Date date = new Date();
		reqStatusDTO.setC_issue_update_date(date);

		// 요구사항 ALM 서버 설정 상태로 c_etc 컬럼을 crud type별 처리
		if (StringUtils.equals(CRUD_타입, CRUDType.생성.getType())) {
			reqStatusDTO.setC_issue_create_date(date);
		}
		else if (StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType()) || StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType())) {
			reqStatusDTO.setC_issue_delete_date(date);
		}

		if (StringUtils.equals(CRUD_타입, CRUDType.생성.getType())) {
			reqStatusDTO.setC_etc(CRUD_타입);
		}

		logger.info("ReqStatusImpl = reqStatusDTO :: " + objectMapper.writeValueAsString(reqStatusDTO));

		return reqStatusDTO;
	}

	private Map<Long, Set<Long>> 지라프로젝트_버전아이디_맵만들기(List<String> 디비에저장된_제품서비스_하위의_버전리스트) {
		Map<Long, Set<Long>> 지라프로젝트_버전아이디_맵 = new HashMap<>();
		for (String 디비에저장된_제품서비스하위_버전 : 디비에저장된_제품서비스_하위의_버전리스트) {
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
			List<GlobalTreeMapEntity> 버전_지라프로젝트_목록 = globalTreeMapService.findAllBy(globalTreeMap).stream()
					.filter(엔티티 -> 엔티티.getJiraproject_link() != null).collect(Collectors.toList());

			for (GlobalTreeMapEntity 엔티티 : 버전_지라프로젝트_목록) {
				Long ALM프로젝트_아이디 = 엔티티.getJiraproject_link();
				Long 버전_아이디 = 엔티티.getPdserviceversion_link();

				if(지라프로젝트_버전아이디_맵.containsKey(ALM프로젝트_아이디)) {
					지라프로젝트_버전아이디_맵.get(ALM프로젝트_아이디).add(버전_아이디);
				} else {
					Set<Long> 버전_셋 = new HashSet<>();
					버전_셋.add(버전_아이디);
					지라프로젝트_버전아이디_맵.put(ALM프로젝트_아이디, 버전_셋);
				}
			}
		}

		return 지라프로젝트_버전아이디_맵;
	}

	public JiraProjectEntity ALM프로젝트_검색(Long ALM_프로젝트_아이디) {
		if (ALM_프로젝트_아이디 == null) {
			return null;
		}

		JiraProjectEntity jiraProjectEntity = null;
		try {
			jiraProjectEntity = TreeServiceUtils.getNode(jiraProject, ALM_프로젝트_아이디, JiraProjectEntity.class);
			boolean isSoftDelete = Optional.ofNullable(jiraProjectEntity)
					.map(JiraProjectEntity::getC_etc)
					.map("delete"::equals)
					.orElse(false);

			if (isSoftDelete) {
				jiraProjectEntity = null;
			}
		}
		catch (Exception e) {
			logger.error("ALM프로젝트_검색 :: 프로젝트 아이디 :: " + ALM_프로젝트_아이디 + " :: " + e.getMessage());
		}

		return jiraProjectEntity;
	}

	public JiraServerEntity ALM서버_검색(Long ALM서버_아이디) {
		if (ALM서버_아이디 == null) {
			return null;
		}

		JiraServerEntity jiraServerEntity = null;
		try {
			jiraServerEntity = TreeServiceUtils.getNode(jiraServer, ALM서버_아이디, JiraServerEntity.class);
		}
		catch (Exception e) {
			logger.error("ALM서버_검색 :: 서버 아이디 :: " + ALM서버_아이디 + " :: " + e.getMessage());
		}

		return jiraServerEntity;
	}

	private String 이슈_본문_설정(TextFormattingType 본문형식, String CRUD_타입, ReqAddEntity reqAddEntity,
							Long 제품서비스_아이디, Long ALM서버_아이디, Long ALM_프로젝트_아이디, String 버전아이디목록) {

		String 이슈_본문_전체;
		if (StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType()) || StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType())) {
			이슈_본문_전체 = this.삭제_이슈_ARMS_안내문가져오기();
		}
		else {
			이슈_본문_전체 = 등록_및_수정_ARMS_안내문가져오기(reqAddEntity, 제품서비스_아이디, ALM서버_아이디, ALM_프로젝트_아이디, 버전아이디목록);
		}

		String ARMS_요구사항_설명 = Optional.ofNullable(reqAddEntity.getC_req_contents()).orElse("이슈 본문 내용 무");

		if (본문형식 == TextFormattingType.MARKDOWN || 본문형식 == TextFormattingType.HTML) {
			이슈_본문_전체 = 이슈_본문_전체.replaceAll("\n", "<br>") + ARMS_요구사항_설명;
		}
		else {
			이슈_본문_전체 = 이슈_본문_전체 + StringUtils.replaceText(StringUtils.removeHtmlTags(Jsoup.clean(ARMS_요구사항_설명, Whitelist.basic())), "&nbsp;", " ");
		}

		return 이슈_본문_전체;
	}

	private String 삭제_이슈_ARMS_안내문가져오기() {
		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 삭제 된 이슈입니다.,\n" +
				"✔ 삭제 된 이슈는 통계에 수집되지 않습니다. \n\n\n";

		return 이슈내용;
	}

	private String 등록_및_수정_ARMS_안내문가져오기(ReqAddEntity reqAddEntity, Long 제품서비스_아이디,
										Long ALM서버_아이디, Long ALM_프로젝트_아이디, String 버전아이디목록) {

		String 추가된_요구사항의_아이디 = reqAddEntity.getC_id().toString();

		String 버전아이디목록_파싱 = 버전아이디목록.replaceAll("\\[|\\]|\"", "").replaceAll(",", ",");

		String 시작일 = Optional.ofNullable(reqAddEntity.getC_req_start_date())
				.map(date -> DateUtils.format("yyyy-MM-dd", date))
				.orElse("시작일 데이터를 확인할 수 없습니다. 버전의 시작일 확인이 필요합니다");
		String 종료일 = Optional.ofNullable(reqAddEntity.getC_req_end_date())
				.map(date -> DateUtils.format("yyyy-MM-dd", date))
				.orElse("종료일 데이터를 확인할 수 없습니다. 버전의 종료일 확인이 필요합니다");

		String ALM서버_아이디_글자 = Optional.ofNullable(ALM서버_아이디).map(Object::toString).orElse("");
		String ALM_프로젝트_아이디_글자 = Optional.ofNullable(ALM_프로젝트_아이디).map(Object::toString).orElse("");

		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 자동으로 관리되므로,\n" +
				"✔ 이슈를 강제로 삭제시 → 연결된 이슈 수집이 되지 않으므로\n" +
				"✔ 현황 통계에서 배제되어 불이익을 받을 수 있습니다.\n" +
				"✔ 아래 링크에서 요구사항을 내용을 확인 할 수 있습니다.\n\n" +
				"※ 본 이슈 하위로 Sub-Task를 만들어서 개발(업무)을 진행 하시거나, \n" +
				"※ 관련한 이슈를 연결 (LINK) 하시면, 현황 통계에 자동으로 수집됩니다.\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n" +
				"자세한 요구사항 내용 확인 ⇒ http://" + armsDetailUrlConfig.getAddress() + "/arms/detail.html?page=detail&pdService=" + 제품서비스_아이디 +
				"&pdServiceVersion=" + 버전아이디목록_파싱 +
				"&reqAdd=" + 추가된_요구사항의_아이디 + "&jiraServer=" + ALM서버_아이디_글자 + "&jiraProject=" + ALM_프로젝트_아이디_글자 + "\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n\n" +
				"시작 일자 : "+ 시작일 +"\n" +
				"종료 일자 : "+ 종료일 +"\n\n" +
				"※ 『 아래는 입력된 요구사항 내용입니다. 』\n\n\n";

		return 이슈내용;
	}

	private PdServiceEntity 제품데이터조회(String pdServiceId) throws Exception {
		return TreeServiceUtils.getNode(pdService, Long.valueOf(pdServiceId), PdServiceEntity.class);
	}

	private Set<Long> 유지된지라프로젝트찾기(Set<Long> 현재버전, Set<Long> 수정할버전) {
		Set<Long> 유지된버전 = new HashSet<>(현재버전);
		유지된버전.retainAll(수정할버전);
		return 유지된버전;
	}

	private Set<Long> 추가된지라프로젝트찾기(Set<Long> 현재버전, Set<Long> 수정할버전) {
		Set<Long> 추가된버전 = new HashSet<>(수정할버전);
		추가된버전.removeAll(현재버전);
		return 추가된버전;
	}

	private Set<Long> 삭제된지라프로젝트찾기(Set<Long> 현재버전, Set<Long> 수정할버전) {
		Set<Long> 삭제된버전 = new HashSet<>(현재버전);
		삭제된버전.removeAll(수정할버전);
		return 삭제된버전;
	}
}
