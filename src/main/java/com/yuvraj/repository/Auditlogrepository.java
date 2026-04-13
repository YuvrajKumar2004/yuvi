package com.yuvraj.repository;

import com.yuvraj.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Auditlogrepository extends JpaRepository<AuditLog,Long> {
}
