package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.dao.AttemptRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.Attempt;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * URL de base du endpoint : http://localhost:8080/login
 */
@Controller
@RequestMapping("login")
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HttpSession session;

    @GetMapping
    public String getLogin(Model model) {
        System.out.println("GET login/");
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping
    public String postLogin(@ModelAttribute User user, Model model, HttpServletResponse httpServletResponse) {
        System.out.println("POST login/");
        User loggedUser = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());

        if (loggedUser != null) {
            //Un utilisateur correspond au mdp et à l'email entrés

            if(loggedUser.getIsDeactivated()){
                //Si l'utilisateur est désactivé, on ne lui permet pas l'accès au site
                model.addAttribute("deactivated", true);
                return "login";
            }

            Attempt userAttempt = attemptRepository.findByUserId(loggedUser.getId());
            if (userAttempt != null) {
                //Si l'utilisateur a une entrée dans la table Attempt, on remet son nombre d'essai à 0
                userAttempt.setNbAttempts(0);
                attemptRepository.saveAndFlush(userAttempt);
            }
            //Paramètres de la session
            session.setAttribute("id", loggedUser.getId());
            session.setAttribute("firstName", loggedUser.getFirstName());
            session.setAttribute("familyName", loggedUser.getFamilyName());
            session.setAttribute("isAdmin", loggedUser.getIsAdmin());
            session.setMaxInactiveInterval(60 * 5); //Laisse 5min d'inactivité possible
            if (loggedUser.getIsAdmin()) {
                //Si c'est un admin, accès à l'interface admin
                return "redirect:/admin/users";
            } else {
                Cookie cookie = new Cookie("userId", ""+loggedUser.getId());
                httpServletResponse.addCookie(cookie);
                return "redirect:http://localhost:3000";

            }
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
                        model.addAttribute("deactivated", true);
                    }
                } else {
                    //Si l'entrée dans Attempts n'existe pas encore, on la crée
                    Attempt attempt = new Attempt(emailMatchingUser.getId(), 1);
                    attemptRepository.saveAndFlush(attempt);
                }
            }
            model.addAttribute("invalid", true);
            return "login";
        }
    }

    @GetMapping("/forgotten")
    public String forgottenPassword(Model model){
        System.out.println("Get login/forgotten");
        model.addAttribute("email", new String());
        return "forgotten_password";
    }
    @PostMapping("/forgotten")
    public String sendForgottenPassword(Model model, @RequestParam(value="email") String email){
        //Envoie un mail contenant le mot de passe de l'utilisateur
        System.out.println("Post login/forgotten");

        //On vérifie que l'adresse email correspond à un User
        User user = userRepository.findByEmail(email);
        if(user == null){
            //si aucun utilisateur ne correspond
            model.addAttribute("emailExists", false);
        } else {
            // si un utilisateur existe, on envoie un mail
            String subject = "Mot de passe Appli de chat";
            String message = "Bonjour "+ user.getFirstName() +",\nVotre mot de passe est : "+ user.getPassword() +" .\n" +
                    "Veuillez le modifier dès votre prochaine connection.\nAu plaisir de vous revoir, \nHugo.";
            emailService.sendEmail(email, subject, message);

            model.addAttribute("emailExists", true);

        }

        model.addAttribute("email", email);
        return "recup_password";
    }
}