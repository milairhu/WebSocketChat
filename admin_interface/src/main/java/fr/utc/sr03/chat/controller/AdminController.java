package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * URL de base du endpoint : http://localhost:8080/admin
 * ex users : http://localhost:8080/admin/users
 */
@Controller
@RequestMapping("admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HttpSession session;

    @GetMapping("/users")
    public String getUserList(Model model, @RequestParam(value = "page", required = false) Integer pageNumber,
                              @RequestParam(value = "sort", required = false) String sort,
                              @RequestParam(value = "filter", required = false) String searchBar) {
        System.out.println("GET admin/users");
        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }


        //On récupère les users avec pagination
        PageRequest page;
        if (sort == null || sort == "null" || sort == "") {
            page = PageRequest.of(pageNumber==null ? 0 : pageNumber,8, Sort.by("email"));
        } else{
            page = PageRequest.of(pageNumber==null ? 0 : pageNumber,8, Sort.by(sort, "email"));
        }
        Page<User> users;
        if (searchBar != null && searchBar != "null"){
            users = userRepository.findByFirstNameContainingIgnoreCaseOrFamilyNameContainingIgnoreCaseOrEmailContainingIgnoreCase(page,
                    searchBar,
                    searchBar,
                    searchBar);
        } else{
            users = userRepository.findAll(page);
        }

        model.addAttribute("users", users.getContent());

        //On passe les paramètres de la sessions
        String firstName = (String) session.getAttribute("firstName");
        String familyName = (String) session.getAttribute("familyName");
        boolean isAdmin = (boolean) session.getAttribute("isAdmin");

        //On les ajoute au modèle
        model.addAttribute("firstName", firstName);
        model.addAttribute("familyName", familyName);
        model.addAttribute("isAdmin", isAdmin);

        //Ajout dans le modèle du numéro de page
        model.addAttribute("page", pageNumber == null? 0 : pageNumber);
        model.addAttribute("maxPage", users.getTotalPages() - 1);

        //Ajout des filtres de recherche :
        model.addAttribute("filter", searchBar == null || searchBar == "null" ? "" : searchBar);

        //Ajout des critères de tris :
        model.addAttribute("sorter", sort == null || sort == "null" ? "" : sort);

        return "user_list";
    }
    @GetMapping("/deactivated_users")
    public String getDeactivatedUsers(Model model, @RequestParam(value = "page", required = false) Integer pageNumber) {
        System.out.println("GET admin/deactivated_users");

        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }

        //On récupère les users
        PageRequest page = PageRequest.of(pageNumber==null ? 0 : pageNumber,8, Sort.by("email"));
        Page<User> users = userRepository.findAllByIsDeactivated(page, true);
        model.addAttribute("users", users.getContent());

        //On passe les paramètres de la sessions
        String firstName = (String) session.getAttribute("firstName");
        String familyName = (String) session.getAttribute("familyName");
        boolean isAdmin = (boolean) session.getAttribute("isAdmin");

        //On les ajoute au modèle
        model.addAttribute("firstName", firstName);
        model.addAttribute("familyName", familyName);
        model.addAttribute("isAdmin", isAdmin);

        //Ajout dans le modèle du numéro de page
        model.addAttribute("page", pageNumber == null? 0 : pageNumber);
        model.addAttribute("maxPage", users.getTotalPages() - 1);

        return "deactivated_users";
    }
    @GetMapping("/add_user")
    public String getAddUserPage(Model model) {

        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }

        System.out.println("GET admin/add_user");
        //On passe les paramètres de la sessions
        String firstName = (String) session.getAttribute("firstName");
        String familyName = (String) session.getAttribute("familyName");
        boolean isAdmin = (boolean) session.getAttribute("isAdmin");

        //On les ajoute au modèle
        model.addAttribute("firstName", firstName);
        model.addAttribute("familyName", familyName);
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("userToAdd", new User());
        return "add_user";
    }

    @GetMapping("/edit_user/{id}")
    public String getEditUserPage(Model model, @PathVariable(value = "id") long id) {
        System.out.println("GET admin/edit_user/"+ id);

        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }

        Optional<User> userToEdit = userRepository.findById(id);
        if(userToEdit!=null && userToEdit.isPresent()){
            //Si l'utilisateur à modifier existe
            model.addAttribute("user", userToEdit.get());
            model.addAttribute("userExists", true);

        }else{
            //Si l'utilisateur à modifier n'existe pas
            model.addAttribute("userExists", false);
        }

        //On passe les paramètres de la sessions
        String firstName = (String) session.getAttribute("firstName");
        String familyName = (String) session.getAttribute("familyName");
        boolean isAdmin = (boolean) session.getAttribute("isAdmin");

        //On les ajoute au modèle
        model.addAttribute("firstName", firstName);
        model.addAttribute("familyName", familyName);
        model.addAttribute("isAdmin", isAdmin);


        return "edit_user";
    }

    @PostMapping("/users")
    public String getUserListByCriteria(Model model, @RequestParam(value = "page", required = false) Integer pageNumber,
                                        @RequestParam(value = "sort", required = false) String sort,
                                        @RequestParam(value="filter") String searchBar) {
        System.out.println("POST admin/users");
        if (session == null || session.getAttribute("id") == null){
            return "redirect:/login";
        }

        //On récupère les users avec pagination
        PageRequest page;
        if (sort == null || sort == "null" || sort == "") {
            page = PageRequest.of(pageNumber==null ? 0 : pageNumber,8, Sort.by("email"));
        } else{
            page = PageRequest.of(pageNumber==null ? 0 : pageNumber,8, Sort.by(sort, "email"));
        }
        Page<User> users;
        if (searchBar != null){
            users = userRepository.findByFirstNameContainingIgnoreCaseOrFamilyNameContainingIgnoreCaseOrEmailContainingIgnoreCase(page,
                    searchBar,
                    searchBar,
                    searchBar);
        } else{
            users = userRepository.findAll(page);
        }
        model.addAttribute("users", users.getContent());

        //On passe les paramètres de la sessions
        String firstName = (String) session.getAttribute("firstName");
        String familyName = (String) session.getAttribute("familyName");
        boolean isAdmin = (boolean) session.getAttribute("isAdmin");
        model.addAttribute("firstName", firstName);
        model.addAttribute("familyName", familyName);
        model.addAttribute("isAdmin", isAdmin);

        //Ajout dans le modèle du numéro de page
        model.addAttribute("page", pageNumber == null? 0 : pageNumber);
        model.addAttribute("maxPage", users.getTotalPages() - 1);

        //Ajout des filtres de recherche :
        model.addAttribute("filter", searchBar == null || searchBar == "null" ? "" : searchBar);


        //Ajout des critères de tris :
        model.addAttribute("sorter", sort == null || sort == "null" ? "" : sort);

        return "user_list";
    }
}

