package com.bookingnwt.notificationservice.mapper;

import com.bookingnwt.notificationservice.dto.MessageResponseDTO;
import com.bookingnwt.notificationservice.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "conversation.id", target = "conversationId")
    MessageResponseDTO toDTO(Message message);
}
