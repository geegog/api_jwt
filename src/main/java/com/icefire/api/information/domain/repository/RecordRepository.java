package com.icefire.api.information.domain.repository;

import com.icefire.api.information.domain.model.Record;
import com.icefire.api.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findAllByUser(User user);

}
