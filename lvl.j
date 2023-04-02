/*
  * Copyright (c) 2015 PayPal. All rights reserved.
 */
package com.paypal.compliance.rbo.impl.statusexpression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.paypal.compliance.AccountBO;
import com.paypal.compliance.TenantInfoBO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ebay.kernel.cal.api.CalEvent;
import com.ebay.kernel.cal.api.sync.CalEventFactory;
import com.paypal.compliance.ComplianceRelationshipBO;
import com.paypal.compliance.ComplianceRelationshipNodeBO;
import com.paypal.compliance.CriteriaDependencyBO;
import com.paypal.compliance.CriterionBO;
import com.paypal.compliance.EntityCardinalityBO;
import com.paypal.compliance.LevelBO;
import com.paypal.compliance.bo.util.CriteriaDependencyBOUtil;
import com.paypal.compliance.enumeration.Authority;
import com.paypal.compliance.enumeration.ComplianceRelationshipNodeType;
import com.paypal.compliance.enumeration.CriteriaClassification;
import com.paypal.compliance.enumeration.CriterionTag;
import com.paypal.compliance.enumeration.DecisionCode;
import com.paypal.compliance.enumeration.EntityType;
import com.paypal.compliance.enumeration.OperatorType;
import com.paypal.compliance.enumeration.StateTransition;
import com.paypal.compliance.onboarding.OnboardingDecisionBO;
import com.paypal.compliance.pilot.impl.PartialVerificationPilotChecker;
import com.paypal.compliance.rbo.api.AccountRBO;
import com.paypal.compliance.rbo.api.CommonEntityRBO;
import com.paypal.compliance.rbo.api.CriteriaRBO;
import com.paypal.compliance.rbo.api.EntityCardinalityRBO;
import com.paypal.compliance.rbo.api.LevelRBO;
import com.paypal.compliance.rbo.api.PartyRBO;
import com.paypal.compliance.rbo.api.RelatedEntityContextRBO;
import com.paypal.compliance.rbo.api.RelationshipRBO;
import com.paypal.compliance.rbo.api.StatusRBO;
import com.paypal.compliance.rbo.api.config.CommonConfig;
import com.paypal.compliance.rbo.api.config.LevelCriteriaDefinitionConfig;
import com.paypal.compliance.rbo.api.config.LevelCriteriaDependencyConfig;
import com.paypal.compliance.rbo.api.config.RemoteConfig;
import com.paypal.compliance.rbo.api.enums.EntityTypeRBO;
import com.paypal.compliance.rbo.api.enums.LevelDefinitionRBO;
import com.paypal.compliance.rbo.api.enums.StateTransitionRBO;
import com.paypal.compliance.rbo.api.statusexpression.LevelEvaluatorManager;
import com.paypal.compliance.rbo.api.statusexpression.StatusExpression;
import com.paypal.compliance.rbo.datatransform.statusexpression.handler.ExpressionTrimmerHandler;
import com.paypal.compliance.rbo.datatransform.statusexpression.handler.PartialVerificationTrimmerHandler;
import com.paypal.compliance.rbo.impl.AccountRBOImpl;
import com.paypal.compliance.rbo.impl.CriteriaRBOImpl;
import com.paypal.compliance.rbo.impl.EntityCardinalityRBOImpl;
import com.paypal.compliance.rbo.impl.LevelRBOImpl;
import com.paypal.compliance.rbo.impl.PartyRBOImpl;
import com.paypal.compliance.rbo.impl.PartyToAccountRelationshipRBOImpl;
import com.paypal.compliance.rbo.impl.PartyToPartyRelationshipRBOImpl;
import com.paypal.compliance.rbo.impl.PolicyRBOImpl;
import com.paypal.compliance.rbo.impl.decisioning.DataPolicyDecisioningCalculatorRBOImpl;
import com.paypal.compliance.rbo.impl.util.CALLogManager;
import com.paypal.compliance.rbo.impl.util.CriteriaDependencyUtil;
import com.paypal.compliance.rbo.impl.util.ReferenceKeyUtil;
import com.paypal.compliance.rbo.impl.util.RevocationUtil;
import com.paypal.compliance.rbo.resolver.EntityTypeResolver;


/**
 * 
 * @author nphung
 *
 */
public class LevelEvaluatorManagerImpl implements LevelEvaluatorManager {

    public static final String LEVEL_REVOCATION_BASED_ON_DATA_CHANGE_ENABLED =
            "level_revocation_based_on_data_change_enabled";
    private static final int MAX_ITERATIONS = 20;

    /** logger */
    private static Logger logger = LoggerFactory.getLogger(LevelEvaluatorManagerImpl.class);
    
    private static final String OVERRIDDEN_TRUE = "Y";

    /** to indicate that client doesn't want level status updated, only the expression state is set */
    private boolean notUpdateLevelStatus = false;
    
    private CALLogManager calManager = new CALLogManager();
    
    private static ApplicationContext context = new ClassPathXmlApplicationContext("CriterionTags.xml");
    @SuppressWarnings("unchecked")
    private static Map<CriterionTag,StateTransitionRBO> tagmp = (Map<CriterionTag, StateTransitionRBO>) 
        context.getBean("tagToLevelMapping");
    /**
     * 
     * @param relatedEntityContext
     */
    @Override
    public void evaluate(RelatedEntityContextRBO relatedEntityContext) throws Exception {
        
        calManager = new CALLogManager(relatedEntityContext);
        String legalCountry = relatedEntityContext.getLegalCountry();
        AccountRBO accountRBO = relatedEntityContext.getAccount();
        String tenant = relatedEntityContext.getAccount() != null?accountRBO.getTenantInfo().getTenant():
                TenantInfoBO.PAYPAL.getTenant();
        // first evaluate all the person parties
        for (PartyRBO personParty : relatedEntityContext.getPersonParties()) {
            logger.debug("evaluate person party {} ", personParty.getReferenceId());
            
            evaluate(personParty, legalCountry);

            // we do the ELC map after level evaluation so that we will not map criteria 
            // when level remains in U
            mapCriteriaToLevel(personParty.getStatus(), relatedEntityContext,
                    EntityTypeRBO.PAYPAL_PARTY_INDIVIDUAL, null);

            // populate statusRBO criterias dependencies, some of them are not processed in CriteriaEvaluatorManagerImpl
            CriteriaDependencyUtil.initializeEntityCriteriaDependencies(personParty);
        }
        
        // evaluate the biz party
        PartyRBO bizParty = relatedEntityContext.getBusinessParty();
        if (bizParty != null) {
            logger.debug("evaluate biz party {} ", bizParty.getReferenceId());
            
            evaluate(bizParty, legalCountry);
            mapCriteriaToLevel(bizParty.getStatus(), relatedEntityContext, EntityTypeRBO.PAYPAL_PARTY_BUSINESS, null);

            // populate statusRBO criterias dependencies
            CriteriaDependencyUtil.initializeEntityCriteriaDependencies(bizParty);
        }
        
        // evaluate the account
        AccountRBO account = relatedEntityContext.getAccount();
        if (account != null) {
            logger.debug("evaluate account {} ", account.getReferenceId());

            evaluate(account, legalCountry);
            mapCriteriaToLevel(account.getStatus(), relatedEntityContext, EntityTypeRBO.PAYPAL_ACCOUNT, null);

            // populate statusRBO criterias dependencies
            CriteriaDependencyUtil.initializeEntityCriteriaDependencies(account);
        }

        LevelCriteriaDependencyConfig.initializeCrossEntityDependency(relatedEntityContext);

        combineLevelsRelationshipNodes(relatedEntityContext);

        combineLevelsCriteriaDependencies(relatedEntityContext);
    }

    /**
     * 
     * @param currentStatusRBO
     * @param relatedEntityContext
     */
    public void mapCriteriaToLevel(StatusRBO currentStatusRBO, RelatedEntityContextRBO relatedEntityContext, 
            EntityTypeRBO entityType, LevelRBO requiredLevel) {  

        if (currentStatusRBO == null) {
            return;
        }
        for (LevelRBO level : currentStatusRBO.getLevels()) {
            if (level.getExpression() == null) {
                logger.debug("mapCriteriaToLevel level expression is null {} {}", level.getName(), level.getVersion());
                continue;
            }
            if (requiredLevel != null && level != requiredLevel) {
                continue;
            }
            
            AbstractStatusExpression levelExpr = (AbstractStatusExpression)level.getExpression();
            List<CriteriaRBO> criteriaRboList = levelExpr.initializeAllCriteriaAndReturnOnlyThoseApplicableForELC(
                    currentStatusRBO, level, relatedEntityContext.getLegalCountry(), entityType,
                    level.getTenantInfo().getTenant());
            Set<MultiKey> uniqueCriteriaNameRegions = new HashSet<>();
            for (CriteriaRBO criteriaRbo : criteriaRboList) {
                logger.debug("adding criterion - {}", criteriaRbo.getUniqueCriteriaName());
                uniqueCriteriaNameRegions
                    .add(getCriteriaKey(criteriaRbo.getUniqueCriteriaName(), criteriaRbo.getComplianceRegion()));
            }
            
            // deactivate criteria
            for (CriteriaRBO criterion : level.getCriteriaV2()) {
                // if criterion doesn't exist in expression
                if (!uniqueCriteriaNameRegions
                    .contains(getCriteriaKey(criterion.getUniqueCriteriaName(), criterion.getComplianceRegion()))) {
                    logger.debug("Remove criteria {} from level ", criterion.getUniqueCriteriaName());
                    level.removeCriteria(criterion);
                }
            }
            
            // map criteria to level
            if(level.getState() != StateTransitionRBO.NOT_AVAILABLE) {
                for (CriteriaRBO criteriaRbo : criteriaRboList) {
                    if (criteriaRbo.getState() != StateTransitionRBO.NOT_AVAILABLE) {
                        logger.debug("map criteria {} to level {}", criteriaRbo.getUniqueCriteriaName(), 
                                level.getName());
                        level.addCriteria(criteriaRbo);
                    }
                }
            }
        }
    }
    
    private MultiKey getCriteriaKey(String name, String region) {
        return new MultiKey(name, region);
    }
    
    /**
     * 
     * @param relatedEntityContext
     */
    @Override
    public void revoke(RelatedEntityContextRBO relatedEntityContext) {
        
        calManager = new CALLogManager(relatedEntityContext);
        
        Set<LevelBO> processedLevels = new HashSet<>();
        // evaluate the account
        AccountRBO account = relatedEntityContext.getAccount();
        if (account != null) {
            logger.debug("revoke account {} ", account.getReferenceId());
            revoke(account, processedLevels);
        }
        
        // first evaluate all the person parties
        for (PartyRBO personParty : relatedEntityContext.getPersonParties()) {
            logger.debug("revoke person party {} ", personParty.getReferenceId());
            revoke(personParty, processedLevels);
        }
        
        // evaluate the biz party
        PartyRBO bizParty = relatedEntityContext.getBusinessParty();
        if (bizParty != null) {
            logger.debug("revoke biz party {} ", bizParty.getReferenceId());
            revoke(bizParty, processedLevels);
        }
    }
    
    /**
     * 
     * @param relatedEntityContext
     */
    @Override
    public void invalidate(RelatedEntityContextRBO relatedEntityContext) {
        calManager = new CALLogManager(relatedEntityContext);
        
        // invalidate the account
        AccountRBO account = relatedEntityContext.getAccount();
        if (account != null) {
            logger.debug("invalidate account {} ", account.getReferenceId());
            invalidate(account);
        }
        
        // first invalidate all the person parties
        for (PartyRBO personParty : relatedEntityContext.getPersonParties()) {
            logger.debug("invalidate person party {} ", personParty.getReferenceId());
            invalidate(personParty);
        }
        
        // invalidate the biz party
        PartyRBO bizParty = relatedEntityContext.getBusinessParty();
        if (bizParty != null) {
            logger.debug("invalidate biz party {} ", bizParty.getReferenceId());
            invalidate(bizParty);
        }
    }
    
    /**
     * 
     * @param entityRBO
     */
    protected void evaluate(CommonEntityRBO entityRBO, String legalCountry) throws Exception {
        
        StatusRBO currentStatusRBO = entityRBO.getStatus();
        if (currentStatusRBO == null) {
            return;
        }

        Queue<LevelRBO> queue = new LinkedList<>();
        Map<LevelDefinitionRBO,List<LevelRBO>> levelNameMap = new HashMap<>();
        Map<MultiKey, CriteriaRBO> criteriaMap = buildCriteriaNameRegionToRBOMap(currentStatusRBO);
        for (LevelRBO level : currentStatusRBO.getLevels()) {
            List<LevelRBO> levelList = levelNameMap.get(level.getName());
            if(levelList == null){
                levelList = new ArrayList<LevelRBO>();
                levelNameMap.put(level.getName(),levelList);
            }

            levelList.add(level);
            queue.add(level);

            setEditableCriteria(criteriaMap, level);
        }
        revokeLevelBeforeEvaluation(legalCountry, currentStatusRBO, levelNameMap);

        while(!queue.isEmpty()) {

            logger.debug("level eval tagmap {}", tagmp);
            LevelRBO level = queue.poll();

            if (!isNotUpdateLevelStatus()) {
                level.setEvaluationDone(true);
            }

            if (level.getExpression() == null ) {
                revokeLevelBasedOnDataChange(level, entityRBO);
                continue;
            }

            // level marked as deprecated, stop to move the level status
            if (level.isDeprecated()) {
                continue;
            }


            AbstractStatusExpression levelExpr = (AbstractStatusExpression)level.getExpression();
            levelExpr.initializeSubLevel(currentStatusRBO, level.getState(), level.getTenantInfo().getTenant());

            // before evaluating level check for criterion tags and bypass level evaluation
            List<CriteriaExpression> uniqueCriterias = levelExpr.getUniqueCriteriaNamesForELC();
            StateTransitionRBO levelState = computeLevelStateUsingCriterionTags(uniqueCriterias, currentStatusRBO,
                    level.getTenantInfo().getTenant());
            populateLevelUniqueKey(level, entityRBO.getId(), entityRBO.getExternalId(), entityRBO.getEntityType());

            if (levelState != null) {
                level.updateState(levelState);
                logger.debug("evaluate for level using tags name {} new state {}", level.getName(), levelState);
                continue;
            }

            boolean isPVEnabled = PartialVerificationPilotChecker.isInPilot(legalCountry);
            StateTransitionRBO newState = null;
            if (isPVEnabled) {
                levelExpr.evaluate(currentStatusRBO,level.getTenantInfo().getTenant());
            } else {
                newState = levelExpr.evaluate(currentStatusRBO, level.getTenantInfo().getTenant());
                logger.debug("evaluate for level name {} new state {}", level.getName(), newState);
            }

            // This has to be after the state evaluation, else the relationship RBO list may not be initialized
            populateComplianceRelationships(entityRBO, level, levelExpr);

            if (isNotUpdateLevelStatus()) {
                continue;
            }

            AbstractStatusExpression trimmedLevelExpr = null;
            if (isPVEnabled) {
                // We are cloning and trimmed the clone expression to evaluate level status
                // using the trimmed expression
                // while maintaining the full expression to support other flow such as admin
                trimmedLevelExpr = cloneExpression(levelExpr, entityRBO, level.getTenantInfo().getTenant());
                trimmedLevelExpr = trimLevelExpression(trimmedLevelExpr, level, entityRBO);
                newState = trimmedLevelExpr.evaluate(currentStatusRBO, level.getTenantInfo().getTenant());
                logger.debug("evaluate with trimmed expr for level name {} new state {}", level.getName(), newState);
            }

            // if the new level status is completed but preReq hasn't been fulfilled, skip level status update
            if (newState == StateTransitionRBO.COMPLETED){
                OnboardingDecisionBO prereqDecision = calculatePrereqDecision(level);
                if (prereqDecision.getDecisionCode() != DecisionCode.ALLOW) continue;
            }

            int count = 0;
            while (newState != level.getState()) {
                logger.debug("newState " + newState.toString());
                logger.debug("getState " + level.getState());

                if (++count > MAX_ITERATIONS) {
                    throw new UnsupportedOperationException("Level.evaluate() state running into a " +
                            "potentially infinite loop " + level.toString());
                }

                if(!RevocationUtil.isInRevocationBeforeEvaluationCountries(legalCountry)){
                    if (isLevelRevoked(level, newState, entityRBO)) {
                        newState = StateTransitionRBO.REVOKED;

                        // handle initializing & evaluating higher versions of the level if lower version is revoked
                        initializeHigherVersionOfLevel(currentStatusRBO, queue, level);
                    } else if (level.getState() == StateTransitionRBO.REVOKED) {
                        initializeHigherVersionOfLevel(currentStatusRBO, queue, level);
                    }
                }

                if (CommonConfig.isValidLevelTransition(level.getState(), newState) &&
                         shouldAllowTransition(level, newState, entityRBO)) {
                    logger.debug("level status updated for level {} from {} to {}", level.getName(),
                            level.getState(), newState);
                    level.updateState(newState);
                } else {
                    logger.debug("level transition is invalid from state {} to {}", level.getState(), newState);
                    break;
                }
                
                if (isPVEnabled) {
                    newState = trimmedLevelExpr.evaluate(currentStatusRBO, level.getTenantInfo().getTenant());
                } else {
                    newState = levelExpr.evaluate(currentStatusRBO, level.getTenantInfo().getTenant());
                }
            }
        }
    }

    private AbstractStatusExpression trimLevelExpression(AbstractStatusExpression exp, LevelRBO levelRBO,
                                                         CommonEntityRBO entityRBO) throws Exception{
        ExpressionTrimmerHandler trimmerHandler = new PartialVerificationTrimmerHandler();
        LevelBO level = ((LevelRBOImpl) levelRBO).getLevelBO();
        if (entityRBO instanceof PartyRBOImpl) {
            return trimmerHandler.trimExpression(((PartyRBOImpl) entityRBO).getPartyBO(), level, exp);
        } else {
            return trimmerHandler.trimExpression(((AccountRBOImpl) entityRBO).getAccountBO(), level, exp);
        }
    }

    private AbstractStatusExpression cloneExpression(AbstractStatusExpression origExpr, CommonEntityRBO entityRBO,
                                                     String tenant) {
        if (origExpr == null) {
            return null;
        }

        AbstractStatusExpression expression;
        if(!(origExpr instanceof CompositeExpression)) {
            expression = origExpr.deepCopy();
            if (expression instanceof CriteriaExpression) {
                ((CriteriaExpression) expression).setDependent(((CriteriaExpression) origExpr).isDependent());
                ((CriteriaExpression) expression).setPotentialParentCriterias(((CriteriaExpression) origExpr)
                        .getPotentialParentCriterias());
                ((CriteriaExpression) expression).setRiskReviewEnabled(((CriteriaExpression) origExpr)
                        .isRiskReviewEnabled());
                ((CriteriaExpression) expression).updateDependentCriteriaStatus(entityRBO.getEntityType(),
                        ((CriteriaExpression) origExpr).levelRegion, ((CriteriaExpression) origExpr).levelName,
                        tenant);
            }
        } else {
            List<StatusExpression> copyExpressions = new LinkedList<>();
            for(AbstractStatusExpression statusExpression: ((CompositeExpression) origExpr).getExpressions()) {
                copyExpressions.add(cloneExpression(statusExpression, entityRBO, tenant));
            }
            expression = new CompositeExpression(((CompositeExpression) origExpr).getOperator(), copyExpressions);
        }

        return expression;
    }


    private void setEditableCriteria(Map<MultiKey, CriteriaRBO> criteriaMap, LevelRBO level) {
        level.getEditableCriteriaList().forEach(compositeKey -> {
            String criterionName = (String) compositeKey.getKey(0);
            String criterionRegion = (String) compositeKey.getKey(1);
            CriteriaRBO criteriaRBO = criteriaMap.get(new MultiKey(criterionName, criterionRegion));
            // only set metaData if criteria is not null
            if (criteriaRBO != null) {
                StateTransitionRBO state = (StateTransitionRBO) compositeKey.getKey(2);
                logger.debug("Setting editable for criterion {}, state {}, value {} ", criterionName, state, true);
                criteriaRBO.setEditable(state, true);
            }
        });
    }

    private Map<MultiKey, CriteriaRBO> buildCriteriaNameRegionToRBOMap(StatusRBO currentStatusRBO) {
        Map<MultiKey, CriteriaRBO> criteriaNameRegionToRBOMap = new HashMap<>();
        for (CriteriaRBO criteriaRBO : currentStatusRBO.getCriteria()) {
            MultiKey key = new MultiKey(criteriaRBO.getUniqueCriteriaName(), criteriaRBO.getComplianceRegion());
            criteriaNameRegionToRBOMap.put(key, criteriaRBO);
        }
        return criteriaNameRegionToRBOMap;
    }

    /**
     * Revoke a level status based on data element change of an entity.
     * If any revocation data element of a level is modified, following method will
     * revoke the level status given it was completed before
     *
     * @param levelRBO
     * @param entityRBO
     */
    private void revokeLevelBasedOnDataChange(LevelRBO levelRBO, CommonEntityRBO entityRBO) {

        if (isNotUpdateLevelStatus() || levelRBO.getState() != StateTransitionRBO.COMPLETED) {
            return;
        }

        if (RevocationUtil.isRevocationDataElementsModified(levelRBO, entityRBO) ||
                RevocationUtil.isRevocationRelatedEntitiesModified(levelRBO, Arrays.asList(entityRBO))) {
            // TODO : Auditing revocation logic when level expression is null. Need to remove after auditing
            CalEvent calEvent = CalEventFactory.create("LEVEL_REVOCATION_AUDIT");
            calEvent.setName("LEVEL_REVOCATION_UPGRADE_ACCOUNT");
            calEvent.addData("levelName", String.valueOf(levelRBO.getName()));
            calEvent.addData("entityId", String.valueOf(entityRBO.getId()));
            calEvent.addData("entityType", String.valueOf(entityRBO.getEntityType()));
            calEvent.addData("region", String.valueOf(levelRBO.getRegion()));
            calEvent.addData("is_enable",
                    RemoteConfig.getPropertyValueAsString(LEVEL_REVOCATION_BASED_ON_DATA_CHANGE_ENABLED));
            calEvent.completed("0");
            if (Boolean.valueOf(RemoteConfig.getPropertyValueAsString(LEVEL_REVOCATION_BASED_ON_DATA_CHANGE_ENABLED))) {
                levelRBO.updateState(StateTransitionRBO.REVOKED);
            }
            return;
        }
    }

    /**
     * This method is for additional safeguard checking while moving the level transition from C -> I.
     *
     */
    private boolean shouldAllowTransition(LevelRBO initialLevel, StateTransitionRBO newState,
                                          CommonEntityRBO entityRBO) {

        Authority authority = null;
        String overridden = null;
        StateTransition oldState = null;
        if (((LevelRBOImpl) initialLevel).getLevelBO() != null) {
            overridden = ((LevelRBOImpl) initialLevel).getLevelBO().getOverridden();
            authority = ((LevelRBOImpl) initialLevel).getLevelBO().getOverrideAuthority();
            oldState = ((LevelRBOImpl) initialLevel).getLevelBO().getState();
        }

        // No need for addition check in case
        // 1. inital level is not completed
        // 2. newState is revoked
        if (StateTransitionRBO.REVOKED == newState || StateTransition.COMPLETED != oldState) {
            return true;
        }

        // if level was overriden and its state is COMPLETED then no further evaluation required
        if (StateTransition.COMPLETED == oldState
                && OVERRIDDEN_TRUE.equalsIgnoreCase(overridden)
                && authority != null) {
            logger.debug("Skip level state update because it is overridden {}, {}", initialLevel.getName(), authority);
            return false;
        }

        // In case the transition is from C -> I, we are not allowing now, because this transition is in audit mode
        // we are auditing to find the impacted user volume.
        // TODO : Need to remove this condition later after auditing
        if (StateTransition.COMPLETED == oldState && StateTransitionRBO.IN_PROGRESS == newState) {
            // Not allowing transition.
            CalEvent calEvent = CalEventFactory.create("LEVEL_TRANSITION_AUDIT");
            calEvent.addData("levelName", String.valueOf(initialLevel.getName()));
            calEvent.addData("levelCompletedTime", String.valueOf(initialLevel.getCompletedTimestamp()));
            calEvent.addData("region", initialLevel.getRegion());
            calEvent.addData("newState", StateTransitionRBO.IN_PROGRESS.name());
            calEvent.addData("entityReferenceId", String.valueOf(entityRBO.getReferenceId()));
            calEvent.addData("entityType", String.valueOf(entityRBO.getEntityType()));
            calEvent.setName("LEVEL_TRANSITION_NOT_ALLOWED");
            calEvent.completed("0");
            return false;

        } else {
            return true;
        }
    }

    /** New revocation capability
     *  Steps:
     *      - revoke level on each entities once one of its criteria is revoked
     *      - and also triggered the revocation on the same level of highest version.
     *      - After revocation, the level will go through evaluation
     *  By right the revocation for criteria and level will transit the criteria/level
     *  from any state (U/N/I/C) to R state.
     *  However for now we are revoking level only when current level or its lower version level
     *  has been completed.
     */
    private void revokeLevelBeforeEvaluation(String legalCountry, StatusRBO currentStatusRBO,
                                             Map<LevelDefinitionRBO, List<LevelRBO>> levelNameMap) {

        if( RevocationUtil.isInRevocationBeforeEvaluationCountries(legalCountry) &&  !isNotUpdateLevelStatus()){
            for (LevelDefinitionRBO levelName : levelNameMap.keySet()) {
                List<LevelRBO> levelList = levelNameMap.get(levelName);

                if (CollectionUtils.isEmpty(levelList)) {
                    continue;
                }

                boolean levelRevoked = false;
                for(LevelRBO level: levelList){
                    if (level.getExpression() == null) {
                        continue;
                    }
                    // level marked as deprecated, stop to move the level status
                    if (level.isDeprecated()) {
                        continue;
                    }
                    boolean result = revokeLevel(currentStatusRBO, level,levelList);
                    levelRevoked = result? true:levelRevoked;
                }
                if(levelRevoked){
                    revokeHighestVersionOfLevel(levelList);
                }
            }
        }
    }

    private boolean revokeLevel(StatusRBO status, LevelRBO level, List<LevelRBO> levelList){
        if (RevocationUtil.isRecentlyRevoked(level) || !level.isRevocationAllowed()) {
            CalEvent calEvent = CalEventFactory.create("LEVEL_REVOCATION_AUDIT");
            calEvent.setName("LEVEL_REVOCATION_NOT_ALLOWED");
            calEvent.addData("levelName", String.valueOf(level.getName()));
            calEvent.addData("entityReferenceId", status.getEntityReferenceId());
            calEvent.addData("entityType", String.valueOf(status.getEntityType()));
            calEvent.addData("region", String.valueOf(level.getRegion()));
            calEvent.completed("0");
            return false;
        }
        AbstractStatusExpression levelExpr = (AbstractStatusExpression) level.getExpression();
        List<CriteriaRBO> allCriteria = new ArrayList<>();
        getAllCriteriaRBO(levelExpr, status, allCriteria, level.getTenantInfo().getTenant());

        for (CriteriaRBO criteriaRBO : allCriteria) {
            if (criteriaRBO != null && criteriaRBO.isRevoked()
                    && (criteriaRBO.hasModifiedState()
                    || ((CriteriaRBOImpl)criteriaRBO).getPreviousInteractionModified())
                    && isRevocationApplicable(level,levelList)) {
                level.updateState(StateTransitionRBO.REVOKED);
                level.setEvaluationDone(true);
                logger.debug("level revoked !!! ");
                return true;
            }
        }
        return false;
    }

    private boolean isRevocationApplicable(LevelRBO level, List<LevelRBO> levelList){
        if(level.getState() == StateTransitionRBO.COMPLETED){
            logger.debug("higher level found - isRevocationApplicable!!! ");
            return true;
        }

        //lower version level is completed
        for (LevelRBO lvLevel : levelList) {//lower version level
            if (lvLevel != null
                    && lvLevel.getVersion() < level.getVersion()
                    && lvLevel.getName() == level.getName()
                    && lvLevel.getRegion().equals(level.getRegion())
                    && lvLevel.getState() == StateTransitionRBO.COMPLETED) {
                logger.debug("lower level found - isRevocationApplicable!!! ");
                return true;
            }
        }
        return false;
    }

    /**
     * get all criteriaRBO in the expression, including criteria in it's sub level
     * @param expression
     * @param currentStatus
     * @param allCriteria
     */
    private void getAllCriteriaRBO(AbstractStatusExpression expression,
                                   StatusRBO currentStatus, List<CriteriaRBO> allCriteria, String tenant) {
        if (allCriteria == null) {
            allCriteria = new ArrayList<CriteriaRBO>();
        }
        if (expression instanceof LevelExpression) {
            if (expression.getEntities() == null) {
                //no subLevel.
            } else {
                for (CommonEntityRBO entity : expression.getEntities().getEntities()) {
                    LevelRBO subLevel = entity.getStatus().getLevel(
                            ((LevelExpression) expression).getName(),
                            ((LevelExpression) expression).getVersion(),
                            ((LevelExpression) expression).getRegion(),
                            tenant);
                    if (subLevel != null) {
                        getAllCriteriaRBO((AbstractStatusExpression) subLevel.getExpression(),
                                entity.getStatus(), allCriteria, tenant);
                    }
                }
            }
        } else if (expression instanceof CriteriaExpression) {
            if (expression.getEntities() == null) {
                CriteriaRBO criteriaRBO
                    = currentStatus.getCriteria(((CriteriaExpression)expression).getUniqueCriteriaName(),
                        ((CriteriaExpression)expression).getCriteriaRegion(), false, tenant);
                allCriteria.add(criteriaRBO);
            } else {
                for (CommonEntityRBO entity : expression.getEntities().getEntities()) {
                    CriteriaRBO criteriaRBO
                        = entity.getStatus().getCriteria(((CriteriaExpression)expression).getUniqueCriteriaName(),
                            ((CriteriaExpression)expression).getCriteriaRegion(), false, tenant);
                    if (criteriaRBO != null) {
                        allCriteria.add(criteriaRBO);
                    }
                }
            }
        } else if (expression instanceof CompositeExpression) {
            for (AbstractStatusExpression subExpression :
                    ((CompositeExpression) expression).getExpressions()) {
                getAllCriteriaRBO(subExpression, currentStatus, allCriteria, tenant);
            }
        }
    }

    private void revokeHighestVersionOfLevel(List<LevelRBO> levels) {
        if(CollectionUtils.isEmpty(levels)){
            return;
        }

        LevelRBO highestVersionOfLevel = levels.get(0);
        for (LevelRBO curLevel : levels) {
            if (curLevel.getName().equals(highestVersionOfLevel.getName())
                    && curLevel.getVersion() > highestVersionOfLevel.getVersion()) {
                highestVersionOfLevel = curLevel;
            }
        }
        if(highestVersionOfLevel.getState() != StateTransitionRBO.REVOKED){
            logger.debug("revoking highest version of revoked level {} ", highestVersionOfLevel);
            // we are doing a force revocation and this will not go through level valid transition checks
            highestVersionOfLevel.updateState(StateTransitionRBO.REVOKED);
            highestVersionOfLevel.setEvaluationDone(true);
            // queue this level for re-evaluation to correct state
        }
    }

    private void populateLevelUniqueKey(LevelRBO level, BigInteger entityId, String entityExternalId,
                                        EntityTypeRBO entityType) {
        LevelBO levelBO = ((LevelRBOImpl)level).getLevelBO();
        levelBO.setEntityID(entityId);
        levelBO.setEntityExternalId(entityExternalId);
        levelBO.setEntityType(EntityTypeResolver.resolve(entityType));
        ReferenceKeyUtil.populateUniqueReferenceKey(levelBO);
    }

	private void populateComplianceRelationships(CommonEntityRBO commonEntityRBO, LevelRBO level,
			AbstractStatusExpression levelExpr) {
        LevelBO levelBO = ((LevelRBOImpl) level).getLevelBO();
        ComplianceRelationshipNodeBO nodeBO = new ComplianceRelationshipNodeBO();
        nodeBO.setNodeType(ComplianceRelationshipNodeType.LEVEL);
        nodeBO.setReferenceKey(levelBO.getReferenceKey());
        nodeBO.setEntityRole(levelExpr.getEntities() != null ?
                levelExpr.getEntities().getRelatedEntityType() : commonEntityRBO.getRelatedEntityType());
        populateComplianceRelationships(commonEntityRBO, level, levelExpr, nodeBO);
        levelBO.setComplianceRelationshipNodeBO(nodeBO);
    }

	private void populateComplianceRelationships(CommonEntityRBO commonEntityRBO, LevelRBO level,
			AbstractStatusExpression levelExpr, ComplianceRelationshipNodeBO parentNodeBO) {
        if(levelExpr instanceof CompositeExpression) {
            ComplianceRelationshipNodeBO nodeBO = new ComplianceRelationshipNodeBO();
            nodeBO.setNodeType(ComplianceRelationshipNodeType.COMPOSITE);
            nodeBO.setOperatorType(OperatorType.valueOf(((CompositeExpression) levelExpr).getOperator().name()));
            for(AbstractStatusExpression expression : ((CompositeExpression)levelExpr).getExpressions()) {
                populateComplianceRelationships(commonEntityRBO, level, expression, nodeBO);
            }
            parentNodeBO.getChildRelationshipNodes().add(nodeBO);
        }else {
            if(levelExpr instanceof LevelExpression) {
                populateLevelToLevelRelationshipsForAllEntities(commonEntityRBO, level,
                        (LevelExpression)levelExpr, parentNodeBO);

            } else if (levelExpr instanceof CriteriaExpression) {
                populateLevelToCriterionRelationshipsForAllEntities(commonEntityRBO, level,
                        (CriteriaExpression)levelExpr, parentNodeBO);
            }
        }
    }

    private void populateLevelToLevelRelationshipsForAllEntities(CommonEntityRBO levelEntity, LevelRBO level,
                    LevelExpression subLevel, ComplianceRelationshipNodeBO parentNode) {
        LevelBO levelBO = ((LevelRBOImpl)level).getLevelBO();

        if(subLevel.getEntities() == null) {
            populateLevelToLevelRelationships(levelEntity, levelBO, subLevel, parentNode);
        }else {
            if(CollectionUtils.isNotEmpty(subLevel.getEntities().getEntities())) {
                for (CommonEntityRBO subLevelEntity : subLevel.getEntities().getEntities()) {
                    populateLevelToLevelRelationships(subLevelEntity, levelBO, subLevel, parentNode);
                }
            }

            if(shouldAppendPlaceHolderInEntities(subLevel)){
                ComplianceRelationshipNodeBO nodeBO = new ComplianceRelationshipNodeBO();
                nodeBO.setNodeType(ComplianceRelationshipNodeType.LEVEL);
                if(populateLevelPlaceHolderRelationships(subLevel, levelEntity.getEntityType(),  nodeBO)) {
                    parentNode.getChildRelationshipNodes().add(nodeBO);
                }
            }

        }
    }

    private boolean shouldAppendPlaceHolderInEntities(AbstractStatusExpression expression) {
        if(expression.getEntities() != null) {
            // This is because all the entities is placeholder, no real entity
            if(CollectionUtils.isEmpty(expression.getEntities().getEntities())) {
                return true;
            }
            // For partial entities is placeholder entity
            // If the dummy entity has been generated, skip it,
            // because it will be handled in populateLevelToLevelRelationships
            for(CommonEntityRBO levelEntity : expression.getEntities().getEntities()) {
                if(null == levelEntity.getReferenceId()) {
                    return false;
                }
            }

            EntityCardinalityRBO cardinalityRBO = expression.getEntities().getCardinality();
            EntityCardinalityBO cardinalityBO = cardinalityRBO != null ?
                    ((EntityCardinalityRBOImpl) cardinalityRBO).getEntityCardinalityBO() : null;
            if(null != cardinalityBO && null != cardinalityBO.getDeclarationCount() &&
                    cardinalityBO.getNumberOfEntitiesDeclared() < cardinalityBO.getDeclarationCount()) {
                return true;
            }
            if(null != cardinalityBO && "YES".equals(cardinalityBO.getExistenceDeclaration()) &&
                    cardinalityBO.getNumberOfEntitiesDeclared() == 0) {
                return true;
            }
        }
        return false;
    }

    private void populateLevelToLevelRelationships(CommonEntityRBO entity, LevelBO level,
                                                   LevelExpression subLevel, ComplianceRelationshipNodeBO parentNode) {
        String levelRelationshipKey = level.getReferenceKey();
        String subLevelRelationshipKey = ReferenceKeyUtil.getUniqueReferenceKey(entity.getReferenceId(),
                        entity.getEntityType(), subLevel);
        // Old relationship structure only for Venmo user case
        level.getComplianceRelationships().add(new ComplianceRelationshipBO(levelRelationshipKey,
                subLevelRelationshipKey));

        // New structure for general relationship tree
        ComplianceRelationshipNodeBO nodeBO = new ComplianceRelationshipNodeBO();
        nodeBO.setNodeType(ComplianceRelationshipNodeType.LEVEL);
        nodeBO.setReferenceKey(subLevelRelationshipKey);
        nodeBO.setEntityRole(subLevel.getEntities() != null ?
                subLevel.getEntities().getRelatedEntityType() : entity.getRelatedEntityType());

        // If current entity is a dummy entity, it means this is a placeholder generated by pre processors
        if(null == entity.getReferenceId()) {
            EntityTypeRBO entityType = subLevel.getEntityType() != null ?
                    EntityTypeResolver.resolve(subLevel.getEntityType()) : entity.getEntityType();
            entityType =  entityType == null ? EntityTypeRBO.PAYPAL_PARTY_INDIVIDUAL : entityType;
            populateLevelPlaceHolderRelationships(subLevel, entityType, nodeBO);
        }
        parentNode.getChildRelationshipNodes().add(nodeBO);
    }

    // If placeholder is populated, it returns true otherwise false
    private boolean populateLevelPlaceHolderRelationships(AbstractStatusExpression levelExpr,
                                                          EntityTypeRBO entityTypeRBO,
                                                          ComplianceRelationshipNodeBO nodeBO) {
        // populate placeholders if exists
        // For placeholder, the entity bo is not empty, entity id and entity type are both null
        if (null != levelExpr.getEntities()) {
            EntityCardinalityRBO cardinalityRBO = levelExpr.getEntities().getCardinality();
            EntityCardinalityBO cardinalityBO = cardinalityRBO != null ?
                    ((EntityCardinalityRBOImpl) cardinalityRBO).getEntityCardinalityBO() : null;

            if (null == cardinalityBO
                       || (null != cardinalityBO.getDeclarationCount() &&
                                   cardinalityBO.getNumberOfEntitiesDeclared() < cardinalityBO.getDeclarationCount())
                       || ("YES".equals(cardinalityBO.getExistenceDeclaration()) &&
                                   cardinalityBO.getNumberOfEntitiesDeclared() == 0)) {
                // The placeholder entity type is the same as common entity
                String subLevelRelationshipKey = ReferenceKeyUtil.getUniqueReferenceKey(null,
                        entityTypeRBO, (LevelExpression) levelExpr);
                nodeBO.setReferenceKey(subLevelRelationshipKey);
                nodeBO.setEntityRole(levelExpr.getEntities().getRelatedEntityType());
                nodeBO.setCardinalityBO(cardinalityBO);
                return true;
            }
        }
        return false;
    }

	private void populateLevelToCriterionRelationshipsForAllEntities(CommonEntityRBO levelEntity, LevelRBO level,
			CriteriaExpression criteriaExpression, ComplianceRelationshipNodeBO parentNode) {
        String uniqueCriteriaName = criteriaExpression.getUniqueCriteriaName();
        String criteriaRegion = criteriaExpression.getCriteriaRegion();
        LevelBO levelBO = ((LevelRBOImpl)level).getLevelBO();

        if(criteriaExpression.getEntities() == null) {
            populateLevelToCriterionRelationships(levelEntity, levelBO, criteriaExpression,
                    uniqueCriteriaName, criteriaRegion, parentNode);
        }else if(CollectionUtils.isNotEmpty(criteriaExpression.getEntities().getEntities())){
            // here the criterion expression could be a rel exp
            if(criteriaExpression instanceof RelationshipCriteriaExpression) {
                RelationshipCriteriaExpression relExp = (RelationshipCriteriaExpression) criteriaExpression;
                for (RelationshipRBO relRBO : relExp.getRelationshipRBOList()) {
                    // relationship criteria such as LOA
                    if (relRBO instanceof PartyToAccountRelationshipRBOImpl) {
                        populateLevelToCriterionRelationships((PartyToAccountRelationshipRBOImpl) relRBO, levelBO,
                                criteriaExpression, uniqueCriteriaName, criteriaRegion, parentNode);
                    } else if (relRBO instanceof PartyToPartyRelationshipRBOImpl) {
                        populateLevelToCriterionRelationships((PartyToPartyRelationshipRBOImpl) relRBO, levelBO,
                                criteriaExpression, uniqueCriteriaName, criteriaRegion, parentNode);
                    } else {
                        logger.error("RelationshipRBO is not instance of PartyToAccountRelationshipRBOImpl" +
                                "or PartyToPartyRelationshipRBOImpl, relationship population is skipped for level - "
                                + levelBO.toString());
                    }
                }
            } else {
                for (CommonEntityRBO criterionEntity : criteriaExpression.getEntities().getEntities()) {
                    populateLevelToCriterionRelationships(criterionEntity, levelBO, criteriaExpression,
                            uniqueCriteriaName, criteriaRegion, parentNode);
                }
            }
        }

        if(shouldAppendPlaceHolderInEntities(criteriaExpression)) {
			populateCriteriaPlaceHolderRelationships(levelEntity, criteriaExpression, parentNode,
                    level.getTenantInfo().getTenant());
        }
    }

    private void populateLevelToCriterionRelationships(CommonEntityRBO entity, LevelBO level,
                                                       CriteriaExpression criteriaExpression,
                                                       String uniqueCriteriaName, String criteriaRegion,
                                                       ComplianceRelationshipNodeBO parentNode) {
        CriterionBO criterionBO = getCriterionBOByUniqueCriteriaName(entity, uniqueCriteriaName, criteriaRegion,
                level.getTenantInfo().getTenant());

        if (criterionBO != null) {
            ReferenceKeyUtil.populateUniqueReferenceKey(criterionBO, entity);
            String levelRelationshipKey = level.getReferenceKey();
            String criterionRelationshipKey = criterionBO.getReferenceKey();

            // Old relationship structure only for Venmo user case
            level.getComplianceRelationships().add(new ComplianceRelationshipBO(levelRelationshipKey,
                            criterionRelationshipKey));

            // New structure for general relationship tree
            ComplianceRelationshipNodeBO nodeBO = new ComplianceRelationshipNodeBO();
            nodeBO.setNodeType(ComplianceRelationshipNodeType.CRITERION);
            nodeBO.setReferenceKey(criterionRelationshipKey);
            nodeBO.setEntityRole(criteriaExpression.getEntities() != null ?
                    criteriaExpression.getEntities().getRelatedEntityType() : entity.getRelatedEntityType());

            // For placeholder
            if(entity.getReferenceId() == null && null != criteriaExpression.getEntities()
                    && null != criteriaExpression.getEntities().getCardinality()) {
                nodeBO.setCardinalityBO(((EntityCardinalityRBOImpl)criteriaExpression.getEntities().getCardinality())
                        .getEntityCardinalityBO());
            }

            parentNode.getChildRelationshipNodes().add(nodeBO);

            // populate criteria dependencies and append to level dependencies set
            // placeholders have no dependencies
            if(null != entity.getReferenceId()) {
                CriteriaDependencyUtil.populateCriteriaDependency(entity, criterionBO);
                CriteriaDependencyBOUtil.mergeDependency(level.getCriteriaDependencies(), criterionBO.getDependency());
            }
        }
    }

    /**
     * For example, the relationship of all the levels is L1 -> L2 -> L3 -> C1
     * After evaluating all levels, what it gets is [L1 -> L2, L2 -> L3, L3 -> C1]
     * Each level relationship nodeBO only contains one step
     * To chain the whole relationship together, combine them according to reference key
     * @param relatedEntityContext
     */
    private void combineLevelsRelationshipNodes(RelatedEntityContextRBO relatedEntityContext) {
        // This map is for quick lookup relationship node
        // The key is reference key + entity type, because there are levels having the same referenceKey but not
        Map<String, ComplianceRelationshipNodeBO> levelNodeMap = new HashMap<>();
        for(CommonEntityRBO entityRBO : relatedEntityContext.getAllEntities()) {
            StatusRBO currentStatusRBO = entityRBO.getStatus();
            if (currentStatusRBO == null) {
                continue;
            }
            for (LevelRBO level : currentStatusRBO.getLevels()) {
                LevelBO levelBO = ((LevelRBOImpl) level).getLevelBO();
                if(levelBO.getReferenceKey() != null
                        && levelBO.getComplianceRelationshipNodeBO() != null
                        && levelBO.getComplianceRelationshipNodeBO().getEntityRole() != null) {
                    String combinedKey = levelBO.getReferenceKey() +
                            levelBO.getComplianceRelationshipNodeBO().getEntityRole().name();
                    levelNodeMap.put(combinedKey, levelBO.getComplianceRelationshipNodeBO());
                }
            }
        }

        /**
         *          Follow the above example, the relationship of L1 should be L1 -> L2 -> L3
         *          but currently what we have is L1 -> L2, L2 -> L3
         *          The basic idea is to traversal L1 subnodes and get L2, then replace it with evaluated L2
         *          which having its subnodes(L2 -> L3), then the full relationship of L1 is done
         *
         *          Use a queue to maintain LEVEL or COMPOSITE nodes to look for their LEVEL children
         *          It's a BFS, if current node is COMPOSITE, append its children to queue
         *          Otherwise, if it's LEVEL, then pick up the sublevel nodes to replace
         */
        Queue<ComplianceRelationshipNodeBO> queue = new LinkedList<>(levelNodeMap.values());
        while(!queue.isEmpty()) {
            ComplianceRelationshipNodeBO currentNode = queue.poll();

            // Traversal the subnodes and determine which nodes should be replaced
            List<ComplianceRelationshipNodeBO> replaceNodes = new ArrayList<>();
            for(ComplianceRelationshipNodeBO subNode : currentNode.getChildRelationshipNodes()) {
                if(subNode.getNodeType().equals(ComplianceRelationshipNodeType.COMPOSITE)) {
                    queue.add(subNode);
                } else if(subNode.getNodeType().equals(ComplianceRelationshipNodeType.LEVEL) &&
                        subNode.getReferenceKey() != null && subNode.getEntityRole() != null &&
                        levelNodeMap.containsKey(subNode.getReferenceKey() + subNode.getEntityRole().name())) {
                        replaceNodes.add(subNode);
                }
            }

            // Replace nodes
            for(ComplianceRelationshipNodeBO replaceNode : replaceNodes) {
                currentNode.getChildRelationshipNodes().remove(replaceNode);
                String combinedKey = replaceNode.getReferenceKey() + replaceNode.getEntityRole().name();
                // For placeholder, the cardinalityBO should be copied too
                levelNodeMap.get(combinedKey).setCardinalityBO(replaceNode.getCardinalityBO());
                currentNode.getChildRelationshipNodes().add(levelNodeMap.get(combinedKey));
            }
        }

    }

    /**
     * Combine all the criteria dependencies for one level
     * for example, for CIP account entry, it have TPV in personal CIP and TPV in biz CIP
     * then we should combine these two TPV into account CIP level
     * Because we get CIP_ACCOUNT -> CIP_PERSON, CIP_ACCOUNT -> CIP_BIZ relationships previously,
     * traversal the ComplianceRelationshipBO list of each level to get its sublevel's criteria dependency
     * @param relatedEntityContext
     */
    private void combineLevelsCriteriaDependencies(RelatedEntityContextRBO relatedEntityContext) {
        Map<String, Set<CriteriaDependencyBO>> LevelToDependenciesMap = new HashMap<>();
        List<LevelBO> levelBOList = new ArrayList<>();
        for(CommonEntityRBO entityRBO : relatedEntityContext.getAllEntities()) {
            StatusRBO currentStatusRBO = entityRBO.getStatus();
            if (currentStatusRBO == null) {
                continue;
            }
            for (LevelRBO level : currentStatusRBO.getLevels()) {
                LevelBO levelBO = ((LevelRBOImpl) level).getLevelBO();
                levelBOList.add(levelBO);
                if(levelBO.getReferenceKey() != null && !levelBO.getCriteriaDependencies().isEmpty()) {
                    LevelToDependenciesMap.put(levelBO.getReferenceKey(), levelBO.getCriteriaDependencies());
                }
            }
        }

        for(LevelBO levelBO : levelBOList) {
            for(ComplianceRelationshipBO relationshipBO : levelBO.getComplianceRelationships()) {
                String objectKey = relationshipBO.getObjectRelationshipKey();
                if(LevelToDependenciesMap.containsKey(objectKey)) {
                    CriteriaDependencyBOUtil.mergeDependencySet(levelBO.getCriteriaDependencies(),
                            LevelToDependenciesMap.get(objectKey));
                }
            }
        }

    }

	private CriterionBO getCriterionBOByUniqueCriteriaName(CommonEntityRBO entity, String uniqueCriteriaName,
			String criteriaRegion, String tenant) {
        // load criteria by level region
        CriteriaRBO criteriaRBO = LevelCriteriaDefinitionConfig.getUniqueCriteria(uniqueCriteriaName, criteriaRegion,
                tenant);

        if (criteriaRBO != null) {
            CriterionBO criterionBO = ((CriteriaRBOImpl)criteriaRBO).getCriterionBO();

            if(criterionBO.getEntityID() == null) {
                criterionBO.setEntityID(entity.getId());
            }

            if (criterionBO.getEntityExternalId() == null) {
                criterionBO.setEntityExternalId(entity.getExternalId());
            }

            if(criterionBO.getEntityType() == null) {
                criterionBO.setEntityType(EntityTypeResolver.resolve(entity.getEntityType()));
            }

            return criterionBO;

        }else{
            //log a warning in cal for criteria definition not found and continue processing
            String errorMsg
                = "cannot find criteria definition with name: " + uniqueCriteriaName + ", region: " + criteriaRegion;
            logger.error(errorMsg);
            CalEventFactory.create(CalEvent.CAL_WARNING, "LevelEvaluatorManagerImpl",
                    "CriteriaDefinitionNotFound", errorMsg).completed();
        }
        return null;
    }

	private void populateCriteriaPlaceHolderRelationships(CommonEntityRBO commonEntityRBO,
			AbstractStatusExpression criteriaExpr, ComplianceRelationshipNodeBO parentNode, String tenant) {
        // populate placeholders if exists
        // For placeholder, the entity bo is empty, entity id and entity type are both null

        if (null != criteriaExpr.getEntities()) {
            EntityCardinalityRBO cardinalityRBO = criteriaExpr.getEntities().getCardinality();
            EntityCardinalityBO cardinalityBO = cardinalityRBO != null ?
                    ((EntityCardinalityRBOImpl) cardinalityRBO).getEntityCardinalityBO() : null;
            if (null == cardinalityBO
                 || (null != cardinalityBO.getDeclarationCount() &&
                             cardinalityBO.getNumberOfEntitiesDeclared() < cardinalityBO.getDeclarationCount())
                 || ("YES".equals(cardinalityBO.getExistenceDeclaration()) &&
                             cardinalityBO.getNumberOfEntitiesDeclared() == 0)) {
                String uniqueCriteriaName = ((CriteriaExpression) criteriaExpr).getUniqueCriteriaName();
                String criteriaRegion = ((CriteriaExpression) criteriaExpr).getCriteriaRegion();
                CriterionBO criterionBO = getCriterionBOByUniqueCriteriaName(commonEntityRBO,
                        uniqueCriteriaName, criteriaRegion, tenant);
                if (criterionBO == null) {
                    logger.warn("Not found criteria for " + uniqueCriteriaName);
                    return;
                }
                // For placeholder the entity id is null and entity type depends on common entity type
                // What we need is the information besides entity
                ComplianceRelationshipNodeBO nodeBO = new ComplianceRelationshipNodeBO();
                nodeBO.setNodeType(ComplianceRelationshipNodeType.CRITERION);
                EntityType entityType = criteriaExpr.getEntityType() != null ? criteriaExpr.getEntityType()
                        : EntityTypeResolver.resolve(commonEntityRBO.getEntityType());
                entityType = entityType == null ? EntityType.PAYPAL_PARTY_INDIVIDUAL : entityType;
                String referenceKey = ReferenceKeyUtil.getUniqueReferenceKey(null, entityType, criterionBO);
                nodeBO.setReferenceKey(referenceKey);
                nodeBO.setEntityRole(criteriaExpr.getEntities() != null ?
                         criteriaExpr.getEntities().getRelatedEntityType() : commonEntityRBO.getRelatedEntityType());
                nodeBO.setCardinalityBO(cardinalityBO);
                parentNode.getChildRelationshipNodes().add(nodeBO);
            }
        }

    }
    
    /**
     * 
     * @param entityRBO
     * @param processedLevels
     */
    protected void revoke(CommonEntityRBO entityRBO, Set<LevelBO> processedLevels) {
        StatusRBO currentStatusRBO = entityRBO.getStatus();

        if (currentStatusRBO == null) {
            return;
        }

        Queue<LevelRBO> queue = new LinkedList<>();
        for (LevelRBO level : currentStatusRBO.getLevels()) {
            if (level.getExpression() == null) {
                continue;
            }
            queue.add(level);
        }

        while (!queue.isEmpty()) {
            LevelRBO level = queue.poll();
            // level marked as deprecated, stop to move the level status
            if (level.isDeprecated()) {
                continue;
            }
            if (level.isOverrideCascading() && level.hasState(StateTransitionRBO.REVOKED) && level.hasStateOverridden()
                    && !processedLevels.contains(getLevelBO(level))) {
                AbstractStatusExpression expression = (AbstractStatusExpression) level.getExpression();
                revoke(entityRBO, expression, level, processedLevels, level.getTenantInfo().getTenant());
                processedLevels.add(getLevelBO(level));
            }
        }
    }
    
    /**
     * 
     * @param entityRBO
     */
    protected void invalidate(CommonEntityRBO entityRBO) {
        StatusRBO currentStatusRBO = entityRBO.getStatus();
        if (currentStatusRBO == null) {
            return;
        }

        for (LevelRBO level : currentStatusRBO.getLevels()) {
            // Set entity type first
            getLevelBO(level).setEntityType(EntityType.valueOf(entityRBO.getEntityType().name()));
            
            if (level.getExpression() == null && LevelCriteriaDefinitionConfig.isInvalidationAllowed(level)
                    && level.getState() != StateTransitionRBO.NOT_AVAILABLE
                    && level.getState() != StateTransitionRBO.COMPLETED) {
                // Calculate prerequisite decision for level invalidation
                // Levels with null expression and met pre-req will be invalidated
                OnboardingDecisionBO levelPrereqDecision = calculatePrereqDecision(level);
                if (levelPrereqDecision.getDecisionCode() == DecisionCode.ALLOW) {
                    logger.info("Level {} is applicable for invalidation. Move state to INACTIVE.", level.getName());
                    level.updateState(StateTransitionRBO.INACTIVE);
                }
                continue;
            }
        }
    }
    
    private OnboardingDecisionBO calculatePrereqDecision(LevelRBO level) {
        logger.info("Calculate level {} prereq decision for level invalidation.", level.getName());
        OnboardingDecisionBO levelPrereqDecision = new OnboardingDecisionBO();
        try {
            PolicyRBOImpl levelPrereqPolicy = (PolicyRBOImpl) level.getLevelPrerequisitePolicyRBO();
            DataPolicyDecisioningCalculatorRBOImpl policyCalc = new DataPolicyDecisioningCalculatorRBOImpl(
                    levelPrereqPolicy);
            policyCalc.calculate();
            levelPrereqDecision = levelPrereqPolicy.getPolicyBO().getDecision();
        } catch (Exception e) {
            levelPrereqDecision.setDecisionCode(DecisionCode.NULL);
        }
        return levelPrereqDecision;
    }
    
    private void initializeHigherVersionOfLevel(StatusRBO currentStatusRBO, Queue<LevelRBO> queue, LevelRBO level) {
        for (LevelRBO curLevel : currentStatusRBO.getLevels()) {
            if (curLevel.getName().equals(level.getName()) && curLevel.getVersion() > level.getVersion()
                    && curLevel.getState() == StateTransitionRBO.NOT_AVAILABLE) {
                logger.debug("initializing higher version of revoked level {} ", curLevel);
                // we are doing a force initialization and this will not go through level valid transition checks
                curLevel.updateState(StateTransitionRBO.NOT_INITIATED);
                // queue this level for re-evaluation to correct state
                queue.add(curLevel);
            }
        }
    }

    private boolean isLevelRevoked(LevelRBO level, StateTransitionRBO newState, CommonEntityRBO entity) {
        boolean isLevelRevoked = false;
        if (level.getState() == StateTransitionRBO.COMPLETED
                && newState != StateTransitionRBO.COMPLETED) {
            // log an event for a potential revocation     
            if(((AbstractStatusExpression) level.getExpression()).isRevocationValid(level, entity, calManager,
                    level.getTenantInfo().getTenant())) {
                // log an event for an actual revocation
                isLevelRevoked = true;
            }
            calManager.addCALLog(level, entity, CALLogManager.POTENTIAL_REVOCATION, String.valueOf(isLevelRevoked));
            // do CAL logs only for evaluation flow
            if(!isNotUpdateLevelStatus()) {
                calManager.completeLoggingEvents();
            }
        }
        return isLevelRevoked;
    }
    
    private void revoke(CommonEntityRBO currentEntity, AbstractStatusExpression expression, LevelRBO parentLevel,
            Set<LevelBO> processedLevels, String tenant) {
        if (expression instanceof LevelExpression) {
            LevelExpression levelExpr = (LevelExpression) expression;
            if (levelExpr.getEntities() != null) {
                revokeLevel(levelExpr.getEntities().getEntities(), levelExpr, parentLevel, processedLevels, tenant);
            } else { // sub level has same entity
                revokeLevel(Arrays.asList(currentEntity), levelExpr, parentLevel, processedLevels, tenant);
            }
        } else if (expression instanceof CompositeExpression) {
            CompositeExpression compExpr = (CompositeExpression) expression;
            logger.debug("Composite Expr {} Entity Id {}", compExpr, currentEntity.getReferenceId());
            for (AbstractStatusExpression subExpr : compExpr.getExpressions()) {
                revoke(currentEntity, subExpr, parentLevel, processedLevels, tenant);
            }
        } else if (expression instanceof CriteriaExpression) {
            if (expression instanceof RelationshipCriteriaExpression) {
                revokeRelationshipCriteria((RelationshipCriteriaExpression) expression, parentLevel, tenant);
            } else {
                CriteriaExpression criteriaExpr = (CriteriaExpression) expression;
                if (criteriaExpr.getEntities() != null) {
                    revokeCriteria(criteriaExpr.getEntities().getEntities(), criteriaExpr, parentLevel, tenant);
                } else {
                    revokeCriteria(Arrays.asList(currentEntity), criteriaExpr, parentLevel, tenant);
                }
            }
        }
    }
    
    private void revokeLevel(List<CommonEntityRBO> entities, LevelExpression levelExpr, LevelRBO parentLevel,
            Set<LevelBO> processedLevels, String tenant) {
        for (CommonEntityRBO entity : entities) {
            LevelRBO levelRBO = entity.getStatus().getLevel(levelExpr.getName(), levelExpr.getVersion(),
                    levelExpr.getRegion(), tenant);
            logger.debug("revoke Level: Name {} Expr {} Role {} Entity Id {} Override Cascading {}", levelExpr
                    .getName(), levelExpr, levelExpr.getEntities() != null ? levelExpr.getEntities()
                    .getRelatedEntityType() : null, entity.getReferenceId(), levelRBO.isOverrideCascading());
            if (levelRBO != null && levelRBO.getState() != StateTransitionRBO.NOT_AVAILABLE) {
                overrideAndRevokeLevel(levelRBO, parentLevel);
            }
            
            if (levelRBO != null && levelRBO.isOverrideCascading() 
                    && !processedLevels.contains(getLevelBO(levelRBO))) {
                revoke(entity, (AbstractStatusExpression) levelRBO.getExpression(), parentLevel, processedLevels,
                        tenant);
                processedLevels.add(getLevelBO(levelRBO));
            }
        }
    }

    private void revokeCriteria(List<CommonEntityRBO> entities, CriteriaExpression criteriaExpr, LevelRBO parentLevel,
                                String tenant) {
        for (CommonEntityRBO entity : entities) {
            logger.debug("revoke criteria: Expr {} Role {} Entity Id {}", criteriaExpr.getUniqueCriteriaName(),
                    criteriaExpr.getEntities() != null ? criteriaExpr.getEntities().getRelatedEntityType() : null,
                    entity.getReferenceId());
            CriteriaRBO criteria = entity.getStatus().getCriteria(criteriaExpr.getUniqueCriteriaName(),
                    criteriaExpr.getCriteriaRegion(),
                    parentLevel != null && StringUtils.isNotEmpty(parentLevel.getRegion()),
                    tenant);
            // Criteria under DATA_COLLECTION_WITHOUT_REVIEW won't be revoked as
            // data had been provided and no re-verification needed    
            if (criteria != null && criteria.getState() != StateTransitionRBO.NOT_AVAILABLE
                    && (criteria.getClassification() == CriteriaClassification.DATA_COLLECTION_WITH_REVIEW
                    	||criteria.getClassification() == CriteriaClassification.AUTO_VERIFICATION)) {
                overrideAndRevokeCriteria(criteria, parentLevel);
                criteria.updateVerificationLogs();
            }
        }
    }

    private void revokeRelationshipCriteria(RelationshipCriteriaExpression relCriteriaExpr, LevelRBO parentLevel,
                                            String tenant) {
        if (relCriteriaExpr.getEntities() != null) {
            AccountRBO account = relCriteriaExpr.getRelatedAccount();
            for (CommonEntityRBO entity : relCriteriaExpr.getEntities().getEntities()) {
                if (entity instanceof PartyRBO) {
                    RelationshipRBO relationship = ((PartyRBOImpl) entity).getMatchingRelationship(account);
                    if (relationship != null) {
                        CriteriaRBO criteria = relationship.getStatus().getCriteria(
                                relCriteriaExpr.getUniqueCriteriaName(), relCriteriaExpr.getCriteriaRegion(),
                                parentLevel != null && StringUtils.isNotEmpty(parentLevel.getRegion()), tenant);
                        if (criteria != null && criteria.getState() != StateTransitionRBO.NOT_AVAILABLE) {
                            overrideAndRevokeCriteria(criteria, parentLevel);
                            criteria.updateVerificationLogs();
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * @return
     */
    public boolean isNotUpdateLevelStatus() {
        return notUpdateLevelStatus;
    }

    /**
     * 
     * @param notUpdateLevelStatus
     */
    public void setNotUpdateLevelStatus(boolean notUpdateLevelStatus) {
        this.notUpdateLevelStatus = notUpdateLevelStatus;
    }

    private CriteriaRBO overrideAndRevokeCriteria(CriteriaRBO criteriaToBeOverridden, LevelRBO source) {
        LevelRBOImpl level = (LevelRBOImpl) source;
        CriteriaRBOImpl criteria = (CriteriaRBOImpl) criteriaToBeOverridden;
        criteria.updateState(StateTransitionRBO.REVOKED);
        CriterionBO criteriaBO = criteria.getCriterionBO();
        criteriaBO.setOverridden(OVERRIDDEN_TRUE);
        criteriaBO.setMemoBO(level.getLevelBO().getMemoBO());
        criteriaBO.setApprover(level.getLevelBO().getApprover());
        criteriaBO.setOverrideAuthority(level.getLevelBO().getOverrideAuthority());
        CriteriaDependencyBOUtil.resetDependency(criteria.getCriterionBO());
        return criteriaToBeOverridden;
    }

    private LevelRBO overrideAndRevokeLevel(LevelRBO levelToBeOverridden, LevelRBO source) {
        LevelBO targetLevelBO = ((LevelRBOImpl) levelToBeOverridden).getLevelBO();
        LevelBO sourceLevelBO = ((LevelRBOImpl) source).getLevelBO();
        levelToBeOverridden.updateState(StateTransitionRBO.REVOKED);
        targetLevelBO.setOverridden(OVERRIDDEN_TRUE);
        targetLevelBO.setMemoBO(sourceLevelBO.getMemoBO());
        targetLevelBO.setApprover(sourceLevelBO.getApprover());
        targetLevelBO.setCompletionTrigger(sourceLevelBO.getCompletionTrigger());
        targetLevelBO.setOverrideAuthority(sourceLevelBO.getOverrideAuthority());
        CriteriaDependencyBOUtil.resetDependency(targetLevelBO);
        return levelToBeOverridden;
    }
    
    
    private StateTransitionRBO computeLevelStateUsingCriterionTags(List<CriteriaExpression> elcCriteriaExpressions, 
            StatusRBO status, String tenant){
        StateTransitionRBO levelState = null;
        for (CriteriaExpression criteriaExpression : elcCriteriaExpressions) {
            String uniqueCriteriaName = criteriaExpression.getUniqueCriteriaName();
            String criteriaRegion = criteriaExpression.getCriteriaRegion();
            // Explictly set level region to null so during evaluation no new criteria's will be created.
            CriteriaRBO criteriaRBO = status.getCriteria(uniqueCriteriaName,criteriaRegion,false, tenant);
            if (criteriaRBO != null && CollectionUtils.isNotEmpty(criteriaRBO.getCriterionTags())
                    && MapUtils.isNotEmpty(tagmp)) {
                for (CriterionTag criterionTag : criteriaRBO.getCriterionTags()) {
                    levelState = tagmp.get(criterionTag);
                }
            }
        }
        return levelState;
    }

    private LevelBO getLevelBO(LevelRBO level) {
        return ((LevelRBOImpl) level).getLevelBO();
    }
}
