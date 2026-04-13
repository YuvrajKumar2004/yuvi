package com.yuvraj.repository;

import com.yuvraj.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Notificationrepository extends JpaRepository<Notification,Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
