package com.bjpowernode.service.impl;

import com.bjpowernode.manager.RedisManager;
import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.mapper.TRoleMapper;
import com.bjpowernode.mapper.TUserMapper;
import com.bjpowernode.model.TUser;
import com.bjpowernode.query.UserQuery;
import com.bjpowernode.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private TUserMapper tUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TRoleMapper tRoleMapper;

    @Mock
    private RedisManager redisManager;

    @Mock
    private TPermissionMapper tPermissionMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private String validToken;

    @BeforeEach
    void setUp() {
        JWTUtils.setSecret("test-secret");
        validToken = JWTUtils.createJWT("{\"id\":1}");
    }

    @Test
    void saveUser_shouldEncodePassword() {
        UserQuery query = new UserQuery();
        query.setLoginAct("testuser");
        query.setLoginPwd("123456");
        query.setName("测试用户");
        query.setToken(validToken);

        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$hashedPassword");
        when(tUserMapper.insertSelective(any(TUser.class))).thenReturn(1);

        int result = userService.saveUser(query);

        assertEquals(1, result);
        verify(passwordEncoder).encode("123456");
        ArgumentCaptor<TUser> captor = ArgumentCaptor.forClass(TUser.class);
        verify(tUserMapper).insertSelective(captor.capture());
        assertEquals("$2a$10$hashedPassword", captor.getValue().getLoginPwd());
    }

    @Test
    void changePassword_withCorrectOldPwd_shouldSucceed() {
        TUser existingUser = new TUser();
        existingUser.setId(1);
        existingUser.setLoginPwd("$2a$10$oldHash");

        when(tUserMapper.selectByPrimaryKey(1)).thenReturn(existingUser);
        when(passwordEncoder.matches("oldPwd", "$2a$10$oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPwd")).thenReturn("$2a$10$newHash");
        when(tUserMapper.updateByPrimaryKeySelective(any(TUser.class))).thenReturn(1);

        boolean result = userService.changePassword(1, "oldPwd", "newPwd");

        assertTrue(result);
        ArgumentCaptor<TUser> captor = ArgumentCaptor.forClass(TUser.class);
        verify(tUserMapper).updateByPrimaryKeySelective(captor.capture());
        assertEquals("$2a$10$newHash", captor.getValue().getLoginPwd());
    }

    @Test
    void changePassword_withWrongOldPwd_shouldFail() {
        TUser existingUser = new TUser();
        existingUser.setId(1);
        existingUser.setLoginPwd("$2a$10$oldHash");

        when(tUserMapper.selectByPrimaryKey(1)).thenReturn(existingUser);
        when(passwordEncoder.matches("wrongPwd", "$2a$10$oldHash")).thenReturn(false);

        boolean result = userService.changePassword(1, "wrongPwd", "newPwd");

        assertFalse(result);
        verify(tUserMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void changePassword_userNotFound_shouldFail() {
        when(tUserMapper.selectByPrimaryKey(999)).thenReturn(null);

        boolean result = userService.changePassword(999, "oldPwd", "newPwd");

        assertFalse(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tUserMapper, never()).updateByPrimaryKeySelective(any());
    }
}