package fr.utc.sr03.chat.controller.rest;

import fr.utc.sr03.chat.dao.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RestTransactionnalController {
    // Créé car mettre des méthodes avec @Transactionnal  dans le controller
    // en annotation fait planter les autres méthodes du controller

    @Autowired
    private InvitationRepository invitationRepository;


    @DeleteMapping("/chats/{chatId}/{userId}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @Transactional
    public void deleteUserFromChat(@PathVariable(value = "chatId") long chatId,
                                    @PathVariable(value = "userId") long userId){
        //Supprime un utilisateur d'un chat
        System.out.println("Delete /chats/" + chatId + "/" + userId);
        try {
            invitationRepository.deleteByChatIdAndUserId(chatId, userId);
        } catch (Exception e) {
            System.out.println("ERREUR : " + e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cet utilisateur n'est pas invité à ce chat");
        }
    }


}
