package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TSystemInfoMapper;
import com.bjpowernode.model.TSystemInfo;
import com.bjpowernode.query.SystemInfoQuery;
import com.bjpowernode.service.SystemInfoService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 系统信息管理服务实现。
 * 管理网站 SEO 元信息（标题、描述、关键词）、Logo、联系方式等全局配置。
 */
@Service
public class SystemInfoServiceImpl implements SystemInfoService {

    @Resource
    private TSystemInfoMapper tSystemInfoMapper;

    @Override
    public PageInfo<TSystemInfo> getSystemInfoByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TSystemInfo> list = tSystemInfoMapper.selectAll();
        return new PageInfo<>(list);
    }

    @Override
    public TSystemInfo getSystemInfoById(Integer id) {
        return tSystemInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int saveSystemInfo(SystemInfoQuery query) {
        TSystemInfo info = new TSystemInfo();
        info.setSystemCode(query.getSystemCode());
        info.setName(query.getName());
        info.setSite(query.getSite());
        info.setLogo(query.getLogo());
        info.setTitle(query.getTitle());
        info.setDescription(query.getDescription());
        info.setKeywords(query.getKeywords());
        info.setShortcuticon(query.getShortcuticon());
        info.setTel(query.getTel());
        info.setWeixin(query.getWeixin());
        info.setEmail(query.getEmail());
        info.setAddress(query.getAddress());
        info.setVersion(query.getVersion());
        info.setClosemsg(query.getClosemsg());
        info.setIsopen(query.getIsopen());
        info.setCreateTime(new Date());
        return tSystemInfoMapper.insertSelective(info);
    }

    @Override
    public int updateSystemInfo(SystemInfoQuery query) {
        TSystemInfo info = new TSystemInfo();
        info.setId(query.getId());
        info.setSystemCode(query.getSystemCode());
        info.setName(query.getName());
        info.setSite(query.getSite());
        info.setLogo(query.getLogo());
        info.setTitle(query.getTitle());
        info.setDescription(query.getDescription());
        info.setKeywords(query.getKeywords());
        info.setShortcuticon(query.getShortcuticon());
        info.setTel(query.getTel());
        info.setWeixin(query.getWeixin());
        info.setEmail(query.getEmail());
        info.setAddress(query.getAddress());
        info.setVersion(query.getVersion());
        info.setClosemsg(query.getClosemsg());
        info.setIsopen(query.getIsopen());
        info.setEditTime(new Date());
        return tSystemInfoMapper.updateByPrimaryKeySelective(info);
    }

    @Override
    public int deleteSystemInfoById(Integer id) {
        return tSystemInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int batchDeleteSystemInfos(List<String> idList) {
        int count = 0;
        for (String id : idList) {
            count += tSystemInfoMapper.deleteByPrimaryKey(Integer.parseInt(id));
        }
        return count;
    }
}