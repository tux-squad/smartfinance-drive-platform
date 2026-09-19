package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.iam.application.commandservices.SalesAgentCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.CreateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ReassignLeadsCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.SalesAgentId;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.SalesAgentRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SalesAgentCommandServiceImpl implements SalesAgentCommandService {

    private final SalesAgentRepository salesAgentRepository;
    private final ProspectRepository prospectRepository;

    public SalesAgentCommandServiceImpl(SalesAgentRepository salesAgentRepository, ProspectRepository prospectRepository) {
        this.salesAgentRepository = salesAgentRepository;
        this.prospectRepository = prospectRepository;
    }

    @Override
    public SalesAgent handle(CreateSalesAgentCommand command) {
        SalesAgent agent = new SalesAgent(
                command.dealerUserId(),
                command.fullName(),
                command.email(),
                command.phone()
        );
        return salesAgentRepository.save(agent);
    }

    @Override
    public SalesAgent handle(UpdateSalesAgentCommand command) {
        SalesAgent agent = salesAgentRepository.findById(new SalesAgentId(command.agentId()))
                .orElseThrow(() -> new DomainValidationException("Sales Agent not found: " + command.agentId()));

        agent.updateDetails(command.fullName(), command.email(), command.phone(), command.active());
        return salesAgentRepository.save(agent);
    }

    @Override
    public void handle(ReassignLeadsCommand command) {
        salesAgentRepository.findById(new SalesAgentId(command.sourceAgentId()))
                .orElseThrow(() -> new DomainValidationException("Source Sales Agent not found: " + command.sourceAgentId()));
        salesAgentRepository.findById(new SalesAgentId(command.targetAgentId()))
                .orElseThrow(() -> new DomainValidationException("Target Sales Agent not found: " + command.targetAgentId()));

        List<Prospect> dealerProspects = prospectRepository.findAllByDealerUserId(command.dealerUserId());
        String sourceAgentIdStr = command.sourceAgentId().toString();
        String targetAgentIdStr = command.targetAgentId().toString();

        for (Prospect prospect : dealerProspects) {
            if (sourceAgentIdStr.equals(prospect.getSalesAgentId())) {
                prospect.setSalesAgentId(targetAgentIdStr);
                prospectRepository.save(prospect);
            }
        }
    }
}
