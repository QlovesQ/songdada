package com.song.songdada.scoring;

import com.song.songdada.common.ErrorCode;
import com.song.songdada.exception.BusinessException;
import com.song.songdada.model.entity.App;
import com.song.songdada.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {

    @Resource
    private List<ScoringStrategy> scoringStrategyList;

    public UserAnswer doScoring(List<String> checkList, App app) throws Exception {
       Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        if (appType == null || scoringStrategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用配置有误，未找到匹配的策略");
        }
        // 根据注解获取配置
        for(ScoringStrategy strategy : scoringStrategyList) {
            if(strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == scoringStrategy){
                    return strategy.doScoring(checkList, app);
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用配置有误，未找到匹配的策略");
    }
}
