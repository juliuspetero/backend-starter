package com.mojagap.backenstarter.repository.user;

import com.mojagap.backenstarter.model.user.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Integer> {
}
