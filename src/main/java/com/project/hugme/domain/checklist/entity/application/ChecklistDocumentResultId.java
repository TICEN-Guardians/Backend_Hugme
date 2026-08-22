package com.project.hugme.domain.checklist.entity.application;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistDocumentResultId implements Serializable {

    @Column(name = "application_id")
    private Long applicationId;


    @Column(name = "document_id")
    private Long documentId;


}
