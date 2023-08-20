package com.arms.util.external_communicate.dto.onpremise;

import com.arms.util.external_communicate.dto.cloud.FieldsDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OnPremiseJiraIssueInputDTO {
    private FieldsDTO fields;
}
