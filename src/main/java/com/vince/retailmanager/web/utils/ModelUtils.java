package com.vince.retailmanager.web.utils;

import com.vince.retailmanager.exception.EntityNotFoundException;
import com.vince.retailmanager.model.entity.authorization.AccessToken;
import com.vince.retailmanager.model.entity.companies.Company;
import com.vince.retailmanager.model.entity.companies.Franchisee;
import com.vince.retailmanager.model.entity.companies.Franchisor;
import com.vince.retailmanager.model.entity.financials.DateRange;
import com.vince.retailmanager.service.FinancialService;
import com.vince.retailmanager.service.FranchiseService;
import com.vince.retailmanager.service.TransactionService;
import com.vince.retailmanager.service.UserService;
import java.time.LocalDate;
import java.util.Set;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 * Adds controller utility methods for authorization and injecting data into the model layer.
 */
@Component
public class ModelUtils {

  @Setter
  private Model model;

  private FranchiseService franchiseService;
  private FinancialService financialService;
  private UserService userService;
  private TransactionService transactionService;

  public ModelUtils(FranchiseService franchiseService,
      FinancialService financialService, UserService userService,
      TransactionService transactionService) {
    this.franchiseService = franchiseService;
    this.financialService = financialService;
    this.userService = userService;
    this.transactionService = transactionService;
  }

  public void addCompany(Integer companyId) throws EntityNotFoundException {
    if (companyId != null) {
      model.addAttribute("company", franchiseService.findCompanyById(companyId));
    }
  }

  public void addDateRange(Model model,
      LocalDate startDate,
      LocalDate endDate) {
    DateRange dateRange = new DateRange(startDate, endDate);
    model.addAttribute("dateRange", dateRange);
  }

  public void addPayment(Integer paymentId) throws EntityNotFoundException {
    if (paymentId != null) {
      model.addAttribute("payment", transactionService.findPaymentById(paymentId));
    }
  }

  public void addInvoice(Integer invoiceId) throws EntityNotFoundException {
    if (invoiceId != null) {
      model.addAttribute("invoice", transactionService.findInvoiceById(invoiceId));
    }
  }

  public void addFranchisee(Integer franchiseeId)
      throws EntityNotFoundException {
    if (franchiseeId != null) {
      model.addAttribute("franchisee", franchiseService.findCompanyById(franchiseeId));
    }
  }

  public void addFranchisor(Integer franchisorId)
      throws EntityNotFoundException {
    if (franchisorId != null) {
      model.addAttribute("franchisor", franchiseService.findCompanyById(franchisorId));
    }
  }

  public void addIncomeStatement(Integer incomeStatementId)
      throws EntityNotFoundException {
    if (incomeStatementId != null) {
      model.addAttribute("incomeStatement",
          financialService.findIncomeStatementById(incomeStatementId));
    }
  }

  public boolean isAuthorized(Company... companies) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    if (username.isEmpty()) {
      return false;
    }
    AccessToken accessToken = userService.findFirstAccessToken(username, Set.of(companies));
    return accessToken != null;
  }

  //if company is a franchisee, authorizes its franchisor
  //or just authorizes the company
  public boolean isAuthorizedInFranchise(Company company) {
    try {
      Franchisee franchisee = (Franchisee) company;
      Franchisor franchisor = franchisee.getFranchisor();
      return this.isAuthorized(franchisor, franchisee);
    } catch (ClassCastException e) {
      return this.isAuthorized(company);
    }

  }

}
