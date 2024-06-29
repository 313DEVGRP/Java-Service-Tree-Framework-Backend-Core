/*
 * @author Dongmin.lee
 * @since 2022-06-17
 * @version 22.06.17
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.product_service.pdservice.service;

import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalContentsTreeMapService;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraproject_pure.model.JiraProjectPureEntity;
import com.arms.api.jira.jiraproject_pure.service.JiraProjectPure;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.api.jira.jiraserver_pure.service.JiraServerPure;
import com.arms.api.product_service.pdservice.model.PdServiceD3Chart;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice_detail.service.PdServiceDetail;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.util.dynamicdbmaker.service.DynamicDBMaker;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.api.util.filerepository.service.FileRepository;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

import static com.arms.egovframework.javaservice.treeframework.remote.Global.chat;

@Slf4j
@RequiredArgsConstructor
@Service("pdService")
public class PdServiceImpl extends TreeServiceImpl implements PdService {

    private final FileRepository fileRepository;

    private final PdServiceVersion pdServiceVersion;

    private final PdServiceDetail pdServiceDetail;

    private final DynamicDBMaker dynamicDBMaker;

    private final GlobalTreeMapService globalTreeMapService;

    private final GlobalContentsTreeMapService globalContentsTreeMapService;

    private final JiraProjectPure jiraProjectPure;

    private final JiraServerPure jiraServerPure;

    @Override
    public List<PdServiceEntity> getNodesWithoutRoot(PdServiceEntity pdServiceEntity) throws Exception {
        pdServiceEntity.setOrder(Order.desc("c_id"));
        Criterion criterion = Restrictions.not(
                // replace "id" below with property name, depending on what you're filtering against
                Restrictions.in("c_id", new Object[]{TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
        );
        pdServiceEntity.getCriterions().add(criterion);
        List<PdServiceEntity> list = this.getChildNode(pdServiceEntity);
        for (PdServiceEntity dto : list) {
            dto.setC_pdservice_contents("force empty");
        }
        return list;
    }


    @Override
    public PdServiceEntity getNodeWithVersionOrderByCidDesc(PdServiceEntity pdServiceEntity) throws Exception {
        PdServiceEntity pdServiceNode = this.getNode(pdServiceEntity);
        Set<PdServiceVersionEntity> sortedSet = new TreeSet<>(Comparator.comparing(PdServiceVersionEntity::getC_id).reversed());
        sortedSet.addAll(pdServiceNode.getPdServiceVersionEntities());
        pdServiceNode.setPdServiceVersionEntities(sortedSet);
        return pdServiceNode;
    }


    @Override
    @Transactional
    public PdServiceEntity addPdServiceAndVersion(PdServiceEntity pdServiceEntity) throws Exception {
        pdServiceEntity.setC_title(Util_TitleChecker.StringReplace(pdServiceEntity.getC_title()));


        //Default Version 생성
        PdServiceVersionEntity baseVerNode = new PdServiceVersionEntity();
        baseVerNode.setRef(TreeConstant.First_Node_CID);
        baseVerNode.setC_title("BaseVersion");
        baseVerNode.setC_type(TreeConstant.Leaf_Node_TYPE);

        // BaseVersion 기본 일자 오늘로 설정
        Date date = new Date();
        baseVerNode.setC_pds_version_start_date(DateUtils.format("yyyy/MM/dd HH:mm", DateUtils.getStartOfDate(date)));
        baseVerNode.setC_pds_version_end_date(DateUtils.format("yyyy/MM/dd HH:mm", DateUtils.getEndOfDate(date)));
        baseVerNode.setC_pds_version_contents("contents");
        baseVerNode.setC_pds_version_etc("etc");
        PdServiceVersionEntity baseNode = pdServiceVersion.addNode(baseVerNode);

        Set<PdServiceVersionEntity> treeset = new HashSet<>();
        treeset.add(baseNode);

        pdServiceEntity.setPdServiceVersionEntities(treeset);

        //제품(서비스) 데이터 등록
        PdServiceEntity addedNode = this.addNode(pdServiceEntity);

        // 제품(서비스) 등록 시 BaseDetail 추가
//        PdServiceDetailEntity pdServiceDetailEntity = new PdServiceDetailEntity();
//        pdServiceDetailEntity.setC_title("BaseDetail");
//        pdServiceDetailEntity.setC_contents("<p>제품(서비스)의 기획서 및 Project Charter 의 내용을 기록합니다.</p>");
//        pdServiceDetailEntity.setRef(TreeConstant.First_Node_CID);
//        pdServiceDetailEntity.setC_type(TreeConstant.Leaf_Node_TYPE);
//        pdServiceDetail.addNodeWithGlobalContentsTreeMap(addedNode.getC_id(), pdServiceDetailEntity);

        //제품(서비스) 생성시 - 요구사항 TABLE 생성
        //제품(서비스) 생성시 - 요구사항 STATUS TABLE 생성
        dynamicDBMaker.createSchema(addedNode.getC_id().toString());

        //C_ETC 컬럼에 요구사항 테이블 이름 기입
        addedNode.setC_pdservice_etc(TreeConstant.REQADD_PREFIX_TABLENAME + addedNode.getC_id().toString());
        this.updateNode(addedNode);

        return addedNode;
    }

    @Override
    @Transactional
    public PdServiceEntity addPdServiceVersion(PdServiceEntity pdServiceEntity) throws Exception {

        PdServiceEntity pdServiceNode = this.getNode(pdServiceEntity);
        Set<PdServiceVersionEntity> 디비_버전들 = pdServiceNode.getPdServiceVersionEntities();

        Set<PdServiceVersionEntity> 요청받은_버전들 = pdServiceEntity.getPdServiceVersionEntities();

        for (PdServiceVersionEntity 요청버전 : 요청받은_버전들) {
            PdServiceVersionEntity 검색결과 = 디비_버전들.stream()
                    .filter(디비버전 -> StringUtils.equalsIgnoreCase(요청버전.getC_title(), 디비버전.getC_title()))
                    .findAny()
                    .orElse(null);
            if (검색결과 == null) {
                PdServiceVersionEntity 추가된버전 = pdServiceVersion.addNode(요청버전);
                디비_버전들.add(추가된버전);
            } else {
                log.info("이미 존재하는 버전 = " + 요청버전);
            }
        }

        pdServiceNode.setPdServiceVersionEntities(디비_버전들);
        this.updateNode(pdServiceNode);

        return pdServiceNode;
    }

    @Override
    @Transactional
    public PdServiceEntity updatePdServiceVersion(Long pdservice_link, PdServiceVersionEntity pdServiceVersionEntity) throws Exception {

        PdServiceEntity searchNode = new PdServiceEntity();
        searchNode.setC_id(pdservice_link);
        PdServiceEntity savedNode = this.getNode(searchNode);

        Set<PdServiceVersionEntity> versionEntityList = savedNode.getPdServiceVersionEntities();
        for (PdServiceVersionEntity versionEntity : versionEntityList) {
            if (versionEntity.getC_id().equals(pdServiceVersionEntity.getC_id())) {
                versionEntity.setC_title(pdServiceVersionEntity.getC_title());
                versionEntity.setC_pds_version_start_date(pdServiceVersionEntity.getC_pds_version_start_date());
                versionEntity.setC_pds_version_end_date(pdServiceVersionEntity.getC_pds_version_end_date());
                versionEntity.setC_pds_version_etc(pdServiceVersionEntity.getC_pds_version_etc());
                versionEntity.setC_pds_version_contents(pdServiceVersionEntity.getC_pds_version_contents());
                pdServiceVersion.updateNode(versionEntity);
            }
        }


        return savedNode;
    }

    @Override
    @Transactional
    public Set<FileRepositoryEntity> uploadFileForPdServiceNode(Long pdservice_link, MultipartHttpServletRequest multiRequest) throws Exception {

        Set<FileRepositoryEntity> fileEntitySet = upload(multiRequest, fileRepository);

        Set<Long> fileCids = new HashSet<>();

        for (FileRepositoryEntity file : fileEntitySet) {

            GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
            globalTreeMap.setPdservice_link(pdservice_link);
            globalTreeMap.setFilerepository_link(file.getC_id());
            List<GlobalTreeMapEntity> searchList = globalTreeMapService.findAllBy(globalTreeMap);
            if (searchList == null || searchList.isEmpty()) {
                GlobalTreeMapEntity savedMap = globalTreeMapService.saveOne(globalTreeMap);
                fileCids.add(savedMap.getFilerepository_link());
            } else {
                log.info("already registe PdService = " + pdservice_link + " & FileRepo = " + globalTreeMap.getFilerepository_link());
                fileCids.add(globalTreeMap.getFilerepository_link());
            }

        }

        Set<FileRepositoryEntity> returnSet = new HashSet<>();
        for (Long fileCid : fileCids) {

            FileRepositoryEntity entity = new FileRepositoryEntity();
            entity.setC_id(fileCid);
            returnSet.add(fileRepository.getNode(entity));

        }

        return returnSet;
    }

    public Set<FileRepositoryEntity> upload(MultipartHttpServletRequest multiRequest,
                                            FileRepository fileRepository) throws Exception {

        // Spring multipartResolver 사용시
        PropertiesReader propertiesReader = new PropertiesReader("com/arms/egovframework/property/globals.properties");
        String uploadDir = propertiesReader.getProperty("Globals.fileStorePath");
        long maxFileSize = TreeConstant.MAX_UPLOAD_FILESIZE;

        List<EgovFormBasedFileVo> list = EgovFileUploadUtil.uploadFiles(multiRequest, uploadDir, maxFileSize);

        Set<FileRepositoryEntity> fileRepositoryEntities = new HashSet<>();

        for (EgovFormBasedFileVo egovFormBasedFileVo : list) {

            FileRepositoryEntity fileRepositoryEntity = new FileRepositoryEntity();
            fileRepositoryEntity.setFileName(egovFormBasedFileVo.getFileName());
            fileRepositoryEntity.setContentType(egovFormBasedFileVo.getContentType());
            fileRepositoryEntity.setServerSubPath(egovFormBasedFileVo.getServerSubPath());
            fileRepositoryEntity.setPhysicalName(egovFormBasedFileVo.getPhysicalName());
            fileRepositoryEntity.setSize(egovFormBasedFileVo.getSize());
            fileRepositoryEntity.setName(egovFormBasedFileVo.getName());

            fileRepositoryEntity.setUrl(egovFormBasedFileVo.getUrl());
            //TODO: 썸네일 개발 필요
            fileRepositoryEntity.setThumbnailUrl(egovFormBasedFileVo.getThumbnailUrl());

            fileRepositoryEntity.setDelete_url(egovFormBasedFileVo.getDelete_url());
            fileRepositoryEntity.setDelete_type(egovFormBasedFileVo.getDelete_type());

            fileRepositoryEntity.setRef(TreeConstant.First_Node_CID);
            fileRepositoryEntity.setC_title("for PdService");
            fileRepositoryEntity.setC_type("default");

            FileRepositoryEntity returnFileRepositoryEntity = fileRepository.addNode(fileRepositoryEntity);
            //delete 파라미터인 id 값을 업데이트 치기 위해서.
            fileRepositoryEntity.setUrl("/auth-user/api/arms/fileRepository" + "/downloadFileByNode/" + returnFileRepositoryEntity.getId());
            fileRepositoryEntity.setThumbnailUrl("/auth-user/api/arms/fileRepository" + "/thumbnailUrlFileToNode/" + returnFileRepositoryEntity.getId());
            fileRepositoryEntity.setDelete_url("/auth-user/api/arms/fileRepository" + "/deleteFileByNode/" + returnFileRepositoryEntity.getId());

            fileRepository.updateNode(fileRepositoryEntity);

            fileRepositoryEntities.add(fileRepositoryEntity);

        }

        return fileRepositoryEntities;
    }

    @Override
    @Transactional
    public PdServiceEntity removeVersionNode(Long pdServiceID, Long versionID) throws Exception {

        PdServiceEntity pdService = new PdServiceEntity();
        pdService.setC_id(pdServiceID);
        PdServiceEntity savedPdServiceNode = this.getNode(pdService);

        Set<PdServiceVersionEntity> versionSet = savedPdServiceNode.getPdServiceVersionEntities();
        PdServiceVersionEntity 지울_버전엔티티 = versionSet.stream().filter(entity -> entity.getC_id().equals(versionID)).findFirst().orElse(null);
        String 지울_버전_이름 = "";
        if (지울_버전엔티티 != null) {
            지울_버전_이름 = 지울_버전엔티티.getC_title();
            versionSet.remove(지울_버전엔티티);
        }
        this.updateNode(pdService);

        PdServiceVersionEntity 삭제대상버전 = new PdServiceVersionEntity();
        삭제대상버전.setC_id(versionID);
        pdServiceVersion.removeNode(삭제대상버전);

        if (StringUtils.isNotEmpty(지울_버전_이름)) {
            chat.sendMessageByEngine(savedPdServiceNode.getC_title() + "의 버전 " + 지울_버전_이름 + "이(가) 삭제되었습니다.");
        }
        return pdService;
    }

    @Override
    public PdServiceD3Chart getD3ChartData() throws Exception {

        List<PdServiceEntity> 서비스_리스트 = this.getNodesWithoutRoot(new PdServiceEntity());

        if (서비스_리스트.isEmpty()) {
            return null;
        } else {
            List<Long> 버전아이디_목록 = new ArrayList<>();
            for (PdServiceEntity 서비스엔티티 : 서비스_리스트) {
                Set<PdServiceVersionEntity> 버전엔티티_세트 = 서비스엔티티.getPdServiceVersionEntities();
                버전아이디_목록.addAll(버전엔티티_세트.stream().map(버전 -> 버전.getC_id()).collect(Collectors.toList()));
            }
            // 1.버전 - 서비스 / 버전 - 지라프로젝트가 있는 엔티티 획득.
            List<GlobalTreeMapEntity> 버전_중심_글로벌트리맵엔티티_목록 = globalTreeMapService.findAllByIds(버전아이디_목록, "pdserviceversion_link");
            // 2.버전 - 지라프로젝트 아이디만 솎아내기
            List<Long> 연관_지라프로젝트_아이디_목록 = 버전_중심_글로벌트리맵엔티티_목록.stream()
                    .map(GlobalTreeMapEntity::getJiraproject_link)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            JiraProjectPureEntity 프로젝트_퓨어_검색 = new JiraProjectPureEntity();
            프로젝트_퓨어_검색.getCriterions().add(Restrictions.in("c_id", 연관_지라프로젝트_아이디_목록));
            // 3. 솎아낸 아이디만 모아서, 지라프로젝트 엔티티 가져오기
            List<JiraProjectPureEntity> 지라프로젝트_목록 = jiraProjectPure.getChildNode(프로젝트_퓨어_검색);
            // 4. 지라프로젝트 맵 생성 (K-V) - (아이디-명)
            Map<Long, String> 지라프로젝트_맵 = 지라프로젝트_목록.stream()
                    .collect(Collectors.toMap(JiraProjectPureEntity::getC_id, JiraProjectPureEntity::getC_jira_name));

            List<GlobalTreeMapEntity> 유니크_지라프로젝트_서버_트리맵 = globalTreeMapService
                    .findAllByIds(연관_지라프로젝트_아이디_목록, "jiraproject_link").stream()
                    .filter(글로벌트리맵엔티티 -> 글로벌트리맵엔티티.getJiraserver_link() != null) // 이 시점에 server와 project 만 있다.
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(
                                    Comparator.comparing(entity -> entity.getJiraproject_link() + "|" + entity.getJiraserver_link()))),
                            ArrayList::new));

            List<PdServiceD3Chart> 레벨2_서비스_리스트 =
                    서비스_리스트.parallelStream().map(
                            서비스 -> {
                                Set<PdServiceVersionEntity> 서비스_버전_리스트 = 서비스.getPdServiceVersionEntities();

                                List<PdServiceD3Chart> 레벨3_버전_리스트 = 서비스_버전_리스트.stream().map(
                                        버전 -> {
                                            List<Long> 해당버전_지라프로젝트_아이디_목록 = 버전_중심_글로벌트리맵엔티티_목록.stream()
                                                    .filter(엔티티 -> Objects.equals(엔티티.getPdserviceversion_link(), 버전.getC_id()))
                                                    .map(GlobalTreeMapEntity::getJiraproject_link).collect(Collectors.toList());

                                            List<PdServiceD3Chart> 레벨4_지라프로젝트_목록 = new ArrayList<>();

                                            for (Long 프로젝트_아이디 : 해당버전_지라프로젝트_아이디_목록) {
                                                if (Objects.isNull(프로젝트_아이디)) {
                                                    continue;
                                                }

                                                String 지라프로젝트_명 = 지라프로젝트_맵.get(프로젝트_아이디);
                                                Long 지라서버_아이디 = 유니크_지라프로젝트_서버_트리맵.stream()
                                                        .filter(프로젝트_서버_트리맵 -> Objects.equals(프로젝트_서버_트리맵.getJiraproject_link(), 프로젝트_아이디))
                                                        .map(GlobalTreeMapEntity::getJiraserver_link).findFirst().orElse(0L);

                                                JiraServerPureEntity 지라서버검색 = new JiraServerPureEntity();
                                                지라서버검색.setC_id(지라서버_아이디);

                                                String 지라서버명 = "";

                                                try {
                                                    JiraServerPureEntity 지라서버퓨어_엔티티 = jiraServerPure.getNode(지라서버검색);
                                                    지라서버명 += "[" + 지라서버퓨어_엔티티.getC_jira_server_name() + "]";
                                                } catch (Exception e) {
                                                    log.info("PdServiceImpl :: getD3ChartData :: error = " + e.getMessage());
                                                    continue;
                                                }
                                                if (!지라서버명.isBlank()) {
                                                    레벨4_지라프로젝트_목록.add(PdServiceD3Chart.builder()
                                                            .type("Jira")
                                                            .name(지라서버명 + " " + 지라프로젝트_명)
                                                            .build());
                                                }
                                            }
                                            return PdServiceD3Chart.builder()
                                                    .type("Version")
                                                    .name(버전.getC_title())
                                                    .children(레벨4_지라프로젝트_목록).build();

                                        }).collect(Collectors.toList()); //레벨3

                                return PdServiceD3Chart.builder()
                                        .type("PdService")
                                        .name(서비스.getC_title())
                                        .children(레벨3_버전_리스트).build();

                            }).collect(Collectors.toList()); // 레벨2_서비스_리스트

            return PdServiceD3Chart.builder()
                    .name("a-RMS") //레벨1
                    .children(레벨2_서비스_리스트)
                    .build();
        }

    }

    @Override
    @Transactional
    public int removeAll(Long pdServiceId) throws Exception {
        // 1. 제품 및 버전 삭제
        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(pdServiceId);
        this.removeNode(pdServiceEntity);

        List<Long> pdServiceDetailLinks = globalContentsTreeMapService.findAllByIds(List.of(pdServiceId), "pdservice_link").stream()
                .filter(globalContentsTreeMapEntity -> globalContentsTreeMapEntity.getPdservicedetail_link() != null)
                .map(GlobalContentsTreeMapEntity::getPdservicedetail_link)
                .collect(Collectors.toList());

        // 2. 제품 디테일 및 파일 삭제
        for (Long pdServiceDetailLink : pdServiceDetailLinks) {
            pdServiceDetail.deleteAll(pdServiceDetailLink);
        }

        return 1;
    }
}
