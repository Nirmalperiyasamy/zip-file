package com.hriday.zip.repository;

import com.hriday.zip.dao.ZipDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZipRepo extends JpaRepository<ZipDetails, Integer> {

    ZipDetails findByFileName(String fileName);

}
