package com.assignment.question;

@WithBuilder
public class MessageBuilder {

    private MessageType messageType;
    private String content;
    private String sender;
    private String recipient;
    private boolean isDelivered;
    private long timestamp;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private MessageBuilder messageBuilder;

        public Builder() {
            messageBuilder = new MessageBuilder();
        }

        public Builder messageType(MessageType messageType) {
            messageBuilder.messageType = messageType;
            return this;
        }

        public Builder content(String content) {
            messageBuilder.content = content;
            return this;
        }

        public Builder sender(String sender) {
            messageBuilder.sender = sender;
            return this;
        }

        public Builder recipient(String recipient) {
            messageBuilder.recipient = recipient;
            return this;
        }

        public Builder isDelivered(boolean isDelivered) {
            messageBuilder.isDelivered = isDelivered;
            return this;
        }

        public Builder timestamp(long timestamp) {
            messageBuilder.timestamp = timestamp;
            return this;
        }

        public MessageBuilder build() {

            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.messageType = this.messageBuilder.messageType;
            messageBuilder.content = this.messageBuilder.content;
            messageBuilder.sender = this.messageBuilder.sender;
            messageBuilder.recipient = this.messageBuilder.recipient;
            messageBuilder.isDelivered = this.messageBuilder.isDelivered;
            messageBuilder.timestamp = this.messageBuilder.timestamp;
            return messageBuilder;
        }
    }

}