package com.bookingnwt.notificationservice.mapper;

import com.bookingnwt.notificationservice.dto.ConversationRequestDTO;
import com.bookingnwt.notificationservice.dto.ConversationResponseDTO;
import com.bookingnwt.notificationservice.model.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "messages", ignore = true)
    Conversation toEntity(ConversationRequestDTO dto);

    ConversationResponseDTO toDTO(Conversation conversation);
}
