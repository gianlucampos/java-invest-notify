package com.github.gianlucampos.config.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.gianlucampos.config.AppConfig;

public class NotifyEmailInvestment implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            context.getLogger().log("Starting application...");
            AppConfig.ruleService().verifyAll();
            return "Portfolio updated successfully!";
        } catch (Exception ex) {
            context.getLogger().log("Update failed: " + ex.getMessage());
            return "Execution failed: " + ex.getMessage();
        }
    }
}
