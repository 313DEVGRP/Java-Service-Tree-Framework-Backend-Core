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
import com.arms.util.external_communicate.dto.*;
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
import org.springframework.security.core.parameters.P;
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
		String randomConnectId = String.valueOf(Math.abs(rand.nextLong()));

		jiraServerEntity.setC_title(Util_TitleChecker.StringReplace(jiraServerEntity.getC_title()));
		jiraServerEntity.setC_jira_server_etc(randomConnectId); // 엔진과 통신할 connectId

		JiraServerEntity addedNodeEntity = this.addNode(jiraServerEntity);

		JiraInfoDTO jiraInfoDTO = new JiraInfoDTO();
		jiraInfoDTO.setConnectId(randomConnectId);
		jiraInfoDTO.setType(addedNodeEntity.getC_jira_server_type());
		jiraInfoDTO.setUri(addedNodeEntity.getC_jira_server_base_url());
		jiraInfoDTO.setUserId(addedNodeEntity.getC_jira_server_connect_id());
		jiraInfoDTO.setPasswordOrToken(addedNodeEntity.getC_jira_server_connect_pw());
		JiraInfoEntity 등록결과 = 엔진통신기.지라서버_등록(jiraInfoDTO);

		logger.info(등록결과.getConnectId());
		logger.info(등록결과.getType());
		logger.info(등록결과.getSelf());
		logger.info(등록결과.getConnectId());
		logger.info(등록결과.getPasswordOrToken());

		if( 등록결과 != null) {

			int 연결할_프로젝트_수 = 0;
			Set<JiraProjectEntity> 지라서버에_붙일_프로젝트_리스트 = new HashSet<>();
			연결할_프로젝트_수 = 지라서버_엔티티에_지라_프로젝트들을_연결(지라서버에_붙일_프로젝트_리스트, 엔진통신기.지라_프로젝트_목록_가져오기(등록결과.getConnectId()), 등록결과.getType());
			if (연결할_프로젝트_수 > 0) {
				addedNodeEntity.setJiraProjectEntities(지라서버에_붙일_프로젝트_리스트);
			}

			int 연결할_이슈_유형_수 = 0;
			Set<JiraIssueTypeEntity> 지라서버에_붙일_이슈_유형_리스트 = new HashSet<>();
			연결할_이슈_유형_수 = 지라서버_엔티티에_지라_이슈_유형들을_연결(지라서버에_붙일_이슈_유형_리스트, 엔진통신기.지라_이슈_유형_가져오기(등록결과.getConnectId()), 등록결과.getType());
			if (연결할_이슈_유형_수 > 0) {
				addedNodeEntity.setJiraIssueTypeEntities(지라서버에_붙일_이슈_유형_리스트);
			}

			int 연결할_이슈_우선순위_수 = 0;
			Set<JiraIssuePriorityEntity> 지라서버에_붙일_이슈_우선순위_리스트 = new HashSet<>();
			연결할_이슈_우선순위_수 = 지라서버_엔티티에_지라_이슈_우선순위들을_연결(지라서버에_붙일_이슈_우선순위_리스트, 엔진통신기.지라_이슈_우선순위_가져오기(등록결과.getConnectId()), 등록결과.getType());
			if (연결할_이슈_우선순위_수 > 0) {
				addedNodeEntity.setJiraIssuePriorityEntities(지라서버에_붙일_이슈_우선순위_리스트);
			}

			int 연결할_이슈_해결책_수 = 0;
			Set<JiraIssueResolutionEntity> 지라서버에_붙일_이슈_해결책_리스트 = new HashSet<>();
			연결할_이슈_해결책_수 = 지라서버_엔티티에_지라_이슈_해결책들을_연결(지라서버에_붙일_이슈_해결책_리스트, 엔진통신기.지라_이슈_해결책_가져오기(등록결과.getConnectId()), 등록결과.getType());
			if (연결할_이슈_해결책_수 > 0) {
				addedNodeEntity.setJiraIssueResolutionEntities(지라서버에_붙일_이슈_해결책_리스트);
			}

			int 연결할_이슈_상태_수 = 0;
			Set<JiraIssueStatusEntity> 지라서버에_붙일_이슈_상태_리스트 = new HashSet<>();
			연결할_이슈_상태_수 = 지라서버_엔티티에_지라_이슈_상태들을_연결(지라서버에_붙일_이슈_상태_리스트, 엔진통신기.지라_이슈_상태_가져오기(등록결과.getConnectId()), 등록결과.getType());
			if (연결할_이슈_상태_수 > 0) {
				addedNodeEntity.setJiraIssueStatusEntities(지라서버에_붙일_이슈_상태_리스트);
			} else {
				logger.info("연결할_이슈_상태_수가 0입니다!!!!!!!!!!!!!!!!!!");
			}

			this.updateNode(addedNodeEntity);
		}

		return addedNodeEntity;
	}

	private int 지라서버_엔티티에_지라_이슈_상태들을_연결(Set<JiraIssueStatusEntity> 지라서버에_붙일_이슈_상태_리스트, List<지라_이슈_상태_데이터_전송_객체> 가져온_이슈_상태_목록, String 서버유형) throws Exception {

		for (지라_이슈_상태_데이터_전송_객체 가져온_이슈_상태 : 가져온_이슈_상태_목록) {
			JiraIssueStatusEntity 검색된_이슈_상태 = 지라_이슈_상태_엔티티_검색(가져온_이슈_상태);
			if (검색된_이슈_상태 == null) {
				지라서버에_붙일_이슈_상태_리스트.add(미등록_이슈_상태_저장_및_저장된_엔티티(서버유형, 가져온_이슈_상태));
			} else {
				logger.info("이미 존재하는 이슈 상태 입니다. -> " + 검색된_이슈_상태.getC_issue_status_name());
				기등록_이슈_상태_갱신_및_갱신된_엔티티(서버유형, 검색된_이슈_상태, 가져온_이슈_상태);
			}
		}
		return 지라서버에_붙일_이슈_상태_리스트.size();
	}

	private JiraIssueStatusEntity 기등록_이슈_상태_갱신_및_갱신된_엔티티(String 서버유형, JiraIssueStatusEntity 갱신할_대상_이슈_상태, 지라_이슈_상태_데이터_전송_객체 가져온_이슈_상태) throws Exception {
		JiraIssueStatusEntity 갱신결과_이슈_상태_엔티티 = 갱신할_대상_이슈_상태;
		//공통
		갱신할_대상_이슈_상태.setC_issue_status_name(가져온_이슈_상태.getName());
		갱신할_대상_이슈_상태.setC_issue_status_desc(가져온_이슈_상태.getDescription());
		jiraIssueStatus.updateNode(갱신할_대상_이슈_상태);
		return 갱신결과_이슈_상태_엔티티;
	}


	private JiraIssueStatusEntity 미등록_이슈_상태_저장_및_저장된_엔티티(String 서버유형, 지라_이슈_상태_데이터_전송_객체 이슈_상태) throws Exception {
		logger.info("저장할 이슈 아이디,이름 입니다. -> {} , {}" + 이슈_상태.getId(), 이슈_상태.getName());
		JiraIssueStatusEntity 저장할_이슈_상태 = new JiraIssueStatusEntity();
		//공통
		저장할_이슈_상태.setC_issue_status_id(이슈_상태.getId());
		저장할_이슈_상태.setC_issue_status_name(이슈_상태.getName());
		저장할_이슈_상태.setC_issue_status_url(이슈_상태.getSelf());
		저장할_이슈_상태.setC_issue_status_desc(이슈_상태.getDescription());
		저장할_이슈_상태.setRef(TreeConstant.First_Node_CID);
		저장할_이슈_상태.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueStatusEntity 저장된_지라이슈상태 = jiraIssueStatus.addNode(저장할_이슈_상태);
		return 저장된_지라이슈상태;
	}

	private int 지라서버_엔티티에_지라_이슈_해결책들을_연결(Set<JiraIssueResolutionEntity> 지라서버에_붙일_이슈_해결책_리스트, List<지라_이슈_해결책_데이터_전송_객체> 가져온_이슈_해결책_목록, String 서버유형) throws Exception {
		for (지라_이슈_해결책_데이터_전송_객체 가져온_이슈_해결책 : 가져온_이슈_해결책_목록) {
			JiraIssueResolutionEntity 검색된_이슈_해결책 = 지라_이슈_해결책_엔티티_검색(가져온_이슈_해결책);
			if (검색된_이슈_해결책 == null) {
				지라서버에_붙일_이슈_해결책_리스트.add(미등록_이슈_해결책_저장_및_저장된_엔티티(서버유형, 가져온_이슈_해결책));
			} else {
				logger.info("이미 존재하는 이슈 해결책 입니다. -> " + 검색된_이슈_해결책.getC_issue_resolution_name());
				기등록_이슈_해결책_갱신_및_갱신된_엔티티(서버유형, 검색된_이슈_해결책, 가져온_이슈_해결책);
			}
		}

		return 지라서버에_붙일_이슈_해결책_리스트.size();
	}

	private JiraIssueResolutionEntity 기등록_이슈_해결책_갱신_및_갱신된_엔티티(String 서버유형, JiraIssueResolutionEntity 갱신할_대상_이슈_해결책, 지라_이슈_해결책_데이터_전송_객체 가져온_이슈_해결책) throws Exception {
		JiraIssueResolutionEntity 갱신결과_이슈_해결책_엔티티 = 갱신할_대상_이슈_해결책;
		갱신할_대상_이슈_해결책.setC_issue_resolution_name(가져온_이슈_해결책.getName());
		갱신할_대상_이슈_해결책.setC_issue_resolution_desc(가져온_이슈_해결책.getDescription());
		갱신할_대상_이슈_해결책.setC_etc(String.valueOf(가져온_이슈_해결책.isDefault()));

		jiraIssueResolution.updateNode(갱신할_대상_이슈_해결책);
		return 갱신결과_이슈_해결책_엔티티;
	}

	private JiraIssueResolutionEntity 미등록_이슈_해결책_저장_및_저장된_엔티티(String 서버유형, 지라_이슈_해결책_데이터_전송_객체 이슈_해결책) throws Exception{
		JiraIssueResolutionEntity 저장할_이슈_해결책 = new JiraIssueResolutionEntity();
		//공통
		저장할_이슈_해결책.setC_issue_resolution_id(이슈_해결책.getId());
		저장할_이슈_해결책.setC_issue_resolution_name(이슈_해결책.getName());
		저장할_이슈_해결책.setC_issue_resolution_url(이슈_해결책.getSelf());
		저장할_이슈_해결책.setC_issue_resolution_desc(이슈_해결책.getDescription());
		저장할_이슈_해결책.setC_etc(String.valueOf(이슈_해결책.isDefault()));
		저장할_이슈_해결책.setRef(TreeConstant.First_Node_CID);
		저장할_이슈_해결책.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraIssueResolutionEntity 저장된_지라이슈해결책 = jiraIssueResolution.addNode(저장할_이슈_해결책);
		return 저장된_지라이슈해결책;
	}


	private int 지라서버_엔티티에_지라_이슈_우선순위들을_연결(Set<JiraIssuePriorityEntity> 지라서버에_붙일_이슈_우선순위_리스트, List<지라_이슈_우선순위_데이터_전송_객체> 가져온_이슈_우선순위_목록, String 서버유형) throws Exception {
		for (지라_이슈_우선순위_데이터_전송_객체 가져온_이슈_우선순위 : 가져온_이슈_우선순위_목록) {
			JiraIssuePriorityEntity 검색된_이슈_우선순위 = 지라_이슈_우선순위_엔티티_검색(가져온_이슈_우선순위);
			if (검색된_이슈_우선순위 == null) {
				지라서버에_붙일_이슈_우선순위_리스트.add(미등록_이슈_우선순위_저장_및_저장된_엔티티(서버유형, 가져온_이슈_우선순위));
			} else {
				logger.info("이미 존재하는 이슈우선순위 입니다. -> " + 검색된_이슈_우선순위.getC_issue_priority_name());
				기등록_이슈_우선순위_갱신_및_갱신된_엔티티(서버유형, 검색된_이슈_우선순위, 가져온_이슈_우선순위);
			}
		}
		return 지라서버에_붙일_이슈_우선순위_리스트.size();
	}

	private JiraIssuePriorityEntity 기등록_이슈_우선순위_갱신_및_갱신된_엔티티(String 서버유형, JiraIssuePriorityEntity 갱신할_대상_이슈_우선순위, 지라_이슈_우선순위_데이터_전송_객체 가져온_이슈_우선순위) throws Exception {
		JiraIssuePriorityEntity 갱신결과_이슈_우선순위_엔티티= 갱신할_대상_이슈_우선순위;
		갱신할_대상_이슈_우선순위.setC_issue_priority_name(가져온_이슈_우선순위.getName());
		갱신할_대상_이슈_우선순위.setC_issue_priority_desc(가져온_이슈_우선순위.getDescription());
		if( 서버유형.equals("클라우드") ) {
			갱신할_대상_이슈_우선순위.setC_etc(String.valueOf(가져온_이슈_우선순위.isDefault()));
		}
		jiraIssuePriority.updateNode(갱신할_대상_이슈_우선순위);
		return 갱신결과_이슈_우선순위_엔티티;
	}

	private JiraIssuePriorityEntity 미등록_이슈_우선순위_저장_및_저장된_엔티티(String 서버유형, 지라_이슈_우선순위_데이터_전송_객체 이슈_우선순위) throws Exception {
		JiraIssuePriorityEntity 저장할_이슈_우선순위 = new JiraIssuePriorityEntity();
		//공통
		저장할_이슈_우선순위.setC_issue_priority_id(이슈_우선순위.getId());
		저장할_이슈_우선순위.setC_issue_priority_name(이슈_우선순위.getName());
		저장할_이슈_우선순위.setC_issue_priority_url(이슈_우선순위.getSelf());
		저장할_이슈_우선순위.setC_issue_priority_desc(이슈_우선순위.getDescription());
		저장할_이슈_우선순위.setRef(TreeConstant.First_Node_CID);
		저장할_이슈_우선순위.setC_type(TreeConstant.Leaf_Node_TYPE);
		if( 서버유형.equals("클라우드")) {
			저장할_이슈_우선순위.setC_etc(String.valueOf(이슈_우선순위.isDefault()));
		}
		JiraIssuePriorityEntity 저장된_지라_이슈_우선순위 = jiraIssuePriority.addNode(저장할_이슈_우선순위);
		return 저장된_지라_이슈_우선순위;
	}


	private int 지라서버_엔티티에_지라_이슈_유형들을_연결(Set<JiraIssueTypeEntity> 지라서버에_붙일_이슈_유형_리스트, List<지라_이슈_유형_데이터_전송_객체> 가져온_이슈_유형_목록, String 서버유형) throws Exception {
		for (지라_이슈_유형_데이터_전송_객체 가져온_이슈_유형 : 가져온_이슈_유형_목록) {
			JiraIssueTypeEntity 검색된_이슈_유형 = 지라_이슈_유형_엔티티_검색(가져온_이슈_유형);
			if (검색된_이슈_유형 == null) {
				지라서버에_붙일_이슈_유형_리스트.add(미등록_이슈_유형_저장_및_저장된_엔티티(서버유형, 가져온_이슈_유형));
			} else {
				logger.info("이미 존재하는 이슈타입 입니다. -> " + 검색된_이슈_유형.getC_issue_type_name());
				기등록_이슈_유형_갱신_및_갱신된_엔티티(서버유형, 검색된_이슈_유형, 가져온_이슈_유형);
			}
		}

		return 지라서버에_붙일_이슈_유형_리스트.size();
	}

	private JiraIssueTypeEntity 기등록_이슈_유형_갱신_및_갱신된_엔티티(String 서버유형, JiraIssueTypeEntity 갱신할_대상_이슈_유형, 지라_이슈_유형_데이터_전송_객체 가져온_이슈_유형) throws Exception {
		JiraIssueTypeEntity 갱신결과_이슈_유형_엔티티 = 갱신할_대상_이슈_유형;
		갱신할_대상_이슈_유형.setC_issue_type_desc(가져온_이슈_유형.getDescription());
		갱신할_대상_이슈_유형.setC_issue_type_name(가져온_이슈_유형.getName());
		갱신할_대상_이슈_유형.setC_desc(가져온_이슈_유형.getSubtask().toString());
		if ( 서버유형.equals("클라우드") ) {
			갱신할_대상_이슈_유형.setC_etc(가져온_이슈_유형.getUntranslatedName());
			갱신할_대상_이슈_유형.setC_contents(가져온_이슈_유형.getHierarchyLevel().toString());
		}
		jiraIssueType.updateNode(갱신할_대상_이슈_유형);
		return 갱신결과_이슈_유형_엔티티;
	}

	private JiraIssueTypeEntity 미등록_이슈_유형_저장_및_저장된_엔티티(String 서버유형, 지라_이슈_유형_데이터_전송_객체 이슈_유형) throws Exception {
		JiraIssueTypeEntity 저장할_이슈_유형 = new JiraIssueTypeEntity();
		// 공통
		저장할_이슈_유형.setC_issue_type_id(이슈_유형.getId());
		저장할_이슈_유형.setC_issue_type_name(이슈_유형.getName());
		저장할_이슈_유형.setC_issue_type_url(이슈_유형.getSelf());
		저장할_이슈_유형.setC_issue_type_desc(이슈_유형.getDescription());
		저장할_이슈_유형.setC_desc(이슈_유형.getSubtask().toString()); //Boolean
		저장할_이슈_유형.setRef(TreeConstant.First_Node_CID);
		저장할_이슈_유형.setC_type(TreeConstant.Leaf_Node_TYPE);
		if ( 서버유형.equals("클라우드") ) {
			저장할_이슈_유형.setC_etc(이슈_유형.getUntranslatedName());
			저장할_이슈_유형.setC_contents(이슈_유형.getHierarchyLevel().toString()); //Integer
		}
		JiraIssueTypeEntity 저장된_이슈_유형 = jiraIssueType.addNode(저장할_이슈_유형);
		return 저장된_이슈_유형;
	}

	private int 지라서버_엔티티에_지라_프로젝트들을_연결(Set<JiraProjectEntity> 지라서버에_붙일_프로젝트_목록,
									   List<지라_프로젝트_데이터_전송_객체> 가져온_지라_프로젝트_목록,
									   String 서버유형) throws Exception {
		for (지라_프로젝트_데이터_전송_객체 가져온_지라_프로젝트 : 가져온_지라_프로젝트_목록) {
			JiraProjectEntity 검색된_지라_프로젝트 = 지라_프로젝트_엔티티_검색(가져온_지라_프로젝트);// 검색은 self, 서버유형 안탐
			if( 검색된_지라_프로젝트 == null ) {
				지라서버에_붙일_프로젝트_목록.add(미등록_프로젝트_저장_및_저장된_엔티티(서버유형, 가져온_지라_프로젝트));
			} else {
				logger.info("이미 존재하는 프로젝트 입니다. -> " + 검색된_지라_프로젝트.getC_jira_key());
				기등록_프로젝트_갱신_및_갱신된_엔티티(서버유형, 검색된_지라_프로젝트, 가져온_지라_프로젝트);
			}
		}
		return 지라서버에_붙일_프로젝트_목록.size();
	}

	private JiraProjectEntity 기등록_프로젝트_갱신_및_갱신된_엔티티(String 서버유형, JiraProjectEntity 갱신_대상_프로젝트_엔티티,
													지라_프로젝트_데이터_전송_객체 가져온_지라_프로젝트) throws Exception {
		JiraProjectEntity 갱신결과_엔티티 = 갱신_대상_프로젝트_엔티티;
		갱신_대상_프로젝트_엔티티.setC_jira_name(가져온_지라_프로젝트.getName());
		jiraProject.updateNode(갱신_대상_프로젝트_엔티티);
		return 갱신결과_엔티티;
	}

	private JiraProjectEntity 미등록_프로젝트_저장_및_저장된_엔티티(String 서버유형, 지라_프로젝트_데이터_전송_객체 지라_프로젝트) throws Exception {
		JiraProjectEntity 지라프로젝트_저장 = new JiraProjectEntity();
		//서버 유형 둘다 동일하게 사용중
		지라프로젝트_저장.setC_jira_name(지라_프로젝트.getName());
		지라프로젝트_저장.setC_jira_key(지라_프로젝트.getKey());
		지라프로젝트_저장.setC_desc(지라_프로젝트.getId());
		지라프로젝트_저장.setC_jira_url(지라_프로젝트.getSelf());
		지라프로젝트_저장.setC_title(지라_프로젝트.getName());
		지라프로젝트_저장.setRef(TreeConstant.First_Node_CID);
		지라프로젝트_저장.setC_type(TreeConstant.Leaf_Node_TYPE);

		JiraProjectEntity 저장된_프로젝트 = jiraProject.addNode(지라프로젝트_저장);
		return 저장된_프로젝트;
	}

	private JiraProjectEntity 지라_프로젝트_엔티티_검색(지라_프로젝트_데이터_전송_객체 검색할_지라_프로젝트) throws Exception{
		JiraProjectEntity 지라_프로젝트_검색 = new JiraProjectEntity();
		지라_프로젝트_검색.setWhere("c_jira_url", 검색할_지라_프로젝트.getSelf());
		return jiraProject.getNode(지라_프로젝트_검색);
	}

	private JiraIssueTypeEntity 지라_이슈_유형_엔티티_검색 (지라_이슈_유형_데이터_전송_객체 검색할_지라_이슈_유형) throws Exception {
		JiraIssueTypeEntity 지라_이슈타입_검색 = new JiraIssueTypeEntity();
		지라_이슈타입_검색.setWhere("c_issue_type_url", 검색할_지라_이슈_유형.getSelf());
		return jiraIssueType.getNode(지라_이슈타입_검색);
	}

	private JiraIssuePriorityEntity 지라_이슈_우선순위_엔티티_검색(지라_이슈_우선순위_데이터_전송_객체 검색할_지라_이슈_우선순위) throws Exception {
		JiraIssuePriorityEntity 지라_이슈_우선순위_검색 = new JiraIssuePriorityEntity();
		지라_이슈_우선순위_검색.setWhere("c_issue_priority_url", 검색할_지라_이슈_우선순위.getSelf());
		return jiraIssuePriority.getNode(지라_이슈_우선순위_검색);
	}

	private JiraIssueResolutionEntity 지라_이슈_해결책_엔티티_검색(지라_이슈_해결책_데이터_전송_객체 검색할_지라_이슈_해결책) throws Exception{
		JiraIssueResolutionEntity 지라_이슈_해결책_검색 = new JiraIssueResolutionEntity();
		지라_이슈_해결책_검색.setWhere("c_issue_resolution_url", 검색할_지라_이슈_해결책.getSelf());
		return jiraIssueResolution.getNode(지라_이슈_해결책_검색);
	}

	private JiraIssueStatusEntity 지라_이슈_상태_엔티티_검색(지라_이슈_상태_데이터_전송_객체 검색할_이슈_상태) throws Exception {
		JiraIssueStatusEntity 지라_이슈_상태_검색 = new JiraIssueStatusEntity();
		지라_이슈_상태_검색.setWhere("c_issue_status_url", 검색할_이슈_상태.getSelf());
		return jiraIssueStatus.getNode(지라_이슈_상태_검색);
	}


}