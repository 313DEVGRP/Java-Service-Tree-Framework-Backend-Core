package com.arms.config;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.arms.egovframework.javaservice.treeframework.util.PaginationInfo;
import com.fasterxml.classmate.TypeResolver;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class Swagger2Config {

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(ModelMap.class, HttpServletRequest.class)
                .directModelSubstitute(TreeBaseEntity.class, Enum.class)
                .directModelSubstitute(PaginationInfo.class, Enum.class)
                .directModelSubstitute(Projection.class, Enum.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .enable(true)
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(List.class, Order.class),
                                typeResolver.resolve(Enum.class)
                        )
                );
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Java Service Tree Framework")
                .description(
                    "<span style='color: #e5603b;font-size: 18px;'><strong>&#8880; Select &trade;&#8881;</strong></span>\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ getNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_id</span> -> ( c_id 값의 Node Return )\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ getChildNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_id</span> -> ( c_id 값의 Node 하위 ( 자식 ) Node Return )\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ getPaginatedChildNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_id</span> -> ( c_id 값의 Node 하위 ( 자식 ) Node를 페이징 처리하여 Return )\n\n" +

                    "<br><br>" +
                    "<span style='color: #e5603b;font-size: 18px;'><strong>&#8880; Insert &trade;&#8881;</strong></span>\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ addNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; ref</span> -> ( 어느 부모 아이디 밑으로 추가할 건지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_position</span> -> ( 추가될 때 어느 포지션 위치 값으로 들어 갈 건지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_title</span> -> ( 추가될 노드의 값 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_type</span> -> ( Branch 인 경우는 folder, Leaf 인 경우는 default )\n\n" +

                    "<br><br>" +
                    "<span style='color: #e5603b;font-size: 18px;'><strong>&#8880; Update &trade;&#8881;</strong></span>\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ updateNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_id</span> -> ( 어느 노드를 업데이트 할 것인지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; update field</span> -> ( 업데이트할 데이터만 셋팅하여 요청하면 값이 있는 데이터만 변경 )\n\n" +

                    "<br><br>" +
                    "<span style='color: #e5603b;font-size: 18px;'><strong>&#8880; Delete &trade;&#8881;</strong></span>\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ removeNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_id</span> -> ( 어느 노드를 삭제 할 것인지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>-> folder 인경우</span> -> ( 하위 childNode 까지 삭제 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>-> default 인경우</span> -> ( c_id 노드만 삭제 )\n\n" +

                    "<br><br>" +
                    "<span style='color: #e5603b;font-size: 18px;'><strong>&#8880; Move &trade;&#8881;</strong></span>\n\n" +

                    "&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #E49400; font-size: 16px;'>[ moveNode.do ]</span>\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_id</span> -> ( 어느 노드를 이동 시킬건지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; ref</span> -> ( 어느 부모노드 아이디 밑으로 이동할 건지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_position</span> -> ( 이동할 때 어느 포지션 위치 값으로 들어 갈 건지 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; c_title</span> -> ( copy&paste 인 경우 노드 이름을 재설정 가능 )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; copy</span> -> ( copy&paste 인 경우 [1] move 인 경우 [0] )\n\n" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: #2477FF; font-size: 14px;'>&copy; multiCounter</span> -> ( 여러개의 노드를 선택하여 move 할 경우 자동 카운터 )\n\n"

                )
                .contact(new Contact("313 DEV GRP", "www.313.co.kr", "313cokr@gmail.com"))
                .version("1.0")
                .build();
    }
}
