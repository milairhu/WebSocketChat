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
    @CrossOrigin(origins = "*", allowedHeaders = "*") // Enables access from multiple applications
    // Returns the list of non-admin users
    public List<User> getNonAdminUsers() {
        System.out.println("Get /users");

        try {
            List<User> users = userRepository.findByIsAdmin(false);
            return users;
        } catch (Error e) {
            System.out.println("ERROR  : GET /users : " + e);
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Request failed");
        }
    }

    @GetMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Returns a user by its id
    public User getUser(@PathVariable(value = "id") long id) {
        System.out.println("Get /users/" + id);

        Optional<User> user = userRepository.findById(id);
        if (user != null && user.isPresent()) {
            return user.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user corresponds to this id");
        }
    }

    @GetMapping("/{id}/ownchats")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Returns the chats owned by a user
    public List<Chat> getOwnChats(@PathVariable(value = "id") long id) {
        System.out.println("Get /users/" + id + "/ownchats");
        Optional<User> user = userRepository.findById(id);
        if (user == null || !user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user corresponds to this id");
        }
        List<Chat> chats = chatRepository.findByOwnerId(id);
        return chats;
    }

    @GetMapping("/{id}/invitations")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Returns the chats to which a user is invited
    public List<Chat> getChatsInvited(@PathVariable(value = "id") long id) {
        System.out.println("Get /users/" + id + "/invitations");
        Optional<User> user = userRepository.findById(id);
        if (user == null || !user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user corresponds to this id");
        }
        List<Chat> chats = userRepository.findUserInvitations(id);
        return chats;
    }

    @GetMapping("/{id}/chats")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Returns the chats of a user
    public List<Chat> getUserChats(@PathVariable(value = "id") long id) {
        System.out.println("Get /users/" + id + "/chats");
        Optional<User> user = userRepository.findById(id);
        if (user == null || !user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user corresponds to this id");
        }
        List<Chat> myChats = chatRepository.findByOwnerId(id);
        List<Chat> invitedChats = userRepository.findUserInvitations(id);

        List<Chat> allChats = Stream.concat(myChats.stream(), invitedChats.stream()).toList();
        return allChats;
    }

    @PutMapping("/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Updates a user
    public User updateUser(@PathVariable(value = "id") long id, @RequestBody @Valid User user) {

        System.out.println("Put /users/" + id);

        Optional<User> userToEdit = userRepository.findById(id);
        if (userToEdit.isPresent()) {
            // If the user to edit exists

            User emailMatchingUser = userRepository.findByEmail(user.getEmail());
            if (emailMatchingUser != null && emailMatchingUser.getId() != userToEdit.get().getId()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Error: an user already has this email");
            } else {
                user.setId(id);
                userRepository.saveAndFlush(user);
                return user;
            }
        } else {
            // Si l'utilisateur à modifier n'existe pas
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user corresponds to this id");
        }
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Returns a user if the email and password match, and creates a token
    public ResponseEntity<User> postLogin(@RequestBody User user) {
        System.out.println("POST users/login");
        User loggedUser = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());

        if (loggedUser != null) {
            // if the email and password match
            if (loggedUser.getIsDeactivated()) {
                // if the user is deactivated
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "User is deactivated");
            }

            Attempt userAttempt = attemptRepository.findByUserId(loggedUser.getId());
            if (userAttempt != null) {
                // if the user has already tried to log in before, we reset the number of
                // attempts
                userAttempt.setNbAttempts(0);
                attemptRepository.saveAndFlush(userAttempt);
            }
            return ResponseEntity.ok() // Code provided by G. Quentin
                    // Ajout de l'entete Access-Control-Expose-Headers pour autoriser le client a
                    // lire l'entete Authorization (ignore par defaut si le client utilise CORS - ex
                    // : navigateur web)
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION)
                    // Ajout de l'entete Authorization avec le token JWT
                    .header(HttpHeaders.AUTHORIZATION,
                            jwtTokenProvider.createSimpleToken(loggedUser.getEmail(),
                                    (loggedUser.getIsAdmin() ? "ADMIN" : "USER")))
                    // Ajout de l'utilisateur dans le body
                    .body(loggedUser);
        } else {
            // Password or email do not match
            User emailMatchingUser = userRepository.findByEmail(user.getEmail());

            if (emailMatchingUser != null) {
                // If the email exists
                Attempt userAttempt = attemptRepository.findByUserId(emailMatchingUser.getId());

                if (userAttempt != null) {
                    // if the user has already tried to log in before, we increment the number of
                    long nbAttempts = userAttempt.getNbAttempts();
                    userAttempt.setNbAttempts(nbAttempts + 1);
                    attemptRepository.saveAndFlush(userAttempt);

                    if (userAttempt.getNbAttempts() == 5) {
                        // if the user has tried to log in 5 times, we deactivate the account
                        emailMatchingUser.setIsDeactivated(true);
                        userRepository.saveAndFlush(emailMatchingUser);

                    }
                    if (userAttempt.getNbAttempts() >= 5) {
                        // display a message if the user has tried to log in 5 times
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "The user has been deactivated");
                    }
                } else {
                    // If the user has never tried to log in before, we create an attempt
                    Attempt attempt = new Attempt(emailMatchingUser.getId(), 1);
                    attemptRepository.saveAndFlush(attempt);
                }
            }
            // If the email does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No user corresponds to this couple email/password");
        }
    }

    @PostMapping("/login/forgotten")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    // Sends an email with the password of the user
    public void sendForgottenPassword(@RequestBody User userReceived) {
        System.out.println("Post users/login/forgotten");

        // Check if the user exists
        User user = userRepository.findByEmail(userReceived.getEmail());
        if (user == null) {
            // if the user does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user corresponds to this email");
        } else {
            // if the user exists we send an email with the password
            String subject = "Password forgotten";
            String message = "Hello " + user.getFirstName() + ",\nYour password is: " + user.getPassword()
                    + " .\n" +
                    "Please change it at your next connection.\nLooking forward to seeing you again, \nHugo.";
            emailService.sendEmail(userReceived.getEmail(), subject, message);
            return;

        }
    }
}
