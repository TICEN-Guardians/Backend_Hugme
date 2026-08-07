package com.project.hugme.domain.user.service;

import com.project.hugme.domain.user.dto.MyInfoResponse;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.AlreadyWithdrawnUserException;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public MyInfoResponse getMyInfo(Long userId) {
        User user = findActiveUser(userId);

        return MyInfoResponse.from(user);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = findActiveUser(userId);

        user.withdraw();
    }


    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new AlreadyWithdrawnUserException();
        }

        return user;
    }
}