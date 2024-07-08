package com.song.songdada.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewRequest implements Serializable {

    private Long id;

    /**
     * 状态：0-待审核，1-审核通过，2-审核不通过
     */
    private Integer reviewStatus;

    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
