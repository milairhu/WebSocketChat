package fr.utc.sr03.chat.snippets;

import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * URL du endpoint : http://localhost:8080/snippets/users/json
 */
@Controller
public class EndpointApiJson {
	@Autowired
    private UserRepository userRepository;

    @GetMapping("/snippets/users/json")
    @ResponseBody // Pour faire sans template html
    public List<User> test() {
        return userRepository.findAll();
    }
}
