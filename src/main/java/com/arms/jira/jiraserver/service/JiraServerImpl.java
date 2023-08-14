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

import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.arms.jira.jiraproject.service.JiraProject;
import com.arms.jira.jiraserver.model.JiraServerEntity;
import com.arms.to_engine.JiraInfoDTO;
import com.arms.to_engine.JiraInfoEntity;
import com.arms.to_engine.OnPremiseJiraProjectDTO;
import com.arms.to_engine.엔진통신기;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.Util_TitleChecker;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@AllArgsConstructor
@Service("jiraServer")
public class JiraServerImpl extends TreeServiceImpl implements JiraServer{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private 엔진통신기 엔진통신기;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

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

		jiraServerEntity.setC_title(Util_TitleChecker.StringReplace(jiraServerEntity.getC_title()));

		JiraServerEntity addedNodeEntity = this.addNode(jiraServerEntity);

		JiraInfoDTO jiraInfoDTO = new JiraInfoDTO();
		jiraInfoDTO.setConnectId(addedNodeEntity.getC_id().toString());
		jiraInfoDTO.setUri(addedNodeEntity.getC_jira_server_base_url());
		jiraInfoDTO.setUserId(addedNodeEntity.getC_jira_server_connect_id());
		jiraInfoDTO.setPasswordOrToken(addedNodeEntity.getC_jira_server_connect_pw());
		JiraInfoEntity 등록결과 = 엔진통신기.지라서버_등록(jiraInfoDTO);

		logger.info(등록결과.getConnectId());
		logger.info(등록결과.getSelf());
		logger.info(등록결과.getConnectId());
		logger.info(등록결과.getPasswordOrToken());

		Set<JiraProjectEntity> 지라서버에_붙일_프로젝트_리스트 = new HashSet<>();

		if ( 등록결과 != null) {
			List<OnPremiseJiraProjectDTO> 지라프로젝트목록 = 엔진통신기.지라_프로젝트_리스트_가져오기(등록결과.getConnectId().toString());

			for ( OnPremiseJiraProjectDTO 지라프로젝트 : 지라프로젝트목록 ){

				//validation
				JiraProjectEntity 지라프로젝트_검색 = new JiraProjectEntity();
				지라프로젝트_검색.setWhere("c_jira_key", 지라프로젝트.getKey());
				JiraProjectEntity 검색결과 = jiraProject.getNode(지라프로젝트_검색);
				if( 검색결과 == null ){

					JiraProjectEntity 지라프로젝트_저장 = new JiraProjectEntity();
					지라프로젝트_저장.setC_jira_name(지라프로젝트.getName());
					지라프로젝트_저장.setC_jira_key(지라프로젝트.getKey());
					지라프로젝트_저장.setC_jira_etc(지라프로젝트.getId());
					지라프로젝트_저장.setC_jira_url(지라프로젝트.getSelf());
					지라프로젝트_저장.setRef(TreeConstant.First_Node_CID);
					지라프로젝트_저장.setC_type(TreeConstant.Leaf_Node_TYPE);
					JiraProjectEntity 저장된_프로젝트 = jiraProject.addNode(지라프로젝트_저장);
					지라서버에_붙일_프로젝트_리스트.add(저장된_프로젝트);

				}else {

					logger.info("이미 존재하는 프로젝트 입니다. -> " + 검색결과.getC_jira_key());

				}

			}

			if(지라서버에_붙일_프로젝트_리스트.size() > 0){
				addedNodeEntity.setJiraProjectEntities(지라서버에_붙일_프로젝트_리스트);
				this.updateNode(addedNodeEntity);
			}

		}


		return addedNodeEntity;
	}
}