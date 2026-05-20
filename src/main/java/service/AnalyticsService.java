package service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight dashboard analytics aggregator.
 *
 * This class is intentionally DB-agnostic for now so teammates can merge
 * their modules first; replace mock values with repository queries during
 * integration.
 */
public class AnalyticsService {

    public Map<String, String> buildSummary() {
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Total Transactions", "0");
        summary.put("Total Categories", "0");
        summary.put("Recurring Expenses", "0");
        summary.put("Net Balance", "$0.00");
        return summary;
    }
}
