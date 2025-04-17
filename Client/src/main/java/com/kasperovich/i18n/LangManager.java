package com.kasperovich.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class LangManager {
    private static Locale currentLocale = Locale.ENGLISH;
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }
}
