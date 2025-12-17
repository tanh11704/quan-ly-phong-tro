package com.tpanh.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UtilityReadingResponse {
    private Integer id;
    private Integer roomId;
    private String roomNo;
    private String month;
    private Integer electricIndex;
    private Integer waterIndex;
    private String imageEvidence;
    private LocalDateTime createdAt;
}
