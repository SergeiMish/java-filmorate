package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FilmDao filmDao; // Для доступа к фильмам и лайкам
    private final UserStorage userStorage; // Для работы с пользователями
    private final UserService userService;

    public List<Film> getRecommendations(Long userId) {
        // Получить лайки текущего пользователя
        Set<Long> userLikedFilmIds = userService.getLikedFilms(userId);

        // Найти всех пользователей (кроме текущего)
        List<User> allUsers = userStorage.getAll().stream()
                .filter(u -> u.getId() != userId)
                .collect(Collectors.toList());

        // Найти наиболее похожего пользователя
        User mostSimilarUser = findMostSimilarUser(userLikedFilmIds, allUsers);

        if (mostSimilarUser == null) {
            return Collections.emptyList(); // Если нет похожих пользователей
        }

        // Получить лайки похожего пользователя
        Set<Long> similarUserLikedFilmIds = userService.getLikedFilms(mostSimilarUser.getId());

        // Определить фильмы, которые текущий пользователь ещё не лайкнул
        Set<Long> recommendedFilmIds = new HashSet<>(similarUserLikedFilmIds);
        recommendedFilmIds.removeAll(userLikedFilmIds);

        // Вернуть список фильмов
        return recommendedFilmIds.stream()
                .map(filmDao::getById)
                .collect(Collectors.toList());
    }


    public User findMostSimilarUser(Set<Long> userLikedFilmIds, List<User> allUsers) {
        User mostSimilarUser = null;
        int maxIntersection = 0;

        for (User otherUser : allUsers) {
            Set<Long> otherUserLikedFilmIds = userService.getLikedFilms(otherUser.getId());
            if (otherUserLikedFilmIds.isEmpty()) {
                continue;
            }

            // Подсчет пересечения
            int intersectionSize = getIntersectionSize(userLikedFilmIds, otherUserLikedFilmIds);

            if (intersectionSize > maxIntersection) {
                maxIntersection = intersectionSize;
                mostSimilarUser = otherUser;
            }
        }
        return mostSimilarUser;
    }

    private int getIntersectionSize(Set<Long> set1, Set<Long> set2) {
        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection.size();
    }

}