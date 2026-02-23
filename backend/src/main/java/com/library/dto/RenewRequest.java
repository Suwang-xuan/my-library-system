package com.library.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class RenewRequest implements Serializable {

    private Long recordId;

    private Integer renewDays;

}
