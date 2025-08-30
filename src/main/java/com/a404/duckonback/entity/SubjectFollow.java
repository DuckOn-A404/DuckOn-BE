package com.a404.duckonback.entity;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.entity.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="subject_follow")
@IdClass(SubjectFollowId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectFollow {
    @Id
    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="subject_id", nullable=false)
    private Subject subject;

    @Id @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
}
