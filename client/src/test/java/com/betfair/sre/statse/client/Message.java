package com.betfair.sre.statse.client;

class Message {
    private final String header;
    private final String body;

    Message(String header, String body) {
        this.header = header;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return body.equals(message.body) && header.equals(message.header);

    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
            "header='" + header + '\'' +
            ", body='" + body + '\'' +
            '}';
    }
}
