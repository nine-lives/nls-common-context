package com.nls.context;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelfReferencingMessageSource implements MessageSource {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\[mp:([^]]+)]");
    private MessageSource messageSource;

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return resolveReferences(messageSource.getMessage(resolvable, locale), locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return resolveReferences(messageSource.getMessage(code, args, locale), locale);
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return resolveReferences(messageSource.getMessage(code, args, defaultMessage, locale), locale);
    }

    private String resolveReferences(String message, Locale locale) throws NoSuchMessageException {
        if (message == null || !message.contains("[mp:")) {
            return message;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = REFERENCE_PATTERN.matcher(message);
        while (matcher.find()) {
            String code = matcher.group(1).trim();
            matcher.appendReplacement(sb, getMessage(new DefaultMessageSourceResolvable(code), locale));
        }
        return matcher.appendTail(sb).toString();
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}
