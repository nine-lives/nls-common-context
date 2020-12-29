package com.nls.context

import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification
import spock.lang.Unroll

class SelfReferencingMessageSourceSpec extends Specification {
    private static StaticMessageSource delegateSource = new StaticMessageSource()
    private static SelfReferencingMessageSource source = new SelfReferencingMessageSource()
    private static Locale locale = Locale.getDefault(Locale.Category.FORMAT)

    def setupSpec() {
        source.setMessageSource(delegateSource)
        delegateSource.addMessage("code1", locale, "ONE")
        delegateSource.addMessage("code2", locale, "TWO [mp:code1]")
        delegateSource.addMessage("code3", locale, "[mp:code1] THREE [mp:code2]")
        delegateSource.addMessage("code4", locale, "[mp:code1] THREE {0}")
        delegateSource.addMessage("code5", locale, "[mp:nocode]")
        delegateSource.alwaysUseMessageFormat = false
    }

    @Unroll("Get message with resolver - #code")
    def "Get message with resolver"() {
        when:
        MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable([code] as String[], args as Object[])
        String message = source.getMessage(resolvable, locale)

        then:
        message == expected

        where:
        code    | args    | expected
        "code1" | []      | "ONE"
        "code2" | []      | "TWO ONE"
        "code3" | []      | "ONE THREE TWO ONE"
        "code4" | ["ARG"] | "ONE THREE ARG"
    }

    @Unroll("Get non existing message with resolver - #code")
    def "Get message with resolver - doesn't exist"() {
        when:
        MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable([code] as String[], args as Object[])
        source.getMessage(resolvable, locale)

        then:
        thrown(NoSuchMessageException)

        where:
        code    | args
        "code5" | []
        "code6" | []
    }

    @Unroll("Get message with params - #code")
    def "Get message with params"() {
        when:
        String message = source.getMessage(code, args as Object[], locale)

        then:
        message == expected

        where:
        code    | args    | expected
        "code1" | []      | "ONE"
        "code2" | []      | "TWO ONE"
        "code3" | []      | "ONE THREE TWO ONE"
        "code4" | ["ARG"] | "ONE THREE ARG"
    }

    @Unroll("Get non existing message with params - #code")
    def "Get non existing message with params"() {
        when:
        source.getMessage(code, null, locale)

        then:
        thrown(NoSuchMessageException)

        where:
        code    | args
        "code5" | []
        "code6" | []
    }

    @Unroll("Get message with params and default - #code")
    def "Get message with params and default"() {
        when:
        String message = source.getMessage(code, args as Object[], "DEFAULT", locale)

        then:
        message == expected

        where:
        code    | args    | expected
        "code1" | []      | "ONE"
        "code2" | []      | "TWO ONE"
        "code3" | []      | "ONE THREE TWO ONE"
        "code4" | ["ARG"] | "ONE THREE ARG"
        "code6" | []      | "DEFAULT"
    }
}
