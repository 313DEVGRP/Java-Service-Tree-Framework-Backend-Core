package com.arms.globaltreemap.model;

import org.springframework.data.jpa.domain.Specification;

public class GlobalTreeMapSpecification {

    public static Specification<GlobalTreeMapEntity> equalMap_key(Long map_key) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("map_key"), map_key);
    }

    public static Specification<GlobalTreeMapEntity> equalFilerepository_link(Long filerepository_link) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("filerepository_link"), filerepository_link);
    }

    public static Specification<GlobalTreeMapEntity> equalJiraissuepriority_link(Long jiraissuepriority_link) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("jiraissuepriority_link"), jiraissuepriority_link);
    }

    public static Specification<GlobalTreeMapEntity> equalPdservice_link(Long pdservice_link) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("pdservice_link"), pdservice_link);
    }

    public static Specification<GlobalTreeMapEntity> equalPdserviceversion_link(Long pdserviceversion_link) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("pdserviceversion_link"), pdserviceversion_link);
    }

}
