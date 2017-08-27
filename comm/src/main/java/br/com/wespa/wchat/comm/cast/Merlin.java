package br.com.wespa.wchat.comm.cast;

import br.com.wespa.wchat.comm.dto.MessageDTO;
import br.com.wespa.wchat.persistency.models.Message;

/**
 * Created by dsouza on 26/08/17.
 */

public class Merlin extends MerlinCast {

    public MessageDTO Cast(Message model, Class<MessageDTO> dtoClass) {
        if (model == null)
            return null;

        MessageDTO dto = new MessageDTO();

        dto.content = model.getContent();
        dto.sender = model.getSender();
        dto.createdAt = model.getCreatedAt();
        dto.uuid = model.getUuid();

        return dto;
    }
}
