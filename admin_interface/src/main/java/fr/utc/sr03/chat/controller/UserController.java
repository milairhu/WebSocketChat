package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.dao.AttemptRepository;
import fr.utc.sr03.chat.dao.ChatRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.Attempt;
import fr.utc.sr03.chat.model.Chat;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttemptRepository attemptRepository;
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private HttpSession session;

    @Autowired
    private EmailService emailService; //Penser à décommenter l'envoi du mail lors de création

    @GetMapping
    public String getUserList(RedirectAttributes redirectAttributes) {
        System.out.println("Get /user");
        return "redirect:/admin/users";
    }


    @GetMapping("/{id}")
    public String getUser(@PathVariable(value = "id") long id,Model model) {
        System.out.println("Get /user/"+id);
        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }

        Optional<User> userToEdit = userRepository.findById(id);
        if(userToEdit != null && userToEdit.isPresent()){
            model.addAttribute("user", userToEdit.get());
            model.addAttribute("userExists", true);
        }
        else{
            model.addAttribute("userExists", false);
        }
        return  "edit_user";
    }

    @GetMapping("/nonadmin") //Pas utilisé
    public String getNonAdminList(Model model) {
        System.out.println("Get /user/nonadmin");
        List<User> users = userRepository.findByIsAdmin(false);
        model.addAttribute("users", users);

        return "user_list";
    }
    @GetMapping("/admin") //Pas utilisé
    public String getAdminList(Model model) {
        System.out.println("Get /user/admin");
        List<User> users = userRepository.findByIsAdmin(true);
        model.addAttribute("users", users);

        return "user_list";
    }

    @PostMapping
    public String addUser(@ModelAttribute("userToAdd") @Valid User user,
                          RedirectAttributes redirectAttrs){
        System.out.println("Post /user");
        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }


        User emailMatchingUser = userRepository.findByEmail(user.getEmail());
        if (emailMatchingUser != null){
            //Un utilisateur existe déjà avec cette adresse mail, on interdit la création
            redirectAttrs.addFlashAttribute("emailExists", true);
            redirectAttrs.addFlashAttribute("userToAdd",new User());
            return "redirect:/admin/add_user";
        }
        else{
            //Si l'email n'existe pas, on crée l'utilisateur
            User createdUser = userRepository.save(user);

            //On envoie un email à la personne :
            String subject = "Inscription application chat";
            String message = "Bonjour "+ user.getFirstName() +",\nMerci de nous avoir rejoint!\nVotre mot de passe temporaire est : "+ user.getPassword() +" .\nAu plaisir de vous revoir, \nHugo.";
            emailService.sendEmail(createdUser.getEmail(), subject, message);

            //Redirection vers admin/users, en précisant qu'on a fait une édition
            redirectAttrs.addFlashAttribute("edited", true);
            return "redirect:/admin/users";
        }
    }


    @PostMapping("/{id}") //Remarque : Put depuis un formulaire ne marche pas
    public String editUser( @PathVariable(value = "id") long id,@ModelAttribute("user") User user,
                           RedirectAttributes  redirectAttributes){

        System.out.println("Put /user/"+id);
        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }
        Optional<User> userToEdit = userRepository.findById(id);
        if(userToEdit.isPresent()){
            //Si l'utilisateur à modifier existe

            User emailMatchingUser = userRepository.findByEmail(user.getEmail());
            if (emailMatchingUser != null && emailMatchingUser.getId() != userToEdit.get().getId()){
                //Si un utilisateur existe déjà avec cette adresse mail, et que ce n'est pas moi on interdit la modification
                redirectAttributes.addFlashAttribute("user", userToEdit.get());
                redirectAttributes.addFlashAttribute("emailExists", true);
                return "redirect:/admin/edit_user/"+userToEdit.get().getId();
            }
            else{
                if (user.getIsDeactivated()){
                    // Si on débloque un utilisateur, on remet son compteur de tentatives à 0
                    Attempt userAttempt = attemptRepository.findByUserId(userToEdit.get().getId());
                    if (userAttempt != null){
                        userAttempt.setNbAttempts(0);
                        attemptRepository.saveAndFlush(userAttempt);
                    }
                }
                user.setId(id);
                userRepository.saveAndFlush(user);
                redirectAttributes.addFlashAttribute("edited", true);
            }
        }
        else{
            //Si l'utilisateur à modifier n'existe pas
            redirectAttributes.addFlashAttribute("error", "Cet utilisateur n'existe pas");
        }
        //On récupère tout et on envoie
        return  getUserList(redirectAttributes);
    }

    @DeleteMapping("/{id}")
    public String deleteUser( @PathVariable(value = "id") long id, RedirectAttributes  redirectAttributes){
        System.out.println("Delete /user/"+id);
        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }

        //Supprime dans les tables Attempt, ChatUser et User les instances relatives à l'user

        //Il faut tester si des instances existent ou pas dans les tables. Sinon : erreur "no entity with id X exists"
        Attempt attempt = attemptRepository.findByUserId(id);
        if (attempt != null){
            attemptRepository.deleteById(id);
        }
        List<Chat> chatsInvitated = userRepository.findUserInvitations(id);
        //Supprime les invitations
        if (!chatsInvitated.isEmpty()) {
            userRepository.deleteUserChats(id);
        }
        List<Chat> chatsOwned = chatRepository.findByOwnerId(id);
        //Supprime les chats propriétaires
        if (!chatsOwned.isEmpty()) {
            chatsOwned.forEach(chat -> chatRepository.deleteById(chat.getId()));
        }
        userRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("edited", true);

        return  getUserList( redirectAttributes);
    }

}
