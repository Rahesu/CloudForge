package com.cloudforge.application.repository;

import com.cloudforge.application.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile,Long> {

    List<ProjectFile> findByProjectId(Long projectId);


    Optional<ProjectFile> findByIdAndProjectId(Long id,Long projectId);


}
