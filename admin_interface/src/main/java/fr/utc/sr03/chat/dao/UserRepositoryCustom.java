package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> findAdminOnly();
}
