package com.library.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BorrowRequest implements Serializable {

    private Long bookId;

    private Long readerId;

    private Long operatorId;

}
