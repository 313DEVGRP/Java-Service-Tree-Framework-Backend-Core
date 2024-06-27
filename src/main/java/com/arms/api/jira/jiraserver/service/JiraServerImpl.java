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
package com.arms.api.jira.jiraserver.service;

import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissuepriority.service.JiraIssuePriority;
import com.arms.api.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.api.jira.jiraissueresolution.service.JiraIssueResolution;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraissuetype.service.JiraIssueType;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraproject_pure.model.JiraProjectPureEntity;
import com.arms.api.jira.jiraproject_pure.service.JiraProjectPure;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.model.enums.EntityType;
import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.api.util.communicate.external.request.지라서버정보_데이터;
import com.arms.api.util.communicate.external.response.jira.*;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.Util_TitleChecker;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
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
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static com.arms.egovframework.javaservice.treeframework.remote.Global.chat;


@AllArgsConstructor
@Service("jiraServer")
public class JiraServerImpl extends TreeServiceImpl implements JiraServer{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private 엔진통신기 엔진통신기;

    @Autowired
    @Qualifier("jiraProject")
    private JiraProject jiraProject;

    @Autowired
    @Qualifier("jiraProjectPure")
    private JiraProjectPure jiraProjectPure;
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

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

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
    public JiraServerEntity 서버_항목별_기본값_설정(EntityType 설정할_항목, Long 항목_c_id, JiraServerEntity jiraServerEntity) throws Exception {
        JiraServerEntity 검색용_서버_엔티티 = new JiraServerEntity();
        검색용_서버_엔티티.setC_id(jiraServerEntity.getC_id());

        JiraServerEntity 검색된_ALM_서버 = this.getNode(검색용_서버_엔티티);

        //이슈 유형
        if(EntityType.이슈유형 == 설정할_항목) {
            Set<JiraIssueTypeEntity> 이슈_유형_목록 = 검색된_ALM_서버.getJiraIssueTypeEntities();
            if(이슈_유형_목록 != null && 이슈_유형_목록.size() != 0) {
                for (JiraIssueTypeEntity 이슈_유형 : 이슈_유형_목록) {
                    if (Objects.equals(이슈_유형.getC_id(), 항목_c_id)) {
                        이슈_유형.setC_check("true");
                    } else {
                        이슈_유형.setC_check("false");
                    }
                }
            }
        }

        //이슈 상태
        if(EntityType.이슈상태 == 설정할_항목) {
            Set<JiraIssueStatusEntity> 이슈_상태_목록 = 검색된_ALM_서버.getJiraIssueStatusEntities();
            if(이슈_상태_목록 != null && 이슈_상태_목록.size() != 0 ) {
                for (JiraIssueStatusEntity 이슈_상태 : 이슈_상태_목록) {
                    if (Objects.equals(이슈_상태.getC_id(), 항목_c_id)) {
                        이슈_상태.setC_check("true");
                    } else {
                        이슈_상태.setC_check("false");
                    }
                }
            }
        }

        //이슈 우선순위
        if(EntityType.이슈우선순위 == 설정할_항목) {
            Set<JiraIssuePriorityEntity> 이슈_우선순위_목록 = 검색된_ALM_서버.getJiraIssuePriorityEntities();
            if(이슈_우선순위_목록 != null && 이슈_우선순위_목록.size() != 0) {
                for (JiraIssuePriorityEntity 이슈_우선순위 : 이슈_우선순위_목록) {
                    if (Objects.equals(이슈_우선순위.getC_id(), 항목_c_id)) {
                        이슈_우선순위.setC_check("true");
                    } else {
                        이슈_우선순위.setC_check("false");
                    }
                }
            }
        }

        //이슈 해결책
        if(EntityType.이슈해결책 == 설정할_항목) {
            Set<JiraIssueResolutionEntity> 이슈_해결책_목록 = 검색된_ALM_서버.getJiraIssueResolutionEntities();
            if(이슈_해결책_목록.size() != 0) {
                for (JiraIssueResolutionEntity 이슈_해결책 : 이슈_해결책_목록) {
                    if (Objects.equals(이슈_해결책.getC_id(), 항목_c_id)) {
                        이슈_해결책.setC_check("true");
                    } else {
                        이슈_해결책.setC_check("false");
                    }
                }
            }
        }

        this.updateNode(검색된_ALM_서버);
        return 검색된_ALM_서버;
    }

    @Override
    public List<JiraProjectEntity> 서버_프로젝트_가져오기(JiraServerEntity jiraServerEntity) throws Exception {
        GlobalTreeMapEntity 트리맵_검색전용 = new GlobalTreeMapEntity();
        트리맵_검색전용.setJiraserver_link(jiraServerEntity.getC_id());
        List<GlobalTreeMapEntity> 지라서버_연결된_정보들 = globalTreeMapService.findAllBy(트리맵_검색전용);

        List<Long> 프로젝트_cId_목록 = new ArrayList<>();
        for (GlobalTreeMapEntity 연결정보 : 지라서버_연결된_정보들) {
            if (연결정보.getJiraproject_link() != null) {
                프로젝트_cId_목록.add(연결정보.getJiraproject_link());
            }
        }

        JiraProjectEntity 프로젝트_검색전용 = new JiraProjectEntity();
        Criterion criterion1 = Restrictions.in("c_id", 프로젝트_cId_목록);
        Criterion criterion2 = Restrictions.or(Restrictions.isNull("c_etc"), Restrictions.ne("c_etc", "delete"));
        Criterion criterion3 = Restrictions.and(criterion1, criterion2);
        프로젝트_검색전용.getCriterions().add(criterion3);

        List<JiraProjectEntity> 지라프로젝트_목록 = jiraProject.getChildNode(프로젝트_검색전용);
        return 지라프로젝트_목록;
    }

    @Override
    public List<JiraProjectPureEntity> 서버_프로젝트만_가져오기(JiraServerEntity jiraServerEntity) throws Exception {
        GlobalTreeMapEntity 트리맵_검색전용 = new GlobalTreeMapEntity();
        트리맵_검색전용.setJiraserver_link(jiraServerEntity.getC_id());
        List<GlobalTreeMapEntity> 지라서버_연결된_정보들 = globalTreeMapService.findAllBy(트리맵_검색전용);

        List<Long> 프로젝트_cId_목록 = new ArrayList<>();

        for (GlobalTreeMapEntity 연결정보 : 지라서버_연결된_정보들) {
            if (연결정보.getJiraproject_link() != null) {
                프로젝트_cId_목록.add(연결정보.getJiraproject_link());
            }
        }

        JiraProjectPureEntity 프로젝트_검색전용 = new JiraProjectPureEntity();
        Criterion criterion1 = Restrictions.in("c_id", 프로젝트_cId_목록);
        Criterion criterion2 = Restrictions.or(Restrictions.isNull("c_etc"), Restrictions.ne("c_etc", "delete"));
        Criterion criterion = Restrictions.and(criterion1, criterion2);
        프로젝트_검색전용.getCriterions().add(criterion);
        List<JiraProjectPureEntity> 지라프로젝트_목록 = jiraProjectPure.getChildNode(프로젝트_검색전용);

        return 지라프로젝트_목록;
    }

    @Override
    public List<JiraIssueTypeEntity> 서버_이슈유형_가져오기(JiraServerEntity jiraServerEntity) throws Exception {
        GlobalTreeMapEntity 트리맵_검색전용 = new GlobalTreeMapEntity();
        트리맵_검색전용.setJiraserver_link(jiraServerEntity.getC_id());
        List<GlobalTreeMapEntity> 지라서버_연결된_정보들 = globalTreeMapService.findAllBy(트리맵_검색전용);

        List<Long> 이슈유형_cId_목록 = new ArrayList<>();
        for (GlobalTreeMapEntity 연결정보 : 지라서버_연결된_정보들) {
            if (연결정보.getJiraissuetype_link() != null) {
                이슈유형_cId_목록.add(연결정보.getJiraissuetype_link());
            }
        }

        JiraIssueTypeEntity 이슈유형_검색전용 = new JiraIssueTypeEntity();
        Criterion criterion1 = Restrictions.in("c_id", 이슈유형_cId_목록);
        Criterion criterion2 = Restrictions.or(Restrictions.isNull("c_etc"), Restrictions.ne("c_etc", "delete"));
        Criterion criterion3 = Restrictions.eq("c_desc", "false");
        Criterion criterion = Restrictions.and(criterion1, criterion2, criterion3);
        이슈유형_검색전용.getCriterions().add(criterion);

        List<JiraIssueTypeEntity> 지라이슈유형_목록 = jiraIssueType.getChildNode(이슈유형_검색전용);

        return 지라이슈유형_목록;
    }

    @Override
    public List<JiraIssueStatusEntity> 서버_이슈상태_가져오기(JiraServerEntity jiraServerEntity) throws Exception {
        GlobalTreeMapEntity 트리맵_검색전용 = new GlobalTreeMapEntity();
        트리맵_검색전용.setJiraserver_link(jiraServerEntity.getC_id());
        List<GlobalTreeMapEntity> 지라서버_연결된_정보들 = globalTreeMapService.findAllBy(트리맵_검색전용);

        List<Long> 이슈상태_cId_목록 = new ArrayList<>();
        for (GlobalTreeMapEntity 연결정보 : 지라서버_연결된_정보들) {
            if (연결정보.getJiraissuestatus_link() != null) {
                이슈상태_cId_목록.add(연결정보.getJiraissuestatus_link());
            }
        }

        JiraIssueStatusEntity 이슈상태_검색전용 = new JiraIssueStatusEntity();
        Criterion criterion1 = Restrictions.in("c_id", 이슈상태_cId_목록);
        Criterion criterion2 = Restrictions.or(Restrictions.isNull("c_etc"), Restrictions.ne("c_etc", "delete"));
        Criterion criterion = Restrictions.and(criterion1, criterion2);
        이슈상태_검색전용.getCriterions().add(criterion);

        List<JiraIssueStatusEntity> 지라이슈상태_목록 = jiraIssueType.getChildNode(이슈상태_검색전용);

        return 지라이슈상태_목록;
    }

    @Override
    public List<JiraIssuePriorityEntity> 서버_이슈우선순위_가져오기(JiraServerEntity jiraServerEntity) throws Exception {
        GlobalTreeMapEntity 트리맵_검색전용 = new GlobalTreeMapEntity();
        트리맵_검색전용.setJiraserver_link(jiraServerEntity.getC_id());
        List<GlobalTreeMapEntity> 지라서버_연결된_정보들 = globalTreeMapService.findAllBy(트리맵_검색전용);

        List<Long> 이슈우선순위_cId_목록 = new ArrayList<>();
        for (GlobalTreeMapEntity 연결정보 : 지라서버_연결된_정보들) {
            if (연결정보.getJiraissuepriority_link() != null) {
                이슈우선순위_cId_목록.add(연결정보.getJiraissuepriority_link());
            }
        }

        JiraIssuePriorityEntity 이슈우선순위_검색전용 = new JiraIssuePriorityEntity();
        Criterion criterion1 = Restrictions.in("c_id", 이슈우선순위_cId_목록);
        Criterion criterion2 = Restrictions.or(Restrictions.isNull("c_etc"), Restrictions.ne("c_etc", "delete"));
        Criterion criterion = Restrictions.and(criterion1, criterion2);
        이슈우선순위_검색전용.getCriterions().add(criterion);

        List<JiraIssuePriorityEntity> 지라이슈우선순위_목록 = jiraIssuePriority.getChildNode(이슈우선순위_검색전용);

        return 지라이슈우선순위_목록;
    }

    @Override
    public List<JiraIssueResolutionEntity> 서버_이슈해결책_가져오기(JiraServerEntity jiraServerEntity) throws Exception {
        GlobalTreeMapEntity 트리맵_검색전용 = new GlobalTreeMapEntity();
        트리맵_검색전용.setJiraserver_link(jiraServerEntity.getC_id());
        List<GlobalTreeMapEntity> 지라서버_연결된_정보들 = globalTreeMapService.findAllBy(트리맵_검색전용);

        List<Long> 이슈해결책_cId_목록 = new ArrayList<>();
        for (GlobalTreeMapEntity 연결정보 : 지라서버_연결된_정보들) {
            if (연결정보.getJiraissueresolution_link() != null) {
                이슈해결책_cId_목록.add(연결정보.getJiraissueresolution_link());
            }
        }

        JiraIssueResolutionEntity 이슈해결책_검색전용 = new JiraIssueResolutionEntity();
        Criterion criterion1 = Restrictions.in("c_id", 이슈해결책_cId_목록);
        Criterion criterion2 = Restrictions.or(Restrictions.isNull("c_etc"), Restrictions.ne("c_etc", "delete"));
        Criterion criterion = Restrictions.and(criterion1, criterion2);
        이슈해결책_검색전용.getCriterions().add(criterion);

        List<JiraIssueResolutionEntity> 지라이슈해결책_목록 = jiraIssueResolution.getChildNode(이슈해결책_검색전용);

        return 지라이슈해결책_목록;
    }

    @Override
    @Transactional
    public int 암스_및_엔진_서버정보수정(JiraServerEntity jiraServerEntity) throws Exception {
        int 결과 = this.updateNode(jiraServerEntity);
        JiraServerEntity 커넥트아이디가져오기 = this.getNode(jiraServerEntity);

        지라서버정보_데이터 서버정보_데이터 = new 지라서버정보_데이터();
        서버정보_데이터.setConnectId(커넥트아이디가져오기.getC_jira_server_etc());
        서버정보_데이터.setType(jiraServerEntity.getC_jira_server_type());
        서버정보_데이터.setUri(jiraServerEntity.getC_jira_server_base_url());
        서버정보_데이터.setUserId(jiraServerEntity.getC_jira_server_connect_id());
        서버정보_데이터.setPasswordOrToken(jiraServerEntity.getC_jira_server_connect_pw());

        /**
         * 엔진 - 암스 오류발생 시 동기화 필요
         **/
        지라서버정보_엔티티 수정결과 = 엔진통신기.지라서버_등록(서버정보_데이터);

        if (수정결과 != null) {
            logger.info(" [ " + this.getClass().getName() + " :: 암스_및_엔진_서버정보수정 ] :: 수정결과 -> " + 수정결과.toString() );
        }

        return 결과;
    }

    @Override
    @Transactional
    public JiraServerEntity addJiraServer(JiraServerEntity jiraServerEntity) throws Exception {
        // 프로젝트 생성 //
        String randomConnectId = String.valueOf(RANDOM.nextLong() & Long.MAX_VALUE);

        String serverUrl = StringUtils.removeTrailingSlash(jiraServerEntity.getC_jira_server_base_url());
//        서버는 1개만 등록 시킬 지 여부
//        if (serverUrl != null) {
//            JiraServerEntity searchServerEntity = new JiraServerEntity();
//            searchServerEntity.getCriterions().add(Restrictions.eq("c_jira_server_base_url", serverUrl));
//            List<JiraServerEntity> jiraServerEntities = this.getChildNode(searchServerEntity);
//            if (jiraServerEntities.size() > 0) {
//                throw new Exception("이미 등록된 서버입니다.");
//            }
//        }

        jiraServerEntity.setC_jira_server_base_url(serverUrl);
        jiraServerEntity.setC_title(Util_TitleChecker.StringReplace(jiraServerEntity.getC_title()));
        jiraServerEntity.setC_jira_server_etc(randomConnectId); // 엔진과 통신할 connectId

        JiraServerEntity addedNodeEntity = this.addNode(jiraServerEntity);

        지라서버정보_데이터 서버정보_데이터 = new 지라서버정보_데이터();
        서버정보_데이터.setConnectId(randomConnectId);
        서버정보_데이터.setType(addedNodeEntity.getC_jira_server_type());
        서버정보_데이터.setUri(addedNodeEntity.getC_jira_server_base_url());
        서버정보_데이터.setUserId(addedNodeEntity.getC_jira_server_connect_id());
        서버정보_데이터.setPasswordOrToken(addedNodeEntity.getC_jira_server_connect_pw());
        지라서버정보_엔티티 등록결과 = 엔진통신기.지라서버_등록(서버정보_데이터);

        if (등록결과 != null) {
            logger.info(" [ 암스_및_엔진_서버정보등록 ] :: 등록결과 -> " + 등록결과.toString() );
            boolean 결과 = this.ALM_서버_전체_항목_갱신(addedNodeEntity);

            if (결과) {
                chat.sendMessageByEngine("서버 등록이 완료되었습니다.");
            }
        }

        return addedNodeEntity;
    }

    @Override
    @Transactional
    public JiraServerEntity 서버_엔티티_항목별_갱신(EntityType 갱신할_항목, String 프로젝트_C아이디, JiraServerEntity jiraServerEntity) throws Exception {
        JiraServerEntity 검색용_서버_엔티티 = new JiraServerEntity();
        검색용_서버_엔티티.setC_id(jiraServerEntity.getC_id());

        JiraServerEntity 검색된_ALM_서버 = this.getNode(검색용_서버_엔티티);

        if (검색된_ALM_서버 == null
                || 검색된_ALM_서버.getC_jira_server_type() == null
                || 검색된_ALM_서버.getC_jira_server_etc() == null) {
                throw new Exception("갱신에 필요한 서버 정보가 없습니다.");
        }

        String 서버유형 = 검색된_ALM_서버.getC_jira_server_type();
        logger.info("[ JiraServerImpl :: 서버_엔티티_항목별_갱신 ] :: 갱신할_항목 → {}, 서버유형 → {}", 갱신할_항목.getType(), 서버유형);

        String 오류_메세지;
        switch (갱신할_항목) {
            case 프로젝트:
                오류_메세지 = this.프로젝트_갱신(검색된_ALM_서버);
                break;
            case 이슈유형:
                오류_메세지 = this.이슈유형_갱신(검색된_ALM_서버, 프로젝트_C아이디);
                break;
            case 이슈상태:
                오류_메세지 = this.이슈상태_갱신(검색된_ALM_서버, 프로젝트_C아이디);
                break;
            case 이슈우선순위:
                오류_메세지 = this.이슈우선순위_갱신(검색된_ALM_서버);
                break;
            case 이슈해결책:
                오류_메세지 = this.이슈해결책_갱신(검색된_ALM_서버);
                break;
            default:
                throw new IllegalArgumentException("알 수 없는 갱신 항목: " + 갱신할_항목);
        }

        String 갱신_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신되었습니다.";
        if (오류_메세지 != null) {
            갱신_메세지 = 오류_메세지;
        }
        chat.sendMessageByEngine(갱신_메세지);

        this.updateNode(검색된_ALM_서버);
        return 검색된_ALM_서버;
    }

    private String 프로젝트_갱신(JiraServerEntity 검색된_ALM_서버) {
        EntityType 갱신할_항목 = EntityType.프로젝트;
        String 엔진_통신_아이디 = 검색된_ALM_서버.getC_jira_server_etc();
        String 서버유형 = 검색된_ALM_서버.getC_jira_server_type();

        Set<JiraProjectEntity> 해당_서버_프로젝트_목록 = Optional.ofNullable(검색된_ALM_서버.getJiraProjectEntities())
                                                        .orElse(new HashSet<>());

        Map<String, JiraProjectEntity> 기존프로젝트_맵 = 해당_서버_프로젝트_목록.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(프로젝트 -> 프로젝트.getC_jira_url(), 프로젝트 -> 프로젝트));

        List<지라프로젝트_데이터> 가져온_프로젝트_목록;
        try {
            가져온_프로젝트_목록 = 엔진통신기.ALM_프로젝트_목록_가져오기(엔진_통신_아이디);
        }
        catch (Exception e) {
            String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
            logger.error(오류_메세지);
            return 오류_메세지;
        }

        Set<JiraProjectEntity> 프로젝트_동기화목록 = 서버_엔티티_동기화(해당_서버_프로젝트_목록, 기존프로젝트_맵, 가져온_프로젝트_목록,
                                                                        서버유형, 엔진_통신_아이디, 갱신할_항목);

        검색된_ALM_서버.setJiraProjectEntities(프로젝트_동기화목록);
        return null;
    }

    private String 이슈유형_갱신(JiraServerEntity 검색된_ALM_서버, String 프로젝트_C아이디) throws Exception {
        EntityType 갱신할_항목 = EntityType.이슈유형;
        String 엔진_통신_아이디 = 검색된_ALM_서버.getC_jira_server_etc();
        String 서버유형 = 검색된_ALM_서버.getC_jira_server_type();

        if (StringUtils.equals(ServerType.JIRA_ON_PREMISE.getType(), 서버유형)) {
            Set<JiraIssueTypeEntity> 해당_서버_이슈유형_목록 = Optional.ofNullable(검색된_ALM_서버.getJiraIssueTypeEntities())
                    .orElse(new HashSet<>());

            Map<String, JiraIssueTypeEntity> 기존이슈유형_맵 = 해당_서버_이슈유형_목록.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(이슈유형 -> 이슈유형.getC_issue_type_url(), 이슈유형 -> 이슈유형));

            List<지라이슈유형_데이터> 가져온_이슈유형_목록;
            try {
                가져온_이슈유형_목록 = 엔진통신기.ALM_이슈_유형_가져오기(엔진_통신_아이디);
            }
            catch (Exception e) {
                String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
                logger.error(오류_메세지);
                return 오류_메세지;
            }

            Set<JiraIssueTypeEntity> 이슈유형_동기화목록 = 서버_엔티티_동기화(해당_서버_이슈유형_목록, 기존이슈유형_맵, 가져온_이슈유형_목록,
                                                                            서버유형, 엔진_통신_아이디, 갱신할_항목);

            검색된_ALM_서버.setJiraIssueTypeEntities(이슈유형_동기화목록);
        }
        else if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), 서버유형) || StringUtils.equals(ServerType.REDMINE_ON_PREMISE.getType(), 서버유형)) {

            // 프로젝트_아이디가 null - 서버 등록. 전체 프로젝트에 대한 이슈유형을 갱신시키는 로직
            // 프로젝트 아이디가 있는 경우 - 해당 프로젝트만 이슈유형을 갱신시키는 로직
            if (프로젝트_C아이디 == null || 프로젝트_C아이디.isEmpty()) {
                Set<JiraProjectEntity> 해당_서버_프로젝트_목록 = Optional.ofNullable(검색된_ALM_서버.getJiraProjectEntities())
                                                                .orElse(new HashSet<>());

                for (JiraProjectEntity 프로젝트 : 해당_서버_프로젝트_목록) {
                    if (프로젝트.getC_etc() != null && StringUtils.equals(프로젝트.getC_etc(), "delete")) {
                        continue;
                    }

                    Set<JiraIssueTypeEntity> 프로젝트의_이슈유형_목록 = Optional.ofNullable(프로젝트.getJiraIssueTypeEntities())
                            .orElse(new HashSet<>());

                    Map<String, JiraIssueTypeEntity> 기존이슈유형_맵 = 프로젝트의_이슈유형_목록.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(이슈유형 -> 이슈유형.getC_issue_type_url(), 이슈유형 -> 이슈유형));

                    List<지라이슈유형_데이터> 가져온_이슈유형_목록;
                    try {
                        가져온_이슈유형_목록 = 엔진통신기.클라우드_프로젝트별_이슈_유형_목록(엔진_통신_아이디, 프로젝트.getC_desc());
                    }
                    catch (Exception e) {
                        String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
                        logger.error(오류_메세지);
                        return 오류_메세지;
                    }

                    Set<JiraIssueTypeEntity> 이슈유형_동기화목록 = 서버_엔티티_동기화(프로젝트의_이슈유형_목록, 기존이슈유형_맵, 가져온_이슈유형_목록,
                                                                                        서버유형, 엔진_통신_아이디, 갱신할_항목);

                    프로젝트.setJiraIssueTypeEntities(이슈유형_동기화목록);
                    해당_서버_프로젝트_목록.add(프로젝트);
                }

                검색된_ALM_서버.setJiraProjectEntities(해당_서버_프로젝트_목록);
            }
            else {
                JiraProjectEntity 프로젝트_검색전용 = new JiraProjectEntity();
                프로젝트_검색전용.setC_id(Long.parseLong(프로젝트_C아이디));
                JiraProjectEntity 프로젝트 = jiraProject.getNode(프로젝트_검색전용);
                Set<JiraIssueTypeEntity> 프로젝트의_이슈유형_목록 = Optional.ofNullable(프로젝트.getJiraIssueTypeEntities())
                        .orElse(new HashSet<>());

                Map<String, JiraIssueTypeEntity> 기존이슈유형_맵 = 프로젝트의_이슈유형_목록.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(이슈유형 -> 이슈유형.getC_issue_type_url(), 이슈유형 -> 이슈유형));

                List<지라이슈유형_데이터> 가져온_이슈유형_목록;
                try {
                    가져온_이슈유형_목록 = 엔진통신기.클라우드_프로젝트별_이슈_유형_목록(엔진_통신_아이디, 프로젝트.getC_desc());
                }
                catch (Exception e) {
                    String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
                    logger.error(오류_메세지);
                    return 오류_메세지;
                }

                Set<JiraIssueTypeEntity> 이슈유형_동기화목록 = 서버_엔티티_동기화(프로젝트의_이슈유형_목록, 기존이슈유형_맵, 가져온_이슈유형_목록,
                                                                                서버유형, 엔진_통신_아이디, 갱신할_항목);

                프로젝트.setJiraIssueTypeEntities(이슈유형_동기화목록);
                Set<JiraProjectEntity> jiraProjectEntities = 검색된_ALM_서버.getJiraProjectEntities();

                if (jiraProjectEntities.removeIf(entity -> entity.getC_id() == 프로젝트.getC_id())) {
                    jiraProjectEntities.add(프로젝트);
                }

                검색된_ALM_서버.setJiraProjectEntities(jiraProjectEntities);
            }
        }

        return null;
    }

    private String 이슈상태_갱신(JiraServerEntity 검색된_ALM_서버, String 프로젝트_C아이디) throws Exception {
        EntityType 갱신할_항목 = EntityType.이슈상태;
        String 엔진_통신_아이디 = 검색된_ALM_서버.getC_jira_server_etc();
        String 서버유형 = 검색된_ALM_서버.getC_jira_server_type();

        if (StringUtils.equals(ServerType.JIRA_ON_PREMISE.getType(), 서버유형) || StringUtils.equals(ServerType.REDMINE_ON_PREMISE.getType(), 서버유형)) {
            Set<JiraIssueStatusEntity> 해당_서버_이슈_상태_목록 = Optional.ofNullable(검색된_ALM_서버.getJiraIssueStatusEntities())
                    .orElse(new HashSet<>());

            Map<String, JiraIssueStatusEntity> 기존이슈상태_맵 = 해당_서버_이슈_상태_목록.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(이슈상태 -> 이슈상태.getC_issue_status_url(), 이슈상태 -> 이슈상태));

            List<지라이슈상태_데이터> 가져온_이슈상태_목록;
            try {
                가져온_이슈상태_목록 = 엔진통신기.ALM_이슈_상태_가져오기(엔진_통신_아이디);
            }
            catch (Exception e) {
                String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
                logger.error(오류_메세지);
                return 오류_메세지;
            }

            Set<JiraIssueStatusEntity> 이슈상태_동기화목록 = 서버_엔티티_동기화(해당_서버_이슈_상태_목록, 기존이슈상태_맵, 가져온_이슈상태_목록, 서버유형, 엔진_통신_아이디, 갱신할_항목);
            검색된_ALM_서버.setJiraIssueStatusEntities(이슈상태_동기화목록);
        }
        else if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), 서버유형)) {

            // 프로젝트_아이디가 null - 서버 등록. 전체 프로젝트에 대한 이슈상태 갱신로직
            // 프로젝트 아이디가 있는 경우 - 해당 프로젝트만 이슈상태 갱신시키는 로직
            if (프로젝트_C아이디 == null || 프로젝트_C아이디.isEmpty()) {
                Set<JiraProjectEntity> 동기화_프로젝트_목록 = new HashSet<>();
                Set<JiraProjectEntity> 해당_서버_프로젝트_목록 = 검색된_ALM_서버.getJiraProjectEntities();

                for (JiraProjectEntity 프로젝트 : 해당_서버_프로젝트_목록) {
                    if (프로젝트.getC_etc() != null && StringUtils.equals(프로젝트.getC_etc(), "delete")) {
                        continue;
                    }

                    Set<JiraIssueStatusEntity> 프로젝트의_이슈상태_목록 = Optional.ofNullable(프로젝트.getJiraIssueStatusEntities())
                            .orElse(new HashSet<>());

                    Map<String, JiraIssueStatusEntity> 기존이슈상태_맵 = 프로젝트의_이슈상태_목록.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(이슈상태 -> 이슈상태.getC_issue_status_url(), 이슈상태 -> 이슈상태));

                    List<지라이슈상태_데이터> 가져온_이슈상태_목록;
                    try {
                        가져온_이슈상태_목록 = 엔진통신기.클라우드_프로젝트별_이슈_상태_목록(엔진_통신_아이디, 프로젝트.getC_desc());
                    }
                    catch (Exception e) {
                        String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
                        logger.error(오류_메세지);
                        return 오류_메세지;
                    }

                    Set<JiraIssueStatusEntity> 이슈상태_동기화목록 = 서버_엔티티_동기화(프로젝트의_이슈상태_목록, 기존이슈상태_맵, 가져온_이슈상태_목록, 서버유형, 엔진_통신_아이디, 갱신할_항목);
                    프로젝트.setJiraIssueStatusEntities(이슈상태_동기화목록);
                    동기화_프로젝트_목록.add(프로젝트);
                }

                검색된_ALM_서버.setJiraProjectEntities(동기화_프로젝트_목록);
            }
            else {
                JiraProjectEntity 프로젝트_검색전용 = new JiraProjectEntity();
                프로젝트_검색전용.setC_id(Long.parseLong(프로젝트_C아이디));
                JiraProjectEntity 프로젝트 = jiraProject.getNode(프로젝트_검색전용);

                Set<JiraIssueStatusEntity> 프로젝트의_이슈상태_목록 = Optional.ofNullable(프로젝트.getJiraIssueStatusEntities())
                        .orElse(new HashSet<>());

                Map<String, JiraIssueStatusEntity> 기존이슈상태_맵 = 프로젝트의_이슈상태_목록.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(이슈상태 -> 이슈상태.getC_issue_status_url(), 이슈상태 -> 이슈상태));

                List<지라이슈상태_데이터> 가져온_이슈상태_목록;
                try {
                    가져온_이슈상태_목록 = 엔진통신기.클라우드_프로젝트별_이슈_상태_목록(엔진_통신_아이디, 프로젝트.getC_desc());
                }
                catch (Exception e) {
                    String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
                    logger.error(오류_메세지);
                    return 오류_메세지;
                }
                Set<JiraIssueStatusEntity> 이슈상태_동기화목록 = 서버_엔티티_동기화(프로젝트의_이슈상태_목록, 기존이슈상태_맵, 가져온_이슈상태_목록, 서버유형, 엔진_통신_아이디, 갱신할_항목);

                프로젝트.setJiraIssueStatusEntities(이슈상태_동기화목록);
                Set<JiraProjectEntity> jiraProjectEntities = 검색된_ALM_서버.getJiraProjectEntities();

                if (jiraProjectEntities.removeIf(entity -> entity.getC_id() == 프로젝트.getC_id())) {
                    jiraProjectEntities.add(프로젝트);
                }

                검색된_ALM_서버.setJiraProjectEntities(jiraProjectEntities);
            }
        }

        return null;
    }

    private String 이슈우선순위_갱신(JiraServerEntity 검색된_ALM_서버) {
        EntityType 갱신할_항목 = EntityType.이슈우선순위;
        String 엔진_통신_아이디 = 검색된_ALM_서버.getC_jira_server_etc();
        String 서버유형 = 검색된_ALM_서버.getC_jira_server_type();

        Set<JiraIssuePriorityEntity> 해당_서버_이슈_우선순위_목록 = Optional.ofNullable(검색된_ALM_서버.getJiraIssuePriorityEntities())
                .orElse(new HashSet<>());

        Map<String, JiraIssuePriorityEntity> 기존이슈우선순위_맵 = 해당_서버_이슈_우선순위_목록.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(우선순위 -> 우선순위.getC_issue_priority_url(), 우선순위 -> 우선순위));

        List<지라이슈우선순위_데이터> 가져온_이슈우선순위_목록 = null;
        try {
            가져온_이슈우선순위_목록 = 엔진통신기.ALM_이슈_우선순위_가져오기(엔진_통신_아이디);
        }
        catch (Exception e) {
            String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
            logger.error(오류_메세지);
            return 오류_메세지;
        }

        Set<JiraIssuePriorityEntity> 우선순위_동기화목록 = 서버_엔티티_동기화(해당_서버_이슈_우선순위_목록, 기존이슈우선순위_맵, 가져온_이슈우선순위_목록,
                                                                            서버유형, 엔진_통신_아이디, 갱신할_항목);

        검색된_ALM_서버.setJiraIssuePriorityEntities(우선순위_동기화목록);
        return null;
    }

    private String 이슈해결책_갱신(JiraServerEntity 검색된_ALM_서버) {
        EntityType 갱신할_항목 = EntityType.이슈해결책;
        String 엔진_통신_아이디 = 검색된_ALM_서버.getC_jira_server_etc();
        String 서버유형 = 검색된_ALM_서버.getC_jira_server_type();

        Set<JiraIssueResolutionEntity> 해당_서버_이슈_해결책_목록 = Optional.ofNullable(검색된_ALM_서버.getJiraIssueResolutionEntities())
                .orElse(new HashSet<>());

        Map<String, JiraIssueResolutionEntity> 기존이슈해결책_맵 = 해당_서버_이슈_해결책_목록.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(이슈해결책 -> 이슈해결책.getC_issue_resolution_url(), 이슈해결책 -> 이슈해결책));

        List<지라이슈해결책_데이터> 가져온_이슈해결책_목록;
        try {
            가져온_이슈해결책_목록 = 엔진통신기.ALM_이슈_해결책_가져오기(엔진_통신_아이디);
        }
        catch (Exception e) {
            String 오류_메세지 = 검색된_ALM_서버.getC_jira_server_name()+"의 "+ 갱신할_항목 + " 이(가) 갱신 실패하습니다." + e.getMessage();
            logger.error(오류_메세지);
            return 오류_메세지;
        }

        Set<JiraIssueResolutionEntity> 해결책_동기화목록 = 서버_엔티티_동기화(해당_서버_이슈_해결책_목록, 기존이슈해결책_맵, 가져온_이슈해결책_목록,
                                                                            서버유형, 엔진_통신_아이디, 갱신할_항목);

        검색된_ALM_서버.setJiraIssueResolutionEntities(해결책_동기화목록);
        return null;
    }

    private <E extends TreeSearchEntity, T extends ALM_데이터> Set<E> 서버_엔티티_동기화(Set<E> 등록된_엔티티_목록, Map<String, E> 기존_맵, List<T> 가져온_엔티티_목록,
                                                                              String 서버유형, String 엔진_연결_아이디, EntityType 갱신항목 ) {

        if (가져온_엔티티_목록 == null || 가져온_엔티티_목록.size() == 0) {
            return null;
        }

        for (T 엔진_엔티티 : 가져온_엔티티_목록) {
            boolean 엔티티존재여부 = 기존_맵 != null && 기존_맵.containsKey(엔진_엔티티.getSelf());
            E 백엔드_엔티티 = 엔티티존재여부 ? 기존_맵.get(엔진_엔티티.getSelf()) : null;
            E 결과 = null;
            try {
                if (백엔드_엔티티 instanceof JiraProjectEntity || EntityType.프로젝트 == 갱신항목) {
                    결과 = (E) 프로젝트_저장_또는_업데이트((지라프로젝트_데이터) 엔진_엔티티, (JiraProjectEntity) 백엔드_엔티티);
                }
                else if (백엔드_엔티티 instanceof JiraIssueTypeEntity || EntityType.이슈유형 == 갱신항목) {
                    결과 = (E) 이슈유형_저장_또는_업데이트((지라이슈유형_데이터) 엔진_엔티티, (JiraIssueTypeEntity) 백엔드_엔티티, 서버유형);
                }
                else if (백엔드_엔티티 instanceof JiraIssueStatusEntity || EntityType.이슈상태 == 갱신항목) {
                    결과 = (E) 이슈상태_저장_또는_업데이트((지라이슈상태_데이터) 엔진_엔티티, (JiraIssueStatusEntity) 백엔드_엔티티);
                }
                else if (백엔드_엔티티 instanceof JiraIssuePriorityEntity || EntityType.이슈우선순위 == 갱신항목) {
                    결과 = (E) 이슈우선순위_저장_또는_업데이트((지라이슈우선순위_데이터) 엔진_엔티티, (JiraIssuePriorityEntity) 백엔드_엔티티, 서버유형);
                }
                else if (백엔드_엔티티 instanceof JiraIssueResolutionEntity || EntityType.이슈해결책 == 갱신항목) {
                    결과 = (E) 이슈해결책_저장_또는_업데이트((지라이슈해결책_데이터) 엔진_엔티티, (JiraIssueResolutionEntity) 백엔드_엔티티, 서버유형);
                }
            }
            catch (Exception e) {
                logger.error("서버_엔티티_동기화 오류 발생 :: 연결아이디 :: {} 서버유형 :: {} :: 갱신항목 ::  {} :: 엔티티 :: {}",
                                                                            엔진_연결_아이디, 서버유형, 갱신항목, 엔진_엔티티.toString());
                logger.error(e.getMessage());
            }

            if (결과 != null && !엔티티존재여부) {
                등록된_엔티티_목록.add(결과);
            }

            기존_맵.remove(엔진_엔티티.getSelf());
        }

        Set<E> 반환할_엔티티_목록 = 소프트_딜리트(기존_맵, 등록된_엔티티_목록);

        return 반환할_엔티티_목록;
    }

    private <T extends TreeSearchEntity> Set<T> 소프트_딜리트(Map<String, T> 기존_맵, Set<T> 등록된_엔티티_목록) {
        /**
         * 이 메소드는 JiraProjectEntity, JiraIssueTypeEntity, JiraIssueStatusEntity, JiraIssueResolutionEntity,
         * JiraIssuePriorityEntity의 [ c_etc ] 컬럼을 Soft Delete 동작을 수행합니다.
         * Soft Delete 처리와 함께 c_check(JiraProjectEntity 제외) 의 값을 false로 변경해줍니다.
         * @author Advanc2d
         * @since 2024-03-31
         **/
        if (기존_맵 != null && 기존_맵.size() > 0) {
            기존_맵.forEach((엔티티키, 엔티티) -> {
                T 반환결과 = null;
                try {
                    int 처리결과 = 0;

                    if (엔티티 instanceof JiraProjectEntity) {
                        JiraProjectEntity 프로젝트 = (JiraProjectEntity) 엔티티;
                        if (프로젝트.getC_etc() == null || !StringUtils.equals("delete", 프로젝트.getC_etc())) {
                            프로젝트.setC_etc("delete");
                        }
                        처리결과 = jiraProject.updateNode(프로젝트);

                        반환결과 = (T) 프로젝트;
                    }
                    else if (엔티티 instanceof JiraIssueTypeEntity) {
                        JiraIssueTypeEntity 이슈유형 = (JiraIssueTypeEntity) 엔티티;
                        if (이슈유형.getC_etc() == null || !StringUtils.equals("delete", 이슈유형.getC_etc())) {
                            이슈유형.setC_etc("delete");
                            이슈유형.setC_check("false");
                        }
                        처리결과 = jiraIssueType.updateNode(이슈유형);

                        반환결과 = (T) 이슈유형;
                    }
                    else if (엔티티 instanceof JiraIssueStatusEntity) {
                        JiraIssueStatusEntity 이슈상태 = (JiraIssueStatusEntity) 엔티티;
                        if (이슈상태.getC_etc() == null || !StringUtils.equals("delete", 이슈상태.getC_etc())) {
                            이슈상태.setC_etc("delete");
                            이슈상태.setC_check("false");
                        }
                        처리결과 = jiraIssueStatus.updateNode(이슈상태);

                        반환결과 = (T) 이슈상태;
                    }
                    else if (엔티티 instanceof JiraIssuePriorityEntity) {
                        JiraIssuePriorityEntity 이슈우선순위 = (JiraIssuePriorityEntity) 엔티티;
                        if (이슈우선순위.getC_etc() == null || !StringUtils.equals("delete", 이슈우선순위.getC_etc())) {
                            이슈우선순위.setC_etc("delete");
                            이슈우선순위.setC_check("false");
                        }
                        처리결과 = jiraIssuePriority.updateNode(이슈우선순위);

                        반환결과 = (T) 이슈우선순위;
                    }
                    else if (엔티티 instanceof JiraIssueResolutionEntity) {
                        JiraIssueResolutionEntity 이슈해결책 = (JiraIssueResolutionEntity) 엔티티;
                        if (이슈해결책.getC_etc() == null || !StringUtils.equals("delete", 이슈해결책.getC_etc())) {
                            이슈해결책.setC_etc("delete");
                            이슈해결책.setC_check("false");
                        }
                        처리결과 = jiraIssueResolution.updateNode(이슈해결책);

                        반환결과 = (T) 이슈해결책;
                    }

                    if(처리결과 == 1) {
                        등록된_엔티티_목록.add(반환결과);
                    }
                    // 반환결과 = 소프트딜리트_설정(엔티티);
                }
                catch (Exception e) {
                    logger.error("소프트 딜리트 처리 오류 발생 :: 엔티티 ::  {}", 엔티티.toString());
                    logger.error(e.getMessage());
                }
            });
        }

        return 등록된_엔티티_목록;
    }

    private JiraProjectEntity 프로젝트_저장_또는_업데이트(지라프로젝트_데이터 엔진_프로젝트, JiraProjectEntity 백엔드_프로젝트) throws Exception {

        boolean isNew = false;

        if (백엔드_프로젝트 == null) {
            isNew= true;
            백엔드_프로젝트 = new JiraProjectEntity();
        }

        // 저장 or 업데이트 공통
        백엔드_프로젝트.setC_jira_name(엔진_프로젝트.getName());
        백엔드_프로젝트.setC_jira_key(엔진_프로젝트.getKey());
        백엔드_프로젝트.setC_desc(엔진_프로젝트.getId());
        백엔드_프로젝트.setC_jira_url(엔진_프로젝트.getSelf());
        백엔드_프로젝트.setC_title(엔진_프로젝트.getName());

        if (isNew) {
            백엔드_프로젝트.setRef(TreeConstant.First_Node_CID);
            백엔드_프로젝트.setC_type(TreeConstant.Leaf_Node_TYPE);
            return jiraProject.addNode(백엔드_프로젝트);
        }
        else {
            /***
             *삭제된 내용이었는데 복구된 경우 Soft Delete 복구처리를 위한 로직
             ***/
            if (백엔드_프로젝트.getC_etc() != null && StringUtils.equals("delete", 백엔드_프로젝트.getC_etc())) {
                백엔드_프로젝트.setC_etc(null);
            }
            int 업데이트결과 = jiraProject.updateNode(백엔드_프로젝트);
            return 업데이트결과 == 1 ? 백엔드_프로젝트 : null;
        }
    }

    private JiraIssueTypeEntity 이슈유형_저장_또는_업데이트(지라이슈유형_데이터 엔진_이슈유형, JiraIssueTypeEntity 백엔드_이슈유형, String 서버유형) throws Exception {

        boolean isNew = false;

        if (백엔드_이슈유형 == null) {
            isNew= true;
            백엔드_이슈유형 = new JiraIssueTypeEntity();
        }

        // 공통
        백엔드_이슈유형.setC_issue_type_id(엔진_이슈유형.getId());
        백엔드_이슈유형.setC_issue_type_name(엔진_이슈유형.getName());
        백엔드_이슈유형.setC_issue_type_url(엔진_이슈유형.getSelf());
        백엔드_이슈유형.setC_issue_type_desc(엔진_이슈유형.getDescription());
        백엔드_이슈유형.setC_desc(엔진_이슈유형.getSubtask().toString());

        if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(),서버유형)) {
            // 백엔드_이슈유형.setC_etc(엔진_이슈유형.getUntranslatedName());
            백엔드_이슈유형.setC_contents(엔진_이슈유형.getHierarchyLevel().toString()); //Integer
        }

        if (isNew) {
            if (엔진_이슈유형.getName().equals("arms-requirement")) {
                백엔드_이슈유형.setC_check("true"); //기본값 false 설정
            }
            else {
                백엔드_이슈유형.setC_check("false"); //기본값 false 설정
            }
            백엔드_이슈유형.setRef(TreeConstant.First_Node_CID);
            백엔드_이슈유형.setC_type(TreeConstant.Leaf_Node_TYPE);
            return jiraIssueType.addNode(백엔드_이슈유형);
        }
        else {
            /***
             *삭제된 내용이었는데 복구된 경우 Soft Delete 복구처리를 위한 로직
             ***/
            if (백엔드_이슈유형.getC_etc() != null && StringUtils.equals("delete", 백엔드_이슈유형.getC_etc())) {
                백엔드_이슈유형.setC_etc(null);
            }
            int 업데이트결과 = jiraIssueType.updateNode(백엔드_이슈유형);
            return 업데이트결과 == 1 ? 백엔드_이슈유형 : null;
        }
    }

    private JiraIssueStatusEntity 이슈상태_저장_또는_업데이트(지라이슈상태_데이터 엔진_이슈상태, JiraIssueStatusEntity 백엔드_이슈상태) throws Exception {

        boolean isNew = false;

        if (백엔드_이슈상태 == null) {
            isNew= true;
            백엔드_이슈상태 = new JiraIssueStatusEntity();
        }

        //공통
        백엔드_이슈상태.setC_issue_status_id(엔진_이슈상태.getId());
        백엔드_이슈상태.setC_issue_status_name(엔진_이슈상태.getName());
        백엔드_이슈상태.setC_issue_status_url(엔진_이슈상태.getSelf());
        백엔드_이슈상태.setC_issue_status_desc(엔진_이슈상태.getDescription());

        if (isNew) {
            백엔드_이슈상태.setC_check("false");
            백엔드_이슈상태.setRef(TreeConstant.First_Node_CID);
            백엔드_이슈상태.setC_type(TreeConstant.Leaf_Node_TYPE);
            return jiraIssueStatus.addNode(백엔드_이슈상태);
        }
        else {
            /***
             *삭제된 내용이었는데 복구된 경우 Soft Delete 복구처리를 위한 로직
             ***/
            if (백엔드_이슈상태.getC_etc() != null && StringUtils.equals("delete", 백엔드_이슈상태.getC_etc())) {
                백엔드_이슈상태.setC_etc(null);
            }
            int 업데이트결과 = jiraIssueStatus.updateNode(백엔드_이슈상태);
            return 업데이트결과 == 1 ? 백엔드_이슈상태 : null;
        }
    }

    private JiraIssuePriorityEntity 이슈우선순위_저장_또는_업데이트(지라이슈우선순위_데이터 엔진_이슈우선순위, JiraIssuePriorityEntity 백엔드_이슈우선순위, String 서버유형) throws Exception {

        boolean isNew = false;

        if (백엔드_이슈우선순위 == null) {
            isNew= true;
            백엔드_이슈우선순위 = new JiraIssuePriorityEntity();
        }

        백엔드_이슈우선순위.setC_issue_priority_id(엔진_이슈우선순위.getId());
        백엔드_이슈우선순위.setC_issue_priority_name(엔진_이슈우선순위.getName());
        백엔드_이슈우선순위.setC_issue_priority_url(엔진_이슈우선순위.getSelf());
        백엔드_이슈우선순위.setC_issue_priority_desc(엔진_이슈우선순위.getDescription());

        if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), 서버유형)
                || StringUtils.equals(ServerType.REDMINE_ON_PREMISE.getType(), 서버유형)) {
            백엔드_이슈우선순위.setC_desc(String.valueOf(엔진_이슈우선순위.isDefault()));
        }

        if (isNew) {
            if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), 서버유형)
                    || StringUtils.equals(ServerType.REDMINE_ON_PREMISE.getType(), 서버유형)) {
                백엔드_이슈우선순위.setC_check(String.valueOf(엔진_이슈우선순위.isDefault()));
            }
            백엔드_이슈우선순위.setRef(TreeConstant.First_Node_CID);
            백엔드_이슈우선순위.setC_type(TreeConstant.Leaf_Node_TYPE);
            return jiraIssuePriority.addNode(백엔드_이슈우선순위);
        }
        else {
            /***
             *삭제된 내용이었는데 복구된 경우 Soft Delete 복구처리를 위한 로직
             ***/
            if (백엔드_이슈우선순위.getC_etc() != null && StringUtils.equals("delete", 백엔드_이슈우선순위.getC_etc())) {
                백엔드_이슈우선순위.setC_etc(null);
            }
            int 업데이트결과 = jiraIssuePriority.updateNode(백엔드_이슈우선순위);
            return 업데이트결과 == 1 ? 백엔드_이슈우선순위 : null;
        }
    }

    private JiraIssueResolutionEntity 이슈해결책_저장_또는_업데이트(지라이슈해결책_데이터 엔진_이슈해결책, JiraIssueResolutionEntity 백엔드_이슈해결책, String 서버유형) throws Exception {

        boolean isNew = false;

        if (백엔드_이슈해결책 == null) {
            isNew= true;
            백엔드_이슈해결책 = new JiraIssueResolutionEntity();
        }

        백엔드_이슈해결책.setC_issue_resolution_id(엔진_이슈해결책.getId());
        백엔드_이슈해결책.setC_issue_resolution_name(엔진_이슈해결책.getName());
        백엔드_이슈해결책.setC_issue_resolution_url(엔진_이슈해결책.getSelf());
        백엔드_이슈해결책.setC_issue_resolution_desc(엔진_이슈해결책.getDescription());
        백엔드_이슈해결책.setC_desc(String.valueOf(엔진_이슈해결책.isDefault()));

        if (isNew) {
            if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), 서버유형) || StringUtils.equals(ServerType.REDMINE_ON_PREMISE.getType(), 서버유형)) {
                백엔드_이슈해결책.setC_check(String.valueOf(엔진_이슈해결책.isDefault()));
            }
            백엔드_이슈해결책.setRef(TreeConstant.First_Node_CID);
            백엔드_이슈해결책.setC_type(TreeConstant.Leaf_Node_TYPE);
            return jiraIssueResolution.addNode(백엔드_이슈해결책);
        }
        else {
            /***
             *삭제된 내용이었는데 복구된 경우 Soft Delete 복구처리를 위한 로직
             ***/
            if (백엔드_이슈해결책.getC_etc() != null && StringUtils.equals("delete", 백엔드_이슈해결책.getC_etc())) {
                백엔드_이슈해결책.setC_etc(null);
            }
            int 업데이트결과 = jiraIssueResolution.updateNode(백엔드_이슈해결책);
            return 업데이트결과 == 1 ? 백엔드_이슈해결책 : null;
        }
    }

    @Override
    @Transactional
    public boolean ALM_서버_전체_항목_갱신(JiraServerEntity jiraServerEntity) throws Exception {

        boolean 결과 = false;

        try {
            서버_엔티티_항목별_갱신(EntityType.프로젝트, null, jiraServerEntity);
            서버_엔티티_항목별_갱신(EntityType.이슈유형, null, jiraServerEntity);
            서버_엔티티_항목별_갱신(EntityType.이슈상태, null, jiraServerEntity);
            서버_엔티티_항목별_갱신(EntityType.이슈우선순위, null, jiraServerEntity);
            서버_엔티티_항목별_갱신(EntityType.이슈해결책, null, jiraServerEntity);

            결과 = true;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (결과) {
            chat.sendMessageByEngine("전체 서버 데이터 갱신이 완료되었습니다.");
        }

        return 결과;
    }
}