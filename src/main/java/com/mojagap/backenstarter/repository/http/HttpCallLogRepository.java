package com.mojagap.backenstarter.repository.http;

import com.mojagap.backenstarter.model.http.HttpCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HttpCallLogRepository extends JpaRepository<HttpCallLog, Integer> {
}
