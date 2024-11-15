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

import static weshare.model.DateHelper.DD_MM_YYYY;
import static weshare.model.MoneyHelper.ZERO_RANDS;

public class ExpensesController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);

        MonetaryAmount nett = expenses.stream()
                .map(Expense::amountLessPaymentsReceived)
                .reduce(MonetaryAmount::add).orElse(ZERO_RANDS);

        Map<String, Object> viewModel = Map.of("expenses", expenses, "nett", nett, "zero", ZERO_RANDS);
        context.render("expenses.html", viewModel);
    };

    public static final Handler create = context -> {
        context.render("newexpense.html");
    };

    public static final Handler add = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String description = context.formParamAsClass("description", String.class)
                .check(Objects::nonNull, "Description is required")
                .get();

        LocalDate date = LocalDate.parse(context.formParamAsClass("date", String.class)
                .check(Objects::nonNull, "Date is required")
                .get(), DD_MM_YYYY);

        MonetaryAmount amount = MoneyHelper.amountOf(context.formParamAsClass("amount", Long.class)
                .check(Objects::nonNull, "Amount is required")
                .get());

        Expense expense = expensesDAO.save(new Expense(personLoggedIn, description, amount, date));
        context.redirect(Routes.EXPENSES);
    };
}
