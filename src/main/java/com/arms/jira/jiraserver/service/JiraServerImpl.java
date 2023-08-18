/*
 * @author Dongmin.lee
 * @since 2023-03-28
 * @version 23.03.28
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.jira.jiraserver.service;

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissuepriority.service.JiraIssuePriority;
import com.arms.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.jira.jiraissueresolution.service.JiraIssueResolution;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jira.jiraissuetype.service.JiraIssueType;
import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.arms.jira.jiraproject.service.JiraProject;
import com.arms.jira.jiraserver.model.JiraServerEntity;
import com.arms.util.external_communicate.*;
import com.arms.util.external_communicate.dto.cloud.*;
import com.arms.util.external_communicate.dto.onpremise.*;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.Util_TitleChecker;
import lombok.AllArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;


@AllArgsConstructor
@Service("jiraServer")
public class JiraServerImpl extends TreeServiceImpl implements JiraServer{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private 엔진통신기 엔진통신기;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

	@Autowired
	@Qualifier("jiraIssueType")
	private JiraIssueType jiraIssueType;

	@Autowired
	@Qualifier("jiraIssuePriority")
	private JiraIssuePriority jiraIssuePriority;

	@Autowired
	@Qualifier("jiraIssueResolution")
	private JiraIssueResolution jiraIssueResolution;

	@Autowired
	@Qualifier("jiraIssueStatus")
	private JiraIssueStatus jiraIssueStatus;

	@Override
	public List<JiraServerEntity> getNodesWithoutRoot(JiraServerEntity jiraServerEntity) throws Exception {
		jiraServerEntity.setOrder(Order.desc("c_id"));
		Criterion criterion = Restrictions.not(
				// replace "id" below with property name, depending on what you're filtering against
				Restrictions.in("c_id", new Object[] {TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
		);
		jiraServerEntity.getCriterions().add(criterion);
		List<JiraServerEntity> list = this.getChildNode(jiraServerEntity);
		for (JiraServerEntity dto : list) {
			dto.setC_jira_server_contents("force empty");   //명세 초기화
			dto.setC_jira_server_connect_pw("force empty"); //비밀번호 초기화
		}
		return list;
	}

	@Override
	@Transactional
	public JiraServerEntity addJiraServer(JiraServerEntity jiraServerEntity) throws Exception {
		Random rand = new Random();
		String randomConnectId = String.valueOf(rand.nextLong());

		jiraServerEntity.setC_title(Util_TitleChecker.StringReplace(jiraServerEntity.getC_title()));
		jiraServerEntity.setC_jira_server_etc(randomConnectId); // Engine에 보낼 connectId

		JiraServerEntity addedNodeEntity = this.addNode(jiraServerEntity);

		JiraInfoDTO jiraInfoDTO = new JiraInfoDTO();
		jiraInfoDTO.setConnectId(randomConnectId);
		jiraInfoDTO.setUri(addedNodeEntity.getC_jira_server_base_url());
		jiraInfoDTO.setUserId(addedNodeEntity.getC_jira_server_connect_id());
		jiraInfoDTO.setPasswordOrToken(addedNodeEntity.getC_jira_server_connect_pw());
		JiraInfoEntity 등록결과 = 엔진통신기.지라서버_등록(jiraInfoDTO);

		logger.info(등록결과.getConnectId());
		logger.info(등록결과.getSelf());
		logger.info(등록결과.getConnectId());
		logger.info(등록결과.getPasswordOrToken());

		Set<JiraProjectEntity> 지라서버에_붙일_프로젝트_리스트 = new HashSet<>();
		Set<JiraIssueTypeEntity> 지라서버에_붙일_이슈타입_리스트 = new HashSet<>();
		Set<JiraIssuePriorityEntity> 지라서버에_붙일_이슈우선순위_리스트 = new HashSet<>();
		Set<JiraIssueResolutionEntity> 지라서버에_붙일_이슈해결책_리스트 = new HashSet<>();
		Set<JiraIssueStatusEntity> 지라서버에_붙일_이슈상태_리스트 = new HashSet<>();

		if ( 등록결과 != null) {
			//cloud
			if (jiraServerEntity.getC_jira_server_type().equals("cloud")) {

				List<CloudJiraProjectDTO> 클라우드_지라_프로젝트목록 = 엔진통신기.클라우드_지라_프로젝트_리스트_가져오기(등록결과.getConnectId().toString());

				for ( CloudJiraProjectDTO 지라프로젝트 : 클라우드_지라_프로젝트목록 ){
					//validation
					JiraProjectEntity 검색결과 = 지라프로젝트_엔티티_검색결과(지라프로젝트);

					if( 검색결과 == null ){
						지라서버에_붙일_프로젝트_리스트.add(검색된프로젝트_엔티티_매핑(지라프로젝트));
					}else {
						logger.info("이미 존재하는 프로젝트 입니다. -> " + 검색결과.getC_jira_key());
						지라서버에_붙일_프로젝트_리스트.add(검색결과);
					}
				}

				// 지라 이슈타입 목록 - 클라우드는 dto 관련 업데이트 되는대로 변경 예정
				List<CloudJiraIssueTypeDTO> 엔진_지라이슈타입목록 = 엔진통신기.클라우드_지라_이슈_타입_가져오기(jiraInfoDTO.getConnectId().toString());

				for ( CloudJiraIssueTypeDTO 이슈타입 : 엔진_지라이슈타입목록) {

					//validation
					JiraIssueTypeEntity 검색결과 = 지라_이슈타입_엔티티_검색결과(이슈타입);

					if( 검색결과 == null) {
						지라서버에_붙일_이슈타입_리스트.add(검색된_이슈타입_엔티티_매핑(이슈타입));

					} else {
						logger.info("이미 존재하는 프로젝트 입니다. -> " + 검색결과.getC_issue_type_id());
						지라서버에_붙일_이슈타입_리스트.add(검색결과);
					}
				}

				// 지라이슈 우선순위 목록
				//PrioritySearchDTO 클라우드_지라_우선순위_가져오기 = 엔진통신기.클라우드_지라_우선순위_가져오기(등록결과.getConnectId());
				List<Priority> 클라우드_지라_우선순위_목록 = 엔진통신기.클라우드_지라_우선순위_가져오기(등록결과.getConnectId()).getValues();

				for (Priority 이슈우선순위 : 클라우드_지라_우선순위_목록 ) {

					//validation
					JiraIssuePriorityEntity 검색결과 = searchJiraIssuePriorityEntity(이슈우선순위);

					if (검색결과 == null) {
						지라서버에_붙일_이슈우선순위_리스트.add(검색된_이슈우선순위_엔티티_매핑(이슈우선순위));
					} else {
						logger.info("이미 존재하는 이슈우선순위 입니다. -> " + 검색결과.getC_issue_priority_id());
						지라서버에_붙일_이슈우선순위_리스트.add(검색결과);
					}
				}

				// 지라이슈 해결책 목록
				List<Resolution> 클라우드_지라_해결책_목록 = 엔진통신기.클라우드_지라_해결책_가져오기(등록결과.getConnectId()).getValues();

				for (Resolution 이슈해결책 : 클라우드_지라_해결책_목록 ) {

					//validation
					JiraIssueResolutionEntity 검색결과 = searchJiraIssueResolutionEntity(이슈해결책);

					if (검색결과 == null) {
						지라서버에_붙일_이슈해결책_리스트.add(검색된_이슈해결책_엔티티_매핑(이슈해결책));
					} else {
						logger.info("이미 존재하는 이슈해결책 입니다. -> " + 검색결과.getC_issue_resolution_id());
						지라서버에_붙일_이슈해결책_리스트.add(검색결과);
					}
				}
				// 지라이슈 상태 목록
				List<Status> 클라우드_지라_상태_목록 = 엔진통신기.클라우드_지라_상태_가져오기(등록결과.getConnectId()).getValues();

				for (Status 이슈상태 : 클라우드_지라_상태_목록 ) {

					//validation
					JiraIssueStatusEntity 검색결과 = searchJiraIssueStatusEntity(이슈상태);

					if (검색결과 == null) {
						지라서버에_붙일_이슈상태_리스트.add(검색된_이슈상태_엔티티_매핑(이슈상태));
					} else {
						logger.info("이미 존재하는 이슈상태 입니다. -> {}", 검색결과.getC_issue_status_id());
						지라서버에_붙일_이슈상태_리스트.add(검색결과);
					}
				}

			}//.cloud

			// on-premise
			if (jiraServerEntity.getC_jira_server_type().equals("on-premise")) {
				// 지라 프로젝트 목록
				List<OnPremiseJiraProjectDTO> 지라프로젝트목록 = 엔진통신기.지라_프로젝트_리스트_가져오기(등록결과.getConnectId());

				for ( OnPremiseJiraProjectDTO 지라프로젝트 : 지라프로젝트목록 ){
					
					if( 지라프로젝트_엔티티_검색결과(지라프로젝트) == null ){
						지라서버에_붙일_프로젝트_리스트.add(검색된_프로젝트_엔티티_매핑(지라프로젝트));
					}else {
						logger.info("이미 존재하는 프로젝트 입니다. -> " + 지라프로젝트_엔티티_검색결과(지라프로젝트).getC_jira_key());
						지라서버에_붙일_프로젝트_리스트.add(지라프로젝트_엔티티_검색결과(지라프로젝트));
					}

				} //.for Loop

				// 지라 이슈타입 목록
				List<OnPremiseJiraIssueTypeDto> 엔진_지라이슈타입목록 = 엔진통신기.지라_이슈_타입_가져오기(jiraInfoDTO.getConnectId());

				for ( OnPremiseJiraIssueTypeDto 이슈타입 : 엔진_지라이슈타입목록) {
					if( 지라_이슈타입_엔티티_검색결과(이슈타입) == null) {
						지라서버에_붙일_이슈타입_리스트.add(검색된_이슈타입_엔티티_매핑(이슈타입));
					} else {
						logger.info("이미 존재하는 이슈타입 입니다. -> " + 지라_이슈타입_엔티티_검색결과(이슈타입).getC_issue_type_id());
						지라서버에_붙일_이슈타입_리스트.add(지라_이슈타입_엔티티_검색결과(이슈타입));
					}
				}

				// 지라이슈 우선순위 목록
				List<OnPremiseJiraPriorityDTO> 엔진_지라이슈우선순위_목록 = 엔진통신기.지라_이슈_우선순위_가져오기(jiraInfoDTO.getConnectId());

				for ( OnPremiseJiraPriorityDTO 이슈우선순위 : 엔진_지라이슈우선순위_목록) {
					//Validation
					JiraIssuePriorityEntity 검색결과 = searchJiraIssuePriorityEntity(이슈우선순위);

					if (검색결과 == null ) {
						지라서버에_붙일_이슈우선순위_리스트.add(검색된_이슈우선순위_엔티티_매핑(이슈우선순위));
					} else {
						logger.info("이미 존재하는 이슈우선순위 입니다. -> " + 검색결과.getC_issue_priority_id());
						지라서버에_붙일_이슈우선순위_리스트.add(검색결과);
					}
				}

				// 지라이슈 해결책 목록
				List<OnPremiseJiraResolutionDTO> 엔진_지라이슈해결책_목록 = 엔진통신기.지라_이슈_해결책_가져오기(jiraInfoDTO.getConnectId());

				for( OnPremiseJiraResolutionDTO 이슈해결책 : 엔진_지라이슈해결책_목록 ) {
					//Validataion
					JiraIssueResolutionEntity 검색결과 = searchJiraIssueResolutionEntity(이슈해결책);

					if (검색결과 == null ) {
						지라서버에_붙일_이슈해결책_리스트.add(검색된_이슈해결책_엔티티_매핑(이슈해결책));
					} else {
						logger.info("이미 존재하는 이슈해결책 입니다. -> " + 검색결과.getC_issue_resolution_id());
						지라서버에_붙일_이슈해결책_리스트.add(검색결과);
					}
				}
				
				// 지라이슈 상태 목록
				List<OnPremiseJiraStatusDTO> 엔진_지라이슈상태_목록 = 엔진통신기.지라_이슈_상태_가져오기(jiraInfoDTO.getConnectId());

				for ( OnPremiseJiraStatusDTO 이슈상태 : 엔진_지라이슈상태_목록) {
					//Validation
					JiraIssueStatusEntity 검색결과 = searchJiraIssueStatusEntity(이슈상태);

					if (검색결과 == null ) {
						JiraIssueStatusEntity 저장할_지라이슈상태 = 검색된_이슈상태_엔티티_매핑(이슈상태);
						지라서버에_붙일_이슈상태_리스트.add(저장할_지라이슈상태);
					} else {
						logger.info("이미 존재하는 이슈상태 입니다. -> {}", 검색결과.getC_issue_status_id());
						지라서버에_붙일_이슈상태_리스트.add(검색결과);
					}
				}


			}//.on-premise

			if(지라서버에_붙일_프로젝트_리스트.size() > 0){
				addedNodeEntity.setJiraProjectEntities(지라서버에_붙일_프로젝트_리스트);
			}
			if(지라서버에_붙일_이슈타입_리스트.size() > 0) {
				addedNodeEntity.setJiraIssueTypeEntities(지라서버에_붙일_이슈타입_리스트);
			}
			if(지라서버에_붙일_이슈우선순위_리스트.size() > 0) {
				addedNodeEntity.setJiraIssuePriorityEntities(지라서버에_붙일_이슈우선순위_리스트);
			}
			if(지라서버에_붙일_이슈해결책_리스트.size() > 0) {
				addedNodeEntity.setJiraIssueResolutionEntities(지라서버에_붙일_이슈해결책_리스트);
			}
			if(지라서버에_붙일_이슈상태_리스트.size() > 0) {
				addedNodeEntity.setJiraIssueStatusEntities(지라서버에_붙일_이슈상태_리스트);
			}
			this.updateNode(addedNodeEntity);
		}

		return addedNodeEntity;
	}

	private JiraIssueStatusEntity 검색된_이슈상태_엔티티_매핑(OnPremiseJiraStatusDTO 이슈상태) throws Exception {
		JiraIssueStatusEntity 지라이슈상태_저장 = new JiraIssueStatusEntity();
		지라이슈상태_저장.setC_issue_status_id(이슈상태.getId());
		지라이슈상태_저장.setC_issue_status_name(이슈상태.getName());
		지라이슈상태_저장.setC_issue_status_url(이슈상태.getSelf());
		지라이슈상태_저장.setC_issue_status_desc(이슈상태.getDescription());
		지라이슈상태_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈상태_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueStatusEntity 저장할_지라이슈상태 = jiraIssueStatus.addNode(지라이슈상태_저장);
		return 저장할_지라이슈상태;
	}

	private JiraIssueResolutionEntity 검색된_이슈해결책_엔티티_매핑(OnPremiseJiraResolutionDTO 이슈해결책) throws Exception {
		JiraIssueResolutionEntity 지라이슈해결책_저장 = new JiraIssueResolutionEntity();
		지라이슈해결책_저장.setC_issue_resolution_id(이슈해결책.getId());
		지라이슈해결책_저장.setC_issue_resolution_name(이슈해결책.getName());
		지라이슈해결책_저장.setC_issue_resolution_url(이슈해결책.getSelf());
		지라이슈해결책_저장.setC_issue_resolution_desc(이슈해결책.getDescription());
		지라이슈해결책_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈해결책_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueResolutionEntity 저장할_지라이슈해결책 = jiraIssueResolution.addNode(지라이슈해결책_저장);
		return 저장할_지라이슈해결책;
	}

	private JiraIssuePriorityEntity 검색된_이슈우선순위_엔티티_매핑(OnPremiseJiraPriorityDTO 이슈우선순위) throws Exception {
		JiraIssuePriorityEntity 지라이슈우선순위_저장 = new JiraIssuePriorityEntity();
		지라이슈우선순위_저장.setC_issue_priority_id(이슈우선순위.getId());
		지라이슈우선순위_저장.setC_issue_priority_name(이슈우선순위.getName());
		지라이슈우선순위_저장.setC_issue_priority_url(이슈우선순위.getSelf());
		지라이슈우선순위_저장.setC_issue_priority_desc(이슈우선순위.getDescription());
		지라이슈우선순위_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈우선순위_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssuePriorityEntity 저장할_지라이슈우선순위 = jiraIssuePriority.addNode(지라이슈우선순위_저장);
		return 저장할_지라이슈우선순위;
	}

	private JiraIssueTypeEntity 검색된_이슈타입_엔티티_매핑(OnPremiseJiraIssueTypeDto 이슈타입) throws Exception {
		JiraIssueTypeEntity 지라이슈타입_저장 = new JiraIssueTypeEntity();
		지라이슈타입_저장.setC_issue_type_id(이슈타입.getId());
		지라이슈타입_저장.setC_issue_type_name(이슈타입.getName());
		지라이슈타입_저장.setC_issue_type_url(이슈타입.getSelf());
		지라이슈타입_저장.setC_issue_type_desc(이슈타입.getDescription());
		지라이슈타입_저장.setC_desc(이슈타입.getSubtask().toString()); //Boolean
		지라이슈타입_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈타입_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueTypeEntity 저장할_지라이슈타입= jiraIssueType.addNode(지라이슈타입_저장);
		return 저장할_지라이슈타입;
	}

	private JiraProjectEntity 검색된_프로젝트_엔티티_매핑(OnPremiseJiraProjectDTO 지라프로젝트) throws Exception {
		JiraProjectEntity 지라프로젝트_저장 = new JiraProjectEntity();
		지라프로젝트_저장.setC_jira_name(지라프로젝트.getName());
		지라프로젝트_저장.setC_jira_key(지라프로젝트.getKey());
		지라프로젝트_저장.setC_desc(지라프로젝트.getId());
		지라프로젝트_저장.setC_jira_url(지라프로젝트.getSelf());
		지라프로젝트_저장.setC_title(지라프로젝트.getName());
		지라프로젝트_저장.setRef(TreeConstant.First_Node_CID);
		지라프로젝트_저장.setC_type(TreeConstant.Leaf_Node_TYPE);
		JiraProjectEntity 저장된_프로젝트 = jiraProject.addNode(지라프로젝트_저장);
		return 저장된_프로젝트;
	}

	private JiraIssueStatusEntity 검색된_이슈상태_엔티티_매핑(Status 이슈상태) throws Exception {
		JiraIssueStatusEntity 지라이슈상태_저장 = new JiraIssueStatusEntity();
		지라이슈상태_저장.setC_issue_status_id(이슈상태.getId());
		지라이슈상태_저장.setC_issue_status_name(이슈상태.getName());
		지라이슈상태_저장.setC_issue_status_url(이슈상태.getSelf());
		지라이슈상태_저장.setC_issue_status_desc(이슈상태.getDescription());
		지라이슈상태_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈상태_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueStatusEntity 저장할_지라이슈상태 = jiraIssueStatus.addNode(지라이슈상태_저장);
		return 저장할_지라이슈상태;
	}

	private JiraIssueResolutionEntity 검색된_이슈해결책_엔티티_매핑(Resolution 이슈해결책) throws Exception {
		JiraIssueResolutionEntity 지라이슈해결책_저장 = new JiraIssueResolutionEntity();
		지라이슈해결책_저장.setC_issue_resolution_id(이슈해결책.getId());
		지라이슈해결책_저장.setC_issue_resolution_name(이슈해결책.getName());
		지라이슈해결책_저장.setC_issue_resolution_url(이슈해결책.getSelf());
		지라이슈해결책_저장.setC_issue_resolution_desc(이슈해결책.getDescription());
		지라이슈해결책_저장.setC_etc(String.valueOf(이슈해결책.isDefault()));
		지라이슈해결책_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈해결책_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueResolutionEntity 저장할_지라이슈해결책 = jiraIssueResolution.addNode(지라이슈해결책_저장);
		return 저장할_지라이슈해결책;
	}

	private JiraIssuePriorityEntity 검색된_이슈우선순위_엔티티_매핑(Priority 이슈우선순위) throws Exception {
		JiraIssuePriorityEntity 지라이슈우선순위_저장 = new JiraIssuePriorityEntity();
		지라이슈우선순위_저장.setC_issue_priority_id(이슈우선순위.getId());
		지라이슈우선순위_저장.setC_issue_priority_name(이슈우선순위.getName());
		지라이슈우선순위_저장.setC_issue_priority_url(이슈우선순위.getSelf());
		지라이슈우선순위_저장.setC_issue_priority_desc(이슈우선순위.getDescription());
		지라이슈우선순위_저장.setC_etc(String.valueOf(이슈우선순위.isDefault()));
		지라이슈우선순위_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈우선순위_저장.setC_type(TreeConstant.Leaf_Node_TYPE);
		JiraIssuePriorityEntity 저장할_지라이슈우선순위 = jiraIssuePriority.addNode(지라이슈우선순위_저장);
		return 저장할_지라이슈우선순위;
	}

	private JiraIssueTypeEntity 검색된_이슈타입_엔티티_매핑(CloudJiraIssueTypeDTO 이슈타입) throws Exception {
		JiraIssueTypeEntity 지라이슈타입_저장 = new JiraIssueTypeEntity();
		지라이슈타입_저장.setC_issue_type_id(이슈타입.getId().toString());
		지라이슈타입_저장.setC_issue_type_name(이슈타입.getName());
		지라이슈타입_저장.setC_issue_type_url(이슈타입.getSelf().toString());
		지라이슈타입_저장.setC_issue_type_desc(이슈타입.getDescription()); // String description
		지라이슈타입_저장.setC_etc(이슈타입.getUntranslatedName());
		지라이슈타입_저장.setC_desc(이슈타입.getSubtask().toString()); // Boolean
		지라이슈타입_저장.setC_contents(이슈타입.getHierarchyLevel().toString()); // Integer
		지라이슈타입_저장.setRef(TreeConstant.First_Node_CID);
		지라이슈타입_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueTypeEntity 저장할_지라이슈타입= jiraIssueType.addNode(지라이슈타입_저장);
		return 저장할_지라이슈타입;
	}

	private JiraProjectEntity 검색된프로젝트_엔티티_매핑(CloudJiraProjectDTO 지라프로젝트) throws Exception {
		JiraProjectEntity 지라프로젝트_저장 = new JiraProjectEntity();
		지라프로젝트_저장.setC_jira_name(지라프로젝트.getName());
		지라프로젝트_저장.setC_jira_key(지라프로젝트.getKey());
		지라프로젝트_저장.setC_desc(지라프로젝트.getId());
		지라프로젝트_저장.setC_jira_url(지라프로젝트.getSelf());
		지라프로젝트_저장.setC_title(지라프로젝트.getName());
		지라프로젝트_저장.setRef(TreeConstant.First_Node_CID);
		지라프로젝트_저장.setC_type(TreeConstant.Leaf_Node_TYPE);
		JiraProjectEntity 저장된_프로젝트 = jiraProject.addNode(지라프로젝트_저장);
		return 저장된_프로젝트;
	}

	private JiraProjectEntity 지라프로젝트_엔티티_검색결과(OnPremiseJiraProjectDTO 지라프로젝트) throws Exception {
		JiraProjectEntity 지라프로젝트_검색 = new JiraProjectEntity();
		지라프로젝트_검색.setWhere("c_jira_key", 지라프로젝트.getKey());
		JiraProjectEntity 검색결과 = jiraProject.getNode(지라프로젝트_검색);
		return 검색결과;
	}

	private JiraIssueTypeEntity 지라_이슈타입_엔티티_검색결과(OnPremiseJiraIssueTypeDto 이슈타입) throws Exception {
		JiraIssueTypeEntity 지라이슈타입_검색 = new JiraIssueTypeEntity();
		지라이슈타입_검색.setWhere("c_issue_type_id", 이슈타입.getId());
		JiraIssueTypeEntity 검색결과 = jiraIssueType.getNode(지라이슈타입_검색);
		return 검색결과;
	}

	private JiraIssuePriorityEntity searchJiraIssuePriorityEntity(OnPremiseJiraPriorityDTO 이슈우선순위) throws Exception {
		JiraIssuePriorityEntity 지라이슈우선순위_검색 = new JiraIssuePriorityEntity();
		지라이슈우선순위_검색.setWhere("c_issue_priority_id", 이슈우선순위.getId());
		JiraIssuePriorityEntity 검색결과 = jiraIssuePriority.getNode(지라이슈우선순위_검색);
		return 검색결과;
	}

	private JiraIssueStatusEntity searchJiraIssueStatusEntity(OnPremiseJiraStatusDTO 이슈상태) throws Exception {
		JiraIssueStatusEntity 지라이슈상태_검색 = new JiraIssueStatusEntity();
		지라이슈상태_검색.setWhere("c_issue_status_id", 이슈상태.getId());
		JiraIssueStatusEntity 검색결과 = jiraIssueStatus.getNode(지라이슈상태_검색);
		return 검색결과;
	}

	private JiraIssueResolutionEntity searchJiraIssueResolutionEntity(OnPremiseJiraResolutionDTO 이슈해결책) throws Exception {
		JiraIssueResolutionEntity 지라이슈해결책_검색 = new JiraIssueResolutionEntity();
		지라이슈해결책_검색.setWhere("c_issue_resolution_id", 이슈해결책.getId());
		JiraIssueResolutionEntity 검색결과 = jiraIssuePriority.getNode(지라이슈해결책_검색);
		return 검색결과;
	}

	private JiraIssueStatusEntity searchJiraIssueStatusEntity(Status 이슈상태) throws Exception {
		JiraIssueStatusEntity 지라이슈상태_검색 = new JiraIssueStatusEntity();
		지라이슈상태_검색.setWhere("c_issue_status_id", 이슈상태.getId());
		JiraIssueStatusEntity 검색결과 = jiraIssueStatus.getNode(지라이슈상태_검색);
		return 검색결과;
	}

	private JiraIssueResolutionEntity searchJiraIssueResolutionEntity(Resolution 이슈해결책) throws Exception {
		JiraIssueResolutionEntity 지라이슈해결책_검색 = new JiraIssueResolutionEntity();
		지라이슈해결책_검색.setWhere("c_issue_resolution_id", 이슈해결책.getId());
		JiraIssueResolutionEntity 검색결과 = jiraIssuePriority.getNode(지라이슈해결책_검색);
		return 검색결과;
	}

	private JiraIssuePriorityEntity searchJiraIssuePriorityEntity(Priority 이슈우선순위) throws Exception {
		JiraIssuePriorityEntity 지라이슈우선순위_검색 = new JiraIssuePriorityEntity();
		지라이슈우선순위_검색.setWhere("c_issue_priority_id", 이슈우선순위.getId());
		JiraIssuePriorityEntity 검색결과 = jiraIssuePriority.getNode(지라이슈우선순위_검색);
		return 검색결과;
	}

	private JiraIssueTypeEntity 지라_이슈타입_엔티티_검색결과 (CloudJiraIssueTypeDTO 이슈타입) throws Exception {
		JiraIssueTypeEntity 지라이슈타입_검색 = new JiraIssueTypeEntity();
		지라이슈타입_검색.setWhere("c_issue_type_id", 이슈타입.getId().toString());
		JiraIssueTypeEntity 검색결과 = jiraIssueType.getNode(지라이슈타입_검색);
		return 검색결과;
	}

	private JiraProjectEntity 지라프로젝트_엔티티_검색결과(CloudJiraProjectDTO 지라프로젝트) throws Exception {
		JiraProjectEntity 지라프로젝트_검색 = new JiraProjectEntity();
		지라프로젝트_검색.setWhere("c_jira_key", 지라프로젝트.getKey());
		JiraProjectEntity 검색결과 = jiraProject.getNode(지라프로젝트_검색);
		return 검색결과;
	}
}