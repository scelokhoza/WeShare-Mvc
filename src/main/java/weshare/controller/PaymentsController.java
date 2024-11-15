package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.*;
import weshare.persistence.ExpenseDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static weshare.model.DateHelper.DD_MM_YYYY;
import static weshare.model.DateHelper.TODAY;
import static weshare.model.MoneyHelper.ZERO_RANDS;

public class PaymentsController {
    public static final Handler view_sent = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest> payments = expensesDAO.findPaymentRequestsSent(personLoggedIn);

        MonetaryAmount total_unpaid = payments.stream().filter(pr -> !pr.isPaid()).map(PaymentRequest::getAmountToPay).reduce(MonetaryAmount::add).orElse(ZERO_RANDS);
        Map<String, Object> viewModel = Map.of("payments", payments, "total_unpaid", total_unpaid);
        context.render("paymentrequests_sent.html", viewModel);
    };

    public static final Handler create = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        UUID expense_id = UUID.fromString(Objects.requireNonNull(context.queryParam("expenseId")));
        Expense expense = expenses.stream().filter(e -> e.getId().toString().equals(expense_id.toString())).findFirst().orElseThrow();

        Collection<PaymentRequest> payments = expense.listOfPaymentRequests();

        Map<String, Object> viewModel = Map.of("expense", expense, "payments", payments);
        context.render("paymentrequest.html", viewModel);
    };

    public static final Handler add = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);

        String email = context.formParamAsClass("email", String.class)
                .check(Objects::nonNull, "Email is required")
                .get();

        MonetaryAmount amount = MoneyHelper.amountOf(context.formParamAsClass("amount", Long.class)
                .check(Objects::nonNull, "Amount is required")
                .get());

        LocalDate due_date = LocalDate.parse(context.formParamAsClass("due_date", String.class)
                .check(Objects::nonNull, "Date is required")
                .get(), DD_MM_YYYY);

        UUID expense_id = UUID.fromString(Objects.requireNonNull(context.queryParam("expenseId")));
        Expense expense = expenses.stream().filter(e -> e.getId().toString().equals(expense_id.toString())).findFirst().orElseThrow();

        expense.requestPayment(new Person(email), amount, due_date);
        context.redirect(Routes.PAYMENT + "?expenseId=" + expense_id);
    };

    public static Handler view_received = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest> payments = expensesDAO.findPaymentRequestsReceived(personLoggedIn);

        MonetaryAmount total_unpaid = payments.stream().filter(pr -> !pr.isPaid()).map(PaymentRequest::getAmountToPay).reduce(MonetaryAmount::add).orElse(ZERO_RANDS);
        Map<String, Object> viewModel = Map.of("payments", payments, "total_unpaid", total_unpaid);
        context.render("paymentrequests_received.html", viewModel);
    };

    public static Handler pay = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest> payments = expensesDAO.findPaymentRequestsReceived(personLoggedIn);

        UUID payment_id = UUID.fromString(context.formParamAsClass("paymentId", String.class)
                .check(Objects::nonNull, "ID is required")
                .get());


        PaymentRequest payment = payments.stream().filter(p -> p.getId().toString().equals(payment_id.toString())).findFirst().orElseThrow();

        payment.pay(personLoggedIn, TODAY);
        expensesDAO.save(new Expense(personLoggedIn, payment.getDescription(), payment.getAmountToPay(), TODAY));
        context.redirect(Routes.PAYMENTS_RECEIVED);
    };
}
