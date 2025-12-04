import java.util.HashMap;
import java.util.Map;

/**
 * TaxCalculator handles tax rate calculations based on state/location.
 * Implements realistic US state tax rates for e-commerce orders.
 */
public class TaxCalculator {
    
    // State tax rates (simplified US state sales tax rates)
    private static final Map<String, Double> STATE_TAX_RATES = new HashMap<>();
    
    static {
        // Major US states with their average sales tax rates
        STATE_TAX_RATES.put("AL", 0.0400); // Alabama - 4%
        STATE_TAX_RATES.put("AK", 0.0000); // Alaska - 0%
        STATE_TAX_RATES.put("AZ", 0.0560); // Arizona - 5.6%
        STATE_TAX_RATES.put("AR", 0.0650); // Arkansas - 6.5%
        STATE_TAX_RATES.put("CA", 0.0725); // California - 7.25%
        STATE_TAX_RATES.put("CO", 0.0290); // Colorado - 2.9%
        STATE_TAX_RATES.put("CT", 0.0635); // Connecticut - 6.35%
        STATE_TAX_RATES.put("DE", 0.0000); // Delaware - 0%
        STATE_TAX_RATES.put("FL", 0.0600); // Florida - 6%
        STATE_TAX_RATES.put("GA", 0.0400); // Georgia - 4%
        STATE_TAX_RATES.put("HI", 0.0400); // Hawaii - 4%
        STATE_TAX_RATES.put("ID", 0.0600); // Idaho - 6%
        STATE_TAX_RATES.put("IL", 0.0625); // Illinois - 6.25%
        STATE_TAX_RATES.put("IN", 0.0700); // Indiana - 7%
        STATE_TAX_RATES.put("IA", 0.0600); // Iowa - 6%
        STATE_TAX_RATES.put("KS", 0.0650); // Kansas - 6.5%
        STATE_TAX_RATES.put("KY", 0.0600); // Kentucky - 6%
        STATE_TAX_RATES.put("LA", 0.0445); // Louisiana - 4.45%
        STATE_TAX_RATES.put("ME", 0.0550); // Maine - 5.5%
        STATE_TAX_RATES.put("MD", 0.0600); // Maryland - 6%
        STATE_TAX_RATES.put("MA", 0.0625); // Massachusetts - 6.25%
        STATE_TAX_RATES.put("MI", 0.0600); // Michigan - 6%
        STATE_TAX_RATES.put("MN", 0.0688); // Minnesota - 6.88%
        STATE_TAX_RATES.put("MS", 0.0700); // Mississippi - 7%
        STATE_TAX_RATES.put("MO", 0.0423); // Missouri - 4.23%
        STATE_TAX_RATES.put("MT", 0.0000); // Montana - 0%
        STATE_TAX_RATES.put("NE", 0.0550); // Nebraska - 5.5%
        STATE_TAX_RATES.put("NV", 0.0685); // Nevada - 6.85%
        STATE_TAX_RATES.put("NH", 0.0000); // New Hampshire - 0%
        STATE_TAX_RATES.put("NJ", 0.0663); // New Jersey - 6.63%
        STATE_TAX_RATES.put("NM", 0.0513); // New Mexico - 5.13%
        STATE_TAX_RATES.put("NY", 0.0400); // New York - 4%
        STATE_TAX_RATES.put("NC", 0.0475); // North Carolina - 4.75%
        STATE_TAX_RATES.put("ND", 0.0500); // North Dakota - 5%
        STATE_TAX_RATES.put("OH", 0.0575); // Ohio - 5.75%
        STATE_TAX_RATES.put("OK", 0.0450); // Oklahoma - 4.5%
        STATE_TAX_RATES.put("OR", 0.0000); // Oregon - 0%
        STATE_TAX_RATES.put("PA", 0.0600); // Pennsylvania - 6%
        STATE_TAX_RATES.put("RI", 0.0700); // Rhode Island - 7%
        STATE_TAX_RATES.put("SC", 0.0600); // South Carolina - 6%
        STATE_TAX_RATES.put("SD", 0.0450); // South Dakota - 4.5%
        STATE_TAX_RATES.put("TN", 0.0700); // Tennessee - 7%
        STATE_TAX_RATES.put("TX", 0.0625); // Texas - 6.25%
        STATE_TAX_RATES.put("UT", 0.0595); // Utah - 5.95%
        STATE_TAX_RATES.put("VT", 0.0600); // Vermont - 6%
        STATE_TAX_RATES.put("VA", 0.0530); // Virginia - 5.3%
        STATE_TAX_RATES.put("WA", 0.0650); // Washington - 6.5%
        STATE_TAX_RATES.put("WV", 0.0600); // West Virginia - 6%
        STATE_TAX_RATES.put("WI", 0.0500); // Wisconsin - 5%
        STATE_TAX_RATES.put("WY", 0.0400); // Wyoming - 4%
        STATE_TAX_RATES.put("DC", 0.0600); // Washington DC - 6%
    }
    
    /**
     * Calculates tax amount based on subtotal and state code
     * @param subtotal The pre-tax subtotal
     * @param stateCode Two-letter state code (e.g., "CA", "NY")
     * @return Tax amount
     */
    public static double calculateTax(double subtotal, String stateCode) {
        if (subtotal <= 0) return 0.0;
        if (stateCode == null || stateCode.trim().isEmpty()) return 0.0;
        
        String state = stateCode.trim().toUpperCase();
        double taxRate = STATE_TAX_RATES.getOrDefault(state, 0.0);
        
        return subtotal * taxRate;
    }
    
    /**
     * Gets the tax rate for a given state
     * @param stateCode Two-letter state code
     * @return Tax rate as decimal (e.g., 0.0725 for 7.25%)
     */
    public static double getTaxRate(String stateCode) {
        if (stateCode == null || stateCode.trim().isEmpty()) return 0.0;
        String state = stateCode.trim().toUpperCase();
        return STATE_TAX_RATES.getOrDefault(state, 0.0);
    }
    
    /**
     * Gets the tax rate as a percentage string
     * @param stateCode Two-letter state code
     * @return Formatted percentage (e.g., "7.25%")
     */
    public static String getTaxRateDisplay(String stateCode) {
        double rate = getTaxRate(stateCode);
        return String.format("%.2f%%", rate * 100);
    }
    
    /**
     * Checks if a state code is valid
     * @param stateCode Two-letter state code
     * @return true if valid, false otherwise
     */
    public static boolean isValidStateCode(String stateCode) {
        if (stateCode == null || stateCode.trim().isEmpty()) return false;
        return STATE_TAX_RATES.containsKey(stateCode.trim().toUpperCase());
    }
    
    /**
     * Gets all valid state codes
     * @return Array of state codes
     */
    public static String[] getAllStateCodes() {
        return STATE_TAX_RATES.keySet().toArray(new String[0]);
    }
    
    /**
     * Calculates total including tax
     * @param subtotal The pre-tax subtotal
     * @param stateCode Two-letter state code
     * @return Total amount (subtotal + tax)
     */
    public static double calculateTotal(double subtotal, String stateCode) {
        double tax = calculateTax(subtotal, stateCode);
        return subtotal + tax;
    }
}