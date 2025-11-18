package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns a formatted statement for this invoice.
     *
     * @return the formatted statement
     * @throws RuntimeException if a play type is unknown
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        String result = "Statement for " + invoice.getCustomer() + "\n";

        for (Performance performance : invoice.getPerformances()) {
            final Play play = plays.get(performance.getPlayID());
            final int thisAmount = getAmount(performance, play);

            volumeCredits += getVolumeCredits(performance, play);

            result += String.format("  %s: %s (%s seats)\n",
                    play.getName(),
                    usd(thisAmount),
                    performance.getAudience());

            totalAmount += thisAmount;
        }

        result += String.format("Amount owed is %s\n", usd(totalAmount));
        result += String.format("You earned %s credits\n", volumeCredits);
        return result;
    }

    private static int getAmount(Performance performance, Play play) {
        int result = 0;
        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE
                        * performance.getAudience();
                break;
            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    private static int getVolumeCredits(Performance performance, Play play) {
        int volumeCredits =
                Math.max(performance.getAudience()
                        - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(play.getType())) {
            volumeCredits += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return volumeCredits;
    }

    private static String usd(int amount) {
        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);
        return frmt.format(amount / Constants.PERCENT_FACTOR);
    }
}
