package com.library.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ReturnRequest implements Serializable {

    private Long recordId;

    private Long operatorId;

}
