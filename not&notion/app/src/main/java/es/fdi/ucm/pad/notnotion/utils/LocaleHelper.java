package es.fdi.ucm.pad.notnotion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_LANGUAGE = "app_language";

    // Cambia el idioma y lo guarda
    public static Context setLocale(Context context, String languageCode) {
        persistLanguage(context, languageCode);
        return updateResources(context, languageCode);
    }

    // Devuelve el idioma guardado o "es"
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "es");
    }

    private static void persistLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }

        return context;
    }

    // Usar en attachBaseContext de las actividades
    public static Context applyLocale(Context context) {
        return updateResources(context, getLanguage(context));
    }
}
