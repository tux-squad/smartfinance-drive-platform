package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.scoring.application.commandservices.CreditScoreCommandService;
import com.smartfinance.smartfinancedriveplatform.scoring.application.queryservices.CreditScoreQueryService;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.DeleteCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetAllCreditScoresQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByProfileIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources.CreditScoreResource;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources.EvaluateCreditScoreResource;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.transform.CreditScoreResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.transform.EvaluateCreditScoreCommandFromResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing credit score risk evaluations.
 */
@RestController
@RequestMapping(value = "/api/v1/credit-scores", produces = "application/json")
public class CreditScoresController {

    private final CreditScoreCommandService creditScoreCommandService;
    private final CreditScoreQueryService creditScoreQueryService;

    public CreditScoresController(CreditScoreCommandService creditScoreCommandService,
                                  CreditScoreQueryService creditScoreQueryService) {
        this.creditScoreCommandService = creditScoreCommandService;
        this.creditScoreQueryService = creditScoreQueryService;
    }

    /**
     * POST /api/v1/credit-scores/evaluate
     * Evaluates credit risk and creates a new credit score evaluation.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<CreditScoreResource> evaluateCreditScore(@RequestBody EvaluateCreditScoreResource resource) {
        var command = EvaluateCreditScoreCommandFromResourceAssembler.toCommandFromResource(resource);
        var scoreOpt = creditScoreCommandService.handle(command);
        return scoreOpt
                .map(score -> new ResponseEntity<>(
                        CreditScoreResourceFromEntityAssembler.toResourceFromEntity(score),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/credit-scores
     * Retrieves all credit score evaluations.
     */
    @GetMapping
    public ResponseEntity<List<CreditScoreResource>> getAllCreditScores() {
        var query = new GetAllCreditScoresQuery();
        var scores = creditScoreQueryService.handle(query);
        var resources = scores.stream()
                .map(CreditScoreResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/credit-scores/{id}
     * Retrieves a credit score evaluation by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditScoreResource> getCreditScoreById(@PathVariable UUID id) {
        var query = new GetCreditScoreByIdQuery(new ScoreId(id));
        var scoreOpt = creditScoreQueryService.handle(query);
        return scoreOpt
                .map(score -> ResponseEntity.ok(CreditScoreResourceFromEntityAssembler.toResourceFromEntity(score)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/credit-scores/profile/{profileId}
     * Retrieves credit score evaluations for a specific customer profile.
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<CreditScoreResource>> getCreditScoresByProfileId(@PathVariable String profileId) {
        var query = new GetCreditScoreByProfileIdQuery(profileId);
        var scores = creditScoreQueryService.handle(query);
        var resources = scores.stream()
                .map(CreditScoreResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * DELETE /api/v1/credit-scores/{id}
     * Deletes a credit score evaluation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCreditScore(@PathVariable UUID id) {
        var command = new DeleteCreditScoreCommand(new ScoreId(id));
        creditScoreCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
