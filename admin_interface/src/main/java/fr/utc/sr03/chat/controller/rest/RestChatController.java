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
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Renvoie les informations sur un chat donné
    private Chat getChat(@PathVariable long chatId) {
        System.out.println("Get /chats/"+chatId);
        Optional<Chat> chat = chatRepository.findById(chatId);
        if(chat == null || !chat.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun chat");
        }
        return chat.get();
    }
    @GetMapping("/{chatId}/users")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Renvoie la liste des utilisateur (propriétaire + invités) d'un chat
    private List<User> getChatUsers(@PathVariable long chatId) {
        System.out.println("Get /chats/"+chatId+"/users");
        Optional<Chat> chat = chatRepository.findById(chatId);
        if(chat == null || !chat.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun chat");
        }
        //On cherche le propriétaire du chat
        User owner = chatRepository.findOwner(chatId);
        //On cherche les invités
        List<User> users = chatRepository.findInvitedUsersByChatId(chatId);

        //Fusion des listes:
        users.add(owner);

        return users;
    }
    @GetMapping("/{chatId}/invited")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Renvoie la liste des utilisateurs invités d'un chat
    private List<User> getChatInvited(@PathVariable long chatId) {
        System.out.println("Get /chats/"+chatId+"/invited");
        Optional<Chat> chat = chatRepository.findById(chatId);
        if(chat == null || !chat.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun chat");
        }
        //On cherche les invités
        List<User> users = chatRepository.findInvitedUsersByChatId(chatId);
        return users;
    }

    @PostMapping
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Permet la création d'un Chat et le renvoie
    private Chat createChat( @RequestBody @Valid Chat chat){
        System.out.println("Post /chats");
        Chat existingChat = chatRepository.findByTitle(chat.getTitle());
        if(existingChat != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Erreur : un chat porte déjà ce nom");
        }
        try {
            Chat newChat = chatRepository.saveAndFlush(chat);
            System.out.println(newChat);
            return newChat;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,"Erreur lors de la création du Chat");
        }
    }

    @PostMapping("/{id}/invitations")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Met à jour la table des invitations pour le chat passé en argument
    private void addInvitations(@PathVariable(value = "id") long chatId,@RequestBody() List<Long> usersId) {
        System.out.println("Post /chats/"+chatId+"/invitations");
        Optional<Chat> chatToEdit = chatRepository.findById(chatId);
        if (chatToEdit.isPresent()) {
            usersId.forEach(userId -> {
                invitationRepository.saveAndFlush(new ChatUser(chatId, userId));
            });
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Pas de chat correspondant à cet Id");
        }
    }


    @PutMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Met à jour les informations d'un chat et le renvoie
    private Chat editChat(@PathVariable(value = "id") long id,@RequestBody @Valid Chat chat){

        System.out.println("Put /chats/"+id);
        Optional<Chat> chatToEdit = chatRepository.findById(id);
        if(chatToEdit.isPresent()){
            Chat existingChat = chatRepository.findByTitle(chat.getTitle());
            if(existingChat != null && existingChat.getId() != id) { //On vérifie que le nom n'est pas déjà pris
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Erreur : un chat porte déjà ce nom");
            }
            chat.setId(id);
            Chat editedChat = chatRepository.saveAndFlush(chat);
            return editedChat;
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Pas de chat correspondant à cet Id");
        }
    }

    @DeleteMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Supprime un chat, et les invitations associées
    private void deleteChat(@PathVariable(value = "id" )long id){
        System.out.println("Delete /chats/"+id);

        try {
            Optional<Chat> chat = chatRepository.findById(id);
            if(!chat.isEmpty()){
                //test validité de la date : on ne supprime que si le chat n'est pas en cours
                LocalDateTime beginDate = chat.get().getDate();
                LocalDateTime endDate = chat.get().getDate().plusMinutes(chat.get().getDuration());
                if(LocalDateTime.now().isAfter(endDate)  || LocalDateTime.now().isBefore(beginDate)){
                    chatRepository.deleteChatUsers(id); //supprime les invitations au chat
                    chatRepository.deleteById(id); //supprime le chat
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Le chat ne peut pas être en cours lors de la suppression");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun chat ne correspond" );
            }

        } catch (Exception e){
            System.out.println("ERREUR : "+e);
            throw  new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Problème lors de suppression" + e);
        }
    }

}
