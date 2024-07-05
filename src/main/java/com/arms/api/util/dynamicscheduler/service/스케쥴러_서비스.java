/*
 * @author Dongmin.lee
 * @since 2023-03-20
 * @version 23.03.20
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.util.dynamicscheduler.service;

import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.api.jira.jiraserver_pure.service.JiraServerPure;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.requirement.reqstatus.model.CRUDType;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.api.util.communicate.internal.InternalService;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service("스케쥴러")
@Slf4j
public class 스케쥴러_서비스 extends TreeServiceImpl implements 스케쥴러 {

    private final PdService pdService;

    private final ReqStatus reqStatus;

    private final JiraServerPure jiraServerPure;

    private final EngineService engineService;

    private final InternalService internalService;

    protected final Chat chat;

    protected final ModelMapper modelMapper;

	@Async
	@Override
	public String 각_제품서비스_별_요구사항이슈_조회_및_ES저장() throws Exception {
		return this.스케쥴러_타입별_요구사항_이슈_처리("요구사항_이슈_검색엔진_벌크_저장");
	}

	@Async
	@Override
	public String 증분이슈_검색엔진_벌크_저장() throws Exception {
		return this.스케쥴러_타입별_요구사항_이슈_처리("증분_요구사항_이슈_검색엔진_벌크_저장");
	}

	@Async
	@Override
	public String 각_제품서비스_별_요구사항_Status_업데이트_From_ES() throws Exception {
		return this.스케쥴러_타입별_요구사항_이슈_처리("FROM_검색엔진_이슈데이터_TO_요구사항_REQSTATUS_동기화");
	}

	@Async
	@Override
	public String 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성() throws Exception {
		return this.스케쥴러_타입별_요구사항_이슈_처리("생성_실패_요구사항_스케줄러");
	}

	private String 스케쥴러_타입별_요구사항_이슈_처리(String 스케쥴러_타입) throws Exception {

		log.info("[ 스케쥴러_서비스 :: 스케쥴러_타입별_요구사항_이슈_처리 ] :: " + 스케쥴러_타입);

		// 제품 조회
		PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
		List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

		for (PdServiceEntity 제품서비스 : 제품서비스_리스트) {
			Long 제품서비스_아이디 = 제품서비스.getC_id();

			ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
			List<ReqStatusEntity> 결과 = internalService.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

			if (결과 == null) {
				log.info("[ " + 스케쥴러_타입 + "] :: " + 제품서비스.getC_title() + "제품의 요구사항이 존재하지 않습니다.");
				continue;
			}

			if (스케쥴러_타입.equals("생성_실패_요구사항_스케줄러")) {
				List<ReqStatusEntity> filteredIssues = Optional.ofNullable(결과)
						.orElse(Collections.emptyList())
						.stream()
						.filter(요구사항_이슈 -> 요구사항_이슈.getC_issue_delete_date() == null)
						.filter(요구사항_이슈 -> 요구사항_이슈.getC_etc() != null && !StringUtils.equals(CRUDType.완료.getType(), 요구사항_이슈.getC_etc()))
						.collect(Collectors.toList());

				filteredIssues.forEach(요구사항_이슈 -> reqStatus.ALM서버_요구사항_처리_및_REQSTATUS_업데이트(요구사항_이슈, 제품서비스_아이디));
			}
			else {
				for (ReqStatusEntity 요구사항_이슈_엔티티 : 결과) {
					JiraServerPureEntity 지라서버_검색 = new JiraServerPureEntity();
					지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
					JiraServerPureEntity 지라서버 = jiraServerPure.getNode(지라서버_검색);

					if (지라서버 == null) {
						log.info("지라서버가 조회되지 않습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());
						continue;
					}

					if (요구사항_이슈_엔티티.getC_issue_key() == null) {
						log.info("해당 요구사항은 ALM 서버에 생성되지 않은 요구사항입니다. 확인이 필요합니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());
						continue;
					}
					else if (요구사항_이슈_엔티티.getC_issue_delete_date() != null) {
						log.info("해당 요구사항은 ARMS에서 삭제된 요구사항입니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());
						continue;
					}

					if (스케쥴러_타입.equals("FROM_검색엔진_이슈데이터_TO_요구사항_REQSTATUS_동기화")) {
						this.FROM_검색엔진_이슈데이터_TO_요구사항_REQSTATUS_동기화(요구사항_이슈_엔티티, 지라서버, 제품서비스_아이디);
					}
					else {
						this.스케쥴러_타입별_요구사항_이슈_ES저장(요구사항_이슈_엔티티, 지라서버, 스케쥴러_타입);
					}
				}
			}
		}

		return "success";
	}

	private void FROM_검색엔진_이슈데이터_TO_요구사항_REQSTATUS_동기화(ReqStatusEntity 요구사항_이슈_엔티티, JiraServerPureEntity 지라서버, Long 제품서비스_아이디) {
		log.info("엔진통신기 = " + 지라서버.getC_jira_server_etc());
		log.info("엔진통신기 = " + 요구사항_이슈_엔티티.getC_jira_project_key());
		log.info("엔진통신기 = " + 요구사항_이슈_엔티티.getC_issue_key());

		지라이슈 ES_지라이슈 = engineService.요구사항이슈_조회(
				Long.parseLong(지라서버.getC_jira_server_etc()),
				요구사항_이슈_엔티티.getC_jira_project_key(),
				요구사항_이슈_엔티티.getC_issue_key()
		);

		if (ES_지라이슈 == null) {
			log.info("지라이슈가 조회되지 않습니다. 조회키 = " + 요구사항_이슈_엔티티.getC_jira_project_key());
		}
		else {
			if (ES_지라이슈.getKey() == null) {
				log.info("ES_지라이슈 = null 이며, 확인이 필요합니다.");
			}
			else if (StringUtils.equals(ES_지라이슈.getStatus().getName(), "해당 요구사항은 지라서버에서 조회가 되지 않는 상태입니다.")) {
				log.info("해당 요구사항은 지라서버에서 조회가 되지 않는 상태입니다. ES_지라이슈 = " + ES_지라이슈.getKey());
			}
			else {
				log.info("ES_지라이슈 = " + ES_지라이슈.getId());
				log.info("ES_지라이슈 = " + ES_지라이슈.getKey());

				if (ES_지라이슈.getAssignee() != null && ES_지라이슈.getAssignee().getDisplayName() != null) {
					log.info("ES_지라이슈 담당자 이름 = " + ES_지라이슈.getAssignee().getDisplayName());
					요구사항_이슈_엔티티.setC_issue_assignee(ES_지라이슈.getAssignee().getDisplayName());
				}

				if (ES_지라이슈.getPriority() != null && ES_지라이슈.getPriority().getName() != null) {
					log.info("ES_지라이슈 우선순위 = " + ES_지라이슈.getPriority().getName());
					요구사항_이슈_엔티티.setC_issue_priority_link(Long.valueOf(ES_지라이슈.getPriority().getId()));
					요구사항_이슈_엔티티.setC_issue_priority_name(ES_지라이슈.getPriority().getName());
				}

				if (ES_지라이슈.getReporter() != null && ES_지라이슈.getReporter().getDisplayName() != null) {
					log.info("ES_지라이슈 보고자 = " + ES_지라이슈.getReporter().getDisplayName());
					요구사항_이슈_엔티티.setC_issue_reporter(ES_지라이슈.getReporter().getDisplayName());
				}

				if (ES_지라이슈.getResolution() != null && ES_지라이슈.getResolution().getName() != null) {
					log.info("ES_지라이슈 해결책 = " + ES_지라이슈.getResolution().getName());
					요구사항_이슈_엔티티.setC_issue_resolution_name(ES_지라이슈.getResolution().getName());
				}

				if (ES_지라이슈.getStatus() != null && ES_지라이슈.getStatus().getName() != null) {
					log.info("ES_지라이슈 상태 = " + ES_지라이슈.getStatus().getName());
					요구사항_이슈_엔티티.setC_issue_status_link(Long.valueOf(ES_지라이슈.getStatus().getId()));
					요구사항_이슈_엔티티.setC_issue_status_name(ES_지라이슈.getStatus().getName());
				}

				ReqStatusDTO statusDTO = modelMapper.map(요구사항_이슈_엔티티, ReqStatusDTO.class);
				ResponseEntity<?> 결과 = internalService.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, statusDTO);

				if (결과.getStatusCode().is2xxSuccessful()) {
					String 성공메세지 = " [ " + statusDTO.getC_jira_server_name() + " :: " + statusDTO.getC_jira_project_name() + " ] :: " +
							statusDTO.getC_issue_key() + " ALM 요구사항 이슈의 데이터와 동기화되었습니다.";
					chat.sendMessageByEngine(성공메세지);
				}
				else {
					log.error("T_ARMS_REQSTATUS_" + 제품서비스_아이디 + " :: 수정 오류 :: " + statusDTO.toString());
				}
			}
		}
	}

	private void 스케쥴러_타입별_요구사항_이슈_ES저장(ReqStatusEntity 요구사항_이슈_엔티티, JiraServerPureEntity 지라서버, String 스케쥴러_타입) {
		String 버전_목록_문자열 = 요구사항_이슈_엔티티.getC_req_pdservice_versionset_link();
		if (버전_목록_문자열 == null || 버전_목록_문자열.isEmpty()) {
			log.info("버전_목록_문자열이 없습니다. 진행중인 ReqStatusEntity c_id => {} 의 버전_목록이 없습니다."
					, 요구사항_이슈_엔티티.getC_id());
			log.info("[{}] {}", 지라서버.getC_jira_server_name(), 요구사항_이슈_엔티티.getC_issue_key());
			return;
		}

		Long[] 버전_아이디_목록_배열 = Arrays.stream(버전_목록_문자열.split("[\\[\\],\"]"))
				.filter(s -> !s.isEmpty())
				.map(Long::valueOf)
				.toArray(Long[]::new);

		if (스케쥴러_타입.equals("요구사항_이슈_검색엔진_벌크_저장")) {
			engineService.이슈_검색엔진_벌크_저장(
					Long.parseLong(지라서버.getC_jira_server_etc()),
					요구사항_이슈_엔티티.getC_issue_key(),
					요구사항_이슈_엔티티.getC_pdservice_link(),
					버전_아이디_목록_배열,
					요구사항_이슈_엔티티.getC_req_link(),
					요구사항_이슈_엔티티.getC_jira_project_key()
			);
		}
		else if (스케쥴러_타입.equals("증분_요구사항_이슈_검색엔진_벌크_저장")) {
			engineService.증분이슈_검색엔진_벌크_저장(
					Long.parseLong(지라서버.getC_jira_server_etc()),
					요구사항_이슈_엔티티.getC_issue_key(),
					요구사항_이슈_엔티티.getC_pdservice_link(),
					버전_아이디_목록_배열,
					요구사항_이슈_엔티티.getC_req_link(),
					요구사항_이슈_엔티티.getC_jira_project_key()
			);
		}

		log.info("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key() + " :: ES 저장");
	}
}