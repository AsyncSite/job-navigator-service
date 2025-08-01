package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JobTechStackId implements Serializable {
    private Long jobPosting;
    private Long techStack;
}