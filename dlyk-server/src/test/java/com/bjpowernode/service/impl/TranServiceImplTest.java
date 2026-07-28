package com.bjpowernode.service.impl;

import com.bjpowernode.mapper.TTranHistoryMapper;
import com.bjpowernode.mapper.TTranMapper;
import com.bjpowernode.mapper.TTranRemarkMapper;
import com.bjpowernode.model.TTran;
import com.bjpowernode.model.TTranHistory;
import com.bjpowernode.query.TranQuery;
import com.bjpowernode.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranServiceImplTest {

    @Mock
    private TTranMapper tTranMapper;

    @Mock
    private TTranHistoryMapper tTranHistoryMapper;

    @Mock
    private TTranRemarkMapper tTranRemarkMapper;

    @InjectMocks
    private TranServiceImpl tranService;

    private String validToken;

    @BeforeEach
    void setUp() {
        JWTUtils.setSecret("test-secret");
        validToken = JWTUtils.createJWT("{\"id\":1}");
    }

    @Test
    void saveTran_shouldInsertTranAndHistory() {
        TranQuery query = new TranQuery();
        query.setCustomerId(1);
        query.setMoney(new BigDecimal("10000"));
        query.setExpectedDate(new Date());
        query.setStage(1);
        query.setToken(validToken);

        when(tTranMapper.insertSelective(any(TTran.class))).thenReturn(1);
        when(tTranHistoryMapper.insertSelective(any(TTranHistory.class))).thenReturn(1);

        int result = tranService.saveTran(query);

        assertEquals(1, result);
        verify(tTranMapper).insertSelective(any(TTran.class));
        verify(tTranHistoryMapper).insertSelective(any(TTranHistory.class));
    }

    @Test
    void saveTran_shouldGenerateTranNo() {
        TranQuery query = new TranQuery();
        query.setCustomerId(1);
        query.setMoney(new BigDecimal("10000"));
        query.setExpectedDate(new Date());
        query.setStage(1);
        query.setToken(validToken);

        when(tTranMapper.insertSelective(any(TTran.class))).thenReturn(1);
        when(tTranHistoryMapper.insertSelective(any(TTranHistory.class))).thenReturn(1);

        tranService.saveTran(query);

        ArgumentCaptor<TTran> captor = ArgumentCaptor.forClass(TTran.class);
        verify(tTranMapper).insertSelective(captor.capture());
        assertNotNull(captor.getValue().getTranNo());
        assertEquals(17, captor.getValue().getTranNo().length()); // yyyyMMddHHmmssSSS
    }

    @Test
    void deleteTranById_shouldDeleteHistoryAndRemarkBeforeTran() {
        when(tTranHistoryMapper.deleteByTranId(1)).thenReturn(1);
        when(tTranRemarkMapper.deleteByTranId(1)).thenReturn(1);
        when(tTranMapper.deleteByPrimaryKey(1)).thenReturn(1);

        int result = tranService.deleteTranById(1);

        assertEquals(1, result);
        InOrder inOrder = inOrder(tTranHistoryMapper, tTranRemarkMapper, tTranMapper);
        inOrder.verify(tTranHistoryMapper).deleteByTranId(1);
        inOrder.verify(tTranRemarkMapper).deleteByTranId(1);
        inOrder.verify(tTranMapper).deleteByPrimaryKey(1);
    }

    @Test
    void updateTran_whenStageChanged_shouldInsertHistory() {
        TTran oldTran = new TTran();
        oldTran.setId(1);
        oldTran.setStage(1); // 原阶段

        TranQuery query = new TranQuery();
        query.setId(1);
        query.setCustomerId(1);
        query.setMoney(new BigDecimal("10000"));
        query.setExpectedDate(new Date());
        query.setStage(2); // 新阶段，与原来不同
        query.setToken(validToken);

        when(tTranMapper.selectByPrimaryKey(1)).thenReturn(oldTran);
        when(tTranMapper.updateByPrimaryKeySelective(any(TTran.class))).thenReturn(1);
        when(tTranHistoryMapper.insertSelective(any(TTranHistory.class))).thenReturn(1);

        int result = tranService.updateTran(query);

        assertEquals(1, result);
        verify(tTranHistoryMapper).insertSelective(any(TTranHistory.class));
    }

    @Test
    void updateTran_whenStageNotChanged_shouldNotInsertHistory() {
        TTran oldTran = new TTran();
        oldTran.setId(1);
        oldTran.setStage(1); // 原阶段

        TranQuery query = new TranQuery();
        query.setId(1);
        query.setCustomerId(1);
        query.setMoney(new BigDecimal("10000"));
        query.setExpectedDate(new Date());
        query.setStage(1); // 阶段未变
        query.setToken(validToken);

        when(tTranMapper.selectByPrimaryKey(1)).thenReturn(oldTran);
        when(tTranMapper.updateByPrimaryKeySelective(any(TTran.class))).thenReturn(1);

        int result = tranService.updateTran(query);

        assertEquals(1, result);
        verify(tTranHistoryMapper, never()).insertSelective(any());
    }
}