package com.smartfinance.smartfinancedriveplatform.iam.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.CreateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ReassignLeadsCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateSalesAgentCommand;

public interface SalesAgentCommandService {
    SalesAgent handle(CreateSalesAgentCommand command);
    SalesAgent handle(UpdateSalesAgentCommand command);
    void handle(ReassignLeadsCommand command);
}
