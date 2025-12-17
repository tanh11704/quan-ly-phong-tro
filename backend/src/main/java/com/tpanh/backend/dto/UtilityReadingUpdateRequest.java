package com.tpanh.backend.dto;

import lombok.Data;

@Data
public class UtilityReadingUpdateRequest {
    private Integer electricIndex;
    private Integer waterIndex;
    private String imageEvidence;
}
