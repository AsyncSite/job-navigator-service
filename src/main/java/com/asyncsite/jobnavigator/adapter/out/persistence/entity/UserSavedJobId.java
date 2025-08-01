package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSavedJobId implements Serializable {
    private Long userId;
    private Long jobPosting;
}