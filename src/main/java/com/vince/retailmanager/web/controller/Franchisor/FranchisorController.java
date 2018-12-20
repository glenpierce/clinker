package com.vince.retailmanager.web.controller.Franchisor;

import com.vince.retailmanager.model.entity.Franchisee;
import com.vince.retailmanager.model.entity.Franchisor;
import com.vince.retailmanager.model.entity.IncomeStatement;
import com.vince.retailmanager.model.entity.Invoice;
import com.vince.retailmanager.model.entity.PercentageFee;
import com.vince.retailmanager.model.entity.User;
import com.vince.retailmanager.service.FinancialService;
import com.vince.retailmanager.service.FranchiseService;
import com.vince.retailmanager.service.TransactionService;
import com.vince.retailmanager.service.UserService;
import com.vince.retailmanager.web.controller.utils.ControllerUtils;
import com.vince.retailmanager.web.exception.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/franchisors"})
public class FranchisorController {

  @Autowired
  public UserService userService;
  @Autowired
  public TransactionService transactionService;
  @Autowired
  public FinancialService financialService;
  @Autowired
  public Validator validator;
  @Autowired
  public ControllerUtils controllerUtils;
  @Autowired
  private FranchiseService franchiseService;

  @ModelAttribute
  public void populateModel(
      Model model,
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User authenticatedUser,
      @PathVariable(value = "id", required = false) Integer id,
      @PathVariable(value = "franchiseeId", required = false) Integer franchiseeId,
      @PathVariable(value = "incomeStatementId", required = false) Integer incomeStatementId
  ) throws EntityNotFoundException {
    if (id != null) {
      model.addAttribute("franchisor", franchiseService.findFranchisorById(id));
      ControllerUtils.addActiveUsername(model, authenticatedUser, id, userService);
    }

    if (franchiseeId != null) {
      model.addAttribute("franchisee", franchiseService.findFranchiseeById(franchiseeId));
    }

    if (incomeStatementId != null) {
      model.addAttribute("incomeStatement",
          financialService.findIncomeStatementById(incomeStatementId));
    }
  }

  @InitBinder("franchisor")
  public void initFranchisorBinder(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @InitBinder("franchisee")
  public void initFranchiseeBinder(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }


  @GetMapping("/{id}")
  public ResponseEntity<Franchisor> findFranchisor(Franchisor franchisor) {
    return new ResponseEntity<>(franchisor, HttpStatus.OK);
  }

  @PostMapping("/new")
  public ResponseEntity<Franchisor> createFranchisor(
      @Valid @RequestBody Franchisor franchisor,
      User user
  ) {
    user.addAccessToken(franchisor);
    Franchisor savedFranchisor = franchiseService.saveFranchisor(franchisor);
    return new ResponseEntity<>(savedFranchisor, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @PreAuthorize("authentication.name == #activeUsername")
  public ResponseEntity<Franchisor> updateCompany(
      Franchisor franchisor,
      @RequestBody Franchisor updatedFranchisor,
      @ModelAttribute("activeUsername") String activeUsername
  ) {
    franchisor.setName(updatedFranchisor.getName());
    franchisor.setDescription(updatedFranchisor.getDescription());
    franchisor.setWebsite(updatedFranchisor.getWebsite());

    franchiseService.saveCompany(franchisor);
    controllerUtils.validate(franchisor);
    return new ResponseEntity<>(franchisor, HttpStatus.OK);
  }

  @PostMapping("/{id}/franchisees/new")
  @PreAuthorize("authentication.name == #activeUsername")
  public ResponseEntity<?> createFranchisee(
      Franchisor franchisor,
      @ModelAttribute("activeUsername") String activeUsername
  ) {
    Franchisee franchisee = Franchisee.builder().build();
    franchisor.addFranchisee(franchisee);
    ControllerUtils.validate(validator, franchisee);
    this.franchiseService.saveFranchisee(franchisee);
    return new ResponseEntity<>(franchisee, HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("authentication.name == #activeUsername")
  public ResponseEntity<Void> deleteFranchisor(Franchisor franchisor) throws Exception {
    franchiseService.disableFranchisor(franchisor);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{id}/franchisees/{franchiseeId}/request-payment")
  public ResponseEntity<Invoice> requestPaymentFromFranchisee(
      Franchisor franchisor,
      Franchisee franchisee,
      @RequestBody Map<String, Object> body
  ) {
    double due = (double) body.get("due");
    Invoice invoice = Invoice.builder()
        .sender(franchisor)
        .recipient(franchisee)
        .due(due)
        .description("requesting royalty payment")
        .build();
    ControllerUtils.validate(validator, invoice);
    transactionService.saveInvoice(invoice);
    return new ResponseEntity<>(invoice, HttpStatus.OK);
  }

  //	Auth: franchisees.getIncomeStatements . contains incomeStatement
  @PostMapping("/{id}/franchisees/{franchiseeId}/request-fees/financials/{incomeStatementId}")
  public ResponseEntity<Object> requestMonthlyFranchiseFees(
      Franchisor franchisor,
      Franchisee franchisee,
      IncomeStatement incomeStatement
  ) {
    List<PercentageFee> fees = franchiseService.createMonthlyFranchiseFees(incomeStatement);
    return new ResponseEntity<>(fees, HttpStatus.OK);
  }

  // get fees payments
  @GetMapping("{id}/fees/{feeType}")
  public ResponseEntity<List<PercentageFee>> getFees(
      Franchisor franchisor,
      @PathVariable(name = "feeType") String type,
      @RequestParam(required = false, name = "fully-paid") final Boolean isFullyPaid
  ) {
    List<PercentageFee> fees = franchiseService.getPercentageFees(franchisor);
    fees = isFullyPaid == null ? fees : filterPercentageFeesByFullyPaid(fees, isFullyPaid);

    System.out.println("fees.get(0).getFeeType() = " + fees.get(0).getFeeType());

    return new ResponseEntity<>(fees, HttpStatus.OK);
  }

  private List<PercentageFee> filterPercentageFeesByFullyPaid(List<PercentageFee> fees,
      boolean isFullyPaid) {
    fees = fees.stream()
        .filter(fee -> fee.getInvoice().isFullyPaid() == isFullyPaid)
        .collect(Collectors.toList());
    return fees;
  }

}
