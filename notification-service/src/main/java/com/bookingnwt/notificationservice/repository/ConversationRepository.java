package com.bookingnwt.notificationservice.repository;

import com.bookingnwt.notificationservice.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByGuestId(Long guestId);
    List<Conversation> findByHostId(Long hostId);
    List<Conversation> findByPropertyId(Long propertyId);
}
