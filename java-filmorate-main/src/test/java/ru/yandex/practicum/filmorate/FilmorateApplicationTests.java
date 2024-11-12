package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private UserController userController;

    @Autowired
    private FilmController filmController;

    @Test
    void testPostUser() {
        User user = new User();
        user.setLogin("common");
        user.setEmail("friend@common.ru");
        user.setBirthday(LocalDate.of(2000, 8, 20));

        User createdUser = userController.postUser(user);

        assertNotNull(createdUser.getId(), "ID не может быть null");
        assertEquals("common", createdUser.getLogin());
        assertEquals("friend@common.ru", createdUser.getEmail());
        assertEquals(LocalDate.of(2000, 8, 20), createdUser.getBirthday());
        assertEquals("common", createdUser.getName(), "Имя может быть пустым, если есть логин");
    }

    @Test
    void testGetUsers() {
        User user1 = new User();
        user1.setLogin("user1");
        user1.setEmail("user1@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userController.postUser(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2.setEmail("user2@example.com");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        userController.postUser(user2);

        Collection<User> users = userController.getUsers();

        assertEquals(2, users.size(), "Должно быть два пользователя");
    }

    @Test
    void testPutUser() {
        User user = new User();
        user.setLogin("original");
        user.setEmail("original@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.postUser(user);

        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setLogin("updated");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setBirthday(LocalDate.of(1990, 1, 1));

        User result = userController.putUser(updatedUser);

        assertEquals("updated", result.getLogin());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void testPostFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.postFilm(film);

        assertNotNull(createdFilm.getId(), "ID не может быть null");
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());
    }

    @Test
    void testGetFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        filmController.postFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(150);
        filmController.postFilm(film2);

        Collection<Film> films = filmController.getFilms();

        assertEquals(2, films.size(), "Должно быть два фильма");
    }

    @Test
    void testPutFilm() {
        Film film = new Film();
        film.setName("Original Film");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.postFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        updatedFilm.setDuration(130);

        Film result = filmController.putFilm(updatedFilm);

        assertEquals("Updated Film", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(130, result.getDuration());
    }
}
