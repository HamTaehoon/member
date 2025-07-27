package com.thham.survey.domain.message.repository;

import com.thham.survey.domain.message.entity.MessageJobs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJobRepository extends JpaRepository<MessageJobs, Long> {
}
