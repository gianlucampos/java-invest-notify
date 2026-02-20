package com.github.gianlucampos;

import com.github.gianlucampos.config.AppConfig;

public class Main {

    public static void main(String[] args) {
        AppConfig.gmailService().sendEmail();
    }
}
