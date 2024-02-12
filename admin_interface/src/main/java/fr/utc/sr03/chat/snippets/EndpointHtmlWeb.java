package fr.utc.sr03.chat.snippets;

import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * URL du endpoint : http://localhost:8080/snippets/users/html
 */
@Controller
public class EndpointHtmlWeb {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/snippets/users/html")
    public String getUserList(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        return "user_list";
    }
}
