package com.cloudforge.application.repository;

import com.cloudforge.application.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOwnerId(
            Long ownerId,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM Project p
        WHERE p.owner.id = :ownerId
        AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Project> searchProjects(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            Pageable pageable
    );
}