package fr.utc.sr03.chat.websocket;

import com.google.gson.JsonObject;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Optional;

@Component
@ServerEndpoint(value="/WsChat/{chatId}/{userId}", configurator= WebSocketServer.EndpointConfigurator.class)
public class WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

    //Pour pouvoir intéragir entre les différentes communications ouvertes
    private static WebSocketServer singleton;
    private final Hashtable<String, Session> sessions = new Hashtable<>();

    private static  UserRepository userRepository;
    private WebSocketServer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //+++++++++++++++++++++++++++++++++++++++++++
    // CONFIG
    // - Singleton => Permet de ne pas avoir une instance différente par client
    // - Le configurateur utilise le singleton
    //+++++++++++++++++++++++++++++++++++++++++++
    public static class EndpointConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getEndpointInstance(Class<T> endpointClass) {
            return (T) WebSocketServer.getInstance();
        }
    }

    public static WebSocketServer getInstance() {
        if (WebSocketServer.singleton == null) {
            WebSocketServer.singleton = new WebSocketServer(userRepository);
        }
        return WebSocketServer.singleton;
    }

    //+++++++++++++++++++++++++++++++++++++++++++
    // CONNECTION + MESSAGES
    //+++++++++++++++++++++++++++++++++++++++++++
    @OnOpen
    public void open(Session session, @PathParam("chatId") long chatId, @PathParam("userId")   long userId) {
        LOGGER.info("Session ouverte pour [" + userId + "] dans chat ["+chatId+"]");
        session.getUserProperties().put("userId", userId);
        session.getUserProperties().put("chatId", chatId);
        //On cherche les infos du user : son nom est son prénom
        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent()) {
            LOGGER.info("User : "+ userId+ " n'existe pas");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User n'existe pas");
        }
        else {
            session.getUserProperties().put("userName", user.get().getFirstName() + " "+ user.get().getFamilyName());
        }

        sessions.put(session.getId(), session);
        //Construction du message :
        JsonObject object = new JsonObject();
        object.addProperty("senderId", userId);
        object.addProperty("userName", (String) session.getUserProperties().get("userName"));
        object.addProperty("message", "Connexion de [" + userId + " sur ["+chatId+"]");
        object.addProperty("status", "onOpen");

        //On signifie tout le monde de la connexion pour que le nom apparaisse dans la liste
        sessions.values().forEach( sess -> {
            //On envoie le message à tout ceux du chat sauf à l'envoyeur
            if (sess.isOpen() && (Long)sess.getUserProperties().get("chatId") == chatId
                    && (Long)sess.getUserProperties().get("userId") != userId ) {
                LOGGER.info("Envoie connexion à " + sess.getUserProperties().get("chatId")+" sur "+ sess.getUserProperties().get("userId"));
                sendMessage(sess, object.toString());
            }
        });
    }

    @OnClose
    public void close(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        Long chatId = (Long) session.getUserProperties().get("chatId");
        LOGGER.info("Session fermee pour [" + userId + "] sur chat ["+ chatId + "]");
        sessions.remove(session.getId());
        //On signifie tout le monde de la connexion pour que le nom disparaisse
        sessions.values().forEach( sess -> {
            //On envoie le message à tous ceux du chat sauf à l'envoyeur
            if (sess.isOpen() && sess.getUserProperties().get("chatId") == chatId
                    && sess.getUserProperties().get("userId") != userId ) {
                LOGGER.info("Envoie message à " + sess.getUserProperties().get("chatId")+" sur "+ sess.getUserProperties().get("userId"));
                //Construction du message :
                JsonObject object = new JsonObject();
                object.addProperty("senderId", userId);
                object.addProperty("userName", "");
                object.addProperty("message", "");
                object.addProperty("status", "onClose");
                LOGGER.info("Message envoyé : ", object);
                sendMessage(sess, object.toString());
            }
        });
    }

    @OnError
    public void onError(Throwable error) {
        LOGGER.error(error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Réception message");
        Long userId = (Long) session.getUserProperties().get("userId");
        Long chatId = (Long) session.getUserProperties().get("chatId");
        LOGGER.info("Message recu de [" + userId +"] sur ["+ chatId + "] : [" + message + "]");
        sessions.values().forEach( sess -> {
            //On envoie le message à tous ceux du chat sauf à l'envoyeur
            if (sess.isOpen() && sess.getUserProperties().get("chatId") == chatId
                    && sess.getUserProperties().get("userId") != userId ) {
                LOGGER.info("Envoie message à " + sess.getUserProperties().get("chatId")+" sur "+ sess.getUserProperties().get("userId"));
                //Construction du message :
                JsonObject object = new JsonObject();
                object.addProperty("senderId", userId);
                object.addProperty("userName", (String) session.getUserProperties().get("userName"));
                object.addProperty("message", message);
                object.addProperty("status", "onMessage");
                LOGGER.info("Message envoyé : ", object);
                sendMessage(sess, object.toString());
            }
        });
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'envoi du message a la session [" + session.getId() + "] : " + e.getMessage());
        }
    }
}