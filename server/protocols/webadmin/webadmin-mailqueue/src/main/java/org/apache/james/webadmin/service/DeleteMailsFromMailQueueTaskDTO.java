package org.apache.james.webadmin.service;

import java.util.Optional;
import java.util.function.Function;

import org.apache.james.core.MailAddress;
import org.apache.james.json.DTOModule;
import org.apache.james.queue.api.MailQueueFactory;
import org.apache.james.queue.api.ManageableMailQueue;
import org.apache.james.server.task.json.dto.TaskDTO;
import org.apache.james.server.task.json.dto.TaskDTOModule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fge.lambdas.Throwing;

public class DeleteMailsFromMailQueueTaskDTO implements TaskDTO {

    public static final Function<MailQueueFactory<ManageableMailQueue>, TaskDTOModule<DeleteMailsFromMailQueueTask, DeleteMailsFromMailQueueTaskDTO>> MODULE = (mailQueueFactory) ->
        DTOModule
            .forDomainObject(DeleteMailsFromMailQueueTask.class)
            .convertToDTO(DeleteMailsFromMailQueueTaskDTO.class)
            .toDomainObjectConverter(dto -> dto.fromDTO(mailQueueFactory))
            .toDTOConverter(DeleteMailsFromMailQueueTaskDTO::toDTO)
            .typeName(DeleteMailsFromMailQueueTask.TYPE.asString())
            .withFactory(TaskDTOModule::new);

    public static DeleteMailsFromMailQueueTaskDTO toDTO(DeleteMailsFromMailQueueTask domainObject, String typeName) {
        return new DeleteMailsFromMailQueueTaskDTO(
            typeName,
            domainObject.getQueue().getName(),
            domainObject.getMaybeSender().map(MailAddress::asString),
            domainObject.getMaybeName(),
            domainObject.getMaybeRecipient().map(MailAddress::asString)
        );
    }

    private final String type;
    private final String queue;
    private final Optional<String> sender;
    private final Optional<String> name;
    private final Optional<String> recipient;

    public DeleteMailsFromMailQueueTaskDTO(@JsonProperty("type") String type,
                                           @JsonProperty("queue") String queue,
                                           @JsonProperty("sender") Optional<String> sender,
                                           @JsonProperty("name") Optional<String> name,
                                           @JsonProperty("recipient") Optional<String> recipient) {
        this.type = type;
        this.queue = queue;
        this.sender = sender;
        this.name = name;
        this.recipient = recipient;
    }

    public DeleteMailsFromMailQueueTask fromDTO(MailQueueFactory<ManageableMailQueue> mailQueueFactory) {
        return new DeleteMailsFromMailQueueTask(
            mailQueueFactory.getQueue(queue).orElseThrow(() -> new DeleteMailsFromMailQueueTask.UnknownSerializedQueue(queue)),
            sender.map(Throwing.<String, MailAddress>function(MailAddress::new).sneakyThrow()),
            name,
            recipient.map(Throwing.<String, MailAddress>function(MailAddress::new).sneakyThrow())
        );
    }

    @Override
    public String getType() {
        return type;
    }

    public String getQueue() {
        return queue;
    }

    public Optional<String> getSender() {
        return sender;
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<String> getRecipient() {
        return recipient;
    }
}