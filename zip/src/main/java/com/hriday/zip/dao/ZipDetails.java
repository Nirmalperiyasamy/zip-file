package com.hriday.zip.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
public class ZipDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String fileName;

    private String status;

    private Long uploadedSize;

    private Long compressedSize;

    private String uploadedTime;

    private String downloadedTime;
}
