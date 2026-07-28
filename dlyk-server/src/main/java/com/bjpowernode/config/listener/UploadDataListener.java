package com.bjpowernode.config.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.bjpowernode.mapper.TClueMapper;
import com.bjpowernode.model.TClue;
import com.bjpowernode.model.TUser;
import com.bjpowernode.util.JSONUtils;
import com.bjpowernode.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * EasyExcel 读取监听器，用于批量导入线索。
 *
 * invoke() 每读取一行 Excel 数据触发一次，攒够 BATCH_COUNT(100) 条批量写入数据库。
 * doAfterAllAnalysed() 在所有行读取完毕后触发，将最后不足 100 条的剩余数据写入。
 *
 * 注意：此监听器不能被 Spring 管理，需要通过构造方法手动传入 Mapper 和 token。
 */
@Slf4j
public class UploadDataListener implements ReadListener<TClue> {

    /** 批量提交阈值：每 100 条写一次数据库，防止几万条数据撑爆内存 */
    private static final int BATCH_COUNT = 100;

    /** 缓存列表，攒够 BATCH_COUNT 后批量写入并清空 */
    private List<TClue> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /** 线索 Mapper，通过构造方法传入（不能 @Autowired，因为此 Listener 不由 Spring 管理） */
    private TClueMapper tClueMapper;

    /** 当前登录用户的 JWT，用于获取创建人 ID */
    private String token;

    /**
     * 构造方法：每次创建 Listener 时传入 Mapper 和 token。
     *
     * @param tClueMapper 线索 Mapper，用于批量写入数据库
     * @param token       当前用户 JWT，用于提取创建人 ID
     */
    public UploadDataListener(TClueMapper tClueMapper, String token) {
        this.tClueMapper = tClueMapper;
        this.token = token;
    }

    /**
     * 每解析一行 Excel 数据触发一次。
     * 设置创建人、创建时间后加入缓存列表，达到 BATCH_COUNT 时批量写入数据库。
     */
    @Override
    public void invoke(TClue tClue, AnalysisContext context) {
        log.info("读取到线索数据: {}", JSONUtils.toJSON(tClue));

        tClue.setCreateTime(new Date());
        TUser tUser = JWTUtils.parseUserFromJWT(token);
        tClue.setCreateBy(tUser.getId());

        cachedDataList.add(tClue);

        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成后的回调。将最后不足 BATCH_COUNT 的剩余数据写入数据库。
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 将缓存列表中的数据批量写入数据库。
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库", cachedDataList.size());
        tClueMapper.saveClue(cachedDataList);
        log.info("存储数据库成功！");
    }
}