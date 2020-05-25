package com.bytearch.generator.domain.entity;

import java.util.Date;
import javax.persistence.*;
import lombok.Data;

@Data
@Table(name = "article")
public class Article {
    /**
     * 文章id
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 作者id
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 文章状态 -1: 删除 1:草稿 2:已发布
     */
    private Byte status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}