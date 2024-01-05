package com.arms.egovframework.javaservice.treeframework.model;

import lombok.*;

import javax.persistence.MappedSuperclass;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class TreeBaseDTO {

/*
    ⊰ Select ™⊱
        [ getNode.do ]
            © c_id -> ( c_id 값의 Node Return )
        [ getChildNode.do ]
            © c_id -> ( c_id 값의 Node 하위 ( 자식 ) Node Return )
        [ getPaginatedChildNode.do ]
            © c_id -> ( c_id 값의 Node 하위 ( 자식 ) Node를 페이징 처리하여 Return )

    ⊰ Insert ™⊱
        [ addNode.do ]
            © ref -> ( 어느 부모 아이디 밑으로 추가할 건지 )
            © c_position -> ( 추가될 때 어느 포지션 위치 값으로 들어 갈 건지 )
            © c_title -> ( 추가될 노드의 값 )
            © c_type -> ( Branch 인 경우는 folder, Leaf 인 경우는 default )

    ⊰ Update ™⊱
        [ updateNode.do ]
            © c_id -> ( 어느 노드를 업데이트 할 것인지 )
            © update field -> ( 업데이트할 데이터만 셋팅하여 요청하면 값이 있는 데이터만 변경 )

    ⊰ Delete ™⊱
        [ removeNode.do ]
            © c_id -> ( 어느 노드를 삭제 할 것인지 )
            -> folder 인경우 -> ( 하위 childNode 까지 삭제 )
            -> default 인경우 -> ( c_id 노드만 삭제 )

    ⊰ Move ™⊱
        [ moveNode.do ]
            © c_id -> ( 어느 노드를 이동 시킬건지 )
            © ref -> ( 어느 부모노드 아이디 밑으로 이동할 건지 )
            © c_position -> ( 이동할 때 어느 포지션 위치 값으로 들어 갈 건지 )
            © c_title -> ( copy&paste 인 경우 노드 이름을 재설정 가능 )
            © copy -> ( copy&paste 인 경우 [1] move 인 경우 [0] )
            © multiCounter -> ( 여러개의 노드를 선택하여 move 할 경우 자동 카운터 )
 */

    private Long c_id;
    private long ref;
    private Long c_position;
    private String c_title;
    private String c_type;
    private long copy;
    private long multiCounter;

}
