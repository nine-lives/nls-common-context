package com.nls.context

import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification
import spock.lang.Unroll

class AggregatingMessageSourceSpec extends Specification {
    private static StaticMessageSource delegateSource1 = new StaticMessageSource()
    private static StaticMessageSource delegateSource2 = new StaticMessageSource()
    private static AggregatingMessageSource source = new AggregatingMessageSource()
    private static Locale locale = Locale.getDefault(Locale.Category.FORMAT)

    def setupSpec() {
        source.setMessageSources(Arrays.asList(delegateSource1, delegateSource2))
        delegateSource1.addMessage("1only", locale, "IN ONE ONLY")
        delegateSource1.addMessage("both", locale, "IN BOTH ONE")
        delegateSource1.addMessage("1onlyArgs", locale, "IN ONE ONLY {0}")
        delegateSource1.addMessage("bothArgs", locale, "IN BOTH ONE {0}")
        delegateSource2.addMessage("2only", locale, "IN TWO ONLY")
        delegateSource2.addMessage("both", locale, "IN BOTH TWO")
        delegateSource2.addMessage("2onlyArgs", locale, "IN TWO ONLY {0}")
        delegateSource2.addMessage("bothArgs", locale, "IN BOTH TWO {0}")
    }

    @Unroll("Get message with resolver - #code")
    def "Get message with resolver"() {
        when:
        MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable([code] as String[], args as Object[])
        String message = source.getMessage(resolvable, locale)

        then:
        message == expected

        where:
        code        | args    | expected
        "1only"     | []      | "IN ONE ONLY"
        "2only"     | []      | "IN TWO ONLY"
        "both"      | []      | "IN BOTH ONE"
        "1onlyArgs" | ["ARG"] | "IN ONE ONLY ARG"
        "2onlyArgs" | ["ARG"] | "IN TWO ONLY ARG"
        "bothArgs"  | ["ARG"] | "IN BOTH ONE ARG"
    }

    @Unroll("Get non existing message with resolver - #code")
    def "Get message with resolver - doesn't exist"() {
        when:
        MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable([code] as String[], args as Object[])
        source.getMessage(resolvable, locale)

        then:
        thrown(NoSuchMessageException)

        where:
        code   | args
        "none" | []
        "none" | ["ARG"]
    }

    @Unroll("Get message with params - #code")
    def "Get message with params"() {
        when:
        String message = source.getMessage(code, args as Object[], locale)

        then:
        message == expected

        where:
        code        | args    | expected
        "1only"     | []      | "IN ONE ONLY"
        "2only"     | []      | "IN TWO ONLY"
        "both"      | []      | "IN BOTH ONE"
        "1onlyArgs" | ["ARG"] | "IN ONE ONLY ARG"
        "2onlyArgs" | ["ARG"] | "IN TWO ONLY ARG"
        "bothArgs"  | ["ARG"] | "IN BOTH ONE ARG"
    }

    @Unroll("Get non existing message with params - #code")
    def "Get non existing message with params"() {
        when:
        source.getMessage(code, args as Object[], locale)

        then:
        thrown(NoSuchMessageException)

        where:
        code   | args
        "none" | null
        "none" | ["ARG"]
    }

    @Unroll("Get message with params and default - #code")
    def "Get message with params and default"() {
        when:
        String message = source.getMessage(code, args as Object[], "DEFAULT", locale)

        then:
        message == expected

        where:
        code        | args    | expected
        "1only"     | []      | "IN ONE ONLY"
        "2only"     | []      | "IN TWO ONLY"
        "both"      | []      | "IN BOTH ONE"
        "1onlyArgs" | ["ARG"] | "IN ONE ONLY ARG"
        "2onlyArgs" | ["ARG"] | "IN TWO ONLY ARG"
        "bothArgs"  | ["ARG"] | "IN BOTH ONE ARG"
        "none"      | ["ARG"] | "DEFAULT"
    }
}
