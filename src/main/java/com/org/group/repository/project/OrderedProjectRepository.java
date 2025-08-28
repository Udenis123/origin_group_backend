package com.org.group.repository.project;

import com.org.group.model.project.OrderedProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderedProjectRepository extends JpaRepository<OrderedProject, UUID> {
}
