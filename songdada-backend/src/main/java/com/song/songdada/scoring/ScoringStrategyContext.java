package com.song.songdada.scoring;

import com.song.songdada.common.ErrorCode;
import com.song.songdada.exception.BusinessException;
import com.song.songdada.model.entity.App;
import com.song.songdada.model.entity.UserAnswer;
import com.song.songdada.model.enums.AppScoringStrategyEnum;
import com.song.songdada.model.enums.AppTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Deprecated
public class ScoringStrategyContext {

    @Resource
    private CustomScoreScoringStrategy customScoreScoringStrategy;

    @Resource
    private CustomTestScoringStrategy customTestScoringStrategy;

    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(app.getAppType());
        AppScoringStrategyEnum appScoringStrategyEnum = AppScoringStrategyEnum.getEnumByValue(app.getScoringStrategy());
        if (appTypeEnum == null || appScoringStrategyEnum == null){
             throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用配置错误，未找到匹配的策略");
        }
        switch (appTypeEnum){
            case SCORE:
                switch (appScoringStrategyEnum){
                    case CUSTOM:
                        return customScoreScoringStrategy.doScoring(choiceList,app);
                    case AI:
                        break;
                }
                break;
            case TEST:
                switch (appScoringStrategyEnum){

                    case CUSTOM:
                        return customTestScoringStrategy.doScoring(choiceList,app);
                    case AI:
                        break;
                }
                break;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用配置错误，未找到匹配的策略");
    }
}
