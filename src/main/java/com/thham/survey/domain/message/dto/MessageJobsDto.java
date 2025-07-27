package com.thham.survey.domain.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thham.survey.domain.message.constants.JobStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageJobsDto(Long id, JobStatus status, String errorMessage) {
}
