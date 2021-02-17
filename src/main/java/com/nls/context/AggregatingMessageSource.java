package com.nls.context;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AggregatingMessageSource implements MessageSource {
    private List<MessageSource> messageSources = new ArrayList<>(1);

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        MessageSourceResolvable nodefault = new NoDefaultMessageSourceResolvable(resolvable);
        for (MessageSource source : messageSources) {
            try {
                String result = source.getMessage(nodefault, locale);
                if (result != null) {
                    return result;
                }
            } catch (NoSuchMessageException ignore) {
            }
        }

        String result = messageSources.get(0).getMessage(resolvable, locale);
        if (result != null) {
            return result;
        }

        String[] codes = resolvable.getCodes();
        throw new NoSuchMessageException(codes.length > 0 ? codes[codes.length - 1] : null, locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        for (MessageSource source : messageSources) {
            try {
                String result = source.getMessage(code, args, locale);
                if (result != null) {
                    return result;
                }
            } catch (NoSuchMessageException ignore) {
            }
        }

        throw new NoSuchMessageException(code, locale);
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            return getMessage(code, args, locale);
        } catch (NoSuchMessageException ignore) {
        }
        return messageSources.get(0).getMessage(code, args, defaultMessage, locale);
    }

    public void setMessageSources(List<MessageSource> messageSources) {
        this.messageSources = messageSources;
    }


    private class NoDefaultMessageSourceResolvable implements MessageSourceResolvable {
        private final MessageSourceResolvable delegate;

        private NoDefaultMessageSourceResolvable(MessageSourceResolvable delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getCodes() {
            return delegate.getCodes();
        }

        @Override
        public Object[] getArguments() {
            return delegate.getArguments();
        }

        @Override
        public String getDefaultMessage() {
            return null;
        }
    }
}
