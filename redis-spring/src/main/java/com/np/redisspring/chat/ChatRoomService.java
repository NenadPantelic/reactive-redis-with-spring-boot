package com.np.redisspring.chat;

import org.redisson.api.RListReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class ChatRoomService implements WebSocketHandler {

    private final RedissonReactiveClient client;

    public ChatRoomService(RedissonReactiveClient client) {
        this.client = client;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // publisher - a person who types some text in the browser
        // subscriber - server
        String room = getChatRoomName(session);
        // the channel for communication between two sides (their chat)
        RTopicReactive topic = this.client.getTopic(room, StringCodec.INSTANCE);
        // to preserve the message history
        RListReactive<String> list = this.client.getList("history:" + room, StringCodec.INSTANCE);

        session.receive()
                // message received
                .map(WebSocketMessage::getPayloadAsText)
                // if we have multiple instances of the same WS server, we should broadcast the message received from a client
                // so the server that's connected to the recipient could send that message to him
                // broadcast it to other instance
                .flatMap(msg -> list.add(msg).then(topic.publish(msg)))
                .doOnError(System.out::println)
                .doFinally(s -> System.out.println("Subscribed finally " + s))
                .subscribe();

        // publisher
        // all text messages
        Flux<WebSocketMessage> flux = topic.getMessages(String.class)
                .startWith(list.iterator()) // load when the connection starts; chat history
                .map(session::textMessage)
                .doOnError(System.out::println)
                .doFinally(s -> System.out.println("Published finally " + s));
        return session.send(flux);
    }

    private String getChatRoomName(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams() // multivalue map
                .toSingleValueMap()
                .getOrDefault("room", "default");
    }
}
