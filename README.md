# Java-Service-Tree-Framework-All-In-One

![스프링클라우드 구조 확장](https://user-images.githubusercontent.com/17264665/137580215-bcd404a2-90c7-40dd-91d9-48a58ffffccf.png)

* Spring Security 와 Keycloak 을 연동한 인증 인가 시스템 적용 ( Spring security 설정과 동작을 분리 )
* Spring OAuth2RestTemplate 을 활용한 연관 시스템간의 통신 적용 완료 ( 사용자 베이스 통신 동작을 분리 )
* Docker file 을 자동으로 구성하도록 설정
* Artifact Version 을 자동으로 구성하도록 설정
* ZuulProxy 를 활용한 MSA Gateway 구성
* Zipkin 을 활용한 Request Flow 추적

## Java Service Tree Framework By 313DEVGRP ##

    * 313 DEV GRP는 2010년 12월에 출범한 자바 개발자 그룹입니다.
    * TSF(jsTree Service Framework)는 MIT License로 배포됩니다.
    * 개인, 기업, 정부에서 무료로 사용이 가능합니다.

### Java Service Tree Framework란: ###

MVC패턴을 구현한 다양한 프레임워크를 대상으로 ( EgovFramework 기반한 ) 서비스레이어의 도메인 객체를 Tree Base Object로 간주하여 서비스 - DAO - DTO - DB를 정형화한 기반 구현체를 제공하는 프레임워크 입니다.

특장점 :

    * 중앙집중형 아키텍쳐
    * 확장(extend)을 염두해 둔 객체지향 개발 방식
    * 안정성
    * Speeeeeeeeeeed~!

### Java Service Tree Framework에서 사용한 기술 ###

> Part              | Detail Skill
> -------------     | -------------
> View Part 	    | html+css, Bootstrap, RequireJS, Bower, Grunt, AngularJS, jQuery, Qunit, Flex, Json, xml, BlazeDS
> Server Part	    | Apache(modjk), Nginx, Tomcat, Resin, Jetty, SiteMash, Tiles, FreeMarker, Velocity
> Framework Part    | Struts, Spring, iBatis, Hibernate, Spring-integration, Spring-security, Spring-Boot, Spring-DW, Spring-WebFlow, Spring-Data(JPA), Spring-Batch, Spring-WebServices, Spring-Mobile, Spring-MVC
> Tool Part         | Quartz, Ehcache, MemCache, Redis, Apache-Commons, EgovFramework(Component)
> CI Part           | Junit, Maven, Hudson, Jenkins, Bamboo, Nexus, Jira, Fisheye, Crucible, Confluence, Sonar
> Database Part     | MySql, Oracle, MS-sql, postgres, Hadoop, Storm, Spark, Cassandra, MongoDB
> Mobile Part       | Android, PhoneGap
> Search Engine Part| Lucene, Elastic Search, Kibana, Logstash, Beats
> Management Part   | PMBOK, MicroService, CBD, PLE, Prototype, PMS, ALM
> Virtual Image Part| Docker
> Microservice Part | Netty - Zookeeper - Finagle




### jsTree Service Framework을 지속적으로 성장시키는 툴 ###

> [ -> 형상관리 바로가기](https://github.com/jstree) : https://github.com/jstree
>
> [ -> 이슈관리 바로가기](https://www.313.co.kr/jira) : https://www.313.co.kr/jira
>
> [ -> 문서관리 바로가기](https://www.313.co.kr/confluence) : https://www.313.co.kr/confluence
>
> [ -> 리뷰관리 바로가기](https://www.313.co.kr/fecru) : https://www.313.co.kr/fecru
>
> [ -> 빌드관리 바로가기](https://www.313.co.kr/bamboo) : https://www.313.co.kr/bamboo
>
> [ -> 품질관리 바로가기](https://www.313.co.kr/sonar) : https://www.313.co.kr/sonar
>
> [ -> 아티팩트관리 바로가기](https://www.313.co.kr/nexus) : https://www.313.co.kr/nexus
>
> [ -> jsTree Service Framework 관리 샘플 사이트](https://www.313.co.kr/index.do) : https://www.313.co.kr/index.do
>
> [ -> jsTree Service Framework 사용 샘플 사이트](https://www.313.co.kr/) : https://www.313.co.kr




### Those who support us! ###
![YourKit](https://www.yourkit.com/images/yklogo.png)
> YourKit, LCC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/) and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
> innovative and intelligent tools for profiling Java and .NET applications.


### Those who support us! ###
1. interceptor 에서 관리되는 동적 테이블은 onetoX 에 해당하는 릴레이션을 관리 할 수 없다.
2. Entity 릴레이션 설정 시. 단방향 설정을 하려거든 (cascade = CascadeType.ALL) 을 삭제하라