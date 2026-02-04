package com.a404.duckonback.common.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configurable
@EnableJpaAuditing
public class JpaAuditingConfig {
}
