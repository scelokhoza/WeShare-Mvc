package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";
    public static final String EXPENSES = "/expenses";
    public static final String NEW_EXPENSE = "/newexpense";
    public static final String NEW_EXPENSES_ACTION = "/newexpense.action";
    public static final String PAYMENTS_SENT = "/paymentrequests_sent";
    public static final String PAYMENTS_RECEIVED = "/paymentrequests_received";
    public static final String PAYMENT = "/paymentrequest";
    public static final String PAYMENT_ACTION = "/paymentrequest.action";
    public static final String PAY = "/payment.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,  PersonController.login);
            get(LOGOUT,         PersonController.logout);
            get(EXPENSES,           ExpensesController.view);
            get(NEW_EXPENSE, ExpensesController.create);
            post(NEW_EXPENSES_ACTION, ExpensesController.add);
            get(PAYMENTS_SENT, PaymentsController.view_sent);
            get(PAYMENT, PaymentsController.create);
            post(PAYMENT_ACTION, PaymentsController.add);
            get(PAYMENTS_RECEIVED, PaymentsController.view_received);
            post(PAY, PaymentsController.pay);
        });
    }
}
