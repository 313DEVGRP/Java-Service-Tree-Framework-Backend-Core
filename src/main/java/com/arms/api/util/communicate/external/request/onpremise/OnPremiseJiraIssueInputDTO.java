package com.arms.api.util.communicate.external.request.onpremise;

import com.arms.api.util.communicate.external.request.cloud.FieldsDTO;
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
