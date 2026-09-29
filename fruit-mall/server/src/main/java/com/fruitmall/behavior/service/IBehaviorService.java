package com.fruitmall.behavior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.behavior.domain.FmUserBehavior;
import com.fruitmall.behavior.dto.BehaviorReportDTO;

/** 用户行为埋点服务。 */
public interface IBehaviorService extends IService<FmUserBehavior> {

    /**
     * 上报行为。已登录时记录会员ID，未登录时记录匿名标识；
     * 埋点失败不能影响业务，调用方（Controller）不感知异常。
     */
    void report(BehaviorReportDTO dto);
}
