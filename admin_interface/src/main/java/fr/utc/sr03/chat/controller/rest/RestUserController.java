package fr.utc.sr03.chat.controller.rest;

import fr.utc.sr03.chat.dao.AttemptRepository;
import fr.utc.sr03.chat.dao.ChatRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.Attempt;
import fr.utc.sr03.chat.model.Chat;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.security.JwtTokenProvider;
import fr.utc.sr03.chat.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("users")
public class RestUserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @GetMapping()
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Retourne la liste des utilisateurs non admin (pouvant participer à des chats)
    public List<User> getNonAdminUsers() {
        System.out.println("Get /users");

        try {
            List<User> users = userRepository.findByIsAdmin(false);
            return users;
        }
        catch (Error e) {
            System.out.println("ERROR  : GET /users : " + e);
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Requête non aboutie");
        }
    }

    @GetMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Retourne les infos sur l'utilisateur correspondant à l'id
    public User getUser(@PathVariable(value = "id") long id) {
        System.out.println("Get /users/"+id);

        Optional<User> user = userRepository.findById(id);
        if(user != null && user.isPresent()){
            return user.get();
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun utilisateur");
        }
    }



    @GetMapping("/{id}/ownchats")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Obtenir les chats dont l'utilisateur est propriétaire
    public List<Chat> getOwnChats(@PathVariable(value = "id") long id){
        System.out.println("Get /users/"+id+"/ownchats");
        Optional<User> user = userRepository.findById(id);
        if(user == null || !user.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun utilisateur");
        }
        List<Chat> chats = chatRepository.findByOwnerId(id);
        return chats;
    }

    @GetMapping("/{id}/invitations")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Obtenir les chats dont l'utilisateur est invité
    public List<Chat> getChatsInvited(@PathVariable(value = "id") long id){
        System.out.println("Get /users/"+id+"/invitations");
        Optional<User> user = userRepository.findById(id);
        if(user == null || !user.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun utilisateur");
        }
        List<Chat> chats = userRepository.findUserInvitations(id);
        return chats;
    }
    @GetMapping("/{id}/chats")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Obtenir les chats dont l'utilisateur est propriétaire ou invité
    public List<Chat> getUserChats(@PathVariable(value = "id") long id){
        System.out.println("Get /users/"+id+"/chats");
        Optional<User> user = userRepository.findById(id);
        if(user == null || !user.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id ne correspond à aucun utilisateur");
        }
        List<Chat> myChats = chatRepository.findByOwnerId(id);
        List<Chat> invitedChats = userRepository.findUserInvitations(id);

        List<Chat> allChats = Stream.concat(myChats.stream(), invitedChats.stream()).toList();
        return allChats;
    }

    @PutMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Met à jour l'utilisateur et le renvoie
    public User updateUser(@PathVariable(value = "id") long id, @RequestBody @Valid User user) {

            System.out.println("Put /users/"+id);

            Optional<User> userToEdit = userRepository.findById(id);
            if(userToEdit.isPresent()){
                //Si l'utilisateur à modifier existe

                User emailMatchingUser = userRepository.findByEmail(user.getEmail());
                if (emailMatchingUser != null && emailMatchingUser.getId() != userToEdit.get().getId()) {
                    //Si un utilisateur existe déjà avec cette adresse mail, et que ce n'est pas moi on interdit la modification
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Un utilisateur existe déjà avec cette adresse email");
                }
                else{
                    user.setId(id);
                    userRepository.saveAndFlush(user);
                    return user;
                }
            }
            else{
                //Si l'utilisateur à modifier n'existe pas
                throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Pas d'utilisateur correspondant à cet Id");
            }
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Renvoie un utilisateur si le mot de passe et l'email envoyés dans le corps sont recevables
    public ResponseEntity<User> postLogin(@RequestBody User user) {
        System.out.println("POST users/login");
        User loggedUser = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());

        if (loggedUser != null) {
            //Un utilisateur correspond au mdp et à l'email entrés
            if(loggedUser.getIsDeactivated()){
                //Si l'utilisateur est désactivé, on ne lui permet pas l'accès au site
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "L'utilisateur est désactivé");
            }

            Attempt userAttempt = attemptRepository.findByUserId(loggedUser.getId());
            if (userAttempt != null) {
                //Si l'utilisateur a une entrée dans la table Attempt, on remet son nombre d'essai à 0
                userAttempt.setNbAttempts(0);
                attemptRepository.saveAndFlush(userAttempt);
            }
            return ResponseEntity.ok() //Code fournit par G. Quentin
                    // Ajout de l'entete Access-Control-Expose-Headers pour autoriser le client a lire l'entete Authorization (ignore par defaut si le client utilise CORS - ex : navigateur web)
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION)
                    // Ajout de l'entete Authorization avec le token JWT
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenProvider.createSimpleToken(loggedUser.getEmail(), (loggedUser.getIsAdmin() ? "ADMIN" : "USER")))
                    // Ajout de l'utilisateur dans le body
                    .body(loggedUser);
        } else {
            //Le mdp entré ou l'email entrée est faux
            User emailMatchingUser = userRepository.findByEmail(user.getEmail());

            if (emailMatchingUser != null) {
                //Si un utilisateur a bien cet email, au bout de 5 tentatives, on désactive le compte
                Attempt userAttempt = attemptRepository.findByUserId(emailMatchingUser.getId());

                if (userAttempt != null) {
                    //Si l'entrée existait déjà
                    long nbAttempts = userAttempt.getNbAttempts();
                    userAttempt.setNbAttempts(nbAttempts + 1);
                    attemptRepository.saveAndFlush(userAttempt); //Mis à jour dans la base du nombre de tentatives

                    if (userAttempt.getNbAttempts() == 5) {
                        //Si cela fait 5 fois que l'utilisateur essaie un mdp, on désactive son compte
                        emailMatchingUser.setIsDeactivated(true);
                        userRepository.saveAndFlush(emailMatchingUser);

                    }
                    if (userAttempt.getNbAttempts() >= 5) {
                        //On affiche que l'utilisateur a été désactivé
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "L'utilisateur est désactivé");
                    }
                } else {
                    //Si l'entrée dans Attempts n'existe pas encore, on la crée
                    Attempt attempt = new Attempt(emailMatchingUser.getId(), 1);
                    attemptRepository.saveAndFlush(attempt);
                }
            }
            //L'email entré ne correspond pas
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun utilisateur ne correspond à ce couple email / mot de passe");
        }
    }

    @PostMapping("/login/forgotten")
    @CrossOrigin(origins = "*", allowedHeaders = "*") //Pour permettre les accès depuis plusieurs applications
    //Envoie un mail contenant le mot de passe de l'utilisateur
    public void sendForgottenPassword(@RequestBody User userReceived){
        System.out.println("Post users/login/forgotten");

        //On vérifie que l'adresse email correspond à un User
        User user = userRepository.findByEmail(userReceived.getEmail());
        if(user == null){
            //si aucun utilisateur ne correspond
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun utilisateur ne correspond à cet email");
        } else {
            // si un utilisateur existe, on envoie un mail
            String subject = "Mot de passe Appli de chat";
            String message = "Bonjour "+ user.getFirstName() +",\nVotre mot de passe est : "+ user.getPassword() +" .\n" +
                    "Veuillez le modifier dès votre prochaine connection.\nAu plaisir de vous revoir, \nHugo.";
            emailService.sendEmail(userReceived.getEmail(), subject, message);
            return;

        }
    }
}
