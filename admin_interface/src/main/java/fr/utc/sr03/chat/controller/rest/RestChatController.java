package fr.utc.sr03.chat.controller.rest;

import fr.utc.sr03.chat.dao.ChatRepository;
import fr.utc.sr03.chat.dao.InvitationRepository;
import fr.utc.sr03.chat.model.Chat;
import fr.utc.sr03.chat.model.ChatUser;
import fr.utc.sr03.chat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("chats")
public class RestChatController {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private InvitationRepository invitationRepository;

    @GetMapping("/{chatId}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Returns information about a given chat
    private Chat getChat(@PathVariable long chatId) {
        System.out.println("Get /chats/" + chatId);
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat == null || !chat.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id does not correspond to any chat");
        }
        return chat.get();
    }

    @GetMapping("/{chatId}/users")
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Returns the list of users (owner + guests) of a chat
    private List<User> getChatUsers(@PathVariable long chatId) {
        System.out.println("Get /chats/" + chatId + "/users");
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat == null || !chat.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id does not correspond to any chat");
        }
        // Looking for the owner of the chat
        User owner = chatRepository.findOwner(chatId);
        // Looking for the guests
        List<User> users = chatRepository.findInvitedUsersByChatId(chatId);

        // Merging the lists:
        users.add(owner);

        return users;
    }

    @GetMapping("/{chatId}/invited")
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Returns the list of invited users of a chat
    private List<User> getChatInvited(@PathVariable long chatId) {
        System.out.println("Get /chats/" + chatId + "/invited");
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat == null || !chat.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id does not correspond to any chat");
        }
        // Looking for the guests
        List<User> users = chatRepository.findInvitedUsersByChatId(chatId);
        return users;
    }

    @PostMapping
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Allows the creation of a Chat and returns it
    private Chat createChat(@RequestBody @Valid Chat chat) {
        System.out.println("Post /chats");
        Chat existingChat = chatRepository.findByTitle(chat.getTitle());
        if (existingChat != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: a chat already has this name");
        }
        try {
            Chat newChat = chatRepository.saveAndFlush(chat);
            System.out.println(newChat);
            return newChat;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Error during the creation of the Chat");
        }
    }

    @PostMapping("/{id}/invitations")
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Updates the invitation table for the chat passed as argument
    private void addInvitations(@PathVariable(value = "id") long chatId, @RequestBody() List<Long> usersId) {
        System.out.println("Post /chats/" + chatId + "/invitations");
        Optional<Chat> chatToEdit = chatRepository.findById(chatId);
        if (chatToEdit.isPresent()) {
            usersId.forEach(userId -> {
                invitationRepository.saveAndFlush(new ChatUser(chatId, userId));
            });
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No chat corresponding to this Id");
        }
    }

    @PutMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Updates the information of a chat and returns it
    private Chat editChat(@PathVariable(value = "id") long id, @RequestBody @Valid Chat chat) {

        System.out.println("Put /chats/" + id);
        Optional<Chat> chatToEdit = chatRepository.findById(id);
        if (chatToEdit.isPresent()) {
            Chat existingChat = chatRepository.findByTitle(chat.getTitle());
            if (existingChat != null && existingChat.getId() != id) { // We check that the name is not already taken
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: a chat already has this name");
            }
            chat.setId(id);
            Chat editedChat = chatRepository.saveAndFlush(chat);
            return editedChat;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No chat corresponding to this Id");
        }
    }

    @DeleteMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") // To allow access from multiple applications
    // Deletes a chat, and the associated invitations
    private void deleteChat(@PathVariable(value = "id") long id) {
        System.out.println("Delete /chats/" + id);

        try {
            Optional<Chat> chat = chatRepository.findById(id);
            if (!chat.isEmpty()) {
                // validity test of the date: we only delete if the chat is not in progress
                LocalDateTime beginDate = chat.get().getDate();
                LocalDateTime endDate = chat.get().getDate().plusMinutes(chat.get().getDuration());
                if (LocalDateTime.now().isAfter(endDate) || LocalDateTime.now().isBefore(beginDate)) {
                    chatRepository.deleteChatUsers(id); // deletes the chat invitations
                    chatRepository.deleteById(id); // deletes the chat
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "The chat cannot be in progress during deletion");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No chat corresponds");
            }

        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Problem during deletion" + e);
        }
    }

}